package nz.eloque.foss_wallet.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlin.concurrent.thread

@HiltAndroidApp
class WalletApplication :
    Application(),
    Configuration.Provider,
    ImageLoaderFactory {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager only reschedules work dropped by a force stop when it initializes, so it must start with the app.
        thread(name = "WorkManagerInit") { WorkManager.getInstance(this) }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
