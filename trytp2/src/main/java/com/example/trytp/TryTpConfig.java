package com.example.trytp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Config file: .minecraft/config/trytp.json (created on first launch). */
public class TryTpConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Delay between detecting TPA-on and sending the command. */
    public int delayMs = 50;

    /** Stop waiting after this many seconds. 0 = wait forever. */
    public int timeoutSeconds = 600;

    /** Command sent (no leading slash). %player% is replaced by the name. */
    public String command = "tpahere %player%";

    /**
     * Regexes matched against server messages. %player% is replaced by the (escaped) name.
     * Edit these to match what YOUR server prints when someone enables TPA.
     */
    public List<String> patterns = List.of(
        "(?i)%player%.*(tpa|teleport requests?).*(enabled|turned on|\\bon\\b|accepting)",
        "(?i)(tpa|teleport requests?).*(enabled|turned on|\\bon\\b).*%player%"
    );

    public static TryTpConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("trytp.json");
        try {
            if (Files.exists(path)) {
                TryTpConfig cfg = GSON.fromJson(Files.readString(path), TryTpConfig.class);
                if (cfg != null) return cfg;
            }
            TryTpConfig cfg = new TryTpConfig();
            Files.writeString(path, GSON.toJson(cfg));
            return cfg;
        } catch (Exception e) {
            e.printStackTrace();
            return new TryTpConfig();
        }
    }
}
