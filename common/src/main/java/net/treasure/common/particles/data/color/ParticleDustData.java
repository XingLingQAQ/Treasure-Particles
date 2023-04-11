package net.treasure.common.particles.data.color;

import lombok.Getter;
import net.treasure.common.particles.data.ParticleData;
import org.bukkit.Color;

@Getter
public abstract class ParticleDustData extends ParticleData {
    protected float red, green, blue;
    protected float size;

    public ParticleDustData(Color color, float size) {
        this.red = color.getRed() / 255f;
        this.green = color.getGreen() / 255f;
        this.blue = color.getBlue() / 255f;
        this.size = size;
    }
}