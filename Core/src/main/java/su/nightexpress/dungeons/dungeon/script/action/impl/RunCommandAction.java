package su.nightexpress.dungeons.dungeon.script.action.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.dungeon.DungeonTarget;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.util.Lists;

import java.util.List;

public class RunCommandAction implements Action {

    private final List<String>  commands;
    private final DungeonTarget target;

    public RunCommandAction(@NonNull List<String> commands, @NonNull DungeonTarget target) {
        this.commands = commands;
        this.target = target;
    }

    @NonNull
    public static RunCommandAction load(@NonNull FileConfig config, @NonNull String path) {
        List<String> commands = ConfigValue.create(path + ".Commands", Lists.newList()).read(config);
        DungeonTarget target = ConfigValue.create(path + ".Target", DungeonTarget.class, DungeonTarget.GLOBAL).read(config);

        return new RunCommandAction(commands, target);
    }

    @Override
    public void write(@NonNull FileConfig config, @NonNull String path) {
        config.set(path + ".Commands", this.commands);
        config.set(path + ".Target", this.target.name());
    }

    @NonNull
    @Override
    public String getName() {
        return ActionId.RUN_COMMAND;
    }

    @Override
    public void perform(@NonNull DungeonInstance dungeon, @NonNull DungeonGameEvent event) {
        dungeon.runCommand(this.commands, this.target, event);
    }
}
