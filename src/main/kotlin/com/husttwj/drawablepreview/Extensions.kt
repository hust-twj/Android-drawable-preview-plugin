package com.husttwj.drawablepreview

fun String.getDigits() = try {
    this.replace("\\D+", "").toInt()
} catch (exception: Exception) {
    null
}