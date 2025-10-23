package nz.eloque.foss_wallet.quick_settings

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import nz.eloque.foss_wallet.MainActivity
import nz.eloque.foss_wallet.R

class WalletTileService : TileService() {

    override fun onStartListening() {
        qsTile.apply {
            label = this@WalletTileService.getString(R.string.app_name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = this@WalletTileService.getString(R.string.open)
            }
            icon = Icon.createWithResource(this@WalletTileService, R.drawable.qs_icon)
            state = Tile.STATE_INACTIVE
            updateTile()
        }
    }

    override fun onClick() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("from_tile", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            @SuppressLint("StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }
}