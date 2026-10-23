package fantasy.gradle.resources

import fantasy.gradle.content.armor.ArmorDefinition
import fantasy.gradle.content.armor.Piece

@DslMarker
annotation class TranslationDsl

class TranslationCatalog internal constructor(
    val namespace: String,
    val entries: Map<String, Map<String, String>>,
    private val armorIds: Map<String, Set<String>>,
) {
    fun resources(): Map<String, String> = entries.mapKeys { (locale, _) -> "assets/$namespace/lang/$locale.json" }.mapValues { json(it.value) }

    fun validateItems(ids: Set<String>, tabKey: String) {
        require("en_us" in entries) { "Required locale en_us is missing" }
        val expected = ids.map { "item.$namespace.$it" }.toSet()
        entries.forEach { (locale, values) ->
            val items = values.keys.filter { it.startsWith("item.$namespace.") }.toSet()
            require(items == expected && tabKey in values) {
                "$locale: missing translations ${expected - items}; unknown translations ${items - expected}"
            }
        }
    }

    fun validate(armor: List<ArmorDefinition>) {
        val known = armor.map { it.id }.toSet()
        armorIds.forEach { (locale, ids) ->
            require((ids - known).isEmpty()) { "Unknown armor translations in $locale: ${ids - known}" }
        }
        val english = requireNotNull(entries["en_us"]) { "Required locale en_us is missing" }
        val required = armor.flatMap { set -> Piece.entries.map { "item.$namespace.${set.id}_${it.id}" } }
        val missing = required.filter { english[it].isNullOrBlank() }
        require(missing.isEmpty()) { "Missing en_us armor names: $missing" }
    }
}

@TranslationDsl
class Translations internal constructor(private val namespace: String, private val tab: String?) {
    private val languages = linkedMapOf<String, Map<String, String>>()
    private val armorIds = linkedMapOf<String, Set<String>>()
    fun language(locale: String, configure: Language.() -> Unit) {
        require(locale.matches(Regex("[a-z]{2,3}_[a-z0-9_]+"))) { "Invalid locale: $locale" }
        require(locale !in languages) { "Duplicate locale: $locale" }
        val language = Language(namespace, tab).apply(configure)
        languages[locale] = language.entries.toMap()
        armorIds[locale] = language.armorIds.toSet()
    }
    fun build(): TranslationCatalog = TranslationCatalog(namespace, languages.toMap(), armorIds.toMap())
}

@TranslationDsl
class Language internal constructor(private val namespace: String, private val tab: String?) {
    fun item(id: String, name: String) = entry("item.$namespace.$id", name)
    fun tabTitle(name: String) = entry("itemGroup.$namespace.${requireNotNull(tab) { "Declare the creative tab ID" }}", name)

    internal val armorIds = linkedSetOf<String>()
    internal val entries = linkedMapOf<String, String>()

    private var names: ArmorNames? = null

    fun armorNames(helmet: String, chestplate: String, leggings: String, boots: String) {
        require(names == null) { "Armor name templates already defined" }
        names = ArmorNames(helmet, chestplate, leggings, boots)
    }

    fun armor(id: String, name: String? = null, configure: ArmorTranslation.() -> Unit) {
        require(id.matches(Regex("[a-z0-9_]+"))) { "Invalid armor ID: $id" }
        armorIds.add(id)
        val templates = requireNotNull(names) { "Declare armorNames before armor translations" }
        val translation = ArmorTranslation().apply(configure)
        for (piece in Piece.entries) {
            val key = "item.$namespace.${id}_${piece.id}"
            val itemName = translation.name(piece) ?: templates.forPiece(piece).replace(
                "{name}", requireNotNull(name) { "Missing name for $id/${piece.id}" },
            )
            entry(key, itemName)
            val description = translation.description(piece) ?: translation.description
            if (description != null) entry("$key.tooltip", description)
        }
    }

    fun entry(key: String, value: String) {
        require(value.isNotBlank()) { "Empty translation: $key" }
        require(entries.putIfAbsent(key, value) == null) { "Duplicate translation key: $key" }
    }
}

fun translations(namespace: String, tab: String? = null, configure: Translations.() -> Unit): TranslationCatalog {
    require(namespace.matches(Regex("[a-z0-9_.-]+"))) { "Invalid namespace: $namespace" }
    return Translations(namespace, tab).apply(configure).build()
}

private data class ArmorNames(
    val helmet: String,
    val chestplate: String,
    val leggings: String,
    val boots: String,
) {
    init {
        require(listOf(helmet, chestplate, leggings, boots).all { "{name}" in it }) {
            "Each armor name template must contain {name}"
        }
    }

    fun forPiece(piece: Piece): String = when (piece) {
        Piece.HELMET -> helmet
        Piece.CHESTPLATE -> chestplate
        Piece.LEGGINGS -> leggings
        Piece.BOOTS -> boots
    }
}

@TranslationDsl
class ArmorTranslation internal constructor() {
    var helmet: String? = null
    var chestplate: String? = null
    var leggings: String? = null
    var boots: String? = null
    var description: String? = null
    var helmetDescription: String? = null
    var chestplateDescription: String? = null
    var leggingsDescription: String? = null
    var bootsDescription: String? = null

    internal fun name(piece: Piece): String? = when (piece) {
        Piece.HELMET -> helmet
        Piece.CHESTPLATE -> chestplate
        Piece.LEGGINGS -> leggings
        Piece.BOOTS -> boots
    }

    internal fun description(piece: Piece): String? = when (piece) {
        Piece.HELMET -> helmetDescription
        Piece.CHESTPLATE -> chestplateDescription
        Piece.LEGGINGS -> leggingsDescription
        Piece.BOOTS -> bootsDescription
    }
}
