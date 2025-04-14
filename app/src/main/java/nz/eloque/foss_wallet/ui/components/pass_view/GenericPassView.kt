package nz.eloque.foss_wallet.ui.components.pass_view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType

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
        HorizontalDivider()
        AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            if (showFront) {
                if (pass.primaryFields.isNotEmpty()) {
                    PassFields(pass.primaryFields)
                    HorizontalDivider()
                }
                if (pass.auxiliaryFields.isNotEmpty()) {
                    PassFields(pass.auxiliaryFields)
                    HorizontalDivider()
                }
                if (pass.secondaryFields.isNotEmpty()) {
                    PassFields(pass.secondaryFields)
                    HorizontalDivider()
                }
                BarcodesView(pass.barCodes)
            } else {
                PassFields(pass.backFields)
            }
        }
        AsyncPassImage(model = pass.logoFile(context), modifier = Modifier.fillMaxWidth())
        AsyncPassImage(model = pass.footerFile(context), modifier = Modifier.fillMaxWidth())
    }
}

@Preview
@Composable
private fun GenericPassPreview() {
    val pass = Pass(
        0,
        "KSC - SV Elversberg",
        1,
        "KSC",
        "serial",
        PassType.BOARDING,
        HashSet(),
        false,
        false,
        false,
        false
    ).also {
        it.relevantDate = 1800000000L
        it.headerFields = mutableListOf(
            nz.eloque.foss_wallet.model.PassField("block", "Block", "S1"),
            nz.eloque.foss_wallet.model.PassField("seat", "Seat", "47"),
        )
        it.primaryFields = mutableListOf(
            nz.eloque.foss_wallet.model.PassField("name", "Name", "Max Mustermann"),
            nz.eloque.foss_wallet.model.PassField("seat", "Seat", "47"),
        )
        it.auxiliaryFields = mutableListOf(
            nz.eloque.foss_wallet.model.PassField("block", "Block", "S1 | Gegengerade"),
            nz.eloque.foss_wallet.model.PassField("seat", "Seat", "36E"),
        )
        it.secondaryFields = mutableListOf(
            nz.eloque.foss_wallet.model.PassField("data1", "data1", "Longer Value here i guess"),
            nz.eloque.foss_wallet.model.PassField("data2", "data2", "Shorter Value"),
        )
    }
    GenericPassView(pass, true)
}