package com.rick.criminalintent.util

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)

    // display size eixo x e eixo y
    return getScaledBitmap(path, size.x, size.y)
}

private fun getScaledBitmap(
    path: String,
    width: Int,
    height: Int
): Bitmap {
    // Read the dimensions of the image on disk
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    // dimensoes da imagem
    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    // calculate how much to scale down by
    var inSampleSize = 1
    if (srcHeight > height || srcWidth > width){
        val heightScale = srcHeight / height
        val widthScale = srcWidth / width

        val sampleScale = if (heightScale > widthScale) heightScale else widthScale
        inSampleSize = Math.round(sampleScale)
    }

    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize

    // Read in  and crate final bitmap
    return BitmapFactory.decodeFile(path, options)
}