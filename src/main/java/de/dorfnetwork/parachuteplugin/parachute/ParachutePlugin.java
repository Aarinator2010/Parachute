package de.dorfnetwork.parachuteplugin.parachute;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class ParachutePlugin extends JavaPlugin implements Listener, CommandExecutor {

    private final HashSet<UUID> toggledOff = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("parachute").setExecutor(this);
    }

    @EventHandler
    public void onFall(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (toggledOff.contains(player.getUniqueId())) return;
        if (getConfig().getBoolean("use-permission") && !player.hasPermission("parachute.use")) return;

        if (player.getFallDistance() >= getConfig().getDouble("min-fall-height")
                && !player.isGliding()
                && player.getLocation().getBlock().getType() == Material.AIR) {
            player.setGliding(true);
        }
    }

    @EventHandler
    public void onGlideToggle(EntityToggleGlideEvent event) {
        // Prevent manual cancelling while still in the air to simulate a parachute
        if (event.getEntity() instanceof Player player) {
            if (!player.isOnGround() && !toggledOff.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (toggledOff.contains(player.getUniqueId())) {
            toggledOff.remove(player.getUniqueId());
            player.sendMessage("Parachute enabled.");
        } else {
            toggledOff.add(player.getUniqueId());
            player.setGliding(false);
            player.sendMessage("Parachute disabled.");
        }
        return true;
    }
}