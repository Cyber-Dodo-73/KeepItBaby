package fr.cyberdodo.keepItBaby;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.GameMode;

public class KeepItBaby extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("KeepItBaby plugin is enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("KeepItBaby plugin is disabled!");
    }

    @EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        // Assure que l'interaction concerne uniquement la main principale
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Vérifie si l'entité est "ageable" (peut grandir)
        if (entity instanceof Ageable ageable) {
            EntityType entityType = entity.getType();
            String entityName = capitalizeFirstLetter(entityType.name().toLowerCase().replace('_', ' '));

            boolean isBaby = !ageable.isAdult();
            boolean ageLocked = ageable.getAgeLock();

            // Gestion des Echo Shard pour bloquer l'âge
            if (itemInHand.getType() == Material.ECHO_SHARD) {
                if (ageLocked) {
                    player.sendMessage("§eLe ou la " + entityName + " est déjà bloqué(e) en mode bébé.");
                } else if (isBaby) {
                    // Empêche la croissance future et retire une Echo Shard (sauf si en mode créatif)
                    ageable.setAgeLock(true);
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }
                    player.sendMessage("§aLe ou la " + entityName + " restera bébé pour toujours !");
                    spawnParticles(entity, Particle.WAX_ON); // Particules
                    playSound(entity, Sound.BLOCK_BELL_USE); // Son de cloche
                } else {
                    player.sendMessage("§cLe ou la " + entityName + " est déjà adulte.");
                }
                return;
            }

            // Gestion des Amethyst Shard pour débloquer l'âge
            if (itemInHand.getType() == Material.AMETHYST_SHARD) {
                if (ageLocked && isBaby) {
                    // Permet la croissance future et retire une Amethyst Shard (sauf si en mode créatif)
                    ageable.setAgeLock(false);
                    if (player.getGameMode() != GameMode.CREATIVE) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }
                    player.sendMessage("§aLe ou la " + entityName + " pourra grandir à nouveau.");
                    spawnParticles(entity, Particle.WAX_OFF); // Particules
                    playSound(entity, Sound.BLOCK_AMETHYST_BLOCK_CHIME); // Son d'améthyste
                } else if (!ageLocked) {
                    player.sendMessage("§eLe ou la " + entityName + " peut déjà grandir.");
                } else {
                    player.sendMessage("§cLe ou la " + entityName + " est déjà adulte.");
                }
            }

            // Si accroupi, on affiche le statut du blocage d'âge sans consommer d'items
            if (player.isSneaking()) {
                if (ageLocked) {
                    player.sendMessage("§eLe ou la " + entityName + " est bloqué(e) en mode bébé.");
                }
            }
        }
    }

    // Fonction pour afficher des particules autour de l'entité
    private void spawnParticles(Entity entity, Particle particle) {
        entity.getWorld().spawnParticle(particle, entity.getLocation(), 20, 0.5, 0.5, 0.5, 0.1);
    }

    // Fonction pour jouer un son autour de l'entité
    private void playSound(Entity entity, Sound sound) {
        entity.getWorld().playSound(entity.getLocation(), sound, 1.0f, 1.0f);
    }

    // Fonction pour capitaliser la première lettre d'un mot
    private String capitalizeFirstLetter(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
