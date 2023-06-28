package net.justonedeveloper.plugins.trading.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TradeInventoryEventHandler implements Listener {

	
	
	public static void CancelAllTrades()
	{
	
	}
	
	public static HashMap<UUID, Trade> Trades = new HashMap<>();
	
	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e)
	{
		if(e.getClickedInventory() == null) return;
		if(!Trades.containsKey(e.getWhoClicked().getUniqueId())) return;	// Player has trade inventory open
		
		UUID uuid = e.getWhoClicked().getUniqueId();
		Trade trade = Trades.get(uuid);
		int slot = e.getSlot();
		int row = slot / 9;
		int col = slot % 9;
		boolean HasConfirmed = trade.IsConfirmed(uuid);
		
		if(e.getClick() == ClickType.DOUBLE_CLICK)
		{
			// Custom Double Click Implementation:
			// - The slot (e.getCurrentItem()) is irrelevant
			// - The Cursor is what was picked up
			// - The Cursor is never null, just AIR or an Item. getCurrentItem() can be null.
			// - The Cursor Item is then filled by all contents
			
			ItemStack cursor = e.getCursor();
			assert cursor != null;
			if(e.getCursor().getType().isAir()) return;	// :) if event is cancelled or something
			
			if(!HasConfirmed) cursor = trade.DoubleClickFillup(cursor, uuid);
			
			Inventory inv = e.getWhoClicked().getInventory();
			for(int i = 9; i < 36; ++i) {
				// Obv can't fill up when max stack size is 1
				if (cursor.getMaxStackSize() == 1 || cursor.getAmount() == cursor.getMaxStackSize()) break;
				
				if(i % 9 > 3) i += 5;	// Skip other half and the middle divider
				ItemStack slotItem = inv.getItem(i);
				if(slotItem == null || slotItem.getType() == Material.AIR) continue;
				if(!slotItem.getType().equals(cursor.getType())) continue;
				if(!Objects.equals(slotItem.getItemMeta(), cursor.getItemMeta())) continue;
				
				// Lets fill this bad boy up
				int subtract = Math.min(cursor.getMaxStackSize() - cursor.getAmount(), slotItem.getAmount());
				slotItem.setAmount(slotItem.getAmount() - subtract);
				cursor.setAmount(cursor.getAmount() + subtract);
			}
			
			e.setCancelled(true);
			return;
		}
		
		if(e.isShiftClick())
		{
			if(HasConfirmed)
			{
				e.setCancelled(true);
				return;
			}
			
			// if upper inventory was clicked
			if(e.getClickedInventory().equals(e.getInventory()))
			{
				// Sync slot for other player
				// check if valid slot
				if(slot % 9 <= 3 && row > 0 && row < 4)
				{
					trade.SyncSlotFor(uuid, e.getSlot());
				}
				else
				{
					e.setCancelled(true);
				}
				return;
			}
			
			// Clicked from self inv to inv
			e.setCancelled(true);
			ItemStack result = trade.ShiftClickInsert(e.getCurrentItem(), uuid);
			e.setCurrentItem(result);
			return;
		}
		
		// Normal Click
		assert e.getClickedInventory() != null;
		if(!e.getClickedInventory().equals(e.getInventory())) return;
		
		// Clicked on Trade Inventory, now Handle
		if(col > 3 || row == 0 || row >= 4 || HasConfirmed)
		{
			// Clicked on middle or other persons side
			// Perhaps use row 0 for currency or the like
			e.setCancelled(true);
			// Confirm clicked
			if(row == 4 && col <= 3)
			{
				trade.ToggleTradeConfirm(uuid);
			}
			return;
		}
		// Update clicked slot, no cancel
		trade.SyncSlotFor(uuid, slot);
	}
	
	public static Set<Integer> IllegalSlots = new HashSet<>();
	
	@EventHandler
	public void OnInventoryDrag(InventoryDragEvent e)
	{
		if(!Trades.containsKey(e.getWhoClicked().getUniqueId())) return;	// Player has trade inventory open
		e.getRawSlots();
		for(int slot : IllegalSlots)
		{
			if(!e.getRawSlots().contains(slot)) continue;
			e.setCancelled(true);
			//e.setResult(Event.Result.DENY);	// This lets the dragging continue but picks everything back up when the player lets go
			break;
		}
		for(int slot : e.getRawSlots())
		{
			int row = slot / 9;
			int col = slot % 9;
			if(col > 3 || row == 0 || row >= 4) continue;	// Not valid area
			UUID uuid = e.getWhoClicked().getUniqueId();
			Trade trade = Trades.get(uuid);
			if(trade.IsConfirmed(uuid)) e.setCancelled(true);
			else trade.SyncSlotFor(e.getWhoClicked().getUniqueId(), slot);
		}
	}
	
	@EventHandler
	public void OnInventoryClose(InventoryCloseEvent e)
	{
		// Also invoked when server crashed and stuff
		if(Trades.containsKey(e.getPlayer().getUniqueId()))
		{
			Trades.get(e.getPlayer().getUniqueId()).CancelTrade(e.getPlayer().getUniqueId());
			Runtime.getRuntime().gc();
		}
		else if(e.getView().getTitle().startsWith(Trade.TitlePrefixTradeConclusion))
		{
			Player p = (Player) e.getPlayer();
			if(e.getInventory().isEmpty()) return;
			List<ItemStack> LeftOvers = new ArrayList<>();
			for(final ItemStack i : e.getInventory().getContents()) {
				if(i == null) continue;
				LeftOvers.addAll(p.getInventory().addItem(i).values());
			}
			for(final ItemStack drops : LeftOvers)
			{
				p.getWorld().dropItemNaturally(p.getLocation(), drops);
			}
		}
		
		// Dev note: we need to remove both players and then close the inv of the one who has not closed his himself, maybe even personalized messages?
	}
	
	@EventHandler
	public void OnDisconnect(PlayerQuitEvent e)
	{
		UUID uuid = e.getPlayer().getUniqueId();
		// Its not necessary to remove trade offers TO this player since.. they can't accept.
		TradeCommand.TradeRequests.remove(uuid);
	}

}
