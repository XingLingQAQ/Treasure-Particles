package net.treasure.particles.effect.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum HandlerEvent {
    ELYTRA("elytra", false),
    MOVING("moving", false),
    STANDING("standing", false),
    SNEAKING("sneaking", false),
    PROJECTILE("projectile"),
    MOB_KILL("mob-kill"),
    MOB_DAMAGE("mob-damage"),
    PLAYER_KILL("player-kill"),
    PLAYER_DAMAGE("player-damage"),
    TAKE_DAMAGE("take-damage");
    private final String translationKey;
    private final boolean isSpecial;

    HandlerEvent(String translationKey) {
        this.translationKey = translationKey;
        isSpecial = true;
    }
}