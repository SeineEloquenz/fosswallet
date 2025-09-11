package nz.eloque.foss_wallet.ui.screens.pass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.PassCard
import nz.eloque.foss_wallet.ui.effects.ForceOrientation
import nz.eloque.foss_wallet.ui.effects.Orientation
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassView(
    pass: Pass,
    barcodePosition: BarcodePosition,
    modifier: Modifier = Modifier,
    increaseBrightness: Boolean,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
    val context = LocalContext.current
    ForceOrientation(Orientation.Locked)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        PassCard(pass) { cardColors ->
            Column(
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AsyncPassImage(model = pass.footerFile(context), modifier = Modifier.fillMaxWidth())
                BarcodesView(pass.barCodes, barcodePosition, increaseBrightness)
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            BackFields(pass.backFields)
            Spacer(modifier = Modifier.padding(4.dp))
            Spacer(modifier = Modifier.imePadding())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PassPreview() {
    val pass = Pass(
        "",
        "KSC - SV Elversberg",
        1,
        "KSC",
        "serial",
        PassType.Generic(),
        HashSet(),
        Instant.ofEpochMilli(0),
        false,
        false,
        false,
        false,
        relevantDate = 1800000000L,
        headerFields = mutableListOf(
            PassField("block", "Block", PassContent.Plain("S1")),
            PassField("seat", "Seat", PassContent.Plain("47")),
        ),
        primaryFields = mutableListOf(
            PassField("name", "Name", PassContent.Plain("Max Mustermann")),
            PassField("seat", "Seat", PassContent.Plain("47")),
        ),
        auxiliaryFields = mutableListOf(
            PassField("block", "Block", PassContent.Plain("S1 | Gegengerade")),
            PassField("seat", "Seat", PassContent.Plain("36E")),
        ),
        secondaryFields = mutableListOf(
            PassField("data1", "data1", PassContent.Plain("Longer Value here i guess")),
            PassField("data2", "data2", PassContent.Plain("Shorter Value")),
        ),
    )
    PassView(pass, BarcodePosition.Center, increaseBrightness = false)
}