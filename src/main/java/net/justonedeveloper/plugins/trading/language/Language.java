package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.TradingMain;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Language {
	
	static File folder = new File(TradingMain.main.getDataFolder(), "Languages/");
	public static final String DefaultLanguage = "en-US";
	private static final HashMap<String, Language> Languages = new HashMap<>();
	private static final HashMap<UUID, String> LanguagesPerPlayer = new HashMap<>();
	
	public static void Init()
	{
		// Import all languages
		if(!folder.exists())
		{
			new Language("en-EN");
		}
		File[] files = folder.listFiles();
		if(files == null) return;
		for(File f : files)
		{
			String code = f.getName().substring(0, f.getName().length() - 4);	// remove .yml, idk if thats necessary
			new Language(code);
		}
	}
	
	public static Language getLanguage(String code)
	{
		if(Languages.containsKey(code)) return Languages.get(code);
		return Languages.get(DefaultLanguage);
	}
	
	public static void CreateLanguageFile(String code)
	{
		File f = new File(TradingMain.main.getDataFolder(), "Languages/" + code + ".yml");
		if(f.exists()) return;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		cfg.set(Phrase.ERROR_PLAYER_NOT_ONLINE.toString(), "§cCould not find player §7%name%§c.");
		cfg.set(Phrase.ERROR_SENDER_NOT_PLAYER.toString(), "§cThis command is for players only!");
		cfg.set(Phrase.ERROR_TRADE_COMMAND_HELP.toString(), "§cError: Unknown usage of /trade. Instead, try:\n§e/trade §7[player] §8or §e/trade <accept, decline, cancel> §7[player]");
		cfg.set(Phrase.TRADE_DENY_WITH_SELF.toString(), "§cYou cannot trade with yourself.");
		cfg.set(Phrase.TRADE_DENY_NO_ACCEPT.toString(), "§cThis player does not allow trade requests.");
		cfg.set(Phrase.TRADE_DENY_ALREADY_PENDING.toString(), "§cYou already have a pending trade offer to this person.");
		cfg.set(Phrase.TRADE_DENY_TRY_ACCEPT_NO_OFFER.toString(), "§cYou have no pending trade offer from %name%");
		cfg.set(Phrase.TRADE_DENY_TRY_CANCEL_NO_OFFER.toString(), "§cYou have no pending trade offer to %name%");
		// Items
		cfg.set(Phrase.ITEM_NAME_CONFIRM_TRADE.toString(), "§c§lConfirm Trade");
		cfg.set(Phrase.ITEM_NAME_TRADE_CONFIRMED.toString(), "§a§lTrade confirmed");
		cfg.set(Phrase.ITEM_LORE_CLICK_TO_RESCIND.toString(), "§cClick to rescind offer");
		cfg.set(Phrase.ITEM_NAME_PARTNER_NOT_CONFIRMED.toString(), "§c§lPartner has not confirmed Trade");
		cfg.set(Phrase.ITEM_NAME_PARTNER_CONFIRMED.toString(), "§a§lPartner has confirmed Trade");
		cfg.set(Phrase.ITEM_NAME_PROCESSING_TRADE.toString(), "§b§lTrading in progress...");
		// Trade Request Messages
		cfg.set(Phrase.TRADE_SENT_MESSAGE_BASE.toString(), "§eYou invited §7%name% §eto trade. They have §730 §eseconds to accept.");
		cfg.set(Phrase.TRADE_SENT_MESSAGE_CANCEL.toString(), "TAKE BACK");
		cfg.set(Phrase.TRADE_SENT_MESSAGE_HOVER_CANCEL.toString(), "§aClick to rescind trade offer");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_BASE.toString(), "§eYou have been invited to trade with §7%name%§e. You have §730 §eseconds to accept.");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_ACCEPT.toString(), "ACCEPT TRADE");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_DECLINE.toString(), "DECLINE TRADE");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_ACCEPT.toString(), "§8Click to accept trade");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_DECLINE.toString(), "§8Click to decline trade");
		// Trade Offer Results
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_SENT.toString(), "§e%name% §aaccepted §eyour trade request");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_RECEIVED.toString(), "§eYou §aaccepted §ethe trade request from §7%name%");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_SENT.toString(), "§e%name% §4declined §eyour trade request");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_RECEIVED.toString(), "§eYou §4declined §ethe trade request from §7%name%");
		cfg.set(Phrase.TRADE_OFFER_CANCELLED_SUCCESS.toString(), "§eYou §4cancelled §ethe trade request to §7%name%§e.");
		// Trade Inventory
		cfg.set(Phrase.TRADE_INVENTORY_TITLE.toString(), "§8Trade with %name%");
		cfg.set(Phrase.TRADE_INVENTORY_CONCLUSION_TITLE.toString(), "§8New Items: Trade with %name%");
		cfg.set(Phrase.TRADE_INVENTORY_CLOSE_CANCELLED_MESSAGE.toString(), "§cThe trade has been canceled.");
		cfg.set(Phrase.TRADE_INVENTORY_UNKNOWN_PLAYER_NAME.toString(), "§c[UNKNOWN]");
		// Settings
		cfg.set(Phrase.TRADE_INVENTORY_MESSAGE_OPENING_SETTINGS_INVENTORY.toString(), "§eOpening Trading Settings..");
		
		try {
			cfg.save(f);
		} catch (IOException e) {
			System.out.println("Failed to create file " + f.getAbsolutePath());
		}
	}
	
	public static String getPhrase(Phrase phrase)
	{
		return getLanguage(DefaultLanguage).get(phrase);
	}
	public static String getPhrase(Phrase phrase, String MentionedPlayerName)
	{
		return getLanguage(DefaultLanguage).get(phrase, MentionedPlayerName);
	}
	public static String get(HumanEntity p, Phrase phrase) { return p == null ? getPhrase(phrase) : get(p.getUniqueId(), phrase); }
	public static String get(UUID uuid, Phrase phrase)
	{
		if (uuid == null) return getPhrase(phrase);
		if (LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getPhrase(phrase);
	}
	public static String get(HumanEntity p, Phrase phrase, String MentionedPlayerName) { return p == null ? getPhrase(phrase, MentionedPlayerName) : get(p.getUniqueId(), phrase, MentionedPlayerName); }
	public static String get(UUID uuid, Phrase phrase, String MentionedPlayerName)
	{
		if (uuid == null) return getPhrase(phrase, MentionedPlayerName);
		if (LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase, MentionedPlayerName);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getPhrase(phrase, MentionedPlayerName);
	}
	
	public static void setPlayerLanguage(UUID Player, String LanguageID)
	{
		LanguagesPerPlayer.put(Player, LanguageID);
		// Todo perhaps update UI?
	}
	
	// Language Class
	
	private HashMap<Phrase, String> Dictionary;
	
	public Language(String LanguageCode)
	{
		if(Languages.containsKey(LanguageCode)) return;
		Languages.put(LanguageCode, this);
		importLang(LanguageCode);
	}
	
	private void importLang(String code)
	{
		File f = new File(folder,  code + ".yml");
		if(!f.exists())
		{
			// Create Default Language File
			CreateLanguageFile(code);
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		for (Phrase phrase : Phrase.values())
		{
			Dictionary.put(phrase, cfg.getString(phrase.toString()));
		}
	}
	
	public String get(Phrase Phrase)
	{
		return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase));
	}
	public String get(Phrase Phrase, String MentionedPlayerName)
	{
		return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase)).replace("%name%", MentionedPlayerName);
	}
	
}
