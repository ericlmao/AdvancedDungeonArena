package su.nightexpress.dungeons.nightcore.commands;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.dungeons.nightcore.commands.builder.HubNodeBuilder;

@FunctionalInterface
@NullMarked
public interface CommandProvider {

    void provideCommands(HubNodeBuilder root);
}
