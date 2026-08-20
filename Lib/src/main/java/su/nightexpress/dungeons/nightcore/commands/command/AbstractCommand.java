package su.nightexpress.dungeons.nightcore.commands.command;


import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.commands.CommandRequirement;
import su.nightexpress.dungeons.nightcore.commands.NodeUtils;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.dungeons.nightcore.commands.context.Suggestions;
import su.nightexpress.dungeons.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.dungeons.nightcore.commands.tree.CommandNode;
import su.nightexpress.dungeons.nightcore.commands.tree.ExecutableNode;
import su.nightexpress.dungeons.nightcore.core.config.CoreLang;
import su.nightexpress.dungeons.nightcore.util.CommandUtil;
import su.nightexpress.dungeons.nightcore.util.Placeholders;
import su.nightexpress.dungeons.nightcore.util.text.night.NightMessage;

public abstract class AbstractCommand<N extends ExecutableNode> extends Command implements NightCommand {

    private final NightPlugin plugin;
    private final N               root;

    public AbstractCommand(@NonNull NightPlugin plugin, @NonNull N root, @NonNull List<String> aliases) {
        super(clean(root.getName()), clean(root.getDescription()), clean(root.getUsage()), aliases);
        this.plugin = plugin;
        this.root = root;
        this.setPermission(root.getPermission());
    }

    @NonNull
    private static String clean(@NonNull String string) {
        return NightMessage.stripTags(string);
    }

    @Override
    @NonNull
    public NightPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    @NonNull
    public N getRoot() {
        return this.root;
    }

    @Override
    public boolean register() {
        boolean registered = CommandUtil.register(this, this.plugin.getName());
        // Upstream never refreshed the client-side command tree, so commands registered at runtime
        // (i.e. on every reload) stayed invisible to tab-completion until relog.
        if (registered) CommandUtil.syncCommands();

        return registered;
    }

    @Override
    public boolean unregister() {
        boolean unregistered = CommandUtil.unregister(this);
        if (unregistered) CommandUtil.syncCommands();

        return unregistered;
    }

    @Override
    public boolean isRegistered() {
        return super.isRegistered();
    }

    @Override
    @NonNull
    public String getName() {
        return super.getName();
    }

    @Override
    @Nullable
    public String getPermission() {
        return super.getPermission();
    }

    @Override
    @NonNull
    public String getLabel() {
        return super.getLabel();
    }

    @Override
    @NonNull
    public List<String> getAliases() {
        return super.getAliases();
    }

    @Override
    @NonNull
    public String getDescription() {
        return super.getDescription();
    }

    @Override
    @NonNull
    public String getUsage() {
        return super.getUsage();
    }

    @Nullable
    private CommandContext parseNodes(@NonNull CommandNode node, @NonNull ArgumentReader reader,
                                      @NonNull CommandContextBuilder builder, boolean forExecution) {
        CommandSender sender = builder.getSender();
        if (!this.testRequirements(sender, node, forExecution)) return null;

        try {
            node.parse(reader, builder);
        }
        catch (CommandSyntaxException exception) {
            if (forExecution) {
                exception.getMessageLocale().withPrefix(this.plugin.getPrefix()).send(sender, replacer -> replacer
                    .replace(Placeholders.GENERIC_NAME, node.getLocalizedName())
                    .replace(Placeholders.GENERIC_INPUT, reader.getCursorArgument())
                    .replace(Placeholders.GENERIC_VALUE, String.valueOf(exception.getValue()))
                );
                return null;
            }
        }

        reader.moveForward();

        if (reader.canMoveForward()) {
            for (CommandNode child : node.getRelevantNodes(reader)) {
                return this.parseNodes(child, reader, builder, forExecution);
            }
        }
        else if (forExecution && node.hasRequiredArguments() && builder.getExecutor() != null) {
            ExecutableNode executable = builder.getExecutor();

            CoreLang.COMMAND_EXECUTION_MISSING_ARGUMENTS.withPrefix(this.plugin).send(sender, replacer -> replacer
                .replace(Placeholders.GENERIC_COMMAND, NodeUtils.formatLabel(executable, builder))
                .replace(Placeholders.GENERIC_DESCRIPTION, executable.getDescription()));
            return null;
        }

        return builder.build();
    }

    private boolean testRequirements(@NonNull CommandSender sender, @NonNull CommandNode node, boolean forExecution) {
        if (!node.hasPermission(sender)) {
            if (forExecution) {
                CoreLang.ERROR_NO_PERMISSION.withPrefix(this.plugin).send(sender);
            }
            return false;
        }

        for (CommandRequirement requirement : node.getRequirements()) {
            if (!requirement.test(sender)) {
                if (forExecution) {
                    requirement.getMessage().withPrefix(this.plugin).send(sender);
                }
                return false;
            }
        }
        return true;
    }

    private void listSuggestions(@NonNull CommandNode node, @NonNull ArgumentReader reader,
                                 @NonNull CommandContext context, @NonNull Suggestions suggestions) {
        if (reader.isEnd()) return;

        reader.moveForward();
        node.suggests(reader, context, suggestions); // Actually modify the suggestions list.

        //System.out.println(node.getName() + ": Parse & Inject suggestions: " + suggestions.getSuggestions());

        // Continue until pass through all user's input.
        if (reader.canMoveForward()) {

            //System.out.println("cursor = '" + reader.getCursorArgument() + "' [" + reader.getCursor() + "]");

            for (CommandNode child : node.getRelevantNodes(reader)) {
                this.listSuggestions(child, reader, context, suggestions);
            }
        }
    }

    @Nullable
    private CommandContext parse(@NonNull CommandSender sender, @NonNull String label, @NonNull String[] args,
                                 boolean forExecution) {
        ArgumentReader reader = ArgumentReader.forArgumentsWithLabel(label, args);
        CommandContextBuilder builder = new CommandContextBuilder(this.plugin, sender, this.root, reader.getString());

        return this.parseNodes(this.root, reader, builder, forExecution);
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String label, @NonNull String @NonNull [] args) {
        CommandContext context = this.parse(sender, label, args, true);
        if (context == null) return false;

        ExecutableNode executor = context.getExecutor();
        if (executor == null) return false;

        try {
            return executor.run(context);
        }
        catch (CommandSyntaxException exception) {
            exception.getMessageLocale().withPrefix(this.plugin).send(sender);
            return false;
        }
    }

    @Override
    @NonNull
    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String label,
                                    @NonNull String @NonNull [] args) {
        if (args.length == 0) return Collections.emptyList();

        CommandContext context = this.parse(sender, label, args, false);
        if (context == null) return Collections.emptyList();

        ArgumentReader reader = ArgumentReader.forArgumentsWithLabel(label, args);
        Suggestions suggestions = new Suggestions();
        this.listSuggestions(this.root, reader, context, suggestions);

        return suggestions.getSuggestions();
    }
}
