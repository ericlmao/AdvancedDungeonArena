package su.nightexpress.dungeons.nightcore.commands.argument.type;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.util.Players;

import java.util.List;

public class PlayerNameArgumentType extends StringArgumentType {

    public PlayerNameArgumentType() {
        super(false);
    }

    @Override
    @NonNull
    public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
        return context.getSender() instanceof Player player ? Players.playerNames(player) : Players.playerNames();
    }
}
