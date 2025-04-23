package com.husttwj.drawablepreview.drawables

import com.husttwj.drawablepreview.factories.IconPreviewFactory
import com.husttwj.drawablepreview.factories.XmlImageFactory
import com.husttwj.drawablepreview.drawables.dom.ColorDrawable
import com.husttwj.drawablepreview.drawables.dom.Drawable
import com.husttwj.drawablepreview.drawables.dom.IconDrawable
import com.husttwj.drawablepreview.util.LogUtil
import org.w3c.dom.Element

object ItemDrawableInflater {

    private const val DRAWABLE = "android:drawable"

    fun getDrawableWithInflate(element: Element): Drawable? {
        return getDrawable(element).let {
            val drawable = it.second
            val elementToUse = it.first
            if (elementToUse != null) {
                drawable?.inflate(elementToUse)
            }
            drawable
        }
    }

    fun getDrawable(element: Element): Pair<Element?, Drawable?> {
        if (element.hasAttribute(DRAWABLE)) {
            return null to getDrawableFromAttribute(element)
        } else if (element.hasChildNodes()) {
            return getDrawableFromChild(element)
        }
        return element to null
    }

    private fun getDrawableFromAttribute(element: Element): Drawable? {
        val drawableAttr = element.getAttribute(DRAWABLE)
        if (drawableAttr.startsWith("#")) {
            return ColorDrawable(drawableAttr)
        } else {
            val drawable = XmlImageFactory.getDrawable(drawableAttr)
            //LogUtil.d("getDrawableFromAttribute() -> (drawable == null):${drawable == null}")
            if (drawable != null) {
                return drawable
            }

            val file = Utils.getPsiFileFromPath(drawableAttr)
            //LogUtil.d("getDrawableFromAttribute() -> (file == null):${file == null}")
            if (file != null) {
                return IconDrawable().apply { childImage = IconPreviewFactory.getImage(file) }
            }
            return null
        }
    }

    private fun getDrawableFromChild(element: Element): Pair<Element, Drawable?> {
        element.childNodes.forEachAsElement { childNode ->
            return childNode to DrawableInflater.getDrawable(childNode)
        }
        return element to null
    }
}