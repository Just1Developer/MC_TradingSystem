package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import net.justonedeveloper.plugins.trading.language.Phrase;
import net.justonedeveloper.plugins.trading.settings.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Trade {

	// I think we should not save the inventory and load the items, but maybe the other way around? Nah its alright like this
	// I'll think of a smart way on how to do this
	Inventory TradeInventoryPlayer1, TradeInventoryPlayer2;
	UUID uuidPlayer1, uuidPlayer2;
	int tradedXPPlayer1 = 0, tradedXPPlayer2 = 0;
	int totalXPPlayer1, totalXPPlayer2;
	int deltaLevelXPToNextLevelPlayer1, deltaLevelXPToNextLevelPlayer2;
	int deltaLevelXPToPrevLevelPlayer1, deltaLevelXPToPrevLevelPlayer2;
	
	public static final int[] OwnConfirmSlots = { 36, 37, 38, 39 };
	public static final int[] OtherConfirmSlots = { 41, 42, 43, 44 };
	
	public static ItemStack EmptyStack;//, ConfirmRedOwn, ConfirmGreenOwn, ConfirmRedOther, ConfirmGreenOther, ConfirmLoadingBar;
	
	private int ConfirmScheduler, ConfirmStage = 0;
	private boolean SchedulerRunning = false;
	
	public Trade(Player Player1, Player Player2)
	{
		uuidPlayer1 = Player1.getUniqueId();
		uuidPlayer2 = Player2.getUniqueId();
		TradeCommand.clearPendingTrades(uuidPlayer1);
		TradeCommand.clearPendingTrades(uuidPlayer2);
		setTotalPlayer1Exp(Player1);
		setTotalPlayer2Exp(Player2);
		TradeInventoryEventHandler.Trades.put(uuidPlayer1, this);
		TradeInventoryEventHandler.Trades.put(uuidPlayer2, this);
		TradeInventoryPlayer1 = GenerateTradeInventory(Player1, Player2.getName());
		TradeInventoryPlayer2 = GenerateTradeInventory(Player2, Player1.getName());
		Player1.openInventory(TradeInventoryPlayer1);
		Player2.openInventory(TradeInventoryPlayer2);
	}
	
	public void setPlayerExp(Player Player)
	{
		if(Player.getUniqueId().equals(uuidPlayer1)) setTotalPlayer1Exp(Player);
		else if(Player.getUniqueId().equals(uuidPlayer2)) setTotalPlayer2Exp(Player);
	}
	public void setTotalPlayer1Exp(Player Player1)
	{
		int[] p1_xp = XPCalc.pointsOf(Player1);
		Bukkit.broadcastMessage("§dGot Data for " + Player1.getName() + ": [0]=" + p1_xp[0] + " - [1]=" + p1_xp[1] + " - [2]=" + p1_xp[2]);
		totalXPPlayer1 = p1_xp[0];
		//deltaLevelXPToPrevLevelPlayer1 = p1_xp[1];	// Deltas are updated in setItems, which is the point
		//deltaLevelXPToNextLevelPlayer1 = p1_xp[2];
	}
	public void setTotalPlayer2Exp(Player Player2)
	{
		int[] p2_xp = XPCalc.pointsOf(Player2);
		Bukkit.broadcastMessage("§dGot Data for " + Player2.getName() + ": [0]=" + p2_xp[0] + " - [1]=" + p2_xp[1] + " - [2]=" + p2_xp[2]);
		totalXPPlayer2 = p2_xp[0];
		//deltaLevelXPToPrevLevelPlayer2 = p2_xp[1];
		//deltaLevelXPToNextLevelPlayer2 = p2_xp[2];
	}
	
	public void updatePlayerExp(Player Player)
	{
		if(Player.getUniqueId().equals(uuidPlayer1)) updatePlayer1Exp(Player);
		else if(Player.getUniqueId().equals(uuidPlayer2)) updatePlayer2Exp(Player);
	}
	public void updatePlayer1Exp(Player Player1)
	{
		setTotalPlayer1Exp(Player1);
		// Make sure traded xp is still possible
		if(totalXPPlayer1 < tradedXPPlayer1)
		{
			setTradedXPPlayer2(Player1, totalXPPlayer1);
			// Todo send error message & notify other player that xp has been traded. Also cancel countdown if its running
			Player1.sendMessage("§eYou suck, that's why your exp wasn't enough and was reset to " + totalXPPlayer2 + " (your entire remaining xp lol)");
		}
	}
	public void updatePlayer2Exp(Player Player2)
	{
		setTotalPlayer2Exp(Player2);
		// Make sure traded xp is still possible
		if(totalXPPlayer2 < tradedXPPlayer2)
		{
			setTradedXPPlayer2(Player2, totalXPPlayer2);
			// Todo send error message & notify other player that xp has been traded. Also cancel countdown if its running
			Player2.sendMessage("§cYou suck, that's why your exp wasn't enough and was reset to " + totalXPPlayer2 + " (your entire remaining xp lol)");
		}
	}
	
	public void setTradedXPPlayer1(Player p, int XP)
	{
		tradedXPPlayer1 = XP;
		if(totalXPPlayer1 < tradedXPPlayer1) tradedXPPlayer1 = totalXPPlayer1;
		else if(tradedXPPlayer1 < 0) tradedXPPlayer1 = 0;
		updatePlayer1Exp(p);
	}
	public void setTradedXPPlayer2(Player p, int XP)
	{
		tradedXPPlayer2 = XP;
		if(totalXPPlayer2 < tradedXPPlayer2) tradedXPPlayer2 = totalXPPlayer2;
		else if(tradedXPPlayer2 < 0) tradedXPPlayer2 = 0;
		updatePlayer2Exp(p);
	}
	private void addTradedXPPlayer1(Player p, int XP) { setTradedXPPlayer1(p, tradedXPPlayer1 + XP); }
	private void addTradedXPPlayer2(Player p, int XP) { setTradedXPPlayer2(p, tradedXPPlayer2 + XP); }
	private void removeTradedXPPlayer1(Player p, int XP) { setTradedXPPlayer1(p, tradedXPPlayer1 - XP); }
	private void removeTradedXPPlayer2(Player p, int XP) { setTradedXPPlayer2(p, tradedXPPlayer2 - XP); }
	
	public void addTradedXP(Player p, int XP)
	{
		if(p.getUniqueId().equals(uuidPlayer1))
		{
			setTradedXPPlayer1(p, tradedXPPlayer1 + XP);
		}
		else if(p.getUniqueId().equals(uuidPlayer2))
		{
			setTradedXPPlayer2(p, tradedXPPlayer2 + XP);
		}
	}
	public void removeTradedXP(Player p, int XP)
	{
		if(p.getUniqueId().equals(uuidPlayer1))
		{
			setTradedXPPlayer1(p, tradedXPPlayer1 - XP);
		}
		else if(p.getUniqueId().equals(uuidPlayer2))
		{
			setTradedXPPlayer2(p, tradedXPPlayer2 - XP);
		}
	}
	
	public static Inventory GenerateTradeInventory(Player Trader, String OtherTraderName)
	{
		Inventory inv = Bukkit.createInventory(null, 54, Language.get(Trader, Phrase.TRADE_INVENTORY_TITLE, OtherTraderName));
		ItemStack red = TradingMain.getConfirmRedOwn(Trader);
		for(int i = 0; i < 9; ++i)
		{
			inv.setItem(i, EmptyStack);
			inv.setItem(i + 36, red);
			if(i == 4) red = TradingMain.getConfirmRedOther(Trader);;
		}
		for(int i = 0; i < 6; ++i)
		{
			inv.setItem(i * 9 + 4, EmptyStack);	// Middle
		}
		
		inv.setItem(22, TradingMain.getXPActivate(Trader));	// Add XP
		return inv;
	}
	
	public void setXPTradeItemBar(Player p)
	{
		Inventory inv = getInventoryOf(p);
		int xp = getTradedXPOf(p);
		int pxp = getTotalXPOf(p);
		int resXP = pxp - xp;
		int[] XPcalc = XPCalc.levelOf(resXP);
		if(p.getUniqueId().equals(uuidPlayer1))
		{
			this.deltaLevelXPToPrevLevelPlayer1 = XPcalc[1];
			this.deltaLevelXPToNextLevelPlayer1 = XPcalc[2];
		}
		else if(p.getUniqueId().equals(uuidPlayer2))
		{
			this.deltaLevelXPToPrevLevelPlayer2 = XPcalc[1];
			this.deltaLevelXPToNextLevelPlayer2 = XPcalc[2];
		}
		inv.setItem(0, TradingMain.getXPTradingMinusTen(p, xp, XPcalc[2]));	// To next
		inv.setItem(1, TradingMain.getXPTradingMinusOne(p, xp));
		inv.setItem(2, getXPTradingCurrent(p, XPcalc[0]));
		inv.setItem(3, TradingMain.getXPTradingPlusOne(p, resXP));
		inv.setItem(4, TradingMain.getXPTradingPlusTen(p, resXP, XPcalc[1]));	// To prev
		/*
		p.sendMessage("XP: " + xp);
		p.sendMessage("Your XP: " + p.getTotalExperience());
		p.sendMessage("Your Exp: " + p.getExp());
		p.sendMessage("Your Level: " + p.getLevel());
		p.sendMessage("Your Calculated Level: " + XPCalc.levelOf(p).Key);
		 */
	}
	
	private ItemStack getXPTradingCurrent(Player p, int newLevel)
	{
		int xp = getTradedXPOf(p);
		ItemStack it = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		m.setDisplayName("§e§lCurrent XP");//Language.get(uuid, Phrase.XP_TRADING_));
		m.setLore(Arrays.asList("§7XP Traded: §e" + xp, "§7Resulting Level: §c" + newLevel));	// Old:  XPCalc.levelOf(p.getTotalExperience() - xp).Key));
		it.setItemMeta(m);
		return it;
	}
	
	private int getTradedXPOf(Player p) { return getTradedXPOf(p.getUniqueId()); }
	private int getTradedXPOf(int player) { return getTradedXPOf(player == 2 ? uuidPlayer2 : uuidPlayer1); }
	private int getTradedXPOf(UUID uuid)
	{
		if(uuid.equals(uuidPlayer1)) return tradedXPPlayer1;
		if(uuid.equals(uuidPlayer2)) return tradedXPPlayer2;
		return 0;
	}
	
	public int getTotalXPOf(Player p) { return getTotalXPOf(p.getUniqueId()); }
	public int getTotalXPOf(int player) { return getTotalXPOf(player == 2 ? uuidPlayer2 : uuidPlayer1); }
	public int getTotalXPOf(UUID uuid)
	{
		if(uuid.equals(uuidPlayer1)) return totalXPPlayer1;
		if(uuid.equals(uuidPlayer2)) return totalXPPlayer2;
		return 0;
	}
	
	public int getDeltaLevelXPToNextLevelOf(Player p) { return getDeltaLevelXPToNextLevelOf(p.getUniqueId()); }
	public int getDeltaLevelXPToNextLevelOf(int player) { return getDeltaLevelXPToNextLevelOf(player == 2 ? uuidPlayer2 : uuidPlayer1); }
	public int getDeltaLevelXPToNextLevelOf(UUID uuid)
	{
		if(uuid.equals(uuidPlayer1)) return deltaLevelXPToNextLevelPlayer1;
		if(uuid.equals(uuidPlayer2)) return deltaLevelXPToNextLevelPlayer2;
		return 0;
	}
	
	public int getDeltaLevelXPToPrevLevelOf(Player p) { return getDeltaLevelXPToPrevLevelOf(p.getUniqueId()); }
	public int getDeltaLevelXPToPrevLevelOf(int player) { return getDeltaLevelXPToPrevLevelOf(player == 2 ? uuidPlayer2 : uuidPlayer1); }
	public int getDeltaLevelXPToPrevLevelOf(UUID uuid)
	{
		if(uuid.equals(uuidPlayer1)) return deltaLevelXPToPrevLevelPlayer1;
		if(uuid.equals(uuidPlayer2)) return deltaLevelXPToPrevLevelPlayer2;
		return 0;
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
			
			TradeInventoryPlayer1.setItem(45 + ConfirmStage, TradingMain.getConfirmLoadingBar(Bukkit.getPlayer(uuidPlayer1)));
			TradeInventoryPlayer2.setItem(45 + ConfirmStage, TradingMain.getConfirmLoadingBar(Bukkit.getPlayer(uuidPlayer2)));
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
		CancelForPlayer(uuidPlayer1, PlayerWhoClosedTheInventory != null && PlayerWhoClosedTheInventory.equals(uuidPlayer1));
		CancelForPlayer(uuidPlayer2, PlayerWhoClosedTheInventory != null && PlayerWhoClosedTheInventory.equals(uuidPlayer2));
	}
	private void CancelForPlayer(UUID uuid, boolean ClosedTheInventory)
	{
		Player p1 = Bukkit.getPlayer(uuid);
		TradeInventoryEventHandler.Trades.remove(uuid);
		if(p1 != null)
		{
			PlaySound(uuid, Sound.ENTITY_WITHER_HURT, 0.8f);
			if(!ClosedTheInventory) p1.closeInventory();	// Otherwise this is invoked recursively
			List<ItemStack> LeftOvers = new ArrayList<>();
			for(final ItemStack i : GetTradedItems(uuid)) {
				LeftOvers.addAll(p1.getInventory().addItem(i).values());
			}
			for(final ItemStack drops : LeftOvers)
			{
				p1.getWorld().dropItemNaturally(p1.getLocation(), drops);
			}
			p1.sendMessage(Language.get(p1, Phrase.TRADE_INVENTORY_CLOSE_CANCELLED_MESSAGE));
		}
	}
	
	public void ToggleTradeConfirm(Player Player) { ToggleTradeConfirm(Player.getUniqueId()); }
	public void ToggleTradeConfirm(UUID Player)
	{
		ItemStack own, other;
		UUID Other = Player.equals(uuidPlayer1) ? uuidPlayer2 : uuidPlayer1;
		
		if(IsConfirmed(Player))
		{
			own = TradingMain.getConfirmRedOwn(Player);
			other = TradingMain.getConfirmRedOther(Other);
		}
		else
		{
			own = TradingMain.getConfirmGreenOwn(Player);
			other = TradingMain.getConfirmGreenOther(Other);
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
		
		if(own.getType().equals(Material.RED_STAINED_GLASS_PANE) && SchedulerRunning)
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
		Inventory inv = getInventoryOf(Player);
		if(inv == null) return SchedulerRunning;
		ItemStack it = inv.getItem(OwnConfirmSlots[0]);
		if(it == null) return SchedulerRunning;
		return SchedulerRunning || it.getType() == Material.LIME_STAINED_GLASS_PANE;
	}
	
	public void PerformTrade()
	{
		SchedulerRunning = false;
		Bukkit.getScheduler().cancelTask(ConfirmScheduler);
		
		Player pl1 = Bukkit.getPlayer(uuidPlayer1), pl2 = Bukkit.getPlayer(uuidPlayer2);
		String TitlePlayer1 = Language.get(pl1, Phrase.TRADE_INVENTORY_CONCLUSION_TITLE, (pl2 != null ? pl2.getName() : Language.get(pl1, Phrase.TRADE_INVENTORY_UNKNOWN_PLAYER_NAME))),
				TitlePlayer2 = Language.get(pl2, Phrase.TRADE_INVENTORY_CONCLUSION_TITLE, (pl1 != null ? pl1.getName() : Language.get(pl2, Phrase.TRADE_INVENTORY_UNKNOWN_PLAYER_NAME)));
		Inventory TradeResultPlayer1 = Bukkit.createInventory(null, 27, TitlePlayer1),
				TradeResultPlayer2 = Bukkit.createInventory(null, 27, TitlePlayer2);
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
		if(pl1 != null && !TradeResultPlayer1.isEmpty())
		{
			if(TradeSettings.getAutoCollectSettingValue(pl1.getUniqueId()))
			{
				Inventory i2 = Bukkit.createInventory(null, 27, TitlePlayer1);
				List<ItemStack> LeftOvers = new ArrayList<>();
				for(ItemStack i : TradeResultPlayer1.getContents()) {
					if(i == null) continue;
					LeftOvers.addAll(pl1.getInventory().addItem(i).values());
				}
				for(ItemStack drops : LeftOvers)
				{
					i2.addItem(drops);
				}
				if(!i2.isEmpty()) pl1.openInventory(i2);
				else pl1.closeInventory();
			}
			else
			{
				pl1.openInventory(TradeResultPlayer1);
			}
		}
		else if(pl1 != null) pl1.closeInventory();
		if(pl2 != null && !TradeResultPlayer2.isEmpty())
		{
			if(TradeSettings.getAutoCollectSettingValue(pl2.getUniqueId()))
			{
				Inventory i2 = Bukkit.createInventory(null, 27, TitlePlayer2);
				List<ItemStack> LeftOvers = new ArrayList<>();
				for(ItemStack i : TradeResultPlayer2.getContents()) {
					if(i == null) continue;
					LeftOvers.addAll(pl2.getInventory().addItem(i).values());
				}
				for(ItemStack drops : LeftOvers)
				{
					i2.addItem(drops);
				}
				if(!i2.isEmpty()) pl2.openInventory(i2);
				else pl2.closeInventory();
			}
			else
			{
				pl2.openInventory(TradeResultPlayer2);
			}
		}
		else if(pl2 != null) pl2.closeInventory();
	}

}
