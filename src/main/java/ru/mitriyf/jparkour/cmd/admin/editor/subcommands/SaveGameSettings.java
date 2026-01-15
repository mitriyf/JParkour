package ru.mitriyf.jparkour.cmd.admin.editor.subcommands;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import ru.mitriyf.jparkour.JParkour;
import ru.mitriyf.jparkour.game.temp.editor.Editor;
import ru.mitriyf.jparkour.utils.Utils;
import ru.mitriyf.jparkour.values.Values;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class SaveGameSettings {
    private final Set<UUID> confirmation = new HashSet<>();
    private final BukkitScheduler scheduler;
    private final JParkour plugin;
    private final Values values;
    private final Utils utils;

    public SaveGameSettings(JParkour plugin) {
        this.plugin = plugin;
        utils = plugin.getUtils();
        values = plugin.getValues();
        scheduler = plugin.getServer().getScheduler();
    }

    public void saveGame(Player p, Editor editor, String name) {
        if (editor.getPose1() == null || editor.getPose2() == null || editor.getPose3() == null) {
            p.sendMessage("§cNo positions are selected. Cancel...");
            return;
        } else if (editor.getSpawn() == null || editor.getPortal() == null || editor.getStart() == null || editor.getEnd() == null) {
            p.sendMessage("§cNo locs are selected. Cancel...");
            return;
        } else if (values.getSchematics().containsKey(name.toLowerCase()) && !confirmation.contains(p.getUniqueId())) {
            p.sendMessage("§cSuch schematics already exist. §aWrite the command again if you are sure that you want to overwrite the schematic.\nYou have 30 seconds to confirm.");
            confirmation.add(p.getUniqueId());
            scheduler.runTaskLater(plugin, () -> confirmation.remove(p.getUniqueId()), 600);
            return;
        } else if (editor.isProcess()) {
            p.sendMessage("§cSaving is already in progress! Please wait...");
            return;
        }
        editor.setProcess(true);
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                CountDownLatch clearLatch = new CountDownLatch(1);
                scheduler.runTask(plugin, () -> {
                    editor.clear();
                    scheduler.runTaskLater(plugin, clearLatch::countDown, 5L);
                });
                clearLatch.await();
                utils.getSchematic().save(name, editor.getPose1(), editor.getPose2(), editor.getPose3());
                values.setup(false);
                CountDownLatch saveLatch = new CountDownLatch(1);
                scheduler.runTask(plugin, () -> {
                    editor.save(name);
                    scheduler.runTaskLater(plugin, saveLatch::countDown, 5L);
                });
                saveLatch.await();
                editor.setProcess(false);
                p.sendMessage("§aSuccessfully!\n§eSettings accepted.\nExit the editor: /jparkour exit");
            } catch (Exception e) {
                plugin.getLogger().warning("Couldn't save schem. Error: " + e);
                p.sendMessage("§cCouldn't save schem.");
            }
        });
    }
}
