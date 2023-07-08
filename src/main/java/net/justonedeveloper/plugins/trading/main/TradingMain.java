package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import net.justonedeveloper.plugins.trading.language.LanguageInventory;
import net.justonedeveloper.plugins.trading.language.Phrase;
import net.justonedeveloper.plugins.trading.settings.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class TradingMain extends JavaPlugin {
	
	// Maybe do a basic own money implementation?
	// Money implementation from other plugins would only work if the store and read live from the file every time
	
	public static TradingMain main;
	
	// For future version: Reload Language feature so server restart is not required (done?)
	
	/**
	* Permission Overview:
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
		this.getCommand("trade").setExecutor(new TradeCommand());
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
		TradeInventoryEventHandler.CancelAllTrades();
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
		m.setDisplayName(Language.get(uuid, Phrase.ITEM_NAME_CONFIRM_TRADE));
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
