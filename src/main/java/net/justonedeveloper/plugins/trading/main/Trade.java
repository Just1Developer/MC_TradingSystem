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
	int tradedXPPlayer1, tradedXPPlayer2;
	int confirmedXPPlayer1, confirmedXPPlayer2;
	int totalXPPlayer1, totalXPPlayer2;
	int deltaLevelXPToNextLevelPlayer1, deltaLevelXPToNextLevelPlayer2;
	int deltaLevelXPToPrevLevelPlayer1, deltaLevelXPToPrevLevelPlayer2;

	private boolean isPlayer1EditingXP = false;
	private boolean isPlayer2EditingXP = false;
	
	public static final int[] OwnConfirmSlots = { 36, 37, 38, 39 };
	public static final int[] OtherConfirmSlots = { 41, 42, 43, 44 };
	public static final int XP_OVERVIEW_SLOT = 40;
	
	public static ItemStack EmptyStack;//, ConfirmRedOwn, ConfirmGreenOwn, ConfirmRedOther, ConfirmGreenOther, ConfirmLoadingBar;
	
	private int ConfirmScheduler, ConfirmStage = 0;
	private boolean SchedulerRunning = false;
	
	public Trade(Player Player1, Player Player2)
	{
		tradedXPPlayer1 = 0;
		tradedXPPlayer2 = 0;
		confirmedXPPlayer1 = 0;
		confirmedXPPlayer2 = 0;
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
	
	public Player getThisTrader(UUID uuid) {
		if (Objects.equals(uuidPlayer1, uuid) || Objects.equals(uuidPlayer2, uuid)) return Bukkit.getPlayer(uuid);
		return null;
	}
	
	public Player getOtherTrader(UUID uuid) {
		if (Objects.equals(uuidPlayer1, uuid)) return Bukkit.getPlayer(uuidPlayer2);
		if (Objects.equals(uuidPlayer2, uuid)) return Bukkit.getPlayer(uuidPlayer1);
		return null;
	}
	
	public int getTradedXPOfTrader(UUID uuid) {
		if (Objects.equals(uuidPlayer1, uuid)) return confirmedXPPlayer1;
		if (Objects.equals(uuidPlayer2, uuid)) return confirmedXPPlayer2;
		return 0;
	}
	
	public int getTradedXPOfOther(UUID uuid) {
		if (Objects.equals(uuidPlayer1, uuid)) return confirmedXPPlayer2;
		if (Objects.equals(uuidPlayer2, uuid)) return confirmedXPPlayer1;
		return 0;
	}
	
	public Player getPlayer1() {
		return Bukkit.getPlayer(uuidPlayer1);
	}
	
	public Player getPlayer2() {
		return Bukkit.getPlayer(uuidPlayer2);
	}
	
	public void setPlayerExp(Player Player)
	{
		if(Player.getUniqueId().equals(uuidPlayer1)) setTotalPlayer1Exp(Player);
		else if(Player.getUniqueId().equals(uuidPlayer2)) setTotalPlayer2Exp(Player);
	}
	public void setTotalPlayer1Exp(Player Player1)
	{
		int[] p1_xp = XPCalc.pointsOf(Player1);
		totalXPPlayer1 = p1_xp[0];
		//deltaLevelXPToPrevLevelPlayer1 = p1_xp[1];	// Deltas are updated in setItems, which is the point
		//deltaLevelXPToNextLevelPlayer1 = p1_xp[2];
	}
	public void setTotalPlayer2Exp(Player Player2)
	{
		int[] p2_xp = XPCalc.pointsOf(Player2);
		totalXPPlayer2 = p2_xp[0];
		//deltaLevelXPToPrevLevelPlayer2 = p2_xp[1];
		//deltaLevelXPToNextLevelPlayer2 = p2_xp[2];
	}
	
	public void updatePlayerExp(Player Player)
	{
		if(Player.getUniqueId().equals(uuidPlayer1)) updatePlayer1Exp(Player);
		else if(Player.getUniqueId().equals(uuidPlayer2)) updatePlayer2Exp(Player);
	}
	private void updatePlayer1Exp(Player Player1)
	{
		setTotalPlayer1Exp(Player1);
		// Make sure traded xp is still possible
		if(totalXPPlayer1 < tradedXPPlayer1)
		{
			setTradedXPPlayer1(Player1, totalXPPlayer1);
			if (IsConfirmed(Player1)) {
				ToggleTradeConfirm(uuidPlayer1);
			}
			Player1.sendMessage(Language.get(Player1, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_SELF));
			Player p2 = Bukkit.getPlayer(this.uuidPlayer2);
			if (p2 == null) return;
			if (IsConfirmed(p2)) {
				ToggleTradeConfirm(uuidPlayer2);
			}
			p2.sendMessage(Language.get(p2, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_OTHER));
			concludeXPSettings(Player1);
		} else if (isPlayer1EditingXP) {
			setXPTradeItemBar(Player1);
		}
	}
	private void updatePlayer2Exp(Player Player2)
	{
		setTotalPlayer2Exp(Player2);
		// Make sure traded xp is still possible
		if(totalXPPlayer2 < tradedXPPlayer2)
		{
			setTradedXPPlayer2(Player2, totalXPPlayer2);
			if (IsConfirmed(Player2)) {
				ToggleTradeConfirm(uuidPlayer2);
			}
			Player2.sendMessage(Language.get(Player2, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_SELF));
			Player p1 = Bukkit.getPlayer(this.uuidPlayer1);
			if (p1 == null) return;
			if (IsConfirmed(p1)) {
				ToggleTradeConfirm(uuidPlayer1);
			}
			p1.sendMessage(Language.get(p1, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_OTHER));
			concludeXPSettings(Player2);
		} else if (isPlayer2EditingXP) {
			setXPTradeItemBar(Player2);
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
	
	public Inventory GenerateTradeInventory(Player Trader, String OtherTraderName)
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
		
		if (TradingMain.isXPTradingEnabled()) {
			inv.setItem(22, TradingMain.getXPActivate(Trader));    // Add XP
			inv.setItem(XP_OVERVIEW_SLOT, TradingMain.getXPOverviewItem(this, Trader));
		}
		return inv;
	}
	
	public void setXPTradeItemBar(Player p)
	{
		Inventory inv = getInventoryOf(p);
		int xp = getTradedXPOf(p);
		setPlayerExp(p);
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
		inv.setItem(22, TradingMain.getXPDeactivate(p));
		setIsPlayerEditingXP(p, true);
	}
	
	public void setConfirmItemLore(Player p, Phrase phrase) { setConfirmItemLore(p.getUniqueId(), phrase); }
	public void setConfirmItemLore(UUID uuid, Phrase phrase) {
		Inventory inv = getInventoryOf(uuid);
		for (int ownConfirmSlot : OwnConfirmSlots) {
			ItemStack item = inv.getItem(ownConfirmSlot);
			if (item == null || !item.hasItemMeta()) continue;
			ItemMeta meta = item.getItemMeta();
			assert meta != null;
			if (phrase == null) meta.setLore(new ArrayList<>());
			else meta.setLore(Collections.singletonList("§7" + Language.get(uuid, phrase)));
			item.setItemMeta(meta);
			inv.setItem(ownConfirmSlot, item);
		}
	}
	
	/**
	 * Gets if a player currently has the XP editor open. Checks the first itemstack in the Inventory against
	 * the regular empty slot item. If it's not that, it has to be the XP trading.
	 * @param p The player.
	 * @return If the player currently has the XP trading bar available.
	 */
	public boolean isEditingXP(Player p) { return isEditingXP(p.getUniqueId()); }
	
	/**
	 * Gets if a player currently has the XP editor open. Checks the first itemstack in the Inventory against
	 * the regular empty slot item. If it's not that, it has to be the XP trading.
	 * @param uuid The player's uuid.
	 * @return If the player currently has the XP trading bar available.
	 */
	public boolean isEditingXP(UUID uuid) {
		if (Objects.equals(uuid, uuidPlayer1)) return isPlayer1EditingXP;
		if (Objects.equals(uuid, uuidPlayer2)) return isPlayer2EditingXP;
		return false;
	}
	
	private void setIsPlayerEditingXP(Player p, boolean isEditingXP) {
		if (Objects.equals(p.getUniqueId(), uuidPlayer1)) isPlayer1EditingXP = isEditingXP;
		else if (Objects.equals(p.getUniqueId(), uuidPlayer2)) isPlayer2EditingXP = isEditingXP;
	}
	
	/**
	 * Concludes XP editing and updates the leveraged XP in both Player's UIs.
	 * @param p The player for which to update the XP.
	 */
	public void concludeXPSettings(Player p) {
		updateConfirmedXP(p);
		removeXPTradeItemBar(p);
		setConfirmItemLore(p, null);
		setIsPlayerEditingXP(p, false);
	}
	
	private void updateConfirmedXP(Player p) { updateConfirmedXP(p.getUniqueId());	}
	private void updateConfirmedXP(UUID uuid) {
		boolean changed = false;
		if (Objects.equals(uuid, uuidPlayer1)) {
			changed = confirmedXPPlayer1 != tradedXPPlayer1;
			confirmedXPPlayer1 = tradedXPPlayer1;
		} else if (Objects.equals(uuid, uuidPlayer2)) {
			changed = confirmedXPPlayer2 != tradedXPPlayer2;
			confirmedXPPlayer2 = tradedXPPlayer2;
		}
		if (changed) updateXPOverviewItem(uuid);
	}
	
	private void updateXPOverviewItem() {
		updateXPOverviewItem(null);
	}
	private void updateXPOverviewItem(UUID changed) {
		ItemStack i1 = TradingMain.getXPOverviewItem(this, uuidPlayer1, (TradeInventoryPlayer1.getItem(XP_OVERVIEW_SLOT) != null && TradeInventoryPlayer1.getItem(XP_OVERVIEW_SLOT).getType() == Material.ENCHANTED_BOOK) || Objects.equals(changed, uuidPlayer2));
		ItemStack i2 = TradingMain.getXPOverviewItem(this, uuidPlayer2, (TradeInventoryPlayer2.getItem(XP_OVERVIEW_SLOT) != null && TradeInventoryPlayer2.getItem(XP_OVERVIEW_SLOT).getType() == Material.ENCHANTED_BOOK) || Objects.equals(changed, uuidPlayer1));
		TradeInventoryPlayer1.setItem(XP_OVERVIEW_SLOT, i1);
		TradeInventoryPlayer2.setItem(XP_OVERVIEW_SLOT, i2);
	}
	public void unenchantXPOverviewItem(UUID changed) {
		if (uuidPlayer1.equals(changed)) {
			ItemStack i = TradingMain.getXPOverviewItem(this, uuidPlayer1, false);
			TradeInventoryPlayer1.setItem(XP_OVERVIEW_SLOT, i);
		} else if (uuidPlayer2.equals(changed)) {
			ItemStack i = TradingMain.getXPOverviewItem(this, uuidPlayer2, false);
			TradeInventoryPlayer2.setItem(XP_OVERVIEW_SLOT, i);
		}
	}
	
	private void removeXPTradeItemBar(Player p)
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
		
		for (int i = 0; i < 5; ++i) {
			inv.setItem(i, Trade.EmptyStack);
		}
		inv.setItem(22, TradingMain.getXPActivate(p));	// Add XP
	}
	
	private ItemStack getXPTradingCurrent(Player p, int newLevel)
	{
		int xp = getTradedXPOf(p);
		ItemStack it = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta m = it.getItemMeta();
		assert m != null;
		UUID uuid = p.getUniqueId();
		m.setDisplayName(Language.get(uuid, Phrase.XP_TRADING_GUI_CURRENT_XP_NAME));
		m.setLore(Arrays.asList(
				Language.get(uuid, Phrase.XP_TRADING_GUI_CURRENT_XP_LORE_1).replace("%points%", "" + xp),
				Language.get(uuid, Phrase.XP_TRADING_GUI_CURRENT_XP_LORE_2).replace("%level%", "" + newLevel)
				));
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
			
			Player p1 = Bukkit.getPlayer(uuidPlayer1);
			Player p2 = Bukkit.getPlayer(uuidPlayer2);
			if (p1 == null || p2 == null) {
				ToggleTradeConfirm(uuidPlayer1);
				ToggleTradeConfirm(uuidPlayer2);
				return;
			}
			
			if (confirmedXPPlayer1 > 0 && confirmedXPPlayer1 > XPCalc.pointsOf(p1)[0]) {
				ToggleTradeConfirm(uuidPlayer1);
				p1.sendMessage(Language.get(p1, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_SELF));
				p2.sendMessage(Language.get(p2, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_OTHER));
				return;
			}
			
			if (confirmedXPPlayer2 > 0 && confirmedXPPlayer2 > XPCalc.pointsOf(p2)[0]) {
				ToggleTradeConfirm(uuidPlayer2);
				p2.sendMessage(Language.get(p2, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_SELF));
				p1.sendMessage(Language.get(p1, Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_OTHER));
				return;
			}
			
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
		p.playSound(p.getLocation(), Sound, 1.0f, pitch);
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
		if(pl1 != null) {
			if (!TradeResultPlayer1.isEmpty()) {
				if(TradeSettings.getAutoCollectSettingValue(pl1.getUniqueId())) {
					Inventory i2 = Bukkit.createInventory(null, 27, TitlePlayer1);
					List<ItemStack> LeftOvers = new ArrayList<>();
					for(ItemStack i : TradeResultPlayer1.getContents()) {
						if(i == null) continue;
						LeftOvers.addAll(pl1.getInventory().addItem(i).values());
					}
					for(ItemStack drops : LeftOvers) {
						i2.addItem(drops);
					}
					if(!i2.isEmpty()) pl1.openInventory(i2);
					else pl1.closeInventory();
				} else {
					pl1.openInventory(TradeResultPlayer1);
				}
			}
			pl1.closeInventory();
			pl1.giveExp(-tradedXPPlayer1);
			pl1.giveExp(tradedXPPlayer2);
		}
		if(pl2 != null) {
			if (!TradeResultPlayer2.isEmpty()) {
				if(TradeSettings.getAutoCollectSettingValue(pl2.getUniqueId())) {
					Inventory i2 = Bukkit.createInventory(null, 27, TitlePlayer2);
					List<ItemStack> LeftOvers = new ArrayList<>();
					for(ItemStack i : TradeResultPlayer2.getContents()) {
						if(i == null) continue;
						LeftOvers.addAll(pl2.getInventory().addItem(i).values());
					}
					for(ItemStack drops : LeftOvers) {
						i2.addItem(drops);
					}
					if(!i2.isEmpty()) pl2.openInventory(i2);
					else pl2.closeInventory();
				} else {
					pl2.openInventory(TradeResultPlayer2);
				}
			}
			pl2.closeInventory();
			pl2.giveExp(-tradedXPPlayer2);
			pl2.giveExp(tradedXPPlayer1);
		}
	}

}
