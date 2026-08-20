package su.nightexpress.dungeons.dungeon.script.condition.type;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.mob.MobIdentifier;
import su.nightexpress.dungeons.dungeon.script.number.NumberComparator;
import su.nightexpress.dungeons.nightcore.config.FileConfig;

@Deprecated
public abstract class MobAmountCondition extends NumberCompareCondition {

    protected final MobIdentifier identifier;

    public record MobData(NumberComparator comparator, double compareValue, MobIdentifier identifier){}

    protected MobAmountCondition(@NonNull MobData mobData) {
        this(mobData.comparator, mobData.compareValue, mobData.identifier);
    }

    public MobAmountCondition(@NonNull NumberComparator comparator, double compareValue, @NonNull MobIdentifier identifier) {
        super(comparator, compareValue);
        this.identifier = identifier;
    }

    @NonNull
    public static MobData readMobData(@NonNull FileConfig config, @NonNull String path) {
        NumberData numberData = readNumberData(config, path);
        MobIdentifier mobId = MobIdentifier.read(config, path + ".MobId");

        return new MobData(numberData.comparator(), numberData.compareValue(), mobId);
    }

    @Override
    protected void writeAdditional(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".MobId", this.identifier.serialize());
    }
}
