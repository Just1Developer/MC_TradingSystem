package net.justonedeveloper.plugins.trading.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Trade {

	// I think we should not save the inventory and load the items, but maybe the other way around? Nah its alright like this
	// I'll think of a smart way on how to do this
	Inventory TradeInventoryPlayer1, TradeInventoryPlayer2;
	UUID uuidPlayer1, uuidPlayer2;
	
	public static final int[] OwnConfirmSlots = { 36, 37, 38, 39 };
	public static final int[] OtherConfirmSlots = { 41, 42, 43, 44 };
	
	public static final String TitlePrefix = "§8Trade with ";
	public static final String TitlePrefixTradeConclusion = "§8New Items: Trade with ";
	
	public static ItemStack EmptyStack, ConfirmRedOwn, ConfirmGreenOwn, ConfirmRedOther, ConfirmGreenOther, ConfirmLoadingBar;
	
	private int ConfirmScheduler, ConfirmStage = 0;
	private boolean SchedulerRunning = false;
	
	public Trade(Player Player1, Player Player2)
	{
		uuidPlayer1 = Player1.getUniqueId();
		uuidPlayer2 = Player2.getUniqueId();
		TradeCommand.clearPendingTrades(uuidPlayer1);
		TradeCommand.clearPendingTrades(uuidPlayer2);
		TradeInventoryEventHandler.Trades.put(uuidPlayer1, this);
		TradeInventoryEventHandler.Trades.put(uuidPlayer2, this);
		TradeInventoryPlayer1 = GenerateTradeInventory(Player2.getName());
		TradeInventoryPlayer2 = GenerateTradeInventory(Player1.getName());
		Player1.openInventory(TradeInventoryPlayer1);
		Player2.openInventory(TradeInventoryPlayer2);
	}
	
	public static Inventory GenerateTradeInventory(String OtherTraderName)
	{
		Inventory inv = Bukkit.createInventory(null, 54, TitlePrefix + OtherTraderName);
		ItemStack red = ConfirmRedOwn;
		for(int i = 0; i < 9; ++i)
		{
			inv.setItem(i, EmptyStack);
			inv.setItem(i + 36, red);
			if(i == 4) red = ConfirmRedOther;
		}
		for(int i = 0; i < 6; ++i)
		{
			inv.setItem(i * 9 + 4, EmptyStack);	// Middle
		}
		return inv;
	}
	
	public List<ItemStack> GetTradedItems(Player Player) { return GetTradedItems(Player.getUniqueId()); }
	public List<ItemStack> GetTradedItems(UUID PlayerUUID)
	{
		if(PlayerUUID.equals(uuidPlayer1)) return GetTradedItems(1);
		else if(PlayerUUID.equals(uuidPlayer2)) return GetTradedItems(2);
		return new ArrayList<>();
	}
	public List<ItemStack> GetTradedItems(int Player)
	{
		List<ItemStack> Items = new ArrayList<>();
		Inventory inv = getInventoryOf(Player);
		// Fill up
		// From 2nd row to max, and leave the last 2 rows + of the 3rd to last row the 2nd half and middle divider
		for(int i = 9; i < inv.getSize() - 23; ++i)
		{
			if(i % 9 > 3) i += 5;	// Skip other half and the middle divider
			ItemStack item = inv.getItem(i);
			if(item == null) continue;
			if(item.getType() == Material.AIR) continue;
			Items.add(item);
		}
		return Items;
	}
	
	public ItemStack ShiftClickInsert(ItemStack item, UUID Player)
	{
		if(item == null || item.getType() == Material.AIR) return item;
		Inventory[] invs = getInventoriesOf(Player);
		// 2 loops, first one iterates through all types, 2nd one takes empty slots into account
		for(int i = 9; i < invs[0].getSize() - 23; ++i)
		{
			// Obv can't fill up when max stack size is 1
			if(item.getMaxStackSize() == 1) break;
			
			if(i % 9 > 3) i += 5;	// Skip other half and the middle divider
			ItemStack slot = invs[0].getItem(i);
			if(slot == null || slot.getType() == Material.AIR) continue;
			if(!slot.getType().equals(item.getType())) continue;
			if(!Objects.equals(slot.getItemMeta(), item.getItemMeta())) continue;
			
			int add = Math.min(slot.getMaxStackSize() - slot.getAmount(), item.getAmount());
			slot.setAmount(slot.getAmount() + add);
			item.setAmount(item.getAmount() - add);
			invs[1].setItem(i + 5, slot);
			if(item.getAmount() == 0) break;
		}
		// 2nd loop looks for empty slots. No check for fill in because if there was, the first loop would've found it
		for(int i = 9; i < invs[0].getSize() - 23; ++i)
		{
			if(i % 9 > 3) i += 5;	// Skip other half and the middle divider
			ItemStack slot = invs[0].getItem(i);
			if(slot != null && slot.getType() != Material.AIR) continue;
			
			// Fill in
			invs[0].setItem(i, item);
			invs[1].setItem(i + 5, item);
			return new ItemStack(Material.AIR);
		}
		return item;
	}
	
	public ItemStack DoubleClickFillup(ItemStack item, UUID Player)
	{
		if(item == null || item.getType() == Material.AIR) return item;
		Inventory[] invs = getInventoriesOf(Player);
		for(int i = 9; i < invs[0].getSize() - 23; ++i) {
			// Obv can't fill up when max stack size is 1
			if (item.getMaxStackSize() == 1 || item.getAmount() == item.getMaxStackSize()) break;
			
			if(i % 9 > 3) i += 5;	// Skip other half and the middle divider
			ItemStack slot = invs[0].getItem(i);
			if(slot == null || slot.getType() == Material.AIR) continue;
			if(!slot.getType().equals(item.getType())) continue;
			if(!Objects.equals(slot.getItemMeta(), item.getItemMeta())) continue;
			
			// Lets fill this bad boy up
			int subtract = Math.min(item.getMaxStackSize() - item.getAmount(), slot.getAmount());
			slot.setAmount(slot.getAmount() - subtract);
			item.setAmount(item.getAmount() + subtract);
			invs[1].setItem(i + 5, slot);
		}
		return item;
	}
	
	public void SyncSlotFor(UUID PlayerUUID, int Slot)
	{
		Bukkit.getScheduler().runTaskLater(TradingMain.main,
				() -> {
					Inventory[] invs = getInventoriesOf(PlayerUUID);    // Must use as inverted, [1] is the one we need to update
					invs[1].setItem(Slot + 5, invs[0].getItem(Slot));
				}, 2);
	}
	
	public Inventory[] getInventoriesOf(UUID PlayerUUID)
	{
		if(PlayerUUID.equals(uuidPlayer1)) return getInventoriesOf(1);
		else if(PlayerUUID.equals(uuidPlayer2)) return getInventoriesOf(2);
		return new Inventory[2];
	}
	public Inventory[] getInventoriesOf(int Player)
	{
		Inventory[] invs = new Inventory[2];
		switch(Player) {
			case 1:
				invs[0] = TradeInventoryPlayer1;
				invs[1] = TradeInventoryPlayer2;
				break;
			case 2:
				invs[0] = TradeInventoryPlayer2;
				invs[1] = TradeInventoryPlayer1;
				break;
		}
		return invs;
	}
	public Inventory getInventoryOf(int Player)
	{
		switch(Player) {
			case 1:
				return TradeInventoryPlayer1;
			case 2:
				return TradeInventoryPlayer2;
			default:
				return null;
		}
	}
	public Inventory getInventoryOf(Entity Player) { return getInventoryOf(Player.getUniqueId()); }
	public Inventory getInventoryOf(UUID PlayerUUID)
	{
		if(PlayerUUID.equals(uuidPlayer1)) return TradeInventoryPlayer1;
		else if(PlayerUUID.equals(uuidPlayer2)) return TradeInventoryPlayer2;
		return null;
	}
	
	private void Confirmed()
	{
		if(SchedulerRunning || Bukkit.getScheduler().isCurrentlyRunning(ConfirmScheduler)) return;
		SchedulerRunning = true;
		ConfirmStage = 0;
		ConfirmScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TradingMain.main, () -> {
			
			if(ConfirmStage == 9)
			{
				PlaySound(uuidPlayer1, true);
				PlaySound(uuidPlayer2, true);
				PerformTrade();
				return;
			}
			
			TradeInventoryPlayer1.setItem(45 + ConfirmStage, ConfirmLoadingBar);
			TradeInventoryPlayer2.setItem(45 + ConfirmStage, ConfirmLoadingBar);
			PlaySound(uuidPlayer1);
			PlaySound(uuidPlayer2);
			ConfirmStage++;
		
		}, 10, 10);
	}
	
	private void PlaySound(UUID PlayerUUID) { PlaySound(PlayerUUID, false); }
	private void PlaySound(UUID PlayerUUID, boolean Last)
	{
		if(Last) PlaySound(PlayerUUID, Sound.ENTITY_PLAYER_LEVELUP, 1.0f);
		else PlaySound(PlayerUUID, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f + 0.05f * (ConfirmStage));
	}
	private void PlaySound(UUID PlayerUUID, Sound Sound, float pitch)
	{
		Player p = Bukkit.getPlayer(PlayerUUID);
		if(p == null) return;
		p.playSound(p.getLocation(), Sound, 3.0f, pitch);
	}
	
	public void CancelTrade(UUID PlayerWhoClosedTheInventory)
	{
		if(SchedulerRunning)
		{
			// Cancel Timer
			Bukkit.getScheduler().cancelTask(ConfirmScheduler);
			SchedulerRunning = false;
		}
		CancelForPlayer(uuidPlayer1, PlayerWhoClosedTheInventory.equals(uuidPlayer1));
		CancelForPlayer(uuidPlayer2, PlayerWhoClosedTheInventory.equals(uuidPlayer2));
	}
	private void CancelForPlayer(UUID uuid, boolean ClosedTheInventory)
	{
		Player p1 = Bukkit.getPlayer(uuid);
		TradeInventoryEventHandler.Trades.remove(uuid);
		if(p1 != null)
		{
			PlaySound(uuid, Sound.ENTITY_WITHER_HURT, 0.8f);
			if(!ClosedTheInventory /*p1.getOpenInventory().getTitle().startsWith(TitlePrefix)*/) p1.closeInventory();	// Otherwise this is invoked recursively
			List<ItemStack> LeftOvers = new ArrayList<>();
			for(final ItemStack i : GetTradedItems(uuid)) {
				LeftOvers.addAll(p1.getInventory().addItem(i).values());
			}
			for(final ItemStack drops : LeftOvers)
			{
				p1.getWorld().dropItemNaturally(p1.getLocation(), drops);
			}
			p1.sendMessage("§cThe trade has been canceled.");
		}
	}
	
	public void ToggleTradeConfirm(Player Player) { ToggleTradeConfirm(Player.getUniqueId()); }
	public void ToggleTradeConfirm(UUID Player)
	{
		ItemStack own, other;
		
		if(IsConfirmed(Player))
		{
			own = ConfirmRedOwn;
			other = ConfirmRedOther;
		}
		else
		{
			own = ConfirmGreenOwn;
			other = ConfirmGreenOther;
		}
		
		Inventory[] inventories = getInventoriesOf(Player);
		for(int slot : OwnConfirmSlots)
		{
			inventories[0].setItem(slot, own);
		}
		for(int slot : OtherConfirmSlots)
		{
			inventories[1].setItem(slot, other);
		}
		
		if(own.equals(ConfirmRedOwn) && SchedulerRunning)
		{
			// Cancel Timer
			PlaySound(uuidPlayer1, Sound.ENTITY_WITHER_HURT, 0.8f);
			PlaySound(uuidPlayer2, Sound.ENTITY_WITHER_HURT, 0.8f);
			Bukkit.getScheduler().cancelTask(ConfirmScheduler);
			ItemStack air = new ItemStack(Material.AIR);
			for(int i = 0; i < 9; ++i)
			{
				TradeInventoryPlayer1.setItem(45 + i, air);
				TradeInventoryPlayer2.setItem(45 + i, air);
			}
			TradeInventoryPlayer1.setItem(49, EmptyStack);
			TradeInventoryPlayer2.setItem(49, EmptyStack);
			SchedulerRunning = false;
		}
		else if(IsConfirmed(uuidPlayer1) && IsConfirmed(uuidPlayer2))
		{
			Confirmed();
		}
	}
	
	public boolean IsConfirmed(Player Player) { return IsConfirmed(Player.getUniqueId()); }
	public boolean IsConfirmed(UUID Player)
	{
		return Objects.equals(getInventoryOf(Player).getItem(OwnConfirmSlots[0]), ConfirmGreenOwn);
	}
	
	public void PerformTrade()
	{
		SchedulerRunning = false;
		Bukkit.getScheduler().cancelTask(ConfirmScheduler);
		
		Player pl1 = Bukkit.getPlayer(uuidPlayer1), pl2 = Bukkit.getPlayer(uuidPlayer2);
		Inventory TradeResultPlayer1 = Bukkit.createInventory(null, 27, TitlePrefixTradeConclusion + (pl2 != null ? pl2.getName() : "§c[UNKNOWN]")),
				TradeResultPlayer2 = Bukkit.createInventory(null, 27, TitlePrefixTradeConclusion + (pl1 != null ? pl1.getName() : "§c[UNKNOWN]"));
		for(ItemStack item : GetTradedItems(2))
		{
			TradeResultPlayer1.addItem(item);
		}
		for(ItemStack item : GetTradedItems(1))
		{
			TradeResultPlayer2.addItem(item);
		}
		TradeInventoryEventHandler.Trades.remove(uuidPlayer1);
		TradeInventoryEventHandler.Trades.remove(uuidPlayer2);
		if(pl1 != null && !TradeResultPlayer1.isEmpty()) pl1.openInventory(TradeResultPlayer1);
		else if(pl1 != null) pl1.closeInventory();
		if(pl2 != null && !TradeResultPlayer2.isEmpty()) pl2.openInventory(TradeResultPlayer2);
		else if(pl2 != null) pl2.closeInventory();
	}

}
