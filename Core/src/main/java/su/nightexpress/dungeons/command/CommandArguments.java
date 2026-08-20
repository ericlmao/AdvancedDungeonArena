package su.nightexpress.dungeons.command;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.dungeon.config.DungeonConfig;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.spot.Spot;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.selection.SelectionType;
import su.nightexpress.dungeons.nightcore.commands.Commands;
import su.nightexpress.dungeons.nightcore.commands.builder.ArgumentNodeBuilder;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.dungeons.nightcore.core.config.CoreLang;
import su.nightexpress.dungeons.nightcore.util.Enums;

import java.util.ArrayList;
import java.util.Optional;

public class CommandArguments {

    public static final String PLAYER     = "player";
    public static final String TYPE       = "type";
    public static final String NAME       = "name";
    public static final String DUNGEON    = "dungeon";
    public static final String KIT        = "kit";
    public static final String STAGE      = "stage";
    public static final String LEVEL      = "level";
    public static final String AMOUNT     = "amount";
    public static final String SPOT       = "spot";
    public static final String REWARD     = "reward";
    public static final String LOOT_CHEST = "lootchest";
    public static final String WEIGHT     = "weight";
    public static final String STATE      = "state";

    @NonNull
    public static ArgumentNodeBuilder<SelectionType> forSelectionType(@NonNull DungeonPlugin plugin) {
        return Commands.argument(TYPE, (context, string) -> Enums.parse(string, SelectionType.class)
                .orElseThrow(() -> CommandSyntaxException.custom(Lang.ERROR_COMMAND_INVALID_SELECTION_ARGUMENT))
            )
            .localized(CoreLang.COMMAND_ARGUMENT_NAME_TYPE)
            .suggestions((_, context) -> Enums.getNames(SelectionType.class));
    }

    @NonNull
    public static ArgumentNodeBuilder<DungeonConfig> forDungeon(@NonNull DungeonPlugin plugin) {
        return Commands.argument(DUNGEON, (context, string) -> Optional.ofNullable(plugin.getDungeonManager().getDungeonById(string))
                .orElseThrow(() -> CommandSyntaxException.custom(Lang.ERROR_COMMAND_INVALID_DUNGEON_ARGUMENT))
            )
            .localized(Lang.COMMAND_ARGUMENT_NAME_DUNGEON)
            .suggestions((_, context) -> new ArrayList<>(plugin.getDungeonManager().getDungeonIds()));
    }

    @NonNull
    public static ArgumentNodeBuilder<Kit> forKit(@NonNull DungeonPlugin plugin) {
        return Commands.argument(KIT, (context, string) -> Optional.ofNullable(plugin.getKitManager().getKitById(string))
                .orElseThrow(() -> CommandSyntaxException.custom(Lang.ERROR_COMMAND_INVALID_KIT_ARGUMENT))
            )
            .localized(Lang.COMMAND_ARGUMENT_NAME_KIT)
            .suggestions((_, context) -> new ArrayList<>(plugin.getKitManager().getKitIds()));
    }

    @Nullable
    public static DungeonInstance getDungeonInstance(@NonNull DungeonPlugin plugin, @NonNull CommandContext context) {
        Player player = context.getPlayerOrThrow();
        return plugin.getDungeonManager().getInstance(player);
    }

    @Nullable
    public static DungeonConfig getDungeonConfig(@NonNull DungeonPlugin plugin, @NonNull CommandContext context) {
        return context.getArguments().contains(DUNGEON) ? context.getArguments().get(DUNGEON, DungeonConfig.class) : null;
    }

    @Nullable
    public static Spot getSpot(@NonNull DungeonPlugin plugin, @NonNull CommandContext context) {
        DungeonConfig config = getDungeonConfig(plugin, context);
        if (config == null) return null;

        String arg = context.getArguments().contains(SPOT) ? context.getArguments().getString(SPOT) : null;
        return arg == null ? null : config.getSpotById(arg);
    }
}
