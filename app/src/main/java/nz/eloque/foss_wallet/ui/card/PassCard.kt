package nz.eloque.foss_wallet.ui.card

import android.os.Build
import android.util.DisplayMetrics
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.Tag
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

object PassCardDefaults {
    /** The padding of the content */
    val padding = 12.dp

    /** The space between rows and columns */
    val spacing = 12.dp

    val labelStyle
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)

    val labelStyleStripImage
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography.bodyLarge
}

@Composable
fun PassCard(
    localizedPass: LocalizedPassWithTags,
    allTags: Set<Tag>,
    modifier: Modifier = Modifier,
    barcode: @Composable () -> Unit = {},
    onTagClick: (Tag) -> Unit = {},
    onTagAdd: (Tag) -> Unit = {},
    onTagCreate: (Tag) -> Unit = {},
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    showEntirePass: Boolean = true,
) {
    val pass = localizedPass.pass
    val context = LocalContext.current
    var boardingPassCutoutOffset by remember { mutableFloatStateOf(0f) }
    val shape =
        when (pass.type) {
            is PassType.Generic -> GenericPassShape
            is PassType.Boarding -> BoardingPassShape(boardingPassCutoutOffset)
            is PassType.Event -> EventTicketShape()
            is PassType.Coupon -> CouponShape()
            is PassType.StoreCard -> StoreCardShape
        }
    val horizontalPaddingModifier = Modifier.padding(horizontal = PassCardDefaults.padding)

    PassCardBase(
        colors = pass.colors ?: PassColors.Fallback,
        shape = shape,
        footer = {
            PassCardFooter(
                localizedPass = localizedPass,
                allTags = allTags,
                onTagClick = onTagClick,
                onTagAdd = onTagAdd,
                onTagCreate = onTagCreate,
                readOnly = !showEntirePass,
                showDates = !showEntirePass,
            )
        },
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        backgroundImage = if (pass.type is PassType.Event && !pass.hasStrip) pass.backgroundFile(context) else null,
    ) {
        HeaderRow(
            pass = pass,
            modifier = horizontalPaddingModifier,
            isSelectable = showEntirePass,
        )

        val passFields: List<List<PassField>>
        when (pass.type) {
            PassType.Event if pass.hasStrip -> {
                StripImagePrimary(
                    primaryFields = pass.primaryFields,
                    stripImage = pass.stripFile(context),
                    modifier = Modifier.height(90.dp),
                    isSelectable = showEntirePass,
                )
                passFields = listOf(pass.secondaryFields, pass.auxiliaryFields)
            }

            is PassType.Generic, PassType.Event -> {
                ThumbnailPrimary(
                    primaryFields = pass.primaryFields,
                    thumbnail = pass.thumbnailFile(context),
                    modifier = horizontalPaddingModifier.height(90.dp),
                    secondaryFields = if (pass.type is PassType.Event) pass.secondaryFields else null,
                    isSelectable = showEntirePass,
                    labelColor = pass.colors?.label ?: Color.Unspecified,
                )
                passFields =
                    if (pass.type is PassType.Event) {
                        listOf(pass.auxiliaryFields)
                    } else {
                        listOf(pass.secondaryFields, pass.auxiliaryFields)
                    }
            }

            is PassType.Boarding -> {
                val passCardPaddingPx = with(LocalDensity.current) { (PassCardDefaults.padding).toPx() }
                BoardingPrimary(
                    primaryFields = pass.primaryFields,
                    transitType = pass.type.transitType,
                    modifier =
                        horizontalPaddingModifier.height(70.dp).onPlaced {
                            // position of the lower edge of the boarding primary in the pass card
                            boardingPassCutoutOffset = it.positionInParent().y + it.size.height + passCardPaddingPx
                        },
                    isSelectable = showEntirePass,
                    labelColor = pass.colors?.label ?: Color.Unspecified,
                )
                passFields = listOf(pass.auxiliaryFields, pass.secondaryFields)
            }

            is PassType.Coupon, PassType.StoreCard -> {
                StripImagePrimary(
                    primaryFields = pass.primaryFields,
                    stripImage = pass.stripFile(context),
                    modifier = Modifier.height(150.dp),
                    isSelectable = showEntirePass,
                )
                passFields = listOf(pass.secondaryFields + pass.auxiliaryFields)
            }
        }

        if (showEntirePass) {
            passFields.forEach {
                FieldsRow(
                    fields = it,
                    labelColor = pass.colors?.label ?: Color.Unspecified,
                    modifier = horizontalPaddingModifier,
                )
            }
            Column(
                modifier = horizontalPaddingModifier,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (pass.type is PassType.Boarding && pass.hasFooter) {
                    AsyncImage(
                        model = pass.footerFile(context),
                        contentDescription = stringResource(R.string.image),
                        modifier = Modifier.height(16.dp),
                    )
                }
                barcode()
            }
        }
    }
}

@Composable
private fun PassCardBase(
    colors: PassColors,
    shape: Shape,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    backgroundImage: File? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val fixedDensity = DisplayMetrics.DENSITY_DEVICE_STABLE.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    val borderColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    CompositionLocalProvider(
        LocalDensity provides Density(density = fixedDensity, fontScale = 1f),
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = colors.background,
            contentColor = colors.foreground,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.1f)),
        ) {
            Box(
                modifier = if (onClick != null) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick) else Modifier,
            ) {
                // blur effect is only supported on Android 12 and above
                if (backgroundImage != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    AsyncImage(
                        model = backgroundImage,
                        contentDescription = null,
                        modifier = Modifier.blur(16.dp).matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column {
                    Column(
                        modifier = Modifier.padding(top = PassCardDefaults.padding),
                        verticalArrangement = Arrangement.spacedBy(PassCardDefaults.spacing),
                        content = content,
                    )
                    Box(modifier = Modifier.heightIn(min = PassCardDefaults.padding)) {
                        footer()
                    }
                }
            }
        }
    }
}
