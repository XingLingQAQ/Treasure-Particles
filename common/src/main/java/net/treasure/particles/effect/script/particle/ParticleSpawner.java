package net.treasure.particles.effect.script.particle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.treasure.particles.TreasureParticles;
import net.treasure.particles.color.data.ColorData;
import net.treasure.particles.color.data.RandomNoteColorData;
import net.treasure.particles.color.data.duo.DuoImpl;
import net.treasure.particles.effect.data.EffectData;
import net.treasure.particles.effect.handler.HandlerEvent;
import net.treasure.particles.effect.script.Script;
import net.treasure.particles.effect.script.argument.type.IntArgument;
import net.treasure.particles.effect.script.argument.type.RangeArgument;
import net.treasure.particles.effect.script.argument.type.VectorArgument;
import net.treasure.particles.util.math.MathUtils;
import net.treasure.particles.util.math.Vectors;
import net.treasure.particles.util.nms.particles.ParticleBuilder;
import net.treasure.particles.util.nms.particles.ParticleEffect;
import net.treasure.particles.util.nms.particles.Particles;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@Accessors(fluent = true)
@NoArgsConstructor
@AllArgsConstructor
public class ParticleSpawner extends Script {

    protected ParticleEffect particle;
    protected ParticleOrigin origin;

    protected VectorArgument position;
    protected VectorArgument offset;
    protected VectorArgument multiplier;

    protected ColorData colorData;
    protected Object particleData;
    protected IntArgument amount;
    protected RangeArgument speed, size;
    protected boolean directional = false;

    protected boolean longDistance = false;

    @Nullable
    public ParticleContext tick(Player player, EffectData data, HandlerEvent event, boolean configureOffset, boolean rotatePos) {
        var entity = switch (event) {
            case ELYTRA, STANDING, MOVING, SNEAKING, TAKE_DAMAGE -> player;
            case MOB_KILL, PLAYER_KILL, PROJECTILE, MOB_DAMAGE, PLAYER_DAMAGE -> data.getTargetEntity();
        };
        if (entity == null) return null;
        var origin = switch (this.origin) {
            case HEAD -> entity instanceof Player p ? p.getEyeLocation() : entity.getLocation();
            case FEET -> entity.getLocation();
            case WORLD -> new Location(entity.getWorld(), 0, 0, 0);
        };
        var direction = origin.getDirection();

        if (multiplier != null)
            origin.add(direction.clone().multiply(multiplier.get(player, this, data)));

        var builder = new ParticleBuilder(particle);

        if (amount != null)
            builder.amount(amount.get(player, this, data));

        if (speed != null)
            builder.speed(speed.get(player, this, data));

        builder.longDistance(longDistance);

        // Rotations
        var angleP = Math.toRadians(origin.getPitch());
        var cosP = MathUtils.cos(angleP);
        var sinP = MathUtils.sin(angleP);

        var angleY = Math.toRadians(-origin.getYaw());
        var cosY = MathUtils.cos(angleY);
        var sinY = MathUtils.sin(angleY);

        // Position
        var pos = this.position != null ? this.position.get(player, this, data) : new Vector(0, 0, 0);
        if (rotatePos)
            origin.add(rotate(direction, pos, cosP, sinP, cosY, sinY));
        else
            origin.add(pos);

        // Offset
        if (configureOffset) {
            var offset = this.offset != null ? this.offset.get(player, this, data) : null;
            if (offset != null) offset.add(rotate(direction, offset, cosP, sinP, cosY, sinY));
            if (offset != null)
                builder.offset(offset);
        }

        // Viewers
        var playerManager = TreasureParticles.getPlayerManager();
        builder.viewers(viewer -> {
            var d = playerManager.getEffectData(viewer);
            return d != null && d.canSeeEffects();
        });

        return new ParticleContext(builder, origin, direction, cosP, sinP, cosY, sinY);
    }

    public void updateParticleData(ParticleBuilder builder, Player player, EffectData data) {
        if (particleData != null) {
            builder.data(particleData);
            return;
        }

        if (colorData == null) {
            particleData = Particles.NMS.getParticleParam(particle);
            builder.data(particleData);
            return;
        }

        if (particle.hasProperty(ParticleEffect.Property.DUST)) {
            var size = this.size != null ? this.size.get(player, this, data) : 1;
            if (particle.equals(ParticleEffect.DUST_COLOR_TRANSITION))
                if (colorData instanceof DuoImpl duo) {
                    var pair = duo.nextDuo();
                    builder.data(Particles.NMS.getColorTransitionData(pair.getKey(), pair.getValue(), size));
                } else
                    builder.data(Particles.NMS.getColorTransitionData(colorData.next(data), colorData.tempNext(data), size));
            else
                builder.data(Particles.NMS.getDustData(colorData.next(data), size));
        } else if (particle.hasProperty(ParticleEffect.Property.OFFSET_COLOR)) {
            if (particle.equals(ParticleEffect.NOTE) && colorData.isNote()) {
                builder.data(Particles.NMS.getParticleParam(particle));
                builder.noteColor(colorData instanceof RandomNoteColorData randomNoteColorData ? randomNoteColorData.random() : colorData.index());
            } else
                builder.offsetColor(colorData.next(data));
        }
    }

    public Location location(ParticleContext context, Vector vector) {
        return context.origin.clone().add(rotate(context, vector));
    }

    public Location location(ParticleContext context, double z, double r, float radius, boolean vertical) {
        var x = MathUtils.cos(r) * radius;
        var y = MathUtils.sin(r) * radius;
        return location(context, vertical ? new Vector(x, y, z) : new Vector(x, z, y));
    }

    public Location location(ParticleContext context, double r, float radius, boolean vertical) {
        var x = MathUtils.cos(r) * radius;
        var y = MathUtils.sin(r) * radius;
        return location(context, vertical ? new Vector(x, y, 0) : new Vector(x, 0, y));
    }

    public Vector rotate(ParticleContext context, Vector vector) {
        return rotate(context.direction, vector, context.cosP, context.sinP, context.cosY, context.sinY);
    }

    public Vector rotate(Vector direction, Vector vector, double cosP, double sinP, double cosY, double sinY) {
        if (directional) {
            Vectors.rotateAroundAxisX(vector, cosP, sinP);
            Vectors.rotateAroundAxisY(vector, cosY, sinY);
            return direction.clone().add(vector);
        }
        return vector;
    }

    @Override
    public TickResult tick(Player player, EffectData data, HandlerEvent event, int times) {
        return null;
    }

    @Override
    public Script clone() {
        return null;
    }
}