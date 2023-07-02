package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import net.justonedeveloper.plugins.trading.language.Phrase;
import net.justonedeveloper.plugins.trading.settings.TradeSettingsInventory;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class TradeCommand implements CommandExecutor {
	
	public static final int TradeRequestTimeMS = 30000;
	public static HashMap<UUID, TradeOfferCollection> TradeRequests = new HashMap<>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage(Language.getPhrase(Phrase.ERROR_SENDER_NOT_PLAYER));
			return false;
		}
		Player p = (Player) sender;
		UUID uuid = p.getUniqueId();
		if(args.length > 2)
		{
			sender.sendMessage(Language.get(p, Phrase.ERROR_TRADE_COMMAND_HELP));
			return false;
		}
		if(args.length == 1)
		{
			Player pl = Bukkit.getPlayer(args[0]);
			if(pl == null)
			{
				sendNotOnlineMessage(sender, args[0]);
				return false;
			}
			if(pl.getName().equals(p.getName()))
			{
				sender.sendMessage(Language.get(p, Phrase.TRADE_DENY_WITH_SELF));
				return false;
			}
			UUID uuid2 = pl.getUniqueId();
			
			// We also need to check if there is a pending trade request between these two players:
			if(TradeRequests.containsKey(uuid) && TradeRequests.get(uuid).contains(uuid2))
			{
				p.sendMessage(Language.get(p, Phrase.TRADE_DENY_ALREADY_PENDING));
				return true;
			}
			
			// Merge trade offers
			if(TradeRequests.containsKey(uuid2) && TradeRequests.get(uuid2).contains(uuid))
			{
				pl.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_SENT));
				p.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_RECEIVED));
				new Trade(pl, p);
				return true;
			}
			
			TextComponent msgAccept = new TextComponent();
			msgAccept.setText("§a§l[" + Language.get(pl, Phrase.TRADE_RECEIVED_MESSAGE_ACCEPT) + "§a§l]");
			msgAccept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Language.get(p, Phrase.TRADE_RECEIVED_MESSAGE_HOVER_ACCEPT))));
			msgAccept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade accept " + p.getName()));
			
			TextComponent msgDecline = new TextComponent();
			msgDecline.setText("§4§l[" + Language.get(pl, Phrase.TRADE_RECEIVED_MESSAGE_DECLINE) + "§4§l]");
			msgDecline.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Language.get(p, Phrase.TRADE_RECEIVED_MESSAGE_HOVER_DECLINE))));
			msgDecline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade decline " + p.getName()));
			
			TextComponent msgCancel = new TextComponent();
			msgCancel.setText("§c§l[" + Language.get(pl, Phrase.TRADE_SENT_MESSAGE_CANCEL) + "§c§l]");
			msgCancel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(Language.get(p, Phrase.TRADE_SENT_MESSAGE_HOVER_CANCEL))));
			msgCancel.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trade cancel " + pl.getName()));
			
			int tradeRequestTimeS = TradeRequestTimeMS / 1000;
			pl.sendMessage(Language.get(pl, Phrase.TRADE_RECEIVED_MESSAGE_BASE, p.getName()).replace("%seconds%", "" + tradeRequestTimeS));
			pl.spigot().sendMessage(new TextComponent("          "), msgAccept, new TextComponent("     "), msgDecline);
			
			p.sendMessage(Language.get(p, Phrase.TRADE_SENT_MESSAGE_BASE, pl.getName()).replace("%seconds%", "" + tradeRequestTimeS));
			p.spigot().sendMessage(new TextComponent("          "), msgCancel);
			
			addTradeRequest(p, pl);
			return true;
		}
		else if(args.length == 2)
		{
			String keyword = args[0].toLowerCase();
			if(!keyword.equals("accept") && !keyword.equals("decline") && !keyword.equals("cancel") && !keyword.equals("rescind") && !keyword.equals("takeback"))
			{
				sender.sendMessage(Language.get(p, Phrase.ERROR_TRADE_COMMAND_HELP));
				return false;
			}
			Player pl = Bukkit.getPlayer(args[1]);
			if(pl == null)
			{
				sendNotOnlineMessage(sender, args[1]);
				return false;
			}
			if(pl.getName().equals(p.getName()))
			{
				sender.sendMessage(Language.get(p, Phrase.TRADE_DENY_WITH_SELF));
				return false;
			}
			UUID uuid2 = pl.getUniqueId();
			
			if(keyword.equals("accept") || keyword.equals("decline"))
			{
				// We are in the list of someone else
				if(!TradeRequests.containsKey(uuid2) || !TradeRequests.get(uuid2).contains(uuid))
				{
					p.sendMessage(Language.get(p, Phrase.TRADE_DENY_TRY_ACCEPT_NO_OFFER));
					return false;
				}
				// Since action is being taken and it exists, lets remove it
				TradeRequests.get(uuid2).remove(uuid);
				if(keyword.equals("accept"))
				{
					pl.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_SENT));
					p.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_RECEIVED));
					new Trade(pl, p);
					return true;
				}
				
				pl.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_SENT));
				p.sendMessage(Language.get(p, Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_RECEIVED));
				return true;
			}
			
			// We are in the list of someone else
			if(!TradeRequests.containsKey(uuid) || !TradeRequests.get(uuid).contains(uuid2))
			{
				p.sendMessage(Language.get(p, Phrase.TRADE_DENY_TRY_CANCEL_NO_OFFER));
				return false;
			}
			TradeRequests.get(uuid).remove(uuid2);
			p.sendMessage(Language.get(p, Phrase.TRADE_OFFER_CANCELLED_SUCCESS));
		}
		p.sendMessage(Language.get(p, Phrase.TRADE_INVENTORY_MESSAGE_OPENING_SETTINGS_INVENTORY));
		TradeSettingsInventory.OpenInventory(p);
		return false;
	}
	
	private static void addTradeRequest(Player Trader, Player Target) { addTradeRequest(Trader.getUniqueId(), Target.getUniqueId());}
	private static void addTradeRequest(UUID Trader, UUID Target)
	{
		TradeOfferCollection activeTradeRequests;
		if(TradeRequests.containsKey(Trader)) activeTradeRequests = TradeRequests.get(Trader);
		else activeTradeRequests = new TradeOfferCollection();
		activeTradeRequests.add(Target, future());
		TradeRequests.put(Trader, activeTradeRequests);
		startTicker();
	}
	
	private static boolean isActive = false;
	private static int scheduler;
	private static void startTicker()
	{
		if(isActive) return;
		isActive = true;
		scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(TradingMain.main, () -> {
			for(UUID trader : TradeRequests.keySet())
			{
				// expire
				TradeOfferCollection trades = TradeRequests.get(trader);
				// Update (remove expired trades)
				for(int i = 0; i < trades.size(); ++i)
				{
					if(trades.get(i).Value > System.currentTimeMillis()) continue;
					trades.remove(i);
					i--;	// Update index since list is now 1 smaller
				}
				if(trades.isEmpty()) TradeRequests.remove(trader);
				else TradeRequests.put(trader, trades);
			}
			
			if(TradeRequests.size() == 0)
			{
				Bukkit.getScheduler().cancelTask(scheduler);
				isActive = false;
			}
		}, 20, 20);
	}
	
	private static long future()
	{
		return System.currentTimeMillis() + TradeCommand.TradeRequestTimeMS;
	}
	
	public static void clearPendingTrades(UUID uuid)
	{
		TradeRequests.remove(uuid);
		for(TradeOfferCollection pending : TradeRequests.values())
		{
			pending.remove(uuid);
		}
	}
	
	private void sendNotOnlineMessage(CommandSender p, String targetName)
	{
		// Either this: "§cThe specified player is not online right now."
		// Or this: "§cCould not find player §7" + targetName + "§c."
		p.sendMessage("§cCould not find player §7" + targetName + "§c.");
	}
	
}
