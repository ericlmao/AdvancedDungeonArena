package su.nightexpress.dungeons.dungeon.feature;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.config.Writeable;
import su.nightexpress.dungeons.nightcore.locale.message.LangMessage;
import su.nightexpress.dungeons.nightcore.locale.message.MessageData;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.placeholder.Replacer;

import java.util.List;

public class KillStreak implements Writeable {

    private final String       id;
    private final int          kills;
    private final boolean      repeatable;
    private final String       rawMessage;
    private final List<String> commands;

    private final LangMessage message;

    public KillStreak(@NonNull String id, int kills, boolean repeatable, @NonNull String rawMessage, @NonNull List<String> commands) {
        this.id = id.toLowerCase();
        this.kills = kills;
        this.repeatable = repeatable;
        this.rawMessage = rawMessage;
        // Was the legacy language system's LangMessage.parse(raw, null); the modern equivalent splits
        // the '[...]' bracket-data prefix off the text first.
        MessageData.Builder builder = MessageData.chat();
        String text = MessageData.extractAndParse(rawMessage, builder);
        this.message = LangMessage.createFromData(text, builder.build());
        this.commands = commands;
    }

    @NonNull
    public static KillStreak read(@NonNull FileConfig config, @NonNull String path, @NonNull String id) {
        int kills = ConfigValue.create(path + ".Kills", 0).read(config);
        boolean repeatable = ConfigValue.create(path + ".Repeatable", false).read(config);
        String rawMessage = config.getString(path + ".Message", "");
        List<String> commands = config.getStringList(path + ".Commands");

        return new KillStreak(id, kills, repeatable, rawMessage, commands);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Kills", this.kills);
        config.set(path + ".Repeatable", this.repeatable);
        config.set(path + ".Message", this.rawMessage);
        config.set(path + ".Commands", this.commands);
    }

    @NonNull
    public String getId() {
        return this.id;
    }

    public int getKills() {
        return this.kills;
    }

    public boolean isRepeatable() {
        return this.repeatable;
    }

    @NonNull
    public LangMessage getMessage() {
        return this.message;
    }

    @NonNull
    public List<String> getCommands() {
        return this.commands;
    }

    public boolean isGoodStreak(int streak) {
        return this.kills == streak || (this.isRepeatable() && streak % this.kills == 0);
    }

    public void run(@NonNull DungeonInstance dungeon, @NonNull DungeonGamer gamer) {
        Player player = gamer.getPlayer();

        this.message.send(player, replacer -> this.replacement(dungeon, gamer, replacer));

        Players.dispatchCommands(player, this.replacement(dungeon, gamer, Replacer.create()).apply(this.commands));
    }

    @NonNull
    private Replacer replacement(@NonNull DungeonInstance dungeon, @NonNull DungeonGamer gamer, @NonNull Replacer replacer) {
        return replacer
            .replace(dungeon.replaceVariables())
            .replace(gamer.replacePlaceholders())
            .replace(Placeholders.forPlayerWithPAPI(gamer.getPlayer()))
            .replace(Placeholders.GENERIC_AMOUNT, String.valueOf(this.kills));
    }
}
