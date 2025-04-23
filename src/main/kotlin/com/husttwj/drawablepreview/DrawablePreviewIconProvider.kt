package com.husttwj.drawablepreview

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import com.husttwj.drawablepreview.factories.IconPreviewFactory

class DrawablePreviewIconProvider : IconProvider() {

    override fun getIcon(element: PsiElement, flags: Int) = IconPreviewFactory.createIcon(element)
}