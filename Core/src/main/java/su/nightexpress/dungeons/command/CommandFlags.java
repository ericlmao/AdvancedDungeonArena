package su.nightexpress.dungeons.command;

public class CommandFlags {

    // The only surviving member of this class. `force()` built a flag through nightcore's deprecated
    // `command.experimental` framework, which was never called and is not vendored.
    public static final String FORCE = "f";

    private CommandFlags() {
    }
}
