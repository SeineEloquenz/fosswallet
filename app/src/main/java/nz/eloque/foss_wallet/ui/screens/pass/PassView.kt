package nz.eloque.foss_wallet.ui.screens.pass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.PassCard
import nz.eloque.foss_wallet.ui.effects.ForceOrientation
import nz.eloque.foss_wallet.ui.effects.Orientation
import nz.eloque.foss_wallet.ui.screens.settings.SettingsSwitch
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassView(
    localizedPass: LocalizedPassWithTags,
    allTags: Set<Tag>,
    onTagClick: (Tag) -> Unit,
    onTagAdd: (Tag) -> Unit,
    onTagCreate: (Tag) -> Unit,
    barcodePosition: BarcodePosition,
    increaseBrightness: Boolean,
    onRenderingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
    val pass = localizedPass.pass
    val metadata = localizedPass.metadata

    val hasBarcodes = pass.barCodes.isNotEmpty()
    val hasLegacyRepresentation = pass.barCodes.any { it.hasLegacyRepresentation() }
    val context = LocalContext.current
    ForceOrientation(Orientation.Locked)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState()),
    ) {
        PassCard(
            localizedPass = localizedPass,
            allTags = allTags,
            onTagClick = onTagClick,
            onTagAdd = onTagAdd,
            onTagCreate = onTagCreate,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(25.dp),
            ) {
                if (hasBarcodes) {
                    AsyncPassImage(model = pass.footerFile(context))
                    BarcodesView(
                        legacyRendering = metadata.renderLegacy && hasLegacyRepresentation,
                        barcodes = pass.barCodes.toList(),
                        barcodePosition = barcodePosition,
                        increaseBrightness = increaseBrightness,
                    )
                }
            }
        }

        if (hasLegacyRepresentation) {
            Card {
                SettingsSwitch(
                    title = stringResource(R.string.compatibility_mode),
                    checked = metadata.renderLegacy,
                    onCheckedChange = onRenderingChange,
                )
            }
        }

        BackFields(pass.backFields)

        Spacer(Modifier.padding(16.dp).navigationBarsPadding())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PassPreview() {
    val kscTag = Tag("KSC", Color.Blue)
    val gameTag = Tag("Spiel", Color.Red)
    val allTags = setOf(kscTag, gameTag)

    val pass =
        Pass(
            "",
            "KSC - SV Elversberg",
            1,
            "KSC",
            "serial",
            PassType.Generic,
            HashSet(),
            Instant.ofEpochSecond(0),
            hasLogo = false,
            hasStrip = false,
            hasThumbnail = false,
            hasFooter = false,
            headerFields =
                mutableListOf(
                    PassField("block", "Block", PassContent.Plain("S1")),
                    PassField("seat", "Seat", PassContent.Plain("47")),
                ),
            primaryFields =
                mutableListOf(
                    PassField("name", "Name", PassContent.Plain("Max Mustermann")),
                    PassField("seat", "Seat", PassContent.Plain("47")),
                ),
            auxiliaryFields =
                mutableListOf(
                    PassField("block", "Block", PassContent.Plain("S1 | Gegengerade")),
                    PassField("seat", "Seat", PassContent.Plain("36E")),
                ),
            secondaryFields =
                mutableListOf(
                    PassField("data1", "data1", PassContent.Plain("Longer Value here i guess")),
                    PassField("data2", "data2", PassContent.Plain("Shorter Value")),
                ),
        )
    PassView(
        localizedPass = LocalizedPassWithTags(pass, PassMetadata(pass.id), allTags),
        allTags = allTags,
        onTagClick = {},
        onTagAdd = {},
        onTagCreate = {},
        barcodePosition = BarcodePosition.Center,
        increaseBrightness = false,
        onRenderingChange = {},
    )
}
