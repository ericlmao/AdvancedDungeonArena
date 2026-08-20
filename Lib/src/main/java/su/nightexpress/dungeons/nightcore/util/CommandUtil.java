package su.nightexpress.dungeons.nightcore.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class CommandUtil {

    private CommandUtil() {
    }

    // Upstream reached the command map through the Software bridge (reflection on Spigot).
    // Paper exposes both the map and its backing table directly.
    @NonNull
    private static SimpleCommandMap getCommandMap() {
        return (SimpleCommandMap) Bukkit.getCommandMap();
    }

    public static boolean register(@NonNull Command command, @NonNull String fallbackPrefix) {
        return getCommandMap().register(fallbackPrefix, command);
    }

    /**
     * Pushes the server-side command tree to every connected client.
     * <p>
     * Upstream never called this (the block existed but was commented out), so commands registered at
     * runtime - i.e. on every {@code /reload} - never showed up in client tab-completion until relog.
     */
    public static void syncCommands() {
        Bukkit.getOnlinePlayers().forEach(org.bukkit.entity.Player::updateCommands);
    }

    public static boolean unregister(@NonNull String name) {
        Command command = getCommand(name).orElse(null);
        if (command == null) return false;

        return unregister(command);
    }

    public static boolean unregister(@NonNull Command command) {
        SimpleCommandMap commandMap = getCommandMap();

        Map<String, Command> knownCommands = commandMap.getKnownCommands();
        if (!command.unregister(commandMap)) return false;

        return knownCommands.keySet().removeIf(key -> key.equalsIgnoreCase(command.getName()) || command.getAliases()
            .contains(key));
    }

    @NonNull
    public static Set<String> getAliases(@NonNull String name) {
        return getAliases(name, false);
    }

    @NonNull
    public static Set<String> getAliases(@NonNull String name, boolean inclusive) {
        Command command = getCommand(name).orElse(null);
        if (command == null) return Collections.emptySet();

        Set<String> aliases = new HashSet<>(command.getAliases());
        if (inclusive) aliases.add(command.getName());
        return aliases;
    }

    @NonNull
    public static Optional<Command> getCommand(@NonNull String name) {
        return getCommandMap().getCommands().stream()
            .filter(command -> command.getName().equalsIgnoreCase(name) || command.getLabel().equalsIgnoreCase(
                name) || command.getAliases().contains(name))
            .findFirst();
    }

    @NonNull
    public static String getCommandName(@NonNull String string) {
        String name = string.split(" ")[0].substring(1);

        String[] pluginPrefix = name.split(":");
        if (pluginPrefix.length == 2) {
            name = pluginPrefix[1];
        }

        return name;
    }

}
