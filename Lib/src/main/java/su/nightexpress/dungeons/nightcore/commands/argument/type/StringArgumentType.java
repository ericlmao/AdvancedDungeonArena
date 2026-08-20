package su.nightexpress.dungeons.nightcore.commands.argument.type;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.commands.SuggestionsProvider;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentType;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.dungeons.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.dungeons.nightcore.util.Lists;

import java.util.List;

public class StringArgumentType implements ArgumentType<String>, SuggestionsProvider {

    private final boolean greedy;

    public StringArgumentType(boolean greedy) {
        this.greedy = greedy;
    }

    @Override
    @NonNull
    public String parse(@NonNull CommandContextBuilder contextBuilder,
                        @NonNull String string) throws CommandSyntaxException {
        return string;
    }

    @Override
    @NonNull
    public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
        return Lists.newList("...");
    }

    public boolean isGreedy() {
        return this.greedy;
    }
}
