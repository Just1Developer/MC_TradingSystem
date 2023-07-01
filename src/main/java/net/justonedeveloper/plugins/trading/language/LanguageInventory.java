package net.justonedeveloper.plugins.trading.language;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class LanguageInventory implements Listener {

	private static final String InventoryTitle = "§8Select Trading System Language";
	
	public static Inventory generateInventoryOf(Player Player)
	{
		Inventory inv = Bukkit.createInventory(null, 54, InventoryTitle);
		
		// put all the languages here, add an enchantment to the language
		
		if(Player.isOp() || Player.hasPermission("trading.admin.language"))
		{
			// Add admin items (like adding / removing a language) here
		}
		
		return inv;
	}

}
