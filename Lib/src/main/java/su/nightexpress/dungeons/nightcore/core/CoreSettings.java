package su.nightexpress.dungeons.nightcore.core;

import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.util.Lists;
import su.nightexpress.dungeons.nightcore.util.number.NumberShortcut;
import su.nightexpress.dungeons.nightcore.util.wrapper.UniFormatter;

/**
 * The handful of {@code core.CoreConfig} values the vendored code actually reads.
 * <p>
 * Upstream these lived in the NightCore plugin's own {@code config.yml}, which no longer exists. The
 * upstream defaults are inlined verbatim so number formatting and compaction render identically.
 */
public class CoreSettings {

    /** Number formatting: upstream {@code Number.Format} default. */
    public static final UniFormatter NUMBER_FORMAT = UniFormatter.of("#,###.###", RoundingMode.HALF_EVEN);

    /** Upstream {@code Number.Shortcut_Step} default. */
    public static final int NUMBER_SHORTCUT_STEP = 1000;

    /** Upstream {@code Number.Shortcut_List} default. */
    public static final List<NumberShortcut> NUMBER_SHORTCUTS = List.of(
        new NumberShortcut(1, "k"),
        new NumberShortcut(2, "m"),
        new NumberShortcut(3, "b"),
        new NumberShortcut(4, "t"),
        new NumberShortcut(5, "q")
    );

    /** Currency ids that must never be registered. Upstream {@code Integrations.Economy.DisabledProviders}. */
    public static final Set<String> ECONOMY_DISABLED_PROVIDERS = Lists.newSet("example_currency", "custom_economy");

    private CoreSettings() {
    }

    @NonNull
    public static UniFormatter getNumberFormat() {
        return NUMBER_FORMAT;
    }
}
