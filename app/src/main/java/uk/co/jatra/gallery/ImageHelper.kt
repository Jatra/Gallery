package uk.co.jatra.gallery

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.IOException

class ImageHelper(val context: Context) {
    @Throws(IOException::class)
    private fun createImageUri(fileNamePrefix: String, shouldSaveInPublicDir: Boolean): Uri {
        val prefix = if (isPrefixUsable(fileNamePrefix)) fileNamePrefix else "zipcar"
        val storageDir = if (shouldSaveInPublicDir) Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        ) else context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return Uri.fromFile(File.createTempFile(prefix, ".jpg", storageDir))
    }

    private fun isPrefixUsable(imageFileNamePrefix: String?): Boolean {
        return imageFileNamePrefix != null && imageFileNamePrefix.length >= 3
    }
}