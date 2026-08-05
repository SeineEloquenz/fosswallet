package nz.eloque.foss_wallet.ui.card.glance

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.LocalizedPassWithTags
import nz.eloque.foss_wallet.model.PassColors
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.field.PassField
import java.io.File

/**
 * Glance-Pendant zu PassCard.
 *
 * WICHTIGE EINSCHRÄNKUNGEN von Glance ggü. normalem Compose (Grund für die Abweichungen unten):
 *  - Kein Canvas/Path/Outline-API: Glance rendert zu RemoteViews, das kann keinen beliebigen
 *    Compose-Shape-Code ausführen. GenericPassShape/StoreCardShape (reines RoundedCornerShape)
 *    bilden wir über cornerRadius() nach. EventTicketShape und CouponShape (vereinfacht) werden
 *    als statische, zur Laufzeit eingefärbte Vector-Drawables nachgebaut, weil unsere
 *    Widget-Größe wegen resizeMode="none" in der Provider-XML fix ist (180x90dp) - die
 *    Geometrie kann also einmalig vorberechnet werden. BoardingPassShape bleibt bewusst als
 *    einfaches Rechteck (siehe Kommentar bei isEventTicket/isCoupon in PassCardGlance()): die
 *    Cutout-Position hängt im Original von onPlaced()/positionInParent() der BoardingPrimary-Row
 *    ab, die es in dieser Mini-Karte gar nicht mehr gibt.
 *  - Kein blur()-Modifier -> das unscharfe Hintergrundbild (Event ohne Strip) entfällt.
 *  - Kein combinedClickable -> nur ein einfacher clickable()-Callback, Long-Click gibt
 *    es in Glance-Widgets nicht.
 *  - Kein Coil AsyncImage -> Bilder müssen synchron als Bitmap geladen und über
 *    ImageProvider(bitmap) übergeben werden (siehe loadBitmap()).
 *  - showEntirePass ist hier fest = false: Es werden nur Header + Primary-Bereich
 *    gerendert, keine Felder-Zeilen, kein Footer-Bild, kein Barcode, keine Tags.
 *  - Die feste 2:1-Ratio wird nicht vom Composable selbst erzwungen (Glance kennt kein
 *    aspectRatio()), sondern kommt aus der Widget-Größe (targetCellWidth=2, targetCellHeight=1,
 *    minWidth/minHeight=180dp/90dp, resizeMode="none") in der AppWidgetProviderInfo.
 */

private object PassCardGlanceDefaults {
    val padding = 8.dp
    val cornerRadius = 12.dp
    val logoSize = 28.dp

    /**
     * Eigener Fallback statt PassColors.Fallback: Letzteres ist
     * @Composable @ReadOnlyComposable und liest MaterialTheme.colorScheme,
     * das im Glance-Compose-Baum nicht automatisch verfügbar ist (Glance
     * nutzt sein eigenes GlanceTheme statt Material3-MaterialTheme).
     * Farben sind hier bewusst statisch gehalten und grob an ein neutrales
     * surfaceVariant/onSurface angelehnt.
     */
    fun fallbackColors(): PassColors =
        PassColors(
            background = Color(0xFFE7E0EC),
            foreground = Color(0xFF1D1B20),
            label = Color(0xFF49454F),
        )
}

@Composable
fun PassCardGlance(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    onClick: (() -> Unit)? = null,
) {
    val pass = localizedPass.pass
    val colors = pass.colors ?: PassCardGlanceDefaults.fallbackColors()
    val isEventTicket = pass.type is PassType.Event
    val isCoupon = pass.type is PassType.Coupon
    // PassType.Boarding, PassType.Generic und PassType.StoreCard fallen bewusst in den
    // Else-Zweig unten (abgerundetes Rechteck) - BoardingPassShape bekommt hier absichtlich
    // KEINE eigene Form (siehe Rückfrage im Chat: "BoardingPassShape als GenericPassShape").
    // Grund: die Cutout-Position im Original hängt von onPlaced()/positionInParent() der
    // BoardingPrimary-Row ab, die es in dieser reduzierten Widget-Karte (showEntirePass=false)
    // gar nicht mehr gibt - ein statischer Cutout hätte also keinen sinnvollen Ankerpunkt.

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .let { base -> if (onClick != null) base.clickable(onClick) else base },
    ) {
        when {
            isEventTicket -> {
                // Statischer Nachbau von EventTicketShape, siehe res/drawable/event_ticket_shape.xml.
                Image(
                    provider = ImageProvider(R.drawable.event_ticket_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(ColorProvider(colors.background)),
                )
            }
            isCoupon -> {
                // Vereinfachter statischer Nachbau von CouponShape, siehe
                // res/drawable/coupon_shape.xml (symmetrische statt versetzte Perforation).
                Image(
                    provider = ImageProvider(R.drawable.coupon_shape),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                    colorFilter = ColorFilter.tint(ColorProvider(colors.background)),
                )
            }
            else -> {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(colors.background))
                        .cornerRadius(PassCardGlanceDefaults.cornerRadius),
                ) {}
            }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    start = PassCardGlanceDefaults.padding,
                    end = PassCardGlanceDefaults.padding,
                    bottom = PassCardGlanceDefaults.padding,
                    // Die Kerbe von event_ticket_shape.xml dippt an der oberen Kantenmitte um
                    // 11.72dp nach innen - zusätzliches Top-Padding verhindert Kollision mit dem
                    // Header. Bei coupon_shape.xml sind die Perforationen nur 4dp tief, das
                    // normale Padding (8dp) reicht dort bereits aus.
                    top = PassCardGlanceDefaults.padding + if (isEventTicket) 12.dp else 0.dp,
                ),
        ) {
            HeaderRowGlance(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
            Box(modifier = GlanceModifier.height(4.dp)) {}
            PrimaryContentGlance(
                localizedPass = localizedPass,
                context = context,
                foreground = colors.foreground,
                labelColor = colors.label,
            )
        }
    }
}

