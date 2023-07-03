package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.settings.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LanguageInventory implements Listener {
	
	private static final int LanguagesPerPage = 7;
	private static int pages()
	{
		return Language.languageAmount() / (LanguagesPerPage+1) + 1;
	}
	
	public static void openInventory(Player p) { openInventory(p, 1); }
	public static void openInventory(Player p, int page)
	{
		boolean hasPerms = p.isOp() || p.hasPermission("trading.admin.language.edit") || p.hasPermission("trading.admin.language.*");
		int invSize = hasPerms ? 36 : 27;
		Inventory inv = Bukkit.createInventory(null, invSize, Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE).replace("%page%", "" + page));
		UUID uuid = p.getUniqueId();
		Language lang = Language.getLanguage(uuid);
		int pages = pages();
		if(page > pages) page %= pages;
		
		// This array fits the number of languages that will be displayed
		int amount = Language.languageAmount() / LanguagesPerPage >= page ? LanguagesPerPage : Language.languageAmount() % LanguagesPerPage;
		int startIndex = (page-1) * LanguagesPerPage;
		int empty = (int) (3.5 - amount * 0.5);
		if(amount == 3) empty -= 1;
		int index = 10 + empty;
		
		for(int i = 0; i < amount; ++i)
		{
			// Add lang item: Perhaps later allow different item types
			Language lang2 = Language.getLanguage(Language.get(startIndex + i));
			
			ItemStack item = new ItemStack(lang2.ItemMaterial);
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName("§d§l" + lang2.LanguageName);
			if(lang2.LanguageCode.equals(lang.LanguageCode))
			{
				meta.setLore(Arrays.asList("§7Code: " + lang2.LanguageCode, lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME)));
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			else
			{
				meta.setLore(Collections.singletonList("§7Code: " + lang2.LanguageCode));
			}
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
			if(amount % 2 == 0 && index == amount / 2 || amount == 3) index++;	// empty in the middle
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
		String title = Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_TITLE).replace("%lang%", ""), title2 = Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE).replace("%page%", "");
		if (e.getClickedInventory() == null || (!e.getView().getTitle().startsWith(title2) && !e.getView().getTitle().startsWith(title)))
			return;
		
		if(e.isShiftClick())
		{
			e.setCancelled(true);
			// Possibly delete language item
			if(!e.getView().getTitle().startsWith(title)) return;
			if(e.getCurrentItem() == null) return;
			if(!e.getCurrentItem().hasItemMeta()) return;
			assert e.getCurrentItem().getItemMeta() != null;
			String dn = e.getCurrentItem().getItemMeta().getDisplayName();
			
			String lang = e.getCurrentItem().getItemMeta().getDisplayName();
			lang = lang.substring(lang.length() - 5);
			
			String lName = Language.getLanguage(lang).LanguageName;
			
			if(e.getCurrentItem().getType() == Material.BARRIER && dn.startsWith(Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_NAME).split("%langName%")[0]))
			{
				if (!p.isOp() && !p.hasPermission("trading.admin.language.delete") && !p.hasPermission("trading.admin.language.*"))
				{
					p.sendMessage(Language.get(p, Phrase.ERROR_INSUFFICIENT_PERMISSIONS));
					return;
				}
				if(!Language.exists(lang))
				{
					p.sendMessage(Language.get(p, Phrase.ERROR_LANGUAGE_NOT_EXIST));
					return;
				}
				
				Language.deleteLanguage(lang);
				p.sendMessage(Language.get(p, Phrase.LANGUAGE_EDIT_MESSAGE_LANG_DELETED).replace("%langName%", lName).replace("%lang%", lang));
				openInventory(p, 1);
				return;
			}
			
			if(e.getCurrentItem().getType() == Material.BEDROCK && dn.startsWith(Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_NAME).split("%langName%")[0]))
			{
				if (!p.isOp() && !p.hasPermission("trading.admin.language.reset") && !p.hasPermission("trading.admin.language.*"))
				{
					p.sendMessage(Language.get(p, Phrase.ERROR_INSUFFICIENT_PERMISSIONS));
					return;
				}
				if(!Language.exists(lang))
				{
					p.sendMessage(Language.get(p, Phrase.ERROR_LANGUAGE_NOT_EXIST));
					return;
				}
				
				Material old = Language.getLanguage(lang).ItemMaterial;
				Language.deleteLanguage(lang);
				Language l = new Language(lang, lName);
				l.setItemMaterial(old);
				
				p.sendMessage(Language.get(p, Phrase.LANGUAGE_EDIT_MESSAGE_LANG_RESET).replace("%langName%", lName).replace("%lang%", lang));
				openInventory(p, 1);
				return;
			}
		}
		
		if (e.getClick().equals(ClickType.DOUBLE_CLICK)) {
			e.setCancelled(true);
			return;
		}
		
		if (!e.getInventory().equals(e.getClickedInventory())) {
			if (e.isShiftClick()) e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		
		if(e.getCurrentItem() == null) return;	// Double but eh..
		
		// Clicked on Settings inv or shift clicked, both should get cancelled
		if(e.getView().getTitle().startsWith(Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE).replace("%page%", ""))) {
			String page = e.getView().getTitle();
			page = "" + page.charAt(page.length() - 1);
			InvokeClickedSlot(p, e.getInventory(), e.getCurrentItem(), e.getRawSlot(), Integer.parseInt(page));
			return;
		}
		
		InvokeLanguageEditClickedSlot(p, e.getInventory(), e.getCurrentItem(), e.getCursor(), e.getRawSlot(), e.getView().getTitle().replace(title, ""));
	}
	
	private static void InvokeClickedSlot(Player p, Inventory inv, ItemStack currentItem, int rawSlot, int CurrentPage)
	{
		if (currentItem == null) return;
		if (currentItem.getType() == Material.AIR) return;
		
		UUID uuid = p.getUniqueId();
		Language lang = Language.getLanguage(uuid);
		
		if(rawSlot == 0)
		{
			TradeSettings.openInventory(p);
			return;
		}
		
		if(rawSlot == 18 || rawSlot == 27)
		{
			// Back a page, since this is definitely an item
			openInventory(p, CurrentPage - 1);
			return;
		}
		if(rawSlot == 26 || rawSlot == 35)
		{
			// Back a page, since this is definitely an item
			openInventory(p, CurrentPage + 1);
			return;
		}
		
		ItemStack langItem = rawSlot > 17 ? inv.getItem(rawSlot - 9) : currentItem;
		if(langItem == null) return;
		if(!langItem.hasItemMeta()) return;
		assert langItem.getItemMeta() != null;
		if(!langItem.getItemMeta().hasLore()) return;
		assert langItem.getItemMeta().getLore() != null;
		if(langItem.getItemMeta().getLore().size() == 0) return;
		
		String code = langItem.getItemMeta().getLore().get(0);
		code = code.substring(code.length() - 5);
		
		if(rawSlot > 17)
		{
			// Third or fourth row
			// Definitely 'Edit language' item
			
			if(!Language.exists(code))
			{
				p.sendMessage(lang.get(Phrase.ERROR_LANGUAGE_NOT_EXIST));
				return;
			}
			openEditLanguageInventory(p, Language.getLanguage(code), CurrentPage);
			return;
		}
		if(langItem.getItemMeta().getEnchants().size() > 0) return;		// Enchanted Item here = Language is already selected
		
		// Language Selection
		if(!Language.exists(code))
		{
			p.sendMessage(lang.get(Phrase.ERROR_LANGUAGE_NOT_EXIST));
			p.closeInventory();
			return;
		}
		TradeSettings.setLanguage(uuid, code);
		p.sendMessage(lang.get(Phrase.TRADE_LANG_SETTINGS_CHANGED_LANGUAGE_MESSAGE).replace("%lang%", currentItem.getItemMeta().getDisplayName()));
		TradeSettings.openInventory(p);
	}
	
	private static void InvokeLanguageEditClickedSlot(Player p, Inventory inv, ItemStack currentItem, ItemStack Cursor, int rawSlot, String LangName)
	{
		switch(rawSlot)
		{
			case 0:
				// Back
				if(currentItem == null) return;	// Actually, Cursor is always != null
				if(currentItem.getType().isAir()) return;
				if(!currentItem.hasItemMeta()) return;
				assert currentItem.getItemMeta() != null;
				if(!currentItem.getItemMeta().hasLore()) return;
				assert currentItem.getItemMeta().getLore() != null;
				if(currentItem.getItemMeta().getLore().size() == 0) return;
				
				String l = currentItem.getItemMeta().getLore().get(0);
				try {
					openInventory(p, Integer.parseInt("" + l.charAt(l.length() - 1)));
				}
				catch (Exception ignored)
				{
					openInventory(p, 1);
				}
				break;
			case 11:
				// Update Item
				if(Cursor == null) return;	// Actually, Cursor is always != null
				if(Cursor.getType().isAir()) return;
				
				Material mat = Cursor.getType();
				Language lang = Language.getLanguage(p);
				lang.setItemMaterial(mat);
				
				ItemStack itemUpdateMaterial = new ItemStack(mat);
				ItemMeta meta = itemUpdateMaterial.getItemMeta();
				assert meta != null;
				meta.setDisplayName(Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_NAME));
				meta.setLore(Arrays.asList(Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_LORE).split("\n")));
				itemUpdateMaterial.setItemMeta(meta);
				inv.setItem(11, itemUpdateMaterial);
				
				p.sendMessage(Language.get(p, Phrase.LANGUAGE_EDIT_MESSAGE_ITEM_UPDATED).replace("%langName%", LangName));
				
				break;
			default:
				// Shift Click Should Handle slots 13 and 15
				break;
		}
	}
	
	public static void openEditLanguageInventory(Player p, Language lang, int page)
	{
		if(!p.isOp() && !p.hasPermission("trading.admin.language.edit") && !p.hasPermission("trading.admin.language.*")) return;
		Inventory inv = Bukkit.createInventory(null, 27, Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_TITLE).replace("%lang%", lang.LanguageName));
		UUID uuid = p.getUniqueId();
		Language langPlayer = Language.getLanguage(uuid);
		
		ItemStack itemUpdateMaterial = new ItemStack(lang.ItemMaterial);
		ItemMeta meta = itemUpdateMaterial.getItemMeta();
		assert meta != null;
		meta.setDisplayName(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_NAME));
		meta.setLore(Arrays.asList(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_LORE).split("\n")));
		itemUpdateMaterial.setItemMeta(meta);
		
		ItemStack itemReset = new ItemStack(Material.BEDROCK);
		meta = itemReset.getItemMeta();
		assert meta != null;
		meta.setDisplayName(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_NAME).replace("%langName%", lang.LanguageName) + lang.LanguageCode);
		meta.setLore(Arrays.asList(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_LORE).split("\n")));
		itemReset.setItemMeta(meta);
		
		ItemStack itemDeleteLanguage;
		if(lang.LanguageCode.equals(Language.DefaultLanguage))
		{
			itemDeleteLanguage = new ItemStack(Material.BLACK_STAINED_GLASS);
			meta = itemDeleteLanguage.getItemMeta();
			assert meta != null;
			meta.setDisplayName(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_NAME).replace("%lang%", lang.LanguageName));
			meta.setLore(Arrays.asList(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_LORE).split("\n")));
			itemDeleteLanguage.setItemMeta(meta);
		}
		else {
			itemDeleteLanguage = new ItemStack(Material.BARRIER);
			meta = itemDeleteLanguage.getItemMeta();
			assert meta != null;
			meta.setDisplayName(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_NAME).replace("%langName%", lang.LanguageName) + lang.LanguageCode);
			meta.setLore(Arrays.asList(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_LORE).split("\n")));
			itemDeleteLanguage.setItemMeta(meta);
		}
		
		if(page > 0) {
			ItemStack itemBackToLang = new ItemStack(Material.REDSTONE);
			meta = itemBackToLang.getItemMeta();
			assert meta != null;
			meta.setDisplayName(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_NAME));
			meta.setLore(Collections.singletonList(langPlayer.get(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_LORE).replace("%page%", "" + page)));
			itemBackToLang.setItemMeta(meta);
			inv.setItem(0, itemBackToLang);
		}
		
		inv.setItem(11, itemUpdateMaterial);
		inv.setItem(13, itemReset);
		inv.setItem(15, itemDeleteLanguage);
		
		p.openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e)
	{
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		String title = Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_TITLE).replace("%lang%", ""), title2 = Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE).replace("%page%", "");
		if ((!e.getView().getTitle().startsWith(title2) && !e.getView().getTitle().startsWith(title)))
			return;
		
		
		for(int slot : e.getRawSlots())
		{
			if(slot < 54)
			{
				e.setCancelled(true);
				if(e.getRawSlots().size() == 1)
				{
					String page = e.getView().getTitle();
					page = "" + page.charAt(page.length() - 1);
					InvokeClickedSlot(p, e.getInventory(), e.getInventory().getItem(slot), slot, Integer.parseInt(page));
				}
				return;
			}
		}
	}
		
}
