package su.nightexpress.dungeons.dungeon.feature.board.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;
import su.nightexpress.dungeons.dungeon.feature.board.AbstractBoard;
import su.nightexpress.dungeons.dungeon.feature.board.BoardLayout;
import su.nightexpress.dungeons.nightcore.util.text.night.NightMessage;

import java.util.Optional;

public class PacketsBoard extends AbstractBoard<PacketWrapper<?>> {

    public PacketsBoard(@NonNull DungeonGamer gamer, @NonNull BoardLayout boardConfig) {
        super(gamer, boardConfig);
    }

    @Override
    @NonNull
    protected WrapperPlayServerScoreboardObjective createObjectivePacket(ObjectiveMode mode, @NonNull String displayName) {
        WrapperPlayServerScoreboardObjective.ObjectiveMode objectiveMode = switch (mode) {
            case CREATE -> WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE;
            case REMOVE -> WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE;
            case UPDATE -> WrapperPlayServerScoreboardObjective.ObjectiveMode.UPDATE;
        };

        return new WrapperPlayServerScoreboardObjective(
            this.identifier,
            objectiveMode,
            NightMessage.parse(displayName),
            WrapperPlayServerScoreboardObjective.RenderType.INTEGER,
            ScoreFormat.blankScore()
        );
    }

    @Override
    @NonNull
    protected WrapperPlayServerResetScore createResetScorePacket(@NonNull String scoreId) {
        return new WrapperPlayServerResetScore(scoreId, this.identifier);
    }

    @Override
    @NonNull
    protected WrapperPlayServerUpdateScore createScorePacket(@NonNull String scoreId, int score, @NonNull String text) {
        WrapperPlayServerUpdateScore scorePacket = new WrapperPlayServerUpdateScore(
            scoreId,
            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
            this.identifier,
            Optional.of(score)
        );

        scorePacket.setEntityDisplayName(NightMessage.parse(text));
        scorePacket.setScoreFormat(ScoreFormat.blankScore());

        return scorePacket;
    }

    @Override
    @NonNull
    protected WrapperPlayServerDisplayScoreboard createDisplayPacket() {
        return new WrapperPlayServerDisplayScoreboard(1, this.identifier);
    }

    @Override
    protected void sendPacket(@NonNull Player player, @NonNull PacketWrapper<?> wrapper) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, wrapper);
    }
}
