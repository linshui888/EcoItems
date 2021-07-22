package com.willfp.ecoweapons.conditions.conditions;

import com.willfp.ecoweapons.conditions.Condition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ConditionBelowY extends Condition<Double> {
    public ConditionBelowY() {
        super("below-y", Double.class);
    }

    @Override
    public boolean isConditionMet(@NotNull final Player player,
                                  @NotNull final Double value) {
        return player.getLocation().getY() < value;
    }
}
