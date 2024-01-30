package nz.eloque.foss_wallet.persistence

import android.graphics.Bitmap
import org.json.JSONObject

class RawPass(
    val passJson: JSONObject,
    val icon: Bitmap,
    val logo: Bitmap?,
    val strip: Bitmap?,
    val footer: Bitmap?)