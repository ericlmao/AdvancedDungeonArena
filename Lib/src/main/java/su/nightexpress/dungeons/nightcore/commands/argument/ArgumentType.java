package su.nightexpress.dungeons.nightcore.commands.argument;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.dungeons.nightcore.commands.context.CommandContextBuilder;
import su.nightexpress.dungeons.nightcore.commands.exceptions.CommandSyntaxException;

@NullMarked
public interface ArgumentType<T> {

    T parse(CommandContextBuilder contextBuilder, String string) throws CommandSyntaxException;
}
