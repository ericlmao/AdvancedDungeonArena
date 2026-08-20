package su.nightexpress.dungeons.nightcore.util.profile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerTextures;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;

public class PlayerProfiles {

    private PlayerProfiles() {
    }

    public static final String TEXTURES_HOST = "http://textures.minecraft.net/texture/";

    @Deprecated
    private static final Map<UUID, CachedProfile> CACHED_PROFILES = new ConcurrentHashMap<>();

    @NonNull
    @Deprecated
    private static CachedProfile cacheTemporary(@NonNull PlayerProfile profile) {
        return cacheProfile(profile, false, false);
    }

    /* @NonNull
    private static CachedProfile cachePermanent(@NonNull PlayerProfile profile) {
        return cacheProfile(profile, true, false);
    } */

    @NonNull
    @Deprecated
    public static CachedProfile cacheExact(@NonNull PlayerProfile profile) {
        return cacheProfile(profile, true, true);
    }

    @NonNull
    @Deprecated
    private static CachedProfile cacheProfile(@NonNull PlayerProfile profile, boolean permanent, boolean noUpdate) {
        if (profile.getId() == null) {
            return new CachedProfile(profile, true, true);
        }

        CachedProfile cachedProfile = new CachedProfile(profile, permanent, noUpdate);
        CACHED_PROFILES.put(profile.getId(), cachedProfile);
        return cachedProfile;
    }

    @NonNull
    @Deprecated
    private static CachedProfile queryOrCache(@NonNull UUID id, @NonNull Supplier<PlayerProfile> supplier) {
        CachedProfile cached = getCachedProfile(id);
        if (cached != null) {
            return cached;
        }

        return cacheTemporary(supplier.get());
    }

    @Nullable
    @Deprecated
    public static CachedProfile getCachedProfile(@NonNull UUID id) {
        return CACHED_PROFILES.get(id);
    }

    @NonNull
    @Deprecated
    public static Set<CachedProfile> getCachedProfiles() {
        return new HashSet<>(CACHED_PROFILES.values());
    }

    @Deprecated
    public static void clear() {
        CACHED_PROFILES.clear();
    }

    @Deprecated
    public static void purgeProfiles() {
        CACHED_PROFILES.values().removeIf(CachedProfile::isPurgeTime);
    }

    @NonNull
    @Deprecated
    public static CachedProfile getProfile(@NonNull OfflinePlayer player) {
        return queryOrCache(player.getUniqueId(), () -> player.getPlayerProfile());
    }

    @NonNull
    @Deprecated
    public static CachedProfile createProfile(@NonNull UUID uuid) {
        return queryOrCache(uuid, () -> Bukkit.createProfile(uuid));
    }

    @NonNull
    @Deprecated
    public static PlayerProfile createProfile(@NonNull String name) {
        return create(name);
    }

    @NonNull
    @Deprecated
    public static CachedProfile createProfile(@NonNull UUID uuid, @Nullable String name) {
        return queryOrCache(uuid, () -> Bukkit.createProfile(uuid, name));
    }

    public static @NonNull PlayerProfile create(@NonNull UUID uuid) {
        return Bukkit.createProfile(uuid);
    }


    public static @NonNull PlayerProfile create(@NonNull String name) {
        return Bukkit.createProfile(name);
    }


    public static @NonNull PlayerProfile create(@NonNull UUID uuid, @Nullable String name) {
        return Bukkit.createProfile(uuid, name);
    }

    @Nullable
    @Deprecated
    public static CachedProfile createProfileBySkinURL(@NonNull String urlData) {
        if (urlData.isBlank()) return null;

        String name = urlData.substring(0, 16);

        if (!urlData.startsWith(TEXTURES_HOST)) {
            urlData = TEXTURES_HOST + urlData;
        }

        try {
            UUID uuid = UUID.nameUUIDFromBytes(urlData.getBytes());
            CachedProfile cached = getCachedProfile(uuid);
            if (cached != null) return cached;

            // If no name, then meta#getOwnerProfile will return 'null'.
            PlayerProfile profile = Bukkit.createProfile(uuid, name);
            URL url = URI.create(urlData).toURL();
            PlayerTextures textures = profile.getTextures();

            textures.setSkin(url);
            profile.setTextures(textures);
            return cacheExact(profile);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }


    public static @NonNull PlayerProfile createStaticTexturedProfile(@NonNull String url) {
        try {
            if (!url.startsWith(TEXTURES_HOST)) {
                url = TEXTURES_HOST + url;
            }

            URL skinUrl = new URI(url).toURL();
            return createStaticTexturedProfile(skinUrl);
        }
        catch (URISyntaxException | MalformedURLException exception) {
            throw new IllegalArgumentException("Could not create profile", exception);
        }
    }

    public static @NonNull PlayerProfile createStaticTexturedProfile(@NonNull URL skinUrl) {
        PlayerProfile profile = Bukkit.createProfile(java.util.UUID.randomUUID(), null);
        PlayerTextures textures = profile.getTextures();

        textures.setSkin(skinUrl);
        profile.setTextures(textures);
        return profile;
    }

    @Nullable
    public static String getProfileSkinURL(@NonNull PlayerProfile profile) {
        URL skin = profile.getTextures().getSkin();
        if (skin == null) return null;

        String raw = skin.toString();
        return raw.substring(TEXTURES_HOST.length());
    }
}
