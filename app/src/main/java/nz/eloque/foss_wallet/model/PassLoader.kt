package nz.eloque.foss_wallet.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

class InvalidPassException : Exception()
object PassLoader {

    private const val TAG = "PassLoader"
    fun load(inputStream: InputStream): RawPass {
        var passJson: JSONObject? = null
        var logo: Bitmap? = null
        var icon: Bitmap? = null
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry

            while (zip.nextEntry.also { entry = it } != null) {
                if (!entry!!.isDirectory) {
                    println("Reading file: ${entry.name}")
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    val baos = ByteArrayOutputStream()

                    while (zip.read(buffer).also { bytesRead = it } != -1) {
                        baos.write(buffer, 0, bytesRead)
                    }
                    if (entry.name == "pass.json") {
                        val content = baos.toString("UTF-8")
                        passJson = JSONObject(content)
                        println("Content:\n$content")
                    } else if (entry.name == ("logo.png")) {
                        logo = loadImage(baos)
                    } else if (entry.name == ("icon.png")) {
                        icon = loadImage(baos)
                    }
                }
            }
        }
        if (logo == null) {
            //TODO use actual placeholder bitmap
            logo = icon ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
        if (icon == null) {
            icon = logo
        }
        //TODO check signature before returning
        if (passJson != null) {
            return RawPass(passJson!!, icon!!, logo!!)
        } else {
            throw InvalidPassException()
        }
    }

    private fun loadImage(baos: ByteArrayOutputStream): Bitmap? {
        val array = baos.toByteArray()
        val image = BitmapFactory.decodeByteArray(array, 0, array.size)
        return if (image == null) {
            Log.w(TAG, "Failed parsing image from pkpass!")
            null
        } else {
            image
        }
    }
}
