package su.nightexpress.dungeons.dungeon.feature.board;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.Placeholders;
import su.nightexpress.dungeons.api.dungeon.Board;
import su.nightexpress.dungeons.api.type.GameState;
import su.nightexpress.dungeons.config.Config;
import su.nightexpress.dungeons.config.Lang;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.nightcore.locale.entry.TextLocale;
import su.nightexpress.dungeons.nightcore.util.Players;
import su.nightexpress.dungeons.nightcore.util.placeholder.Replacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBoard<T> implements Board {

    protected final BoardLayout  layout;
    protected final DungeonGamer gamer;
    protected final Player       player;
    protected final String       identifier;

    /**
     * The lines currently on the player's screen, as a whole-value snapshot.
     * <p>
     * Rendering reads this to work out which lines the previous frame left behind and have to be reset.
     * It used to be a mutable map cleared and refilled in place at the end of {@link #update()}, which
     * meant a reader between those two statements saw an empty scoreboard and reset every line - a visible
     * flicker at best. The reference is swapped for a new immutable map instead, so every read sees one
     * complete frame or the other.
     */
    protected volatile Map<Integer, String> scores = Map.of();

    public AbstractBoard(@NotNull DungeonGamer gamer, @NotNull BoardLayout layout) {
        this.layout = layout;
        this.gamer = gamer;
        this.player = gamer.getPlayer();
        this.identifier = createIdentifier(this.player).substring(0, 16);
    }

    @NotNull
    public static String createIdentifier(@NotNull Player player) {
        String uuid = player.getUniqueId().toString();

        // Bedrock players have UUIDs leading with zeros.
        if (Players.isBedrock(player)) {
            uuid = new StringBuilder(uuid).reverse().toString();
        }

        return uuid;
    }

    @NotNull
    public final BoardLayout getLayout() {
        return this.gamer.getDungeon().getState() == GameState.INGAME ? this.layout : Config.SCOREBOARD_LOBBY_LAYOUT.get();
    }

    @NotNull
    private String getScoreIdentifier(int score) {
        return "line_" + score;
    }

    protected enum ObjectiveMode {
        CREATE,
        REMOVE,
        UPDATE
    }

    protected abstract void sendPacket(@NotNull Player player, @NotNull T packet);

    @NotNull
    protected abstract T createObjectivePacket(ObjectiveMode mode, @NotNull String displayName);

    @NotNull
    protected abstract T createResetScorePacket(@NotNull String scoreId);

    @NotNull
    protected abstract T createScorePacket(@NotNull String scoreId, int score, @NotNull String text);

    @NotNull
    protected abstract T createDisplayPacket();

    @Override
    public void create() {
        this.sendPacket(this.player, this.createObjectivePacket(ObjectiveMode.CREATE, ""));
        this.sendPacket(this.player, this.createDisplayPacket());
    }

    @Override
    public void remove() {
        this.sendPacket(this.player, this.createObjectivePacket(ObjectiveMode.REMOVE, ""));

        Map<Integer, String> previous = this.scores;
        this.scores = Map.of();

        previous.keySet().forEach(score -> this.sendPacket(this.player, this.createResetScorePacket(this.getScoreIdentifier(score))));
    }

    @NotNull
    private String replacePlaceholders(@NotNull String string) {
        return Replacer.create()
            .replace(Placeholders.forPlayerWithPAPI(this.player))
            .replace(this.gamer.replacePlaceholders())
            .replace(this.gamer.getDungeon().replacePlaceholders())
            .apply(string);
    }

    @NotNull
    private List<String> getFormattedTasks() {
        DungeonInstance dungeon = this.gamer.getDungeon();
        List<String> list = new ArrayList<>();

        if (!dungeon.hasTasks()) {
            list.add(Lang.UI_TASK_EMPTY_LIST.text());
            return list;
        }

        dungeon.getTaskProgress().forEach((stageTask, progress) -> {
            TextLocale format = progress.isCompleted() ? Lang.UI_TASK_COMPLETED : Lang.UI_TASK_INCOMPLETED;
            list.add(format.text()
                .replace(Placeholders.GENERIC_NAME, stageTask.getParams().getDisplay())
                .replace(Placeholders.GENERIC_VALUE, progress.format(this.gamer.getPlayer()))
            );
        });

        return list;
    }

    /**
     * Peer lines are read pre-rendered rather than formatted here: producing them would mean reading every
     * other participant's display name and state from this player's thread, which on a regionised server is
     * exactly the cross-region access that publishing them per-player avoids. See
     * {@link DungeonGamer#getBoardEntry()}.
     */
    @NotNull
    private List<String> getFormattedPlayers() {
        return this.gamer.getDungeon().getPlayers().stream().map(DungeonGamer::getBoardEntry).toList();
    }

    @Override
    public void update() {
        BoardLayout layout = this.getLayout();
        String title = layout.getTitle();
        List<String> lines = new ArrayList<>();

        for (String line : layout.getLines()) {
            if (line.equalsIgnoreCase(Placeholders.GENERIC_TASKS)) {
                lines.addAll(this.getFormattedTasks());
                continue;
            }
            else if (line.equalsIgnoreCase(Placeholders.GENERIC_PLAYERS)) {
                lines.addAll(this.getFormattedPlayers());
                continue;
            }
            lines.add(line);
        }

        Map<Integer, String> frame = new HashMap<>();
        int index = lines.size();

        for (String line : lines) {
            frame.put(index--, this.replacePlaceholders(line));
        }
        title = this.replacePlaceholders(title);

        // Read once. The field can be reassigned by another render of this same board between here and the
        // reset loop below, and diffing the new frame against half of the old one and half of a newer one
        // leaves stale lines on screen.
        Map<Integer, String> previous = this.scores;

        this.sendPacket(this.player, this.createObjectivePacket(ObjectiveMode.UPDATE, title));

        frame.forEach((score, text) -> this.sendPacket(this.player, this.createScorePacket(this.getScoreIdentifier(score), score, text)));

        previous.keySet().stream()
            .filter(score -> !frame.containsKey(score))
            .forEach(score -> this.sendPacket(this.player, this.createResetScorePacket(this.getScoreIdentifier(score))));

        this.scores = Map.copyOf(frame);
    }
}
