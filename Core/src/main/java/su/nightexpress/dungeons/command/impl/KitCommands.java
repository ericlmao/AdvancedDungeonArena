package su.nightexpress.dungeons.command.impl;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.command.CommandArguments;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.config.Perms;
import su.nightexpress.dungeons.kit.KitUtils;
import su.nightexpress.dungeons.kit.impl.Kit;
import su.nightexpress.dungeons.nightcore.commands.Arguments;
import su.nightexpress.dungeons.nightcore.commands.Commands;
import su.nightexpress.dungeons.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.commands.context.ParsedArguments;
import su.nightexpress.dungeons.nightcore.core.config.CoreLang;
import su.nightexpress.dungeons.nightcore.locale.entry.MessageLocale;

public class KitCommands {

    public static void load(@NonNull DungeonPlugin plugin, @NonNull HubNodeBuilder root) {
        var kitRoot = Commands.hub( "kit")
            .description(Lang.COMMAND_KIT_DESC)
            .permission(Perms.COMMAND_KIT);

        kitRoot.branch(Commands.literal("create")
            .playerOnly()
            .description(Lang.COMMAND_KIT_CREATE_DESC)
            .withArguments(Arguments.string(CommandArguments.NAME).localized(CoreLang.COMMAND_ARGUMENT_NAME_NAME))
            .executes((context, arguments) -> createKit(plugin, context, arguments))
        );

        kitRoot.branch(Commands.literal("setitems")
            .playerOnly()
            .description(Lang.COMMAND_KIT_SET_ITEMS_DESC)
            .withArguments(CommandArguments.forKit(plugin))
            .executes((context, arguments) -> updateKit(plugin, context, arguments))
        );

        if (!KitUtils.isRentMode()) {
            kitRoot.branch(Commands.literal("grant")
                .description(Lang.COMMAND_KIT_GRANT_DESC)
                .withArguments(
                    CommandArguments.forKit(plugin),
                    Arguments.playerName(CommandArguments.PLAYER)
                )
                .executes((context, arguments) -> grantOrRevokeKit(plugin, context, arguments, true))
            );

            kitRoot.branch(Commands.literal("revoke")
                .description(Lang.COMMAND_KIT_REVOKE_DESC)
                .withArguments(
                    CommandArguments.forKit(plugin),
                    Arguments.playerName(CommandArguments.PLAYER)
                )
                .executes((context, arguments) -> grantOrRevokeKit(plugin, context, arguments, false))
            );
        }

        root.branch(kitRoot);
    }

    private static boolean createKit(@NonNull DungeonPlugin plugin, @NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        String name = arguments.getString(CommandArguments.NAME);

        plugin.getKitManager().createKit(player, name);
        return true;
    }

    private static boolean updateKit(@NonNull DungeonPlugin plugin, @NonNull CommandContext context, @NonNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        Kit kit = arguments.get(CommandArguments.KIT, Kit.class);

        plugin.getKitManager().updateKit(player, kit);
        return true;
    }

    private static boolean grantOrRevokeKit(@NonNull DungeonPlugin plugin, @NonNull CommandContext context, @NonNull ParsedArguments arguments, boolean grant) {
        String playerName = arguments.getString(CommandArguments.PLAYER);
        Kit kit = arguments.get(CommandArguments.KIT, Kit.class);

        // manageUser resumes on the ForkJoin common pool when the user is not cached - not a plugin thread at
        // all, and one that outlives our disable. The Synchronized variant resumes on the global region
        // instead, which is where mutating user data and replying to the command sender belong.
        plugin.getUserManager().manageUserSynchronized(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            MessageLocale text;
            if (grant) {
                user.addKit(kit);
                text = Lang.KIT_GRANT_DONE;
            }
            else {
                user.removeKit(kit);
                text = Lang.KIT_REVOKE_DONE;
            }

            plugin.getUserManager().save(user);

            context.send(text, replacer -> replacer.replace(kit.replacePlaceholders()).replace(Placeholders.PLAYER_NAME, user.getName()));
        });
        return true;
    }
}
