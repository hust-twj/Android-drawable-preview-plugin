package com.husttwj.drawablepreview.factories

import com.android.ide.common.rendering.api.ResourceNamespace
import com.android.ide.common.rendering.api.ResourceValue
import com.android.ide.common.rendering.api.ResourceValueImpl
import com.android.ide.common.resources.ResourceResolver
import com.android.ide.common.vectordrawable.VdPreview
import com.android.resources.ResourceType
import com.android.resources.ResourceUrl
import com.android.tools.configurations.Configuration
import com.android.tools.idea.configurations.ConfigurationManager
import com.husttwj.drawablepreview.drawables.DrawableInflater
import com.husttwj.drawablepreview.drawables.Utils
import com.husttwj.drawablepreview.drawables.dom.Drawable
import com.husttwj.drawablepreview.drawables.forEach
import com.husttwj.drawablepreview.settings.SettingsUtils
import com.husttwj.drawablepreview.util.LogUtil
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.awt.image.BufferedImage
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object XmlImageFactory {

    fun createXmlImage(path: String): BufferedImage? {
        val document = parseDocument(path)
        if (document == null) {
            LogUtil.d("createXmlImage() error -> parseDocument null. path=$path")
            return null
        }

        val image = getDrawableImage(document.documentElement)
        if (image != null) {
            LogUtil.d("createXmlImage() error -> getDrawableImage null. path=$path")
            return image
        }

        val stringBuilder = StringBuilder(100)
        val imageTargetSize: VdPreview.TargetSize? = VdPreview.TargetSize::class.java.let { clazz ->
            clazz.methods.forEach { method ->
                when (method.name) {
                    "createSizeFromWidth", "createFromMaxDimension" ->
                        return@let method.invoke(null, SettingsUtils.getPreviewSize()) as? VdPreview.TargetSize
                }
            }
            null
        }
        //LogUtil.d("createXmlImage() -> imageTargetSize=$imageTargetSize")
        return imageTargetSize?.let { VdPreview.getPreviewFromVectorDocument(imageTargetSize, document, stringBuilder) }
    }

    fun getDrawable(path: String): Drawable? =
        parseDocument(path)?.let { DrawableInflater.getDrawable(it.documentElement) }

    private fun parseDocument(path: String): Document? {
        val supportedFolder = Constants.SUPPORTED_FOLDERS.fold(false) { acc, next -> acc || path.contains(next) }
        if (!(path.endsWith(Constants.XML_TYPE) && supportedFolder)) {
            LogUtil.d("parseDocument() error -> not supported file. path=$path")
            return null
        }

//        val documentBuilderFactory = DocumentBuilderFactory.newInstance()
//        val documentBuilder = documentBuilderFactory.newDocumentBuilder()
//        val document = documentBuilder.parse(File(path)) ?: return null
//        val root = document.documentElement ?: return null
        val document = parseXmlFile(File(path))
        val root = document?.documentElement
        if (root == null) {
            LogUtil.d("parseDocument() error -> root == null. path=$path | document=${document}")
            return null
        }
        val resolver = getResourceResolver(Utils.getPsiFileFromPath(path))
        // LogUtil.d("parseDocument() ->  getResourceResolver() is null: ${resolver == null}")
        if (resolver != null) {
            replaceResourceReferences(root, resolver)
        }
        // LogUtil.d("parseDocument() success. path=$path")
        return document
    }

    private fun parseXmlFile(file: File): Document? {
        try {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            return dBuilder.parse(file)
        } catch (e: Exception) {
            LogUtil.d("parseXmlFile() error. name=${file.name}  path=${file.path}  msg=${e.message}")
            e.printStackTrace()
        }
        return null
    }

    private fun getDrawableImage(rootElement: Element): BufferedImage? {
        return try {
            val drawable = DrawableInflater.getDrawable(rootElement)
            if (drawable == null) {
                LogUtil.d("getDrawableImage: DrawableInflater returned null for tag: ${rootElement.tagName}")
                return null
            }

            val size = SettingsUtils.getPreviewSize()
            BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).also { image ->
                try {
                    drawable.draw(image)
                } catch (e: Exception) {
                    LogUtil.d("getDrawableImage: Error while drawing drawable: ${e.stackTraceToString()}")
                }
            }
        } catch (e: Exception) {
            LogUtil.d("getDrawableImage: Exception in getDrawableImage: ${e.stackTraceToString()}")
            null
        }
    }

    private fun replaceResourceReferences(node: Node, resolver: ResourceResolver) {
        if (node.nodeType == Node.ELEMENT_NODE) {
            node.attributes.forEach { attribute ->
                val value = attribute.nodeValue
                if (isReference(value)) {
                    val resolvedValue = resolveStringValue(resolver, value)
                    if (!isReference(resolvedValue)) {
                        attribute.nodeValue = resolvedValue
                    }
                }
            }
        }

        var newNode = node.firstChild
        while (newNode != null) {
            replaceResourceReferences(newNode, resolver)
            newNode = newNode.nextSibling
        }
    }

    private fun resolveStringValue(resolver: ResourceResolver, value: String): String {
        val resValue = findResValue(resolver, value) ?: return value
        return resolveNullableResValue(resolver, resValue)?.value ?: value
    }

    private fun findResValue(resolver: ResourceResolver, value: String): ResourceValue? {
        return resolver.dereference(
            ResourceValueImpl(
                ResourceNamespace.RES_AUTO,
                ResourceType.ID,
                "com.android.ide.common.rendering.api.RenderResources",
                value
            )
        )
    }

    private fun resolveNullableResValue(resolver: ResourceResolver, res: ResourceValue?): ResourceValue? {
        if (res == null) {
            return null
        }
        return resolver.resolveResValue(res)
    }

    private fun isReference(attributeValue: String) = ResourceUrl.parse(attributeValue) != null

    private fun getResourceResolver(element: PsiFile?): ResourceResolver? {

        if (element == null) return null

        val module = ProjectRootManager.getInstance(element.project)
            .fileIndex.getModuleForFile(element.virtualFile)
            ?: return null

        var resolver: ResourceResolver? = null

        //fix java.lang.NoSuchMethodError
        //'com.android.tools.idea.configurations.Configuration com.android.tools.idea.configurations.ConfigurationManager.getConfiguration(com.intellij.openapi.vfs.VirtualFile)'
        if (resolver == null) {
            resolver = try {
                val configurationManager = ConfigurationManager.getOrCreateInstance(module)
                val virtualFile = element.virtualFile
                // 尝试调用新版本 API
                val method = ConfigurationManager::class.java.getMethod("getConfiguration", VirtualFile::class.java)
                val configuration = method.invoke(configurationManager, virtualFile)
                val resourceResolverMethod = configuration.javaClass.getMethod("getResourceResolver")
                resourceResolverMethod.invoke(configuration) as? ResourceResolver
            } catch (e: NoSuchMethodException) {
                LogUtil.d("NoSuchMethodException reflect error1: ${e.message}")
                null
            } catch (e: Exception) {
                LogUtil.d("getConfiguration reflect error2: ${e.message}")
                null
            }
        }

        if (resolver == null) {
            resolver = try {
                val manager = ConfigurationManager.getOrCreateInstance(module)
                val configuration = getConfigurationCompat(manager, element.virtualFile)
                configuration?.resourceResolver
            } catch (e: Exception) {
                LogUtil.d("getConfiguration error: ${e.message}")
                null
            }
        }

        return resolver
    }

    /**
     * invoke ConfigurationManager.getConfiguration by reflect
     */
    fun getConfigurationCompat(manager: ConfigurationManager, file: VirtualFile): Configuration? {
        return try {
            val clazz = manager.javaClass

            // 获取 myCache 字段
            val cacheField = clazz.getDeclaredField("myCache")
            cacheField.isAccessible = true
            val cache = cacheField.get(manager) as? MutableMap<VirtualFile, Configuration>
                ?: return null

            // 如果缓存中有就返回
            cache[file]?.let { return it }

            // 反射调用 create(file) 方法
            val createMethod = clazz.getDeclaredMethod("create", VirtualFile::class.java)
            createMethod.isAccessible = true
            val config = createMethod.invoke(manager, file) as? Configuration ?: return null

            // 放入缓存并返回
            cache[file] = config
            config
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}