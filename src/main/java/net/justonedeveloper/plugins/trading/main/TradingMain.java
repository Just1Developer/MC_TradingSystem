package net.justonedeveloper.plugins.trading.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TradingMain extends JavaPlugin {
	
	// Maybe do a basic own money implementation?
	// Money implementation from other plugins would only work if the store and read live from the file every time
	
	// Also planned: Custom Language System with LangConfig and in the file there is language name and phrases, and you can generate a new language file via a command
	
	public static TradingMain main;
	
	@Override
	public void onEnable() {
		// Plugin startup logic
		main = this;
		Init();
		// trade command, setting page with privacy and 2fa/double check like in Rocket League
		Bukkit.getPluginManager().registerEvents(new TradeInventoryEventHandler(), this);
		Bukkit.getPluginManager().registerEvents(new TradeSettingsInventory(), this);
		this.getCommand("trade").setExecutor(new TradeCommand());
		ItemStack i = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta = i.getItemMeta();
		meta.setLore(Collections.singletonList("§cClick to rescind offer"));
		i.setItemMeta(meta);
	}
	
	@Override
	public void onDisable() {
		// Plugin shutdown logic
		TradeInventoryEventHandler.CancelAllTrades();
	}
	
	public void Init()
	{
		ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName("§f ");
		it.setItemMeta(m);
		Trade.EmptyStack = it;
		
		it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		m.setDisplayName("§c§lConfirm Trade");
		it.setItemMeta(m);
		Trade.ConfirmRedOwn = it;
		
		it = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
		m.setDisplayName("§c§lPartner has not confirmed Trade");
		it.setItemMeta(m);
		Trade.ConfirmRedOther = it;
		
		it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
		m.setDisplayName("§a§lTrade confirmed");
		m.setLore(Collections.singletonList("§cClick to rescind offer"));
		it.setItemMeta(m);
		Trade.ConfirmGreenOwn = it;
		
		it = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
		m = it.getItemMeta();
		assert m != null;
		m.setDisplayName("§a§lPartner has confirmed Trade");
		it.setItemMeta(m);
		Trade.ConfirmGreenOther = it;
		
		it = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
		m.setDisplayName("§b§lTrading in progress...");
		it.setItemMeta(m);
		Trade.ConfirmLoadingBar = it;
		
		for(int i = 0; i < 54; ++i)
		{
			int col = i % 9, row = i / 9;
			if(row > 0 && row < 4 && col <= 3) continue;
			TradeInventoryEventHandler.IllegalSlots.add(i);
		}
	}
}
