package su.nightexpress.dungeons.nightcore.commands.command;

import java.util.List;

import org.jspecify.annotations.NonNull;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.commands.tree.LiteralNode;

public class LiteralCommand extends AbstractCommand<LiteralNode> {

    public LiteralCommand(@NonNull NightPlugin plugin, @NonNull LiteralNode root, @NonNull List<String> aliases) {
        super(plugin, root, aliases);
    }
}
