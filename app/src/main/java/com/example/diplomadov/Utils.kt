package com.example.diplomadov

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class Utils {
    companion object {
        const val tag = "AmazingStore"
        const val dateFormat = "dd/MM/yyyy HH:mm"
        const val requestImage = 10000
        const val imageWith = 1000
        const val imageHeight = 1000
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
