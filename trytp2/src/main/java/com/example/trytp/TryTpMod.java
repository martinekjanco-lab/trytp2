package com.example.trytp;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TryTpMod implements ClientModInitializer {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "trytp-scheduler");
        t.setDaemon(true);
        return t;
    });

    private static TryTpConfig config;
    private static volatile String target = null;
    private static volatile long startedAt = 0;

    @Override
    public void onInitializeClient() {
        config = TryTpConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommandManager.literal("trytp")
                .then(ClientCommandManager.literal("cancel").executes(ctx -> {
                    target = null;
                    say("Cancelled.");
                    return 1;
                }))
                .then(ClientCommandManager.literal("reload").executes(ctx -> {
                    config = TryTpConfig.load();
                    say("Config reloaded.");
                    return 1;
                }))
                .then(ClientCommandManager.argument("player", StringArgumentType.word()).executes(ctx -> {
                    target = StringArgumentType.getString(ctx, "player");
                    startedAt = System.currentTimeMillis();
                    say("Waiting for " + target + " to turn TPA on... (/trytp cancel to stop)");
                    return 1;
                }))));

        // GAME = server/system messages only, so players can't trigger it by typing in chat.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (overlay) return;
            String name = target;
            if (name == null) return;

            if (config.timeoutSeconds > 0
                    && System.currentTimeMillis() - startedAt > config.timeoutSeconds * 1000L) {
                target = null;
                say("Timed out waiting for " + name + ".");
                return;
            }

            String text = message.getString();
            for (String p : config.patterns) {
                String regex = p.replace("%player%", Pattern.quote(name));
                if (Pattern.compile(regex).matcher(text).find()) {
                    target = null; // fire only once
                    String cmd = config.command.replace("%player%", name);
                    SCHEDULER.schedule(() -> Minecraft.getInstance().execute(() -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player != null) mc.player.connection.sendCommand(cmd);
                    }), Math.max(0, config.delayMs), TimeUnit.MILLISECONDS);
                    return;
                }
            }
        });
    }

    private static void say(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[TryTP] " + msg), false);
    }
}
