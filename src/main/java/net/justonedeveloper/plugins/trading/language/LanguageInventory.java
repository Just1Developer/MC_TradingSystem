package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.settings.TradeSettingsInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.UUID;

public class LanguageInventory implements Listener {
	
	private static final int LanguagesPerPage = 7;
	private static int pages()
	{
		return Language.languageAmount() / (LanguagesPerPage+1) + 1;
	}
	
	public static void openInventory(Player p) { openInventory(p, 1); }
	public static void openInventory(Player p, int page)
	{
		boolean hasPerms = p.isOp() || p.hasPermission("trading.admin.language");
		int invSize = hasPerms ? 36 : 27;
		Inventory inv = Bukkit.createInventory(null, invSize, Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE, p.getName()));
		UUID uuid = p.getUniqueId();
		Language lang = Language.getLanguage(uuid);
		int pages = pages();
		if(page > pages) page %= pages;
		
		// This array fits the number of languages that will be displayed
		int amount = Language.languageAmount() / LanguagesPerPage >= page ? LanguagesPerPage : Language.languageAmount() % LanguagesPerPage;
		int startIndex = (page-1) * LanguagesPerPage;
		int empty = (int) (3.5 - amount * 0.5);
		int index = 10 + empty;
		
		for(int i = 0; i < amount; ++i)
		{
			// Add lang item: Perhaps later allow different item types
			Language lang2 = Language.getLanguage(Language.get(startIndex + i));
			
			ItemStack item = new ItemStack(lang2.ItemMaterial);
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName("§d§l" + lang2.LanguageName);
			meta.setLore(Collections.singletonList("§7Code: " + lang2.LanguageCode));
			item.setItemMeta(meta);
			inv.setItem(index, item);
			
			if(hasPerms)
			{
				ItemStack edit = new ItemStack(Material.IRON_AXE);
				meta = edit.getItemMeta();
				assert meta != null;
				meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_ADMIN_EDIT_LANGUAGE_NAME));
				edit.setItemMeta(meta);
				inv.setItem(index + 9, edit);
			}
			index++;
			if(amount % 2 == 0 && index > amount / 2) index++;	// empty in the middle
		}
		
		ItemStack item;
		ItemMeta meta;
		
		if(page > 1)
		{
			item = new ItemStack(Material.ARROW);
			meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_PREV_PAGE_NAME));
			item.setItemMeta(meta);
			inv.setItem(invSize - 9, item);
		}
		if(page < pages)
		{
			item = new ItemStack(Material.ARROW);
			meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_NEXT_PAGE_NAME));
			item.setItemMeta(meta);
			inv.setItem(invSize - 1, item);
		}
		
		ItemStack backToSettings = new ItemStack(Material.REDSTONE);
		meta = backToSettings.getItemMeta();
		assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_BACK_TO_SETTINGS_NAME));
		backToSettings.setItemMeta(meta);
		inv.setItem(0, backToSettings);
		
		p.openInventory(inv);
	}
	
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if (e.getClickedInventory() == null || !e.getView().getTitle().equals(Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE, p.getName())))
			return;
		
		if (e.getClick().equals(ClickType.DOUBLE_CLICK)) {
			e.setCancelled(true);
			return;
		}
		
		if (!e.getInventory().equals(e.getClickedInventory())) {
			if (e.isShiftClick()) e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		
		// Clicked on Settings inv or shift clicked, both should get cancelled
		InvokeClickedSlot(p, e.getInventory(), e.getCurrentItem(), e.getRawSlot());
	}
	
	private static void InvokeClickedSlot(Player p, Inventory inv, ItemStack currentItem, int rawSlot)
	{
		if (currentItem == null) return;
		if (currentItem.getType() == Material.AIR) return;
		
		UUID uuid = p.getUniqueId();
		int prevSlot;
		ItemStack enabled;
		Language lang = Language.getLanguage(uuid);
		
		// ...
		Bukkit.broadcastMessage("Processing InventoryClick in Lang Settings...");
		
		// Todo Others
		if(rawSlot == 0)
		{
			TradeSettingsInventory.OpenInventory(p);
			return;
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e)
	{
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(!e.getView().getTitle().equals(Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE, p.getName()))) return;
		
		for(int slot : e.getRawSlots())
		{
			if(slot < 54)
			{
				e.setCancelled(true);
				if(e.getRawSlots().size() == 1)
					InvokeClickedSlot(p, e.getInventory(), e.getInventory().getItem(slot), slot);
				return;
			}
		}
	}
		
}
