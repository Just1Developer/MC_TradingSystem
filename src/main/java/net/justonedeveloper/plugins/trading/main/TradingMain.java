package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import net.justonedeveloper.plugins.trading.language.LanguageInventory;
import net.justonedeveloper.plugins.trading.language.Phrase;
import net.justonedeveloper.plugins.trading.settings.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public final class TradingMain extends JavaPlugin {

	// Maybe do a basic own money implementation?
	// Money implementation from other plugins would only work if the store and read live from the file every time
	
	public static TradingMain main;

	/**
	* Permission Overview:
	 * trading.admin.* - All of them
	 * trading.admin.reloadsettings - Reload the settings
	 * trading.admin.language.* - All of them
	 * trading.admin.language.create - Create Languages
	 * trading.admin.language.reload - Reload Languages
	 * trading.admin.language.edit - Edit Languages / See the edit screen
	 * trading.admin.language.reset - Reset Languages
	 * trading.admin.language.delete - Delete Languages (not the main one)
	* */
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		main = this;
		Init();
		GlobalConfig.LoadConfig();
		Language.Init();
		// trade command, setting page with privacy and 2fa/double check like in Rocket League
		Bukkit.getPluginManager().registerEvents(new TradeInventoryEventHandler(), this);
		Bukkit.getPluginManager().registerEvents(new TradeSettings(), this);
		Bukkit.getPluginManager().registerEvents(new LanguageInventory(), this);
		Objects.requireNonNull(this.getCommand("trade")).setExecutor(new TradeCommand());
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
		closeAllTradingRelatedInventories();
	}

	public static InventoryCloseResult closeAllTradingRelatedInventories() {
		TradeInventoryEventHandler.CancelAllTrades();
		InventoryCloseResult result;
		try {
			result = new InventoryCloseResult();
		} catch (NoClassDefFoundError ignored) {
			// For some reason, cache issues can induce this problem
			// We still want the following logic to happen, so we have to catch the error.
			result = null;
		}
		for (Player p : Bukkit.getOnlinePlayers()) {
			InventoryView view = p.getOpenInventory();

			// For everything with a variable, compare an item, the prefix and the suffix
			
			if (isLanguageInventory(p, view)) {
				if (result != null) result.put(p, LanguageInventory::openInventory);
				p.closeInventory();
				continue;
			}
			
			if (isLanguageEditInventory(p, view)) {
				if (result != null) result.put(p, LanguageInventory::openInventory);
				p.closeInventory();
				continue;
			}
			
			if (isSettingsInventory(p, view)) {
				if (result != null) result.put(p, TradeSettings::openInventory);
				p.closeInventory();
			}
		}

		return result;
	}
	
	public static boolean isLanguageInventory(Player p, InventoryView view) {
		return isLanguageInventory(Language.getLanguage(p), view);
	}
	public static boolean isLanguageInventory(Language lang, InventoryView view) {
		// Trailing space to ensure suffix
		String title = view.getTitle() + " ";
		String realTitle = lang.get(Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE) + " ";
		if (realTitle.contains("%page%")) {
			String[] invTitle = realTitle.split("%page%");
			return title.startsWith(invTitle[0]) && title.endsWith(invTitle[invTitle.length - 1])
					&& view.getItem(0) != null && Objects.requireNonNull(view.getItem(0)).getType() == Material.REDSTONE;
		}
		return realTitle.equals(title);
	}
	
	public static boolean isLanguageEditInventory(Player p, InventoryView view) {
		return isLanguageEditInventory(Language.getLanguage(p), view);
	}
	public static boolean isLanguageEditInventory(Language lang, InventoryView view) {
		// Trailing space to ensure suffix
		String title = view.getTitle() + " ";
		String realTitle = lang.get(Phrase.LANGUAGE_EDIT_INVENTORY_TITLE) + " ";
		if (realTitle.contains("%lang%")) {
			String[] invTitle = realTitle.split("%lang%");
			return title.startsWith(invTitle[0]) && title.endsWith(invTitle[invTitle.length - 1])
					&& view.getItem(0) != null && Objects.requireNonNull(view.getItem(0)).getType() == Material.REDSTONE;
		}
		return realTitle.equals(title);
	}
	
	public static boolean isSettingsInventory(Player p, InventoryView view) {
		return isSettingsInventory(Language.getLanguage(p), view);
	}
	public static boolean isSettingsInventory(Language lang, InventoryView view) {
		// Trailing space to ensure suffix
		String title = view.getTitle() + " ";
		String realTitle = lang.get(Phrase.TRADE_SETTINGS_INVENTORY_TITLE) + " ";
		if (realTitle.contains("%name%")) {
			String[] invTitle = realTitle.split("%name%");
			return title.startsWith(invTitle[0]) && title.endsWith(invTitle[invTitle.length - 1])
					&& view.getItem(14) != null && Objects.requireNonNull(view.getItem(14)).getType() == Material.ENDER_CHEST;
		}
		return realTitle.equals(title);
	}

	/**
	 * If a player is OP or has the given permission, or any parent permission with .*
	 * @param p The player.
	 * @param permission The permission
	 * @return True if the player is OP or should have that permission
	 */
	public static boolean hasPermission(Player p, String permission) {
		if (p.isOp()) return true;
		if (p.hasPermission("*")) return true;
		if (!permission.contains(".")) return p.hasPermission(permission);
		String[] split = permission.split("\\.");
		StringBuilder perm = new StringBuilder(split[0]);
		for (int i = 1; i < split.length; ++i) {
			if (p.hasPermission(perm + ".*")) return true;
			perm.append(split[i]);
		}
		return p.hasPermission(perm.toString());
	}
	
	public static boolean isXPTradingEnabled() {
		return GlobalConfig.EnableXPTrading;
	}
	
	public static ItemStack getConfirmRedOwn(Player player) { return getConfirmRedOwn(player == null ? null : player.getUniqueId()); }
	public static ItemStack getConfirmRedOwn(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_CONFIRM_TRADE));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getConfirmRedOther(Player player) { return getConfirmRedOther(player == null ? null : player.getUniqueId()); }
	public static ItemStack getConfirmRedOther(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PARTNER_NOT_CONFIRMED));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getConfirmGreenOwn(Player player) { return getConfirmGreenOwn(player == null ? null : player.getUniqueId()); }
	public static ItemStack getConfirmGreenOwn(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_TRADE_CONFIRMED));
		m.setLore(Collections.singletonList(Language.get(uuid, Phrase.ITEM_LORE_CLICK_TO_RESCIND)));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getConfirmGreenOther(Player player) { return getConfirmGreenOther(player == null ? null : player.getUniqueId()); }
	public static ItemStack getConfirmGreenOther(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PARTNER_CONFIRMED));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getConfirmLoadingBar(Player player) { return getConfirmLoadingBar(player == null ? null : player.getUniqueId()); }
	public static ItemStack getConfirmLoadingBar(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_PROCESSING_TRADE));
		it.setItemMeta(m);
		return it;
	}
	
	// XP Trading
	
	// If less than the target value (e.g. 10) is available, the String will change to value/Maxvalue, so if its meant for 10 but we have 5 the method returns "5/10", if we have 10 or more it just returns 10
	public static String stringOf(int actualGivenValue, int actualTargetValue)
	{
		return (actualGivenValue < actualTargetValue ? actualGivenValue + "/" + actualTargetValue : "" + actualTargetValue);
	}
	
	public static ItemStack getXPTradingPlusOne(Player player, int points) { return getXPTradingPlusOne(player == null ? null : player.getUniqueId(), points); }
	public static ItemStack getXPTradingPlusOne(UUID uuid, int points)
	{
		ItemStack it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_NAME));
		m.setLore(Arrays.asList(Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_LORE_SINGLE).replace("%points%", stringOf(points, 1)),
				Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_LORE_SINGLE_SHIFT).replace("%points%", stringOf(points, 10))));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPTradingPlusTen(Player player, int points, int levelDiff) { return getXPTradingPlusTen(player == null ? null : player.getUniqueId(), points, levelDiff); }
	public static ItemStack getXPTradingPlusTen(UUID uuid, int points, int levelDiff)
	{
		ItemStack it = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_NAME));
		m.setLore(Arrays.asList(Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_LORE_LEVEL).replace("%points%", stringOf(points, levelDiff)),
				Language.get(uuid, Phrase.XP_TRADING_GUI_ADD_XP_LORE_LEVEL_SHIFT).replace("%points%", String.valueOf(points))));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPTradingMinusOne(Player player, int points) { return getXPTradingMinusOne(player == null ? null : player.getUniqueId(), points); }
	public static ItemStack getXPTradingMinusOne(UUID uuid, int points)
	{
		ItemStack it = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_NAME));
		m.setLore(Arrays.asList(Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_SINGLE).replace("%points%", stringOf(points, 1)),
				Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_SINGLE_SHIFT).replace("%points%", stringOf(points, 10))));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPTradingMinusTen(Player player, int points, int levelDiff) { return getXPTradingMinusTen(player == null ? null : player.getUniqueId(), points, levelDiff); }
	public static ItemStack getXPTradingMinusTen(UUID uuid, int points, int levelDiff)
	{
		ItemStack it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_NAME));
		m.setLore(Arrays.asList(Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_LEVEL).replace("%points%", stringOf(points, levelDiff)),
				Language.get(uuid, Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_LEVEL_SHIFT).replace("%points%", String.valueOf(points))));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPActivate(Player player) { return getXPActivate(player == null ? null : player.getUniqueId()); }
	public static ItemStack getXPActivate(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_EDIT_TRADED_XP_NAME));
		m.setLore(Collections.singletonList(Language.get(uuid, Phrase.XP_TRADING_GUI_EDIT_TRADED_XP_LORE)));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPDeactivate(Player player) { return getXPDeactivate(player == null ? null : player.getUniqueId()); }
	public static ItemStack getXPDeactivate(UUID uuid)
	{
		ItemStack it = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_CONFIRM_TRADED_XP_NAME));
		m.setLore(Collections.singletonList(Language.get(uuid, Phrase.XP_TRADING_GUI_CONFIRM_TRADED_XP_LORE)));
		it.setItemMeta(m);
		return it;
	}
	
	public static ItemStack getXPOverviewItem(Trade trade, Player player) { return getXPOverviewItem(trade, player == null ? null : player.getUniqueId()); }
	public static ItemStack getXPOverviewItem(Trade trade, UUID uuid) { return getXPOverviewItem(trade, uuid, false); }
	public static ItemStack getXPOverviewItem(Trade trade, UUID uuid, boolean enchant)
	{
		ItemStack it = new ItemStack(enchant ? Material.ENCHANTED_BOOK : Material.BOOK, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		
		Player p1 = trade.getThisTrader(uuid), p2 = trade.getOtherTrader(uuid);
		
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_OVERVIEW_NAME));
		m.setLore(Arrays.asList(
				Language.getWithApostrophe(uuid, Phrase.XP_TRADING_GUI_OVERVIEW_LORE_SELF, p1 == null ? null : p1.getName()).replace("%points%", trade.getTradedXPOfTrader(uuid) + ""),
				Language.getWithApostrophe(uuid, Phrase.XP_TRADING_GUI_OVERVIEW_LORE_OTHER, p2 == null ? null : p2.getName()).replace("%points%", trade.getTradedXPOfOther(uuid) + "")));
		
		it.setItemMeta(m);
		return it;
	}
	
	public void Init()
	{
		ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName("§f ");
		it.setItemMeta(m);
		Trade.EmptyStack = it;
		
		for(int i = 0; i < 54; ++i)
		{
			int col = i % 9, row = i / 9;
			if(row > 0 && row < 4 && col <= 3) continue;
			TradeInventoryEventHandler.IllegalSlots.add(i);
		}
	}
}
