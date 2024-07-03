package net.justonedeveloper.plugins.trading.settings;

import net.justonedeveloper.plugins.trading.language.Language;
import net.justonedeveloper.plugins.trading.language.LanguageInventory;
import net.justonedeveloper.plugins.trading.language.Phrase;
import net.justonedeveloper.plugins.trading.main.GlobalConfig;
import net.justonedeveloper.plugins.trading.main.TradingMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TradeSettings implements Listener {
	
	// Settings: Anyone can send trades, auto-accept, auto-decline (funny, ik), auto-collect items (maybe overflow stays in inventory), also dont-notify-players-on-cancel

	private static final File folder = new File(TradingMain.main.getDataFolder(), "UserData");
	
	private static final PrivacySettingValue DefaultPrivacySettingValue = PrivacySettingValue.REQUEST;
	private static final boolean DefaultAutoCollectItemsValue = false;
	
	private static YamlConfiguration getConfigurationOf(Player p) { return getConfigurationOf(p.getUniqueId(), p.getName()); }
	private static YamlConfiguration getConfigurationOf(UUID uuid) { return getConfigurationOf(uuid, "unknown"); }
	private static YamlConfiguration getConfigurationOf(UUID uuid, String playername)
	{
		File f = new File(folder, uuid + ".yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		if(!f.exists())
		{
			cfg.options().copyDefaults(true);
			cfg.addDefault("Last known name", playername);
			cfg.addDefault("Trade Privacy Setting", DefaultPrivacySettingValue.toString());
			cfg.addDefault("Auto Collect Items", DefaultAutoCollectItemsValue);
			cfg.addDefault("Language", Language.DefaultLanguage);
			saveFile(f, cfg, "Failed to save user data file for player " + playername + " (UUID: " + playername + ")");
		}
		return cfg;
	}

	@EventHandler
	public void debug(PlayerMoveEvent e) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			e.getPlayer().sendMessage(p.getName() + " : " + String.valueOf(p.getOpenInventory().getTitle()) + " + " + p.getOpenInventory().getBottomInventory() + " + " + p.getOpenInventory().getTopInventory());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		YamlConfiguration cfg = getConfigurationOf(p);
		tryLoadLanguageOf(p, false);
		cfg.set("Last known name", p.getName());
		saveFile(new File(folder, p.getUniqueId() + ".yml"), cfg);
	}

	public static void tryLoadLanguageOf(Player p) {
		tryLoadLanguageOf(p, true);
	}
	private static void tryLoadLanguageOf(Player p, boolean saveAfter) {
		YamlConfiguration cfg = getConfigurationOf(p);
		String lang = cfg.getString("Language");
		if(!Language.exists(lang))
		{
			if(lang == null) lang = "null";
			p.sendMessage(Language.getPhrase(Phrase.ERROR_LANGUAGE_WAS_DELETED).replace("%lang%", lang).replace("%defaultLang%", Language.getDefaultLanguage().LanguageName));
			cfg.set("Language", Language.DefaultLanguage);
			if (saveAfter) saveFile(new File(folder, p.getUniqueId() + ".yml"), cfg);
		}
	}
	
	private static void saveFile(File f, YamlConfiguration cfg) { saveFile(f, cfg, "Failed to save file " + f.getAbsolutePath()); }
	private static void saveFile(File f, YamlConfiguration cfg, String customErrorMessage)
	{
		try {
			cfg.save(f);
		} catch (IOException e) {
			System.out.println(customErrorMessage);
		}
	}
	
	public static PrivacySettingValue getPrivacySettingValue(UUID uuid)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		return PrivacySettingValue.valueOf(cfg.getString("Trade Privacy Setting"));
	}
	
	public static String getLanguage(UUID uuid)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		return cfg.getString("Language");
	}
	
	public static boolean getAutoCollectSettingValue(UUID uuid)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		return cfg.getBoolean("Auto Collect Items");
	}
	
	public static void setPrivacySettingValue(UUID uuid, PrivacySettingValue value)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		cfg.set("Trade Privacy Setting", value.toString());
		saveFile(new File(folder, uuid + ".yml"), cfg);
	}
	
	public static void setAutoCollectSettingValue(UUID uuid, boolean value)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		cfg.set("Auto Collect Items", value);
		saveFile(new File(folder, uuid + ".yml"), cfg);
		/*
		if(UserSettings.containsKey(uuid)) UserSettings.get(uuid).Value = value;
		else UserSettings.put(uuid, new KeyValuePair<>(DefaultPrivacySettingValue, value));
		 */
	}
	
	public static void setLanguage(UUID uuid, String languageCode)
	{
		YamlConfiguration cfg = getConfigurationOf(uuid);
		cfg.set("Language", languageCode);
		saveFile(new File(folder, uuid + ".yml"), cfg);
	}
	
	public static void openInventory(Player p)
	{
		Inventory inv = Bukkit.createInventory(null, 54, Language.get(p, Phrase.TRADE_SETTINGS_INVENTORY_TITLE, p.getName()));
		UUID uuid = p.getUniqueId();
		Language lang = Language.getLanguage(uuid);
		boolean autoFill = getAutoCollectSettingValue(uuid);
		
		ItemStack autoAccept = new ItemStack(Material.CAKE);
		ItemMeta meta = autoAccept.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_ACCEPT_NAME));
		autoAccept.setItemMeta(meta);
		
		ItemStack request = new ItemStack(Material.LEAD);
		meta = request.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_TRADE_ON_REQUEST_NAME));
		request.setItemMeta(meta);
		
		ItemStack autoDecline = new ItemStack(Material.BARRIER);
		meta = autoDecline.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_DECLINE_NAME));
		autoDecline.setItemMeta(meta);
		
		ItemStack autoCollect = new ItemStack(Material.ENDER_CHEST);
		meta = autoCollect.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_NAME));
		List<String> l = new ArrayList<>();
		for(String s : lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE).split("\n"))
		{
			l.add("§7" + s);
		}
		if (autoFill) l.add(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME));
		else l.add(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME));
		meta.setLore(l);
		autoCollect.setItemMeta(meta);
		
		ItemStack language = new ItemStack(Material.DARK_OAK_SIGN);
		meta = language.getItemMeta();
		assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_SET_LANGUAGE_NAME));
		language.setItemMeta(meta);
		
		ItemStack activated = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		meta = activated.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME));
		meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_LORE)));
		activated.setItemMeta(meta);
		
		ItemStack deactivated = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		meta = deactivated.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME));
		meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_LORE)));
		deactivated.setItemMeta(meta);
		
		ItemStack deactivated_gray = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		meta = deactivated_gray.getItemMeta(); assert meta != null;
		meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_NAME));
		meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_LORE)));
		deactivated_gray.setItemMeta(meta);
		
		inv.setItem(11, autoAccept);
		inv.setItem(20, request);
		inv.setItem(29, autoDecline);
		inv.setItem(14, autoCollect);
		inv.setItem(32, language);
		
		switch(getPrivacySettingValue(uuid))
		{
			case REQUEST:
				inv.setItem(10, deactivated_gray);
				inv.setItem(19, activated);
				inv.setItem(28, deactivated_gray);
				break;
			case AUTO_ACCEPT:
				inv.setItem(10, activated);
				inv.setItem(19, deactivated_gray);
				inv.setItem(28, deactivated_gray);
				break;
			default:
				inv.setItem(10, deactivated_gray);
				inv.setItem(19, deactivated_gray);
				inv.setItem(28, activated);
				break;
		}
		
		if(autoFill) inv.setItem(15, activated);
		else inv.setItem(15, deactivated);

		if (TradingMain.hasPermission(p, "trading.admin.reloadsettings")) {
			ItemStack reloadAll = new ItemStack(Material.SLIME_BLOCK);
			meta = reloadAll.getItemMeta();
			if (meta != null) {
				meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_REFRESH_ALL_NAME));
				meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_REFRESH_ALL_LORE)));
				reloadAll.setItemMeta(meta);
			}
			inv.setItem(53, reloadAll);
		}
		
		p.openInventory(inv);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e)
	{
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(e.getClickedInventory() == null || !e.getView().getTitle().equals(Language.get(p, Phrase.TRADE_SETTINGS_INVENTORY_TITLE, p.getName()))) return;
		
		if(e.getClick().equals(ClickType.DOUBLE_CLICK)) { e.setCancelled(true); return; }
		
		if(!e.getInventory().equals(e.getClickedInventory()))
		{
			if(e.isShiftClick()) e.setCancelled(true);
			return;
		}
		e.setCancelled(true);
		
		// Clicked on Settings inv or shift clicked, both should get cancelled
		InvokeClickedSlot(p, e.getInventory(), e.getCurrentItem(), e.getRawSlot());
	}
	
	private static void InvokeClickedSlot(Player p, Inventory inv, ItemStack currentItem, int rawSlot)
	{
		if(currentItem == null) return;
		if(currentItem.getType() == Material.AIR) return;
		
		UUID uuid = p.getUniqueId();
		PrivacySettingValue p1 = getPrivacySettingValue(uuid);
		int prevSlot;
		ItemStack enabled;
		Language lang = Language.getLanguage(uuid);
		
		switch(rawSlot)
		{
			case 10:
			case 11:
				// Clicked Auto-Accept
				if(p1 == PrivacySettingValue.AUTO_ACCEPT) break;
				
				prevSlot = p1 == PrivacySettingValue.AUTO_DECLINE ? 28 : 19;
				enabled = inv.getItem(prevSlot);
				setPrivacySettingValue(uuid, PrivacySettingValue.AUTO_ACCEPT);
				inv.setItem(prevSlot, inv.getItem(10));
				inv.setItem(10, enabled);
				return;
			case 19:
			case 20:
				// Clicked On Request
				if(p1 == PrivacySettingValue.REQUEST) break;
				
				prevSlot = p1 == PrivacySettingValue.AUTO_DECLINE ? 28 : 10;
				enabled = inv.getItem(prevSlot);
				setPrivacySettingValue(uuid, PrivacySettingValue.REQUEST);
				inv.setItem(prevSlot, inv.getItem(19));
				inv.setItem(19, enabled);
				return;
			case 28:
			case 29:
				// Clicked On Request
				if(p1 == PrivacySettingValue.AUTO_DECLINE) break;
				
				prevSlot = p1 == PrivacySettingValue.REQUEST ? 19 : 10;
				enabled = inv.getItem(prevSlot);
				setPrivacySettingValue(uuid, PrivacySettingValue.AUTO_DECLINE);
				inv.setItem(prevSlot, inv.getItem(28));
				inv.setItem(28, enabled);
				return;
			case 14:
			case 15:
				boolean hasAutoEnabled = getAutoCollectSettingValue(uuid);
				if(hasAutoEnabled)
				{
					ItemStack deactivated = new ItemStack(Material.RED_STAINED_GLASS_PANE);
					ItemMeta meta = deactivated.getItemMeta();
					assert meta != null;
					meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME));
					meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_LORE)));
					deactivated.setItemMeta(meta);
					
					ItemStack autoCollect = new ItemStack(Material.ENDER_CHEST);
					meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_NAME));
					List<String> l = new ArrayList<>();
					for(String s : lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE).split("\n"))
					{
						l.add("§7" + s);
					}
					l.add(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME));
					meta.setLore(l);
					autoCollect.setItemMeta(meta);
					
					inv.setItem(14, autoCollect);
					inv.setItem(15, deactivated);
				}
				else
				{
					ItemStack activated = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
					ItemMeta meta = activated.getItemMeta();
					assert meta != null;
					meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME));
					meta.setLore(Collections.singletonList(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_LORE)));
					activated.setItemMeta(meta);
					
					ItemStack autoCollect = new ItemStack(Material.ENDER_CHEST);
					meta.setDisplayName(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_NAME));
					List<String> l = new ArrayList<>();
					for(String s : lang.get(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE).split("\n"))
					{
						l.add("§7" + s);
					}
					l.add(lang.get(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME));
					meta.setLore(l);
					autoCollect.setItemMeta(meta);
					
					inv.setItem(14, autoCollect);
					inv.setItem(15, activated);
				}
				setAutoCollectSettingValue(uuid, !hasAutoEnabled);
				return;
			case 32:
				LanguageInventory.openInventory(p);
				return;
			case 53:
				boolean hasPerms = TradingMain.hasPermission(p, "trading.admin.reloadsettings");
				if (!hasPerms) {
					// Remove the item if the player lost permission between opening and clicking
					inv.setItem(53, new ItemStack(Material.AIR));
					return;
				}

				GlobalConfig.LoadConfig();
				Language.InitDefaultOnly();
				p.sendMessage(lang.get(Phrase.TRADE_SETTINGS_REFRESH_MSG_REFRESHED_SETTINGS));
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e)
	{
		if(!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if(!e.getView().getTitle().equals(Language.get(p, Phrase.TRADE_SETTINGS_INVENTORY_TITLE, p.getName()))) return;
		
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
