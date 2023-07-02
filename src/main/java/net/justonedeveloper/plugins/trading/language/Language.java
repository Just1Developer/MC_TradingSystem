package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.TradingMain;
import net.justonedeveloper.plugins.trading.settings.TradeSettingsInventory;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Language {
	
	static File folder = new File(TradingMain.main.getDataFolder(), "Languages/");
	public static final String DefaultLanguage = "en-US";
	public static final List<String> LanguageIDs = new ArrayList<>();
	public static final HashMap<String, Language> Languages = new HashMap<>();
	// This is just for 'faster' access. Might remove / rework later
	//private static final HashMap<UUID, String> LanguagesPerPlayer = new HashMap<>();
	
	public static void Init()
	{
		// Import all languages
		if(!new File(folder, DefaultLanguage + ".yml").exists())
		{
			new Language(DefaultLanguage, "English");
		}
		File[] files = folder.listFiles();
		if(files == null) return;
		for(File f : files)
		{
			String code = f.getName().substring(0, f.getName().length() - 4);	// remove .yml, idk if thats necessary
			new Language(code);
		}
	}
	
	public static boolean exists(String languageCode)
	{
		return LanguageIDs.contains(languageCode);
	}
	
	public static int languageAmount()
	{
		return LanguageIDs.size();
	}
	
	public static String get(int index)
	{
		return LanguageIDs.get(index);
	}
	
	public static Language getLanguage(String code)
	{
		return Languages.getOrDefault(code, Languages.get(DefaultLanguage));
	}
	
	public static Language getLanguage(Player p) { return getLanguage(p.getUniqueId()); }
	public static Language getLanguage(UUID uuid)
	{
		return getLanguage(TradeSettingsInventory.getLanguage(uuid));
		// if(LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid));
		// return Languages.get(DefaultLanguage);
	}
	
	public static void CreateLanguageFile(String code, String LanguageName)
	{
		File f = new File(TradingMain.main.getDataFolder(), "Languages/" + code + ".yml");
		if(f.exists()) return;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		cfg.set("Language.LanguageName", LanguageName);
		cfg.set("Language.ItemMaterial", Material.DARK_OAK_SIGN.toString());
		if(code.equals(DefaultLanguage)) cfg.set(Phrase.ERROR_LANGUAGE_NOT_EXIST.toString(), "§cYour selected language (%langCode%) does not seems to exist anymore. Your language has been reset to english.");
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
		cfg.set(Phrase.TRADE_SENT_MESSAGE_BASE.toString(), "§eYou invited §7%name% §eto trade. They have §7%seconds% §eseconds to accept.");
		cfg.set(Phrase.TRADE_SENT_MESSAGE_CANCEL.toString(), "TAKE BACK");
		cfg.set(Phrase.TRADE_SENT_MESSAGE_HOVER_CANCEL.toString(), "§aClick to rescind trade offer");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_BASE.toString(), "§eYou have been invited to trade with §7%name%§e. You have §7%seconds% §eseconds to accept.");
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
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_TITLE.toString(), "§8Trade Settings: %name%");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME.toString(), "§a§lEnabled");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_LORE.toString(), "§8Click to disable");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME.toString(), "§c§lDisabled");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_LORE.toString(), "§8Click to enable");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_NAME.toString(), "§8Inactive");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_LORE.toString(), "§8Click to enable");
		// Settings inventory
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_ACCEPT_NAME.toString(), "§aAuto-Accept Trades");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_TRADE_ON_REQUEST_NAME.toString(), "§eTrade on Request");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_DECLINE_NAME.toString(), "§cAuto-Decline Trades");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_NAME.toString(), "§dAuto-Collect Items");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE.toString(), "§8Items are automatically added to\n§8your inventory post-trade.");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_SET_LANGUAGE_NAME.toString(), "§eSet Language");
		// Language Inventory
		cfg.set(Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE.toString(), "§8Language Settings: %name%");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_BACK_TO_SETTINGS_NAME.toString(), "§cBack to Settings");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_ADMIN_EDIT_LANGUAGE_NAME.toString(), "§eEdit Language");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_NEXT_PAGE_NAME.toString(), "§7Next Page");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_PREV_PAGE_NAME.toString(), "§7Previous Page");
		
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
		return getLanguage(TradeSettingsInventory.getLanguage(uuid)).get(phrase);
		/*
		if (LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getPhrase(phrase);
		 */
	}
	public static String get(HumanEntity p, Phrase phrase, String MentionedPlayerName) { return p == null ? getPhrase(phrase, MentionedPlayerName) : get(p.getUniqueId(), phrase, MentionedPlayerName); }
	public static String get(UUID uuid, Phrase phrase, String MentionedPlayerName)
	{
		if (uuid == null) return getPhrase(phrase, MentionedPlayerName);
		return getLanguage(TradeSettingsInventory.getLanguage(uuid)).get(phrase, MentionedPlayerName);
		/*
		if (LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase, MentionedPlayerName);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getPhrase(phrase, MentionedPlayerName);
		 */
	}
	
	// Language Class
	
	public String LanguageCode;
	public String LanguageName;
	public Material ItemMaterial;
	private HashMap<Phrase, String> Dictionary;
	
	public Language(String LanguageCode)
	{
		this(LanguageCode, "Unknown");
	}
	public Language(String LanguageCode, String LanguageName)
	{
		this.LanguageCode = LanguageCode;
		if(LanguageIDs.contains(LanguageCode)) return;
		Languages.put(LanguageCode, this);
		LanguageIDs.add(LanguageCode);
		Dictionary = new HashMap<>();
		importLang(LanguageCode, LanguageName);
	}
	
	private void importLang(String code, String LanguageName)
	{
		File f = new File(folder,  code + ".yml");
		if(!f.exists())
		{
			// Create Default Language File
			CreateLanguageFile(code, LanguageName);
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		for (Phrase phrase : Phrase.values())
		{
			Dictionary.put(phrase, cfg.getString(phrase.toString()));
		}
		this.LanguageName = cfg.getString("Language.LanguageName");
		this.ItemMaterial = Material.valueOf(cfg.getString("Language.ItemMaterial"));
		if(this.LanguageName == null) this.LanguageName = LanguageName;
	}
	
	public String get(Phrase Phrase)
	{
		if(!LanguageCode.equals(DefaultLanguage)) return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase));
		else return Dictionary.getOrDefault(Phrase, "§cunknown");
	}
	public String get(Phrase Phrase, String MentionedPlayerName)
	{
		if(!LanguageCode.equals(DefaultLanguage)) return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase)).replace("%name%", MentionedPlayerName);
		else return Dictionary.getOrDefault(Phrase, "§cunknown").replace("%name%", MentionedPlayerName);
	}
	
}
