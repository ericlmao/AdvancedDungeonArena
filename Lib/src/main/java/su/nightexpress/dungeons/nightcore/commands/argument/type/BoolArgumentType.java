package su.nightexpress.dungeons.nightcore.commands.argument.type;

import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.nightcore.commands.SuggestionsProvider;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentReader;
import su.nightexpress.dungeons.nightcore.commands.argument.ArgumentType;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContext;
import su.nightexpress.dungeons.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.dungeons.nightcore.commands.exceptions.CommandSyntaxException;

import java.util.Arrays;
import java.util.List;

public class BoolArgumentType implements ArgumentType<Boolean>, SuggestionsProvider {

    private static final List<String> EXAMPLES = Arrays.asList("true", "false");

    @Override
    @NonNull
    public Boolean parse(@NonNull CommandContextBuilder contextBuilder,
                         @NonNull String string) throws CommandSyntaxException {
        return Boolean.parseBoolean(string);
    }

    @Override
    @NonNull
    public List<String> suggest(@NonNull ArgumentReader reader, @NonNull CommandContext context) {
        return EXAMPLES;
    }
}
