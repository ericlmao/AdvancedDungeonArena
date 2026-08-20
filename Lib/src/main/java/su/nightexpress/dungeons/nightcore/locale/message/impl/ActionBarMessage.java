package su.nightexpress.dungeons.nightcore.locale.message.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.locale.message.LangMessage;
import su.nightexpress.dungeons.nightcore.locale.message.MessageData;
import su.nightexpress.dungeons.nightcore.util.Players;
import net.kyori.adventure.text.Component;
import su.nightexpress.dungeons.nightcore.util.text.night.NightMessage;

import java.util.Collection;

public class ActionBarMessage extends LangMessage {

    public ActionBarMessage(@NonNull String text, @NonNull MessageData data) {
        super(text, data);
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    protected void send(@NonNull Collection<? extends CommandSender> receivers, @NonNull String text) {
        Component component = NightMessage.parse(text);
        receivers.forEach(sender -> {
            if (sender instanceof Player player) Players.sendActionBar(player, component);
        });
    }
}
