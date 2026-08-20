package su.nightexpress.dungeons.nightcore.commands.command;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.command.PluginIdentifiableCommand;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.commands.Commands;
import su.nightexpress.dungeons.nightcore.commands.builder.ExecutableNodeBuilder;
import su.nightexpress.dungeons.nightcore.commands.builder.HubNodeBuilder;
import su.nightexpress.dungeons.nightcore.commands.builder.LiteralNodeBuilder;
import su.nightexpress.dungeons.nightcore.commands.tree.CommandNode;
import su.nightexpress.dungeons.nightcore.commands.tree.ExecutableNode;
import su.nightexpress.dungeons.nightcore.commands.tree.HubNode;
import su.nightexpress.dungeons.nightcore.commands.tree.LiteralNode;
import su.nightexpress.dungeons.nightcore.util.Lists;

@NullMarked
public interface NightCommand extends PluginIdentifiableCommand {

    NightPlugin getPlugin();

    CommandNode getRoot();

    boolean register();

    boolean unregister();

    boolean isRegistered();

    String getName();

    @Nullable
    String getPermission();

    String getLabel();

    List<String> getAliases();

    String getDescription();

    String getUsage();

    static HubCommand forPlugin(NightPlugin plugin, Consumer<HubNodeBuilder> consumer) {
        String[] aliases = plugin.getDetails().getCommandAliases();

        return hub(plugin, aliases, builder -> {
            builder.localized(plugin.getNameLocalized());
            consumer.accept(builder);
        });
    }

    static LiteralCommand literal(NightPlugin plugin, LiteralNode node) {
        return new LiteralCommand(plugin, node, Collections.emptyList());
    }


    static LiteralCommand literal(NightPlugin plugin, String name,
                                  Consumer<LiteralNodeBuilder> consumer) {
        return literal(plugin, new String[]{name}, consumer);
    }


    static LiteralCommand literal(NightPlugin plugin, String[] names,
                                  Consumer<LiteralNodeBuilder> consumer) {
        return create(names, Commands::literal, (root, aliases) -> new LiteralCommand(plugin, root, aliases),
            consumer);
    }


    static HubCommand hub(NightPlugin plugin, HubNode node) {
        return new HubCommand(plugin, node, Collections.emptyList());
    }


    static HubCommand hub(NightPlugin plugin, String name,
                          Consumer<HubNodeBuilder> consumer) {
        return hub(plugin, new String[]{name}, consumer);
    }


    static HubCommand hub(NightPlugin plugin, String[] names,
                          Consumer<HubNodeBuilder> consumer) {
        return create(names, Commands::hub, (root, aliases) -> new HubCommand(plugin, root, aliases), consumer);
    }


    @Deprecated
    static <C extends NightCommand, N extends ExecutableNode, B extends ExecutableNodeBuilder<N, B>> C create(
                                                                                                              NightPlugin plugin,
                                                                                                              String[] names,
                                                                                                              Function<String, B> builderFunction,
                                                                                                              BiFunction<N, List<String>, C> commandFunction,
                                                                                                              Consumer<B> consumer
    ) {
        if (names == null || names.length == 0)
            throw new IllegalStateException("Could not create root node for empty aliases!");

        List<String> aliases = Lists.newList(names);

        B builder = builderFunction.apply(aliases.removeFirst());
        consumer.accept(builder);

        return commandFunction.apply(builder.build(), aliases);
    }

    static <C extends NightCommand, N extends ExecutableNode, B extends ExecutableNodeBuilder<N, B>> C create(String[] names,
                                                                                                              Function<String, B> builderFunction,
                                                                                                              BiFunction<N, List<String>, C> commandFunction,
                                                                                                              Consumer<B> consumer
    ) {
        if (names == null || names.length == 0)
            throw new IllegalStateException("Could not create root node for empty aliases!");

        List<String> aliases = Lists.newList(names);

        B builder = builderFunction.apply(aliases.removeFirst());
        consumer.accept(builder);

        return commandFunction.apply(builder.build(), aliases);
    }
}
