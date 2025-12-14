package nz.eloque.foss_wallet.ui.screens.create

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.loader.PassBitmaps


@HiltViewModel
class CreateViewModel @Inject constructor(
    application: Application,
    @param:ApplicationContext private val context: Context,
    private val passStore: PassStore,
) : AndroidViewModel(application) {

    fun addPass(pass: Pass) {
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.icon, null)!!
        val icon = drawableToBitmap(drawable, 64, 64)
        val logo = drawableToBitmap(drawable, 256, 256)

        passStore.create(
            pass = pass.copy(hasLogo = true),
            bitmaps = PassBitmaps(
                icon = icon,
                logo = logo,
                strip = null,
                thumbnail = null,
                footer = null
            )
        )
    }

    private fun drawableToBitmap(drawable: Drawable, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }

}