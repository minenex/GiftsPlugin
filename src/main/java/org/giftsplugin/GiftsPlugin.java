package org.giftsplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.giftsplugin.command.GiftCommand;
import org.giftsplugin.config.PluginConfig;
import org.giftsplugin.gift.GiftRegistry;
import org.giftsplugin.listener.GiftBreakListener;
import org.giftsplugin.listener.GiftFindListener;
import org.giftsplugin.listener.GiftPlaceListener;
import org.giftsplugin.menu.GiftMenu;
import org.giftsplugin.menu.MenuListener;
import org.giftsplugin.reward.RewardHandler;
import org.giftsplugin.service.FireworkService;
import org.giftsplugin.service.MessageService;
import org.giftsplugin.service.SoundService;

public final class GiftsPlugin extends JavaPlugin {

    private PluginConfig pluginConfig;
    private GiftRegistry giftRegistry;

    @Override
    public void onEnable() {
        NamespacedKey giftTypeKey = new NamespacedKey(this, "gift_type");
        NamespacedKey giftIdKey = new NamespacedKey(this, "gift_id");

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        giftRegistry = new GiftRegistry(this);
        giftRegistry.load();

        MessageService messageService = new MessageService(pluginConfig);
        SoundService soundService = new SoundService(pluginConfig);
        FireworkService fireworkService = new FireworkService(this, pluginConfig);
        RewardHandler rewardHandler = new RewardHandler(pluginConfig);
        GiftMenu giftMenu = new GiftMenu(pluginConfig, giftRegistry, soundService, giftIdKey);

        GiftCommand command = new GiftCommand(
                pluginConfig,
                giftRegistry,
                messageService,
                soundService,
                giftMenu,
                giftTypeKey
        );

        getCommand("gifts").setExecutor(command);
        getCommand("gifts").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(
                new GiftPlaceListener(giftRegistry, messageService, soundService, giftTypeKey),
                this
        );

        getServer().getPluginManager().registerEvents(
                new GiftFindListener(this, giftRegistry, messageService, soundService, rewardHandler, fireworkService),
                this
        );

        getServer().getPluginManager().registerEvents(
                new GiftBreakListener(giftRegistry, messageService, soundService),
                this
        );

        getServer().getPluginManager().registerEvents(
                new MenuListener(messageService, soundService),
                this
        );

        getLogger().info("GiftsPlugin enabled");
    }

    @Override
    public void onDisable() {
        if (giftRegistry != null) {
            giftRegistry.save();
        }
        getLogger().info("GiftsPlugin disabled");
    }
}