@Composable
private fun HeaderRowGlance(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: Color,
    labelColor: Color,
) {
    val pass = localizedPass.pass
    val logoBitmap = remember(pass.logoFile(context)) { loadBitmap(pass.logoFile(context)) }
    // Original HeaderRow zeigt logoText als Titel, NICHT pass.organization/description
    // (das hatte ich in der ersten Fassung falsch angenommen). Zusätzlich werden dort
    // die headerFields rechtsbündig angezeigt; hier aus Platzgründen (2:1-Karte) nur
    // das erste.
    val firstHeaderField = pass.headerFields.firstOrNull()

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        if (logoBitmap != null) {
            Image(
                provider = ImageProvider(logoBitmap),
                contentDescription = null,
                modifier = GlanceModifier
                    .width(PassCardGlanceDefaults.logoSize)
                    .height(PassCardGlanceDefaults.logoSize),
                contentScale = ContentScale.Fit,
            )
            Box(modifier = GlanceModifier.width(6.dp)) {}
        }

        if (pass.logoText != null) {
            Text(
                text = pass.logoText,
                maxLines = 1,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(foreground),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
            )
        } else {
            Box(modifier = GlanceModifier.defaultWeight()) {}
        }

        if (firstHeaderField != null) {
            Column(horizontalAlignment = Alignment.Horizontal.End) {
                firstHeaderField.label?.let { label ->
                    Text(
                        text = label,
                        maxLines = 1,
                        style = TextStyle(
                            color = ColorProvider(labelColor),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                        ),
                    )
                }
                Text(
                    text = firstHeaderField.content.prettyPrint(),
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(foreground),
                        fontSize = 11.sp,
                    ),
                )
            }
        }
    }
}

/**
 * Ersetzt ThumbnailPrimary / StripImagePrimary / BoardingPrimary aus der
 * Compose-Variante. Da bei showEntirePass = false ohnehin nur wenig Platz
 * (2:1-Karte) zur Verfügung steht, wird bewusst nur der wichtigste
 * Primary-Wert plus – falls vorhanden – ein kleines Vorschaubild gezeigt.
 */
@Composable
private fun PrimaryContentGlance(
    localizedPass: LocalizedPassWithTags,
    context: Context,
    foreground: Color,
    labelColor: Color,
) {
    val pass = localizedPass.pass
    // getOrElse(0) { PassField.Empty } wie im Compose-Original (ThumbnailPrimary/BoardingPrimary),
    // damit auch bei fehlendem primaryField sauber "leer" statt null gerendert wird.
    val primaryField = pass.primaryFields.getOrElse(0) { PassField.Empty }

    val previewFile: File? = when (pass.type) {
        is PassType.Boarding -> null // Kerbe/Cutout-Form entfällt in Glance, daher kein Extra-Bild
        is PassType.Coupon, PassType.StoreCard -> pass.stripFile(context)
        is PassType.Event -> if (pass.hasStrip) pass.stripFile(context) else pass.thumbnailFile(context)
        is PassType.Generic -> pass.thumbnailFile(context)
    }
    val previewBitmap = remember(previewFile) { previewFile?.let { loadBitmap(it) } }

    Row(
        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Column(modifier = GlanceModifier.defaultWeight()) {
            primaryField.label?.let { label ->
                Text(
                    text = label,
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                )
            }
            Text(
                // .content ist vom Typ PassContent, kein String -> prettyPrint() liefert die
                // reine Textdarstellung. HTML-Links (AnnotatedString.fromHtml/parseHtml wie im
                // Compose-Original) werden von Glance-Text nicht unterstützt, daher hier bewusst
                // nur der reine Text ohne Link-Styling/HTML.
                text = primaryField.content.prettyPrint(),
                maxLines = 1,
                style = TextStyle(
                    color = ColorProvider(foreground),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                ),
            )
        }
        if (previewBitmap != null) {
            Image(
                provider = ImageProvider(previewBitmap),
                contentDescription = null,
                modifier = GlanceModifier
                    .width(48.dp)
                    .height(48.dp)
                    .cornerRadius(6.dp),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

/** Synchrones, einfaches Laden – für Widgets ausreichend, da Bilder lokal vorliegen. */
private fun loadBitmap(file: File?): android.graphics.Bitmap? {
    if (file == null || !file.exists()) return null
    return try {
        BitmapFactory.decodeFile(file.absolutePath)
    } catch (e: Exception) {
        null
    }
}
