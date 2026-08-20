package su.nightexpress.dungeons.nightcore.configuration.codec.core;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.configuration.codec.ConfigCodec;
import su.nightexpress.dungeons.nightcore.configuration.exception.CodecReadException;
import su.nightexpress.dungeons.nightcore.util.ArrayUtil;

@NullMarked
public class IntArrayCodec implements ConfigCodec<int[]> {

    @Override
    public int[] read(FileConfig config, String path) throws CodecReadException {
        String str = config.getString(path);
        return str == null ? new int[0] : ArrayUtil.parseIntArray(str);
    }

    @Override
    public void write(FileConfig config, String path, int[] array) {
        config.set(path, ArrayUtil.arrayToString(array));
    }
}
