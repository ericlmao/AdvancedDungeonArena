package su.nightexpress.dungeons.nightcore.commands.command;

import java.util.List;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.commands.tree.HubNode;

public class HubCommand extends AbstractCommand<HubNode> {

    public HubCommand(@NonNull NightPlugin plugin, @NonNull HubNode root, @NonNull List<String> aliases) {
        super(plugin, root, aliases);
    }
}
