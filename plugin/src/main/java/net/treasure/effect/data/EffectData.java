package net.treasure.effect.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.treasure.color.scheme.ColorScheme;
import net.treasure.common.Permissions;
import net.treasure.core.TreasurePlugin;
import net.treasure.effect.Effect;
import net.treasure.effect.EffectManager;
import net.treasure.effect.TickHandler;
import net.treasure.util.TimeKeeper;
import net.treasure.util.tuples.Pair;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Setter
@Getter
@RequiredArgsConstructor
public class EffectData {

    public final Player player;

    private boolean effectsEnabled = true, notificationsEnabled;

    private Map<String, ColorScheme> colorPreferences = new HashMap<>();

    // Fields for current effect
    private Effect currentEffect;
    private List<Pair<String, Double>> variables;
    private List<TickHandler> tickHandlers;

    // Handler Event
    private HandlerEvent currentEvent;

    private Vector lastVector;
    private int notMovingInterval;
    private boolean moving;

    // Last time elytra boosted with firework
    private long lastBoostMillis;

    // Target entity
    private Entity targetEntity;

    public EffectData(List<Pair<String, Double>> variables) {
        this.player = null;
        this.variables = variables;
    }

    public void setCurrentEffect(Effect currentEffect) {
        if (player == null) return;

        this.resetEvent();

        this.currentEffect = currentEffect;

        if (currentEffect == null) {
            this.variables = null;
            this.tickHandlers = null;

            return;
        }

        if (currentEffect.getColorGroup() != null && getColorPreference(currentEffect) == null)
            setColorPreference(currentEffect, currentEffect.getColorGroup().getAvailableOptions().get(0).colorScheme());
        currentEffect.initialize(player, this, debugModeEnabled);
    }

    public void setColorPreference(Effect effect, ColorScheme scheme) {
        colorPreferences.put(effect.getKey(), scheme);
    }

    public ColorScheme getColorPreference(Effect effect) {
        return colorPreferences.get(effect.getKey());
    }

    public Pair<String, Double> getVariable(String variable) {
        if (variable == null)
            return null;
        for (var pair : variables)
            if (pair.getKey().equals(variable))
                return pair;
        if (player == null)
            return null;
        var value = switch (variable) {
            case "playerYaw" -> (double) player.getLocation().getYaw();
            case "playerPitch" -> (double) player.getLocation().getPitch();
            case "playerX" -> player.getLocation().getX();
            case "playerY" -> player.getLocation().getY();
            case "playerZ" -> player.getLocation().getZ();
            case "velocityX" -> player.getVelocity().getX();
            case "velocityY" -> player.getVelocity().getY();
            case "velocityZ" -> player.getVelocity().getZ();
            case "velocityLength" -> player.getVelocity().lengthSquared();
            case "lastBoostMillis", "LBM" -> (double) lastBoostMillis;
            default -> null;
        };
        return value == null ? null : new Pair<>(variable, value);
    }

    public String replaceVariables(String line) {
        StringBuilder builder = new StringBuilder();

        var array = line.toCharArray();
        int startPos = -1;
        StringBuilder variable = new StringBuilder();
        StringBuilder format = new StringBuilder();
        for (int pos = 0; pos < array.length; pos++) {
            var c = array[pos];
            switch (c) {
                case '{' -> {
                    if (startPos != -1) return null;
                    startPos = pos;
                }
                case '}' -> {
                    if (startPos == -1) return null;

                    var result = variable.toString();
                    var p = getVariable(result);
                    double value;
                    if (p == null) {
                        Double preset = switch (result) {
                            case "TICK" -> (double) TimeKeeper.getTimeElapsed();
                            case "PI" -> Math.PI;
                            case "RANDOM" -> Math.random();
                            case "currentTimeMillis", "CTM" -> (double) System.currentTimeMillis();
                            default -> null;
                        };
                        if (preset == null) break;
                        value = preset;
                    } else
                        value = p.getValue();

                    if (!format.isEmpty())
                        builder.append(new DecimalFormat(format.toString(), new DecimalFormatSymbols(Locale.ENGLISH)).format(value));
                    else
                        builder.append(value);

                    startPos = -1;
                    variable = new StringBuilder();
                    format = new StringBuilder();
                }
                case ':' -> {
                    if (startPos != -1) {
                        format = variable;
                        variable = new StringBuilder();
                    } else
                        builder.append(c);
                }
                default -> {
                    if (startPos != -1)
                        variable.append(c);
                    else
                        builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    public boolean canSeeEffects() {
        return player != null && effectsEnabled && (!EffectManager.EFFECTS_VISIBILITY_PERMISSION || player.hasPermission(Permissions.CAN_SEE_EFFECTS));
    }

    // Moving
    public void increaseInterval() {
        this.notMovingInterval += 5;
        if (this.notMovingInterval > 40)
            this.moving = false;
    }

    public void resetInterval() {
        this.notMovingInterval = 0;
        this.moving = true;
    }

    // Handler Event
    public void setCurrentEvent(HandlerEvent currentEvent) {
        this.currentEvent = currentEvent;
        this.targetEntity = null;
    }

    public void resetEvent() {
        this.currentEvent = null;
        this.targetEntity = null;
    }
}