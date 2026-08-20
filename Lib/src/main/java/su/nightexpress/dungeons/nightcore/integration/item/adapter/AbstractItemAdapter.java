package su.nightexpress.dungeons.nightcore.integration.item.adapter;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.dungeons.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.integration.item.impl.AdaptedItemStack;
import su.nightexpress.dungeons.nightcore.integration.item.adapter.impl.VanillaItemAdapter;
import su.nightexpress.dungeons.nightcore.util.LowerCase;

public abstract class AbstractItemAdapter<T> implements ItemAdapter<T> {

    protected final String name;

    public AbstractItemAdapter(@NonNull String name) {
        this.name = LowerCase.INTERNAL.apply(name);
    }

    @Nullable
    public abstract AdaptedItemStack<T> readItem(@NonNull FileConfig config, @NonNull String path);

    @Override
    @NonNull
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isVanilla() {
        return this == VanillaItemAdapter.INSTANCE;
    }
}
