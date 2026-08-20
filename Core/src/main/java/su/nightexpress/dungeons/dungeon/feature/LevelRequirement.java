package su.nightexpress.dungeons.dungeon.feature;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.registry.level.LevelProvider;
import su.nightexpress.dungeons.registry.level.LevelRegistry;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;

public record LevelRequirement(@NonNull String provider, int minLevel, int maxLevel) implements Writeable {

    public LevelRequirement {
        provider = provider.toLowerCase();
    }

    @NonNull
    public static LevelRequirement read(@NonNull FileConfig config, @NonNull String path) {
        String provider = ConfigValue.create(path + ".Provider", "null").read(config);
        int minLevel = ConfigValue.create(path + ".MinLevel", -1).read(config);
        int maxLevel = ConfigValue.create(path + ".MaxLevel", -1).read(config);

        return new LevelRequirement(provider, minLevel, maxLevel);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Provider", this.provider);
        config.set(path + ".MinLevel", this.minLevel);
        config.set(path + ".MaxLevel", this.maxLevel);
    }

    public boolean isGoodLevel(@NonNull Player player) {
        LevelProvider levelProvider = LevelRegistry.getProvider(this.provider);
        if (levelProvider == null) return true;

        int playerLevel = levelProvider.getLevel(player);
        boolean underMin = !this.hasMinValue() || playerLevel >= this.minLevel;
        boolean underMax = !this.hasMaxValue() || playerLevel <= this.maxLevel;

        return underMin && underMax;
    }

    public boolean isRequired() {
        return this.hasMinValue() || this.hasMaxValue();
    }

    public boolean hasMinValue() {
        return this.minLevel > 0;
    }

    public boolean hasMaxValue() {
        return this.maxLevel > 0;
    }
}
