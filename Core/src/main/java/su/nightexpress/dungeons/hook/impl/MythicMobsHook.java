package su.nightexpress.dungeons.hook.impl;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MythicMobsHook {

    private static final MythicBukkit MYTHIC_MOBS = MythicBukkit.inst();

    public static boolean isMythicMob(@NonNull Entity entity) {
        return MYTHIC_MOBS.getAPIHelper().isMythicMob(entity);
    }

    @Nullable
    public static ActiveMob getMobInstance(@NonNull Entity entity) {
        return MYTHIC_MOBS.getAPIHelper().getMythicMobInstance(entity);
    }

    @Nullable
    public static MythicMob getMobConfig(@NonNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getType() : null;
    }

    @Nullable
    public static MythicMob getMobConfig(@NonNull String mobId) {
        return MYTHIC_MOBS.getAPIHelper().getMythicMob(mobId);
    }

    @NonNull
    public static String getMobInternalName(@NonNull Entity entity) {
        MythicMob mythicMob = getMobConfig(entity);
        return mythicMob != null ? mythicMob.getInternalName() : "null";
    }

    @NonNull
    public static String getMobDisplayName(@NonNull String mobId) {
        MythicMob mythicMob = getMobConfig(mobId);
        PlaceholderString string = mythicMob != null ? mythicMob.getDisplayName() : null;
        return string != null ? string.get() : mobId;
    }

    public static double getMobLevel(@NonNull Entity entity) {
        ActiveMob mob = getMobInstance(entity);
        return mob != null ? mob.getLevel() : 0;
    }

    @NonNull
    public static List<String> getMobConfigIds() {
        return new ArrayList<>(MYTHIC_MOBS.getMobManager().getMobNames());
    }
}
