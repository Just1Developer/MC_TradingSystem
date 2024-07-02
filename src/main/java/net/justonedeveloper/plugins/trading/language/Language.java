package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.GlobalConfig;
import net.justonedeveloper.plugins.trading.main.TradingMain;
import net.justonedeveloper.plugins.trading.settings.TradeSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Language {
	
	private static final Pattern LANG_FILE_REGEX = Pattern.compile("^[a-z]{2}-[A-Z]{2}$");
	private static final Map<Phrase, String> PHRASE_DEFAULTS = new HashMap<>();
	static {
		PHRASE_DEFAULTS.put(Phrase.ERROR_LANGUAGE_WAS_DELETED, "§cYour selected language (%lang%) has been deleted. Your language has been reset to %defaultLang%.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_LANGUAGE_ALREADY_EXISTS, "§cSorry, that language already exists.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_LANGUAGE_NOT_EXIST, "§cSorry, that language does not seem to exist.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_INSUFFICIENT_PERMISSIONS, "§cSorry, you don't have permission to do that.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_PLAYER_NOT_ONLINE, "§cCould not find player §7%name%§c.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_SENDER_NOT_PLAYER, "§cThis command is for players only!");
		PHRASE_DEFAULTS.put(Phrase.ERROR_PLAYER_IN_SPECTATOR_MODE, "§cYou cannot accept trades while in spectator mode!");
		PHRASE_DEFAULTS.put(Phrase.ERROR_OTHER_PLAYER_IN_SPECTATOR_MODE, "§7%name% §ccannot trade right now.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_TRADE_COMMAND_HELP, "§cError: Unknown usage of /trade. Instead, try:\n§e/trade §7[player]§8, §e/trade <accept, decline, cancel> §7[player] §8or §e/trade language ...");
		PHRASE_DEFAULTS.put(Phrase.ERROR_TRADE_LANGUAGE_COMMAND_HELP, "§cError: Unknown usage of /trade language. Instead, try:\n§e/trade language <create/reload/edit/reset/delete> <LanguageCode> §7[for create: LanguageName]");
		PHRASE_DEFAULTS.put(Phrase.ERROR_TRADE_LANGUAGE_COMMAND_HELP_CREATE, "§cError: Unknown usage of /trade language create. Instead, try:\n§e/trade language create <LanguageCode> <LanguageName>");
		PHRASE_DEFAULTS.put(Phrase.ERROR_INCORRECT_LANGUAGE_CODE_FORMAT, "§cError: Incorrect Language Code format. The correct format is: §exx-XX§c.\n§cFor example: §7en-US §cor §7de-DE§c.");
		PHRASE_DEFAULTS.put(Phrase.ERROR_MESSAGE_CANNOT_DELETE_DEFAULT_LANGUAGE, "§4§lError: Cannot delete default language!");
		PHRASE_DEFAULTS.put(Phrase.SUCCESS_LANGUAGE_CREATED, "§aSuccess! §eYou created the language §c%lang% §7(%langName%)§e.");
		PHRASE_DEFAULTS.put(Phrase.SUCCESS_LANGUAGE_RELOADED, "§eReloaded language §7%lang%§e.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_DENY_WITH_SELF, "§cYou cannot trade with yourself.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_DENY_NO_ACCEPT, "§cThis player does not allow trade requests.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_DENY_ALREADY_PENDING, "§cYou already have a pending trade offer to §7%name%§c.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_DENY_TRY_ACCEPT_NO_OFFER, "§cYou have no pending trade offer from §7%name%§c.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_DENY_TRY_CANCEL_NO_OFFER, "§cYou have no pending trade offer to §7%name%§c.");
		// Items
		PHRASE_DEFAULTS.put(Phrase.ITEM_NAME_CONFIRM_TRADE, "§c§lConfirm trade");
		PHRASE_DEFAULTS.put(Phrase.ITEM_NAME_TRADE_CONFIRMED, "§a§lTrade confirmed");
		PHRASE_DEFAULTS.put(Phrase.ITEM_LORE_CLICK_TO_RESCIND, "§cClick to rescind offer");
		PHRASE_DEFAULTS.put(Phrase.ITEM_NAME_PARTNER_NOT_CONFIRMED, "§c§lPartner has not confirmed trade");
		PHRASE_DEFAULTS.put(Phrase.ITEM_NAME_PARTNER_CONFIRMED, "§a§lPartner has confirmed trade");
		PHRASE_DEFAULTS.put(Phrase.ITEM_NAME_PROCESSING_TRADE, "§b§lTrading in progress...");
		// Trade Request Messages
		PHRASE_DEFAULTS.put(Phrase.TRADE_SENT_MESSAGE_BASE, "§eYou invited §7%name% §eto trade. They have §7%seconds% §eseconds to accept.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SENT_MESSAGE_CANCEL, "TAKE BACK");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SENT_MESSAGE_HOVER_CANCEL, "§8Click to rescind trade offer");
		PHRASE_DEFAULTS.put(Phrase.TRADE_RECEIVED_MESSAGE_BASE, "§eYou have been invited to trade with §7%name%§e. You have §7%seconds% §eseconds to accept.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_RECEIVED_MESSAGE_ACCEPT, "ACCEPT TRADE");
		PHRASE_DEFAULTS.put(Phrase.TRADE_RECEIVED_MESSAGE_DECLINE, "DECLINE TRADE");
		PHRASE_DEFAULTS.put(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_ACCEPT, "§8Click to accept trade");
		PHRASE_DEFAULTS.put(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_DECLINE, "§8Click to decline trade");
		// Trade Offer Results
		PHRASE_DEFAULTS.put(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_SENT, "§e%name% §aaccepted §eyour trade request.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_RECEIVED, "§eYou §aaccepted §ethe trade request from §7%name%.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_SENT, "§e%name% §4declined §eyour trade request.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_RECEIVED, "§eYou §4declined §ethe trade request from §7%name%.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_OFFER_CANCELLED_SUCCESS, "§eYou §4cancelled §ethe trade request to §7%name%§e.");
		// Trade Inventory
		PHRASE_DEFAULTS.put(Phrase.TRADE_INVENTORY_TITLE, "§8Trade with %name%");
		PHRASE_DEFAULTS.put(Phrase.TRADE_INVENTORY_CONCLUSION_TITLE, "§8New Items: Trade with %name%");
		PHRASE_DEFAULTS.put(Phrase.TRADE_INVENTORY_CLOSE_CANCELLED_MESSAGE, "§cThe trade has been cancelled.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_INVENTORY_UNKNOWN_PLAYER_NAME, "§c[UNKNOWN]");
		// Settings
		PHRASE_DEFAULTS.put(Phrase.TRADE_INVENTORY_MESSAGE_OPENING_SETTINGS_INVENTORY, "§eOpening Trading Settings..");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_TITLE, "§8Trade Settings: %name%");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_NAME, "§a§lEnabled");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_SELECTED_NAME, "§a§lSelected");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_ENABLED_LORE, "§8Click to disable");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_NAME, "§c§lDisabled");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_DISABLED_LORE, "§8Click to enable");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_NAME, "§8Inactive");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_MULTIPLE_INACTIVE_LORE, "§8Click to enable");
		// Settings inventory
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_ACCEPT_NAME, "§aAuto-Accept Trades");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_TRADE_ON_REQUEST_NAME, "§eTrade on Request §7(Recommended)");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_DECLINE_NAME, "§cAuto-Decline Trades");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_NAME, "§dAuto-Collect Items");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE, "Items are automatically added to\nyour inventory post-trade.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_INVENTORY_SET_LANGUAGE_NAME, "§eSet Language");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_REFRESH_ALL_NAME, "§d§lRefresh Global Settings");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_REFRESH_ALL_LORE, "§7Re-imports the settings from the config file.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_SETTINGS_REFRESH_MSG_REFRESHED_SETTINGS, "§eRe-imported all settings.");
		// Language Inventory
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE, "§8Language Settings | Page %page%");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_BACK_TO_SETTINGS_NAME, "§cBack to Settings");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_ADMIN_EDIT_LANGUAGE_NAME, "§eEdit Language");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_NEXT_PAGE_NAME, "§7Next Page");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_PREV_PAGE_NAME, "§7Previous Page");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_CHANGED_LANGUAGE_MESSAGE, "§eChanged Language to §7%lang%§e.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_REFRESH_LANGS_NAME, "§d§lRefresh Languages");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_REFRESH_LANGS_LORE, "§7Re-imports all languages from the files.");
		PHRASE_DEFAULTS.put(Phrase.TRADE_LANG_SETTINGS_MSG_REFRESHED_LANGUAGES, "§eRe-imported all languages.");
		// Edit Language Inventory
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_TITLE, "§8Editing Language §d§l%lang%");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_NAME, "§eChange Language Material");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_LORE, "§7Click on this with an Item to change the material.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_NAME, "§4§lDelete Language: %langName% | ");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_LORE, "§c§lThis action cannot be undone.\n§c§lShift-click to delete.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_NAME, "§c§lReset Language: %langName% | ");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_LORE, "§e§lThis action cannot be undone.\n§e§lShift-click to reset.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_NAME, "§8§lCannot delete language: §c%lang%");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_LORE, "§7§lCannot delete the default language.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_NAME, "§cBack to Languages");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_LORE, "§7Page %page%");
		// Edit Language Messages
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_MESSAGE_LANG_DELETED, "§cDeleted language §4§l%langName% §7(%lang%)§c.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_MESSAGE_ITEM_UPDATED, "§eUpdated Item for §d§l%langName%§e.");
		PHRASE_DEFAULTS.put(Phrase.LANGUAGE_EDIT_MESSAGE_LANG_RESET, "§cReset language §e§l%langName% §7(%lang%)§c.");
		// XP-Trading GUI
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_CURRENT_XP_NAME, "§e§lCurrent XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_CURRENT_XP_LORE_1, "§7XP Traded: §e%points%");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_CURRENT_XP_LORE_2, "§7Resulting Level: §c%level%");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_ADD_XP_NAME, "§a§lAdd XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_ADD_XP_LORE_SINGLE, "§7Click: +%points% XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_ADD_XP_LORE_SINGLE_SHIFT, "§7Shift-Click: +%points% XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_ADD_XP_LORE_LEVEL, "§7Click: +1 Level §c(+%points% XP)");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_ADD_XP_LORE_LEVEL_SHIFT, "§eShift-Click: All of it §c(+%points% XP)");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_REMOVE_XP_NAME, "§c§lRemove XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_SINGLE, "§7Click: -%points% XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_SINGLE_SHIFT, "§7Shift-Click: -%points% XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_LEVEL, "§7Click: -1 Level §c(-%points% XP)");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_REMOVE_XP_LORE_LEVEL_SHIFT, "§eShift-Click: All of it §c(-%points% XP)");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_EDIT_TRADED_XP_NAME, "§e§lEdit Traded XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_EDIT_TRADED_XP_LORE, "§7Click to add/remove XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_CONFIRM_TRADED_XP_NAME, "§e§lConfirm Traded XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_CONFIRM_TRADED_XP_LORE, "§7Click to confirm XP");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_OVERVIEW_NAME, "§b§lXP Trade Information");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_OVERVIEW_LORE_SELF, "§7Your traded XP: %points%");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_GUI_OVERVIEW_LORE_OTHER, "§7%name% traded XP: %points%");
		// XP-Trading
		//PHRASE_DEFAULTS.put(Phrase.XP_TRADING_STILL_EDITING_XP_NO_CONFIRM, "§cCannot confirm trade, you have to confirm your traded XP first!");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_STILL_EDITING_XP_NO_CONFIRM_LORE, "First confirm your traded XP. (The bottle)");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_SELF, "§cYour XP fell below the amount you locked in for the trade, the amount of traded XP has been adjusted accordingly.");
		PHRASE_DEFAULTS.put(Phrase.XP_TRADING_XP_CHANGED_TO_TOO_LOW_OTHER, "§cYour trade partner changed XP unexpectedly. You may need to re-confirm the trade.");
	}
	
	static File folder = new File(TradingMain.main.getDataFolder(), "Languages/");
	public static String DefaultLanguage = "en-US";
	private static final List<String> LanguageIDs = new ArrayList<>();
	private static final HashMap<String, Language> Languages = new HashMap<>();
	private static final String DefaultValueString = "§cunknown";
	
	// INFO: Per Player Language saving is located inside TradeSettings class
	
	public static void Init()
	{
		// Import all languages
		InitDefaultOnly();
		
		File[] files = folder.listFiles();
		if(files == null) return;
		for(File f : files)
		{
			String code = f.getName().substring(0, f.getName().length() - 4);
			if (isInvalidLanguageCode(code)) continue;
			if (LanguageIDs.contains(code)) continue;
			new Language(code);
		}
	}
	
	public static boolean isInvalidLanguageCode(String code) {
		return !LANG_FILE_REGEX.matcher(code).matches();
	}

	public static void ReInit()
	{
		LanguageIDs.clear();
		Languages.clear();
		Init();
		for (Player player : Bukkit.getOnlinePlayers()) {
			TradeSettings.tryLoadLanguageOf(player);
		}
	}

	public static void InitDefaultOnly() {
		if(!new File(folder, DefaultLanguage + ".yml").exists())
		{
			new Language(DefaultLanguage, "English");
		}
		else
		{
			// Import Default Language First
			new Language(DefaultLanguage);
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
		return getLanguage(TradeSettings.getLanguage(uuid));
	}
	public static Language getDefaultLanguage() { return getLanguage(Language.DefaultLanguage); }

	public static void CreateLanguageFile(String code, String LanguageName) { CreateLanguageFile(code, LanguageName, false); }
	public static void CreateLanguageFile(String code, String LanguageName, boolean ignoreExists)
	{
		File f = new File(TradingMain.main.getDataFolder(), "Languages/" + code + ".yml");
		if(f.exists() && !ignoreExists) return;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		cfg.set("Language.LanguageName", LanguageName);
		cfg.set("Language.ItemMaterial", GlobalConfig.DefaultLanguageItem.toString());
		for (Phrase phrase : Phrase.values()) {
			cfg.set(phrase.toString(), PHRASE_DEFAULTS.getOrDefault(phrase, DefaultValueString));
		}
		
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
	public static String getPhraseWithApostrophe(Phrase phrase, String MentionedPlayerName)
	{
		return getLanguage(DefaultLanguage).get(phrase, MentionedPlayerName);
	}
	public static String get(HumanEntity p, Phrase phrase) { return p == null ? getPhrase(phrase) : get(p.getUniqueId(), phrase); }
	public static String get(UUID uuid, Phrase phrase)
	{
		if (uuid == null) return getPhrase(phrase);
		return getLanguage(TradeSettings.getLanguage(uuid)).get(phrase);
	}
	public static String get(HumanEntity p, Phrase phrase, String MentionedPlayerName) { return p == null ? getPhrase(phrase, MentionedPlayerName) : get(p.getUniqueId(), phrase, MentionedPlayerName); }
	public static String get(UUID uuid, Phrase phrase, String MentionedPlayerName)
	{
		if (uuid == null) return getPhrase(phrase, MentionedPlayerName);
		return getLanguage(TradeSettings.getLanguage(uuid)).get(phrase, MentionedPlayerName);
	}
	public static String getWithApostrophe(HumanEntity p, Phrase phrase, String MentionedPlayerName) { return p == null ? getPhrase(phrase, MentionedPlayerName) : get(p.getUniqueId(), phrase, MentionedPlayerName); }
	public static String getWithApostrophe(UUID uuid, Phrase phrase, String MentionedPlayerName)
	{
		if (uuid == null) return getPhraseWithApostrophe(phrase, MentionedPlayerName);
		return getLanguage(TradeSettings.getLanguage(uuid)).getWithApostrophe(phrase, MentionedPlayerName);
	}
	
	public static void deleteLanguage(String code)
	{
		if(code.equals(DefaultLanguage)) return; // Failsafe
		File f = new File(folder,  code + ".yml");
		if(!f.exists()) return;
		String langName = Language.exists(code) ? Language.getLanguage(code).LanguageName : code;
		Languages.remove(code);
		LanguageIDs.remove(code);
		for(Player p : Bukkit.getOnlinePlayers())
		{
			if(TradeSettings.getLanguage(p.getUniqueId()).equals(code))
			{
				TradeSettings.setLanguage(p.getUniqueId(), DefaultLanguage);
				p.sendMessage(getPhrase(Phrase.ERROR_LANGUAGE_WAS_DELETED).replace("%lang%", langName));
			}
		}
		f.delete();
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
		/*{
			Languages.remove(LanguageCode);
			LanguageIDs.remove(LanguageCode);
		}*/
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
		HashMap<Phrase, String> updates = new HashMap<>();
		for (Phrase phrase : Phrase.values())
		{
			String s = cfg.getString(phrase.toString());
			if(s == null)
			{
				if(code.equals(DefaultLanguage)) s = PHRASE_DEFAULTS.getOrDefault(phrase, DefaultValueString);
				else s = getPhrase(phrase);		// Get Default Language Phrase
				updates.put(phrase, s);
			}
			Dictionary.put(phrase, s);
		}
		if (!updates.isEmpty()) {
			cfg = YamlConfiguration.loadConfiguration(new File(f.getName()));
			for (Map.Entry<Phrase, String> entry : updates.entrySet()) {
				cfg.set(entry.getKey().toString(), entry.getValue());
			}
			try { cfg.save(f); } catch (IOException ignored) { }
		}
		this.LanguageName = cfg.getString("Language.LanguageName");
		this.ItemMaterial = Material.valueOf(cfg.getString("Language.ItemMaterial"));
		if(this.LanguageName == null) this.LanguageName = LanguageName;
	}
	
	public void reload()
	{
		Dictionary.clear();
		importLang(LanguageCode, LanguageName);
	}
	
	public void setItemMaterial(Material mat)
	{
		ItemMaterial = mat;
		File f = new File(folder,  LanguageCode + ".yml");
		if(!f.exists())
		{
			// Create Default Language File
			CreateLanguageFile(LanguageCode, LanguageName);
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		cfg.set("Language.ItemMaterial", mat.toString());
		try {
			cfg.save(f);
		} catch (IOException ignored) {
		}
	}
	
	public String get(Phrase Phrase)
	{
		if(!LanguageCode.equals(DefaultLanguage)) return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase));
		else return Dictionary.getOrDefault(Phrase, DefaultValueString);
	}
	public String get(Phrase Phrase, String MentionedPlayerName)
	{
		if(!LanguageCode.equals(DefaultLanguage)) return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase)).replace("%name%", MentionedPlayerName);
		else return Dictionary.getOrDefault(Phrase, DefaultValueString).replace("%name%", MentionedPlayerName);
	}
	public String getWithApostrophe(Phrase Phrase, String MentionedPlayerName)
	{
		String name;
		if (MentionedPlayerName == null) {
			name = "null";
		} else {
			name = MentionedPlayerName + "'";
			if (!MentionedPlayerName.endsWith("s")) name = name + "s";
		}
		if(!LanguageCode.equals(DefaultLanguage)) return Dictionary.getOrDefault(Phrase, getLanguage(DefaultLanguage).get(Phrase)).replace("%name%", name);
		else return Dictionary.getOrDefault(Phrase, DefaultValueString).replace("%name%", name);
	}
	
}
