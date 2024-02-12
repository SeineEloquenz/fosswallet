package nz.eloque.foss_wallet.ui.components.pass_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass

@Composable
fun GenericPassView(
    pass: Pass,
    showFront: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        PassTopBar(pass)
        Divider()
        AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            if (showFront) {
                if (pass.primaryFields.isNotEmpty()) {
                    PassFields(pass.primaryFields)
                    Divider()
                }
                if (pass.auxiliaryFields.isNotEmpty()) {
                    PassFields(pass.auxiliaryFields)
                    Divider()
                }
                if (pass.secondaryFields.isNotEmpty()) {
                    PassFields(pass.secondaryFields)
                    Divider()
                }
                BarcodesView(pass.barCodes)
            } else {
                PassField(stringResource(R.string.serial_number), pass.serialNumber)
                PassField(stringResource(R.string.organization), pass.organization)
                PassFields(pass.backFields)
            }
        }
        AsyncPassImage(model = pass.logoFile(context), modifier = Modifier.fillMaxWidth())
        AsyncPassImage(model = pass.footerFile(context), modifier = Modifier.fillMaxWidth())
    }
}