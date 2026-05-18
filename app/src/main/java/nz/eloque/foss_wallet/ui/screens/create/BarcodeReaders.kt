package nz.eloque.foss_wallet.ui.screens.create

import zxingcpp.BarcodeReader

object BarcodeReaders {
    val primary =
        BarcodeReader(
            BarcodeReader.Options(
                tryHarder = true,
                tryRotate = true,
                tryInvert = true,
            ),
        )

    val decoders =
        listOf(
            primary,
            BarcodeReader(
                BarcodeReader.Options(
                    tryHarder = true,
                    tryRotate = true,
                    tryInvert = true,
                    tryDenoise = true,
                    tryDownscale = false,
                    binarizer = BarcodeReader.Binarizer.GLOBAL_HISTOGRAM,
                ),
            ),
        )

    val symbolLocator =
        BarcodeReader(
            BarcodeReader.Options(
                tryHarder = true,
                tryRotate = true,
                tryInvert = true,
                tryDenoise = true,
                tryDownscale = false,
                returnErrors = true,
            ),
        )
}
