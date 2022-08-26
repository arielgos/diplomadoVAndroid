package com.agos.astore

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class Utils {
    companion object {
        const val tag = "AmazingStore"
        const val dateFormat = "dd/MM/yyyy HH:mm"
        const val imageWith = 1500
        const val imageHeight = 1500
        const val imageUrl = "https://firebasestorage.googleapis.com/v0/b/diplomadov-f5e39.appspot.com/o/"
        const val requestSearchImage = 10000
        const val requestNewProduct = 2000
    }
}

/**
 * Extensions
 */

fun Bitmap.createFile(path: String): File {
    val file = File(path)
    val bos = ByteArrayOutputStream();
    this.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val bitmapData = bos.toByteArray()
    val fos = FileOutputStream(file)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
    return file
}

fun String?.toIntOrDefault(default: Int = 0): Int {
    return this?.toIntOrNull() ?: default
}

fun Double.format(digits: Int = 2) = "%.${digits}f".format(this)
