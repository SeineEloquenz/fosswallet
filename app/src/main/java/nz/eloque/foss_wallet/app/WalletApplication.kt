package nz.eloque.foss_wallet.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.svg.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import nz.eloque.foss_wallet.persistence.BackfillWorker
import nz.eloque.foss_wallet.persistence.SettingsStore
import kotlin.concurrent.thread

@HiltAndroidApp
class WalletApplication :
    Application(),
    Configuration.Provider,
    SingletonImageLoader.Factory {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var settingsStore: SettingsStore

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        // WorkManager only reschedules work dropped by a force stop when it initializes, so it must start with the app.
        thread(name = "WorkManagerInit") { BackfillWorker.enqueueIfNeeded(WorkManager.getInstance(this), settingsStore) }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
