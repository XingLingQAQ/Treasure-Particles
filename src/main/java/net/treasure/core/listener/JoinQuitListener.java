package net.treasure.core.listener;

import lombok.AllArgsConstructor;
import net.treasure.common.Permissions;
import net.treasure.core.TreasurePlugin;
import net.treasure.locale.Translations;
import net.treasure.util.message.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class JoinQuitListener implements Listener {

    final TreasurePlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getPlayerManager().initializePlayer(player, data -> {
            if (player.hasPermission(Permissions.ADMIN) && plugin.getNotificationManager().isEnabled() && data.isNotificationsEnabled()) {
                MessageUtils.sendParsed(player, Translations.PREFIX + "<aqua><b><click:suggest_command:'/trelytra changelog'><hover:show_text:'<aqua>Click!'>Changelog</click></b> <dark_gray>|</dark_gray> " +
                        "<b><click:open_url:'https://www.spigotmc.org/resources/99860/'><hover:show_text:'<aqua>Click'>Spigot Page</b> <dark_gray>|</dark_gray> " +
                        "<b><click:open_url:'https://github.com/Treasure-Inc/Treasure-Elytra/wiki/'><hover:show_text:'<aqua>Click!'>Wiki Page");
            }
        });
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        plugin.getPlayerManager().remove(event.getPlayer());
    }
}