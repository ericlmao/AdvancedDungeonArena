package su.nightexpress.dungeons.nightcore.bridge;

import su.nightexpress.dungeons.nightcore.bridge.currency.Currency;
import su.nightexpress.dungeons.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.dungeons.nightcore.bridge.registry.NightRegistry;

public class Registries {

    @Deprecated
    public static final NightRegistry<String, Currency>       CURRENCY     = new NightRegistry<>();
    public static final NightRegistry<String, ItemAdapter<?>> ITEM_ADAPTER = new NightRegistry<>();

}
