package nz.eloque.foss_wallet

import java.io.File
import java.nio.charset.StandardCharsets

val PASSES: List<String> = listOf(
    "deutschlandticket",
    "europapark",
    "eurostar",
    "ksc",
    "oeat",
    "swiss",
    "ticketpay",
    "vector"
)

fun loadJson(passName: String): String? {
    val file = File("src/test/res/$passName.json")
    return file.inputStream().bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
}