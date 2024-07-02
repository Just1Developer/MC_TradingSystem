package net.justonedeveloper.plugins.trading.main;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class InventoryCloseResult {

    private Map<Player, InventoryOpenRunnable> inventories;

    public InventoryCloseResult() {
        inventories = new HashMap<>();
    }

    public void reopen() {
        for (Map.Entry<Player, InventoryOpenRunnable> entry : inventories.entrySet()) {
            entry.getValue().run(entry.getKey());
        }
    }

    public void put(Player p, InventoryOpenRunnable runnable) {
        inventories.put(p, runnable);
    }

    public interface InventoryOpenRunnable {
        void run(Player player);
    }
}
