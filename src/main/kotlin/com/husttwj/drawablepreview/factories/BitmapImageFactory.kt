package com.husttwj.drawablepreview.factories

import com.husttwj.drawablepreview.drawables.Utils
import com.husttwj.drawablepreview.settings.SettingsUtils
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object BitmapImageFactory {
    fun createBitmapImage(path: String): BufferedImage? {
        return ImageIO.read(File(path))?.let {
            val output = BufferedImage(SettingsUtils.getPreviewSize(), SettingsUtils.getPreviewSize(), BufferedImage.TYPE_INT_ARGB)
            Utils.drawResizedIcon(it, output)
            output
        }
    }
}