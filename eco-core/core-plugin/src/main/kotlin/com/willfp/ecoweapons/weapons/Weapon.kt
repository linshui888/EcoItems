package com.willfp.ecoweapons.weapons

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.config.interfaces.JSONConfig
import com.willfp.eco.core.display.Display
import com.willfp.eco.core.items.CustomItem
import com.willfp.eco.core.items.Items
import com.willfp.eco.core.items.builder.ItemStackBuilder
import com.willfp.eco.core.recipe.Recipes
import com.willfp.libreforge.Holder
import com.willfp.libreforge.conditions.Conditions
import com.willfp.libreforge.effects.Effects
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.jetbrains.annotations.NotNull
import java.util.Objects

class Weapon(
    private val config: JSONConfig,
    private val plugin: EcoPlugin
) : Holder {
    val id = config.getString("name")

    override val effects = config.getSubsections("effects").mapNotNull {
        Effects.compile(it, "Weapon ID $id")
    }.toSet()

    override val conditions = config.getSubsections("conditions").mapNotNull {
        Conditions.compile(it, "Weapon ID $id")
    }.toSet()

    val itemStack: ItemStack = run {
        val itemConfig = config.getSubsection("item")
        ItemStackBuilder(Items.lookup(itemConfig.getString("item")).item).apply {
            setDisplayName(itemConfig.getFormattedString("displayName"))
            addItemFlag(
                *itemConfig.getStrings("flags")
                    .mapNotNull { ItemFlag.valueOf(it.uppercase()) }
                    .toTypedArray<@NotNull ItemFlag>()
            )
            setUnbreakable(itemConfig.getBool("unbreakable"))
            addLoreLines(
                itemConfig.getFormattedStrings("lore").map { "${Display.PREFIX}$it" })
            writeMetaKey(
                this@Weapon.plugin.namespacedKeyFactory.create("weapon"),
                PersistentDataType.STRING,
                this@Weapon.id
            )
        }.build()
    }

    val customItem = CustomItem(
        plugin.namespacedKeyFactory.create(id),
        { test -> WeaponUtils.getWeaponFromItem(test) == this },
        itemStack
    ).apply { register() }

    val craftingRecipe = if (config.getBool("item.craftable")) {
        Recipes.createAndRegisterRecipe(
            plugin,
            id,
            itemStack,
            config.getStrings("item.recipe")
        )
    } else null

    val fuelEnabled = config.getBool("fuel.enabled")

    val fuelItem: ItemStack = run {
        val itemConfig = config.getSubsection("fuel")
        ItemStackBuilder(Items.lookup(itemConfig.getString("item")).item).apply {
            setDisplayName(itemConfig.getFormattedString("displayName"))
            addLoreLines(
                itemConfig.getFormattedStrings("lore").map { "${Display.PREFIX}$it" })
            writeMetaKey(
                this@Weapon.plugin.namespacedKeyFactory.create("fuel"),
                PersistentDataType.STRING,
                this@Weapon.id
            )
        }.build()
    }

    val fuelRecipe = if (config.getBool("fuel.craftable")) {
        Recipes.createAndRegisterRecipe(
            plugin,
            id,
            fuelItem.clone().apply { amount = config.getInt("fuel.recipeGiveAmount") },
            config.getStrings("fuel.recipe")
        )
    } else null

    override fun equals(other: Any?): Boolean {
        if (other !is Weapon) {
            return false
        }

        return this.id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(this.id)
    }

    override fun toString(): String {
        return "Weapon{$id}"
    }
}