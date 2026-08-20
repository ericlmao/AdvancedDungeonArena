package su.nightexpress.dungeons.nightcore.integration.currency;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.dungeons.nightcore.NightPlugin;
import su.nightexpress.dungeons.nightcore.bridge.currency.Currency;
import su.nightexpress.dungeons.nightcore.config.ConfigValue;
import su.nightexpress.dungeons.nightcore.config.FileConfig;
import su.nightexpress.dungeons.nightcore.core.CoreSettings;
import su.nightexpress.dungeons.nightcore.integration.currency.impl.ItemStackCurrency;
import su.nightexpress.dungeons.nightcore.integration.currency.impl.VaultEconomyCurrency;
import su.nightexpress.dungeons.nightcore.integration.currency.impl.XPLevelsCurrency;
import su.nightexpress.dungeons.nightcore.integration.currency.type.IncompleteCurrency;
import su.nightexpress.dungeons.nightcore.util.Plugins;
import su.nightexpress.dungeons.nightcore.util.Strings;

/**
 * Detects and registers currencies into {@link EconomyBridge}.
 * <p>
 * Upstream this was an {@code AbstractManager} owned by the NightCore plugin, with a plugin-load
 * listener, a {@code /currency} command tree and adapters for CoinsEngine, PlayerPoints,
 * UltraEconomy, BeastTokens, VotingPlugin and EliteMobs. Only the built-in providers survive here:
 * Vault, vanilla XP levels and config-defined item currencies. Those third-party bridges were dead
 * code in this plugin.
 */
public class CurrencyManager {

    public static final String FILE_NAME = "currencies.yml";

    private static final CurrencyRegistry REGISTRY = new CurrencyRegistry();

    private static NightPlugin plugin;
    private static FileConfig  config;

    private CurrencyManager() {
    }

    public static void load(@NonNull NightPlugin owner) {
        plugin = owner;
        EconomyBridge.register(new EconomyBridgeProvider(REGISTRY));

        config = FileConfig.loadOrExtract(owner, FILE_NAME);

        if (Plugins.isInstalled(CurrencyPlugins.VAULT)) {
            loadIncompleted(VaultEconomyCurrency::new);
        }
        loadIncompleted(XPLevelsCurrency::new);
        loadItemCurrencies();

        config.saveChanges();
    }

    public static void unload() {
        REGISTRY.clear();
        config = null;
        plugin = null;
    }

    public static void loadItemCurrencies() {
        String path = "Items";

        if (!config.contains(path)) {
            ItemStackCurrency.createDefaults()
                .forEach(currency -> config.set(path + "." + currency.getInternalId(), currency));
        }

        config.getSection(path).forEach(sId -> {
            try {
                loadIncompleted(() -> ItemStackCurrency.read(config, path + "." + sId, sId));
            }
            catch (IllegalStateException exception) {
                plugin.error("Item currency '" + sId + "' not loaded: " + exception.getMessage());
            }
        });
    }

    @Nullable
    public static ItemStackCurrency createItemCurrency(@NonNull String name, @NonNull ItemStack itemStack) {
        String id = Strings.filterForVariable(name);
        if (REGISTRY.contains(id)) return null;

        ItemStackCurrency currency = new ItemStackCurrency(id, itemStack);

        config.set("Items." + currency.getInternalId(), currency);
        config.save();

        loadIncompleted(() -> currency);
        return currency;
    }

    public static <T extends IncompleteCurrency> void loadIncompleted(@NonNull Supplier<T> supplier) {
        register(supplier.get(), currency -> {
            String path = currency instanceof ItemStackCurrency ? "Items" : "Currencies";
            CurrencySettings settings = ConfigValue.create(path + "." + currency.getInternalId() + ".Settings",
                CurrencySettings::load, currency.getDefaultSettings()).read(config);
            currency.setSettings(settings);
        });
    }

    private static <T extends Currency> void register(@NonNull T currency, @Nullable Consumer<T> preRegister) {
        String id = currency.getInternalId();
        if (CoreSettings.ECONOMY_DISABLED_PROVIDERS.contains(id)) return;

        if (preRegister != null) preRegister.accept(currency);

        REGISTRY.register(currency);
        plugin.info("Currency registered: '" + id + "'.");
    }
}
