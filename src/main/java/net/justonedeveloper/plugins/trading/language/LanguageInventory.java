package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.InventoryCloseResult;
import net.justonedeveloper.plugins.trading.main.TradingMain;
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

import java.util.Arrays;
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
		boolean hasPerms = TradingMain.hasPermission(p, "trading.admin.language.edit");
		int invSize = hasPerms ? 36 : 27;
		Inventory inv = Bukkit.createInventory(null, invSize, Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE).replace("%page%", "" + page));
		UUID uuid = p.getUniqueId();
		Language lang = Language.getLanguage(uuid);
		int pages = pages();
		if(page > pages) page %= pages;
		
		// This array fits the number of languages that will be displayed
		int amount = Language.languageAmount() / LanguagesPerPage >= page ? LanguagesPerPage : Language.languageAmount() % LanguagesPerPage;
		int startIndex = (page-1) * LanguagesPerPage;
		/*
		int empty = (int) (3.5 - amount * 0.5);
		if(amount == 3) empty -= 1;
		int index = 10 + empty;
		*/
		
		int[] indexes;
		switch (amount)
		{
			case 1:
				indexes = new int[] { 13 };
				break;
			case 2:
				indexes = new int[] { 12, 14 };
				break;
			case 3:
				indexes = new int[] { 11, 13, 15 };
				break;
			case 4:
				indexes = new int[] { 11, 12, 14, 15 };
				break;
			case 5:
				indexes = new int[] { 10, 11, 13, 15, 16 };
				break;
			case 6:
				indexes = new int[] { 10, 11, 12, 14, 15, 16 };
				break;
			default:
				indexes = new int[] { 10, 11, 12, 13, 14, 15, 16 };
				break;
		}
		
		for(int i = 0; i < indexes.length; ++i)
		{
			int index = indexes[i];
			// Add lang item: Perhaps later allow different item types
			Language lang2 = Language.getLanguage(Language.get(startIndex + i));
			
			ItemStack item = new ItemStack(lang2.ItemMaterial);
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			meta.setDisplayName("§d§l" + lang2.LanguageName);
			if(lang2.LanguageCode.equals(lang.LanguageCode))
			{
				meta.setLore(Arrays.asList("§7Code: " + lang2.LanguageCode, lang.get(Phrase.TRADE_SETTINGS_INVENTORY_SELECTED_NAME)));
				meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			else
			{
				meta.setLore(Collections.singletonList("§7Code: " + lang2.LanguageCode));
			}
			item.setItemMeta(meta);
			inv.setItem(index, item);
			
			if (hasPerms)
			{
				ItemStack edit = new ItemStack(Material.IRON_AXE);
				meta = edit.getItemMeta();
				assert meta != null;
				meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_ADMIN_EDIT_LANGUAGE_NAME));
				edit.setItemMeta(meta);
				inv.setItem(index + 9, edit);
			}
			index++;
			//if(amount % 2 == 0 && index == amount / 2 || amount == 3) index++;	// empty in the middle
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

		if (hasPerms) {
			ItemStack refreshLangs = new ItemStack(Material.SLIME_BLOCK);
			meta = refreshLangs.getItemMeta();
			assert meta != null;
			meta.setDisplayName(lang.get(Phrase.TRADE_LANG_SETTINGS_REFRESH_LANGS_NAME));
			meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_LANG_SETTINGS_REFRESH_LANGS_LORE)));
			refreshLangs.setItemMeta(meta);
			inv.setItem(8, refreshLangs);
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
		String editTitle = Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_TITLE), invTitle = Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE);
		String[] edit = editTitle.split("%lang%"), invT = invTitle.split("%page%");
		
		String edit1 = editTitle.startsWith("%lang%") ? "" : edit[0];
		String edit2 = editTitle.endsWith("%lang%") ? "" : edit[edit.length - 1];
		String inv1 = invTitle.startsWith("%page%") ? "" : invT[0];
		String inv2 = invTitle.endsWith("%page%") ? "" : invT[invT.length - 1];
		String title = e.getView().getTitle();
		
		// Continue if: starts end ends with edit or starts and ends with inv
		
		if(!(title.startsWith(edit1) && title.endsWith(edit2) || title.startsWith(inv1) && title.endsWith(inv2)))
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
				if (!TradingMain.hasPermission(p, "trading.admin.language.delete"))
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
				if (!TradingMain.hasPermission(p, "trading.admin.language.reset"))
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
				Language.CreateLanguageFile(lang, lName, true);
				Language language = Language.getLanguage(lang);
				language.reload();
				language.setItemMaterial(old);
				
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
		
		boolean hasPerms = TradingMain.hasPermission(p, "trading.admin.language.edit");

		if(rawSlot == 8 && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()
				&& currentItem.getItemMeta().getDisplayName().equals(lang.get(Phrase.TRADE_LANG_SETTINGS_REFRESH_LANGS_NAME)))
		{
			if (!hasPerms) {
				// Remove the item if the player lost permission between opening and clicking
				inv.setItem(8, new ItemStack(Material.AIR));
				return;
			}

			InventoryCloseResult result = TradingMain.closeAllTradingRelatedInventories();

			Language.ReInit();

			// Refresh all views for all players who currently have it open.
			result.reopen();
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
		if(langItem.getItemMeta().getLore().isEmpty()) return;
		
		String code = langItem.getItemMeta().getLore().get(0);
		code = code.substring(code.length() - 5);
		
		if(rawSlot > 17)
		{
			// Third or fourth row
			// Definitely 'Edit language' item

			if (!hasPerms) {
				// Remove the item if the player lost permission between opening and clicking
				inv.setItem(rawSlot, new ItemStack(Material.AIR));
				return;
			}
			if(!Language.exists(code))
			{
				p.sendMessage(lang.get(Phrase.ERROR_LANGUAGE_NOT_EXIST));
				return;
			}
			openEditLanguageInventory(p, Language.getLanguage(code), CurrentPage);
			return;
		}
		if(!langItem.getItemMeta().getEnchants().isEmpty()) return;		// Enchanted Item here = Language is already selected
		
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
				if(currentItem.getItemMeta().getLore().isEmpty()) return;
				
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
		if(!TradingMain.hasPermission(p, "trading.admin.language.edit")) return;
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
		String editTitle = Language.get(p, Phrase.LANGUAGE_EDIT_INVENTORY_TITLE), invTitle = Language.get(p, Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE);
		String[] edit = editTitle.split("%lang%"), invT = invTitle.split("%page%");
		
		String edit1 = editTitle.startsWith("%lang%") ? "" : edit[0];
		String edit2 = editTitle.endsWith("%lang%") ? "" : edit[edit.length - 1];
		String inv1 = invTitle.startsWith("%page%") ? "" : invT[0];
		String inv2 = invTitle.endsWith("%page%") ? "" : invT[invT.length - 1];
		String title = e.getView().getTitle();
		
		// Continue if: starts end ends with edit or starts and ends with inv
		
		if(!(title.startsWith(edit1) && title.endsWith(edit2) || title.startsWith(inv1) && title.endsWith(inv2)))
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

		/*
		System.out.println("editTitle: '" + editTitle + "'");
		System.out.println("invTitle: '" + invTitle + "'");
		System.out.println("editSize: '" + edit.length + "'");
		System.out.println("invSize: '" + invT.length + "'");
		System.out.println("edit1: '" + edit1 + "'");
		System.out.println("edit2: '" + edit2 + "'");
		System.out.println("inv1: '" + inv1 + "'");
		System.out.println("inv2: '" + inv2 + "'");
		System.out.println("title: '" + title + "'");
		System.out.println("condition1: '" + title.startsWith(edit1) + "'");
		System.out.println("condition2: '" + title.endsWith(edit2) + "'");
		System.out.println("condition3: '" + title.startsWith(inv1) + "'");
		System.out.println("condition4: '" + title.endsWith(inv2) + "'");
		*/
		