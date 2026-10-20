package fantasy.gradle.content.armor

import fantasy.gradle.resources.json
import fantasy.gradle.minecraft.*

import com.google.gson.annotations.SerializedName

internal data class TagResource(val values: List<String>, val replace: Boolean = false)

internal data class ItemModelResource(
    val textures: Map<String, String>,
    val overrides: List<ModelOverride>? = null,
    val parent: String = "item/generated",
)

internal data class ModelOverride(val predicate: Map<String, Double>, val model: String)
internal data class ItemDefinition(val model: ItemModelNode)

internal sealed interface ItemModelNode

internal data class ModelReference(
    val model: String,
    val tints: List<Tint>? = null,
) : ItemModelNode {
    val type = "minecraft:model"
}

internal data class DyedCondition(
    @SerializedName("on_true") val onTrue: ItemModelNode,
    @SerializedName("on_false") val onFalse: ItemModelNode,
) : ItemModelNode {
    val type = "minecraft:condition"
    val property = "minecraft:has_component"
    val component = "minecraft:dyed_color"
}

internal sealed interface Tint
internal data class ConstantTint(val value: Int) : Tint {
    val type = "minecraft:constant"
}
internal data class DyeTint(@SerializedName("default") val defaultColor: Int = -1) : Tint {
    val type = "minecraft:dye"
}
