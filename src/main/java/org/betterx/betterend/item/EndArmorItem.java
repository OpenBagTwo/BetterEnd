package org.betterx.betterend.item;

import net.minecraft.world.entity.EquipmentSlot;
import org.betterx.bclib.interfaces.ItemModelProvider;
import org.betterx.betterend.BetterEnd;
import org.betterx.betterend.item.material.EndArmorTier;
import org.betterx.betterend.registry.EndItems;
import org.betterx.wover.complex.api.equipment.ArmorSlot;
import org.betterx.wover.complex.api.equipment.ArmorTier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class EndArmorItem extends ArmorItem implements ItemModelProvider {
    public static final ResourceLocation BASE_BLINDNESS_RESISTANCE = BetterEnd.C.mk("base_blindness_resistance");
    public static final ResourceLocation[] KNOCKBACK_RESISTANCES = {
        BetterEnd.C.mk("base_knockback_resistance_helmet"),
        BetterEnd.C.mk("base_knockback_resistance_chestplate"),
        BetterEnd.C.mk("base_knockback_resistance_leggings"),
        BetterEnd.C.mk("base_knockback_resistance_boots")};
    public static final ResourceLocation MAX_HEALTH_BOOST = BetterEnd.C.mk("max_health_boost");
    public static final ResourceLocation[] TOUGHNESS_BOOSTS = {
        BetterEnd.C.mk("toughness_boost_helmet"),
        BetterEnd.C.mk("toughness_boost_chestplate"),
        BetterEnd.C.mk("toughness_boost_leggings"),
        BetterEnd.C.mk("toughness_boost_boots")
    };
    public static final ResourceLocation[] ARMOR_BOOSTS = {
        BetterEnd.C.mk("armor_boost_helmet"),
        BetterEnd.C.mk("armor_boost_chestplate"),
        BetterEnd.C.mk("armor_boost_leggings"),
        BetterEnd.C.mk("armor_boost_boots")
    };

    public static Properties createDefaultEndArmorSettings(ArmorSlot slot, ArmorTier tier) {
        var values = tier.getValues(slot);
        if (values == null) {
            throw new IllegalArgumentException("Values for " + slot + " are not defined for " + tier);
        }

        return EndItems.defaultSettings().durability(slot.armorType.getDurability(values.durability()));
    }

    public static ItemAttributeModifiers.Builder startAttributeBuilder(
            ArmorSlot slot,
            ArmorTier tier
    ) {
        return startAttributeBuilder(slot, tier,
                tier.armorMaterial
                        .value()
                        .getDefense(slot.armorType),
                tier.armorMaterial
                        .value()
                        .toughness(),
                0.0f
        );
    }

    public static ItemAttributeModifiers.Builder startAttributeBuilder(
            ArmorSlot slot,
            ArmorTier tier,
            int defense,
            float toughness,
            float knockbackResistance
    ) {
        EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(slot.armorType.getSlot());

        int boosterIdx = 0;
        if (slot == ArmorSlot.CHESTPLATE_SLOT){
            boosterIdx = 1;
        } else if (slot == ArmorSlot.LEGGINGS_SLOT) {
            boosterIdx = 2;
        } else if (slot == ArmorSlot.BOOTS_SLOT) {
            boosterIdx = 3;
        }

        final ItemAttributeModifiers.Builder builder = ItemAttributeModifiers
                .builder()
                .add(
                    Attributes.ARMOR,
                    new AttributeModifier(
                            ARMOR_BOOSTS[boosterIdx],
                            defense,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
                )
                .add(
                    Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            TOUGHNESS_BOOSTS[boosterIdx],
                            toughness,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slotGroup
                );

        if (knockbackResistance > 0.0f) {
            builder.add(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(
                        KNOCKBACK_RESISTANCES[boosterIdx],
                        knockbackResistance,
                        AttributeModifier.Operation.ADD_VALUE
                ),
                slotGroup
            );
        }

        return builder;
    }

    public static Properties createDefaultEndArmorSettings(
            ArmorSlot slot,
            ArmorTier tier,
            ItemAttributeModifiers attributes
    ) {
        final var props = createDefaultEndArmorSettings(slot, tier)
                .rarity(Rarity.RARE);
        if (attributes != null) {
            props.attributes(attributes);
        }
        return props;

    }

    public EndArmorItem(ArmorTier tier, ArmorSlot slot, Properties settings) {
        super(tier.armorMaterial, slot.armorType, settings);
    }
}
