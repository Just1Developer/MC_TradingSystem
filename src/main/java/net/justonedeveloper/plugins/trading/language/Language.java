package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.GlobalConfig;
import net.justonedeveloper.plugins.trading.main.TradeCommand;
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
import java.util.UUID;

public class Language {
	
	static File folder = new File(TradingMain.main.getDataFolder(), "Languages/");
	public static String DefaultLanguage = "en-US";
	private static final List<String> LanguageIDs = new ArrayList<>();
	private static final HashMap<String, Language> Languages = new HashMap<>();
	private static final String DefaultValueString = "§cunknown";
	
	// INFO: Per Player Language saving is located inside TradeSettings class
	
	public static void Init()
	{
		// Import all languages
		if(!new File(folder, DefaultLanguage + ".yml").exists())
		{
			new Language(DefaultLanguage, "English");
		}
		else
		{
			// Import Default Language First
			new Language(DefaultLanguage);
		}
		
		File[] files = folder.listFiles();
		if(files == null) return;
		for(File f : files)
		{
			String code = f.getName().substring(0, f.getName().length() - 4);
			if(TradeCommand.incorrectLanguageCodeFormat(code)) continue;
			if(LanguageIDs.contains(code)) continue;
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
		return getLanguage(TradeSettings.getLanguage(uuid));
	}
	
	public static void CreateLanguageFile(String code, String LanguageName) { CreateLanguageFile(code, LanguageName, false); }
	public static void CreateLanguageFile(String code, String LanguageName, boolean ignoreExists)
	{
		File f = new File(TradingMain.main.getDataFolder(), "Languages/" + code + ".yml");
		if(f.exists() && !ignoreExists) return;
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		cfg.set("Language.LanguageName", LanguageName);
		cfg.set("Language.ItemMaterial", GlobalConfig.DefaultLanguageItem.toString());
		cfg.set(Phrase.ERROR_LANGUAGE_WAS_DELETED.toString(), "§cYour selected language (%lang%) has been deleted. Your language has been reset to English.");
		cfg.set(Phrase.ERROR_LANGUAGE_ALREADY_EXISTS.toString(), "§cSorry, that language already exists.");
		cfg.set(Phrase.ERROR_LANGUAGE_NOT_EXIST.toString(), "§cSorry, that language does not seem to exist.");
		cfg.set(Phrase.ERROR_INSUFFICIENT_PERMISSIONS.toString(), "§cSorry, you don't have permission to do that.");
		cfg.set(Phrase.ERROR_PLAYER_NOT_ONLINE.toString(), "§cCould not find player §7%name%§c.");
		cfg.set(Phrase.ERROR_SENDER_NOT_PLAYER.toString(), "§cThis command is for players only!");
		cfg.set(Phrase.ERROR_PLAYER_IN_SPECTATOR_MODE.toString(), "§cYou cannot accept trades while in spectator mode!");
		cfg.set(Phrase.ERROR_OTHER_PLAYER_IN_SPECTATOR_MODE.toString(), "§7%name% §7cannot trade right now.");
		cfg.set(Phrase.ERROR_TRADE_COMMAND_HELP.toString(), "§cError: Unknown usage of /trade. Instead, try:\n§e/trade §7[player]§8, §e/trade <accept, decline, cancel> §7[player] §8or §e/trade language ...");
		cfg.set(Phrase.ERROR_TRADE_LANGUAGE_COMMAND_HELP.toString(), "§cError: Unknown usage of /trade language. Instead, try:\n§e/trade language <create/reload/edit/reset/delete> <LanguageCode> §7[for create: LanguageName]");
		cfg.set(Phrase.ERROR_TRADE_LANGUAGE_COMMAND_HELP_CREATE.toString(), "§cError: Unknown usage of /trade language create. Instead, try:\n§e/trade language create <LanguageCode> <LanguageName>");
		cfg.set(Phrase.ERROR_INCORRECT_LANGUAGE_CODE_FORMAT.toString(), "§cError: Incorrect Language Code format. The correct format is: §exx-XX§c.\n§cFor example: §7en-US §cor §7de-DE§c.");
		cfg.set(Phrase.ERROR_MESSAGE_CANNOT_DELETE_DEFAULT_LANGUAGE.toString(), "§4§lError: Cannot delete default language!");
		cfg.set(Phrase.SUCCESS_LANGUAGE_CREATED.toString(), "§aSuccess! §eYou created the language §c%lang% §7(%langName%)§e.");
		cfg.set(Phrase.SUCCESS_LANGUAGE_RELOADED.toString(), "§eReloaded language §7%lang%§e.");
		cfg.set(Phrase.TRADE_DENY_WITH_SELF.toString(), "§cYou cannot trade with yourself.");
		cfg.set(Phrase.TRADE_DENY_NO_ACCEPT.toString(), "§cThis player does not allow trade requests.");
		cfg.set(Phrase.TRADE_DENY_ALREADY_PENDING.toString(), "§cYou already have a pending trade offer to §7%name%§c.");
		cfg.set(Phrase.TRADE_DENY_TRY_ACCEPT_NO_OFFER.toString(), "§cYou have no pending trade offer from §7%name%§c.");
		cfg.set(Phrase.TRADE_DENY_TRY_CANCEL_NO_OFFER.toString(), "§cYou have no pending trade offer to §7%name%§c.");
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
		cfg.set(Phrase.TRADE_SENT_MESSAGE_HOVER_CANCEL.toString(), "§8Click to rescind trade offer");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_BASE.toString(), "§eYou have been invited to trade with §7%name%§e. You have §7%seconds% §eseconds to accept.");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_ACCEPT.toString(), "ACCEPT TRADE");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_DECLINE.toString(), "DECLINE TRADE");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_ACCEPT.toString(), "§8Click to accept trade");
		cfg.set(Phrase.TRADE_RECEIVED_MESSAGE_HOVER_DECLINE.toString(), "§8Click to decline trade");
		// Trade Offer Results
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_SENT.toString(), "§e%name% §aaccepted §eyour trade request.");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_ACCEPTED_RECEIVED.toString(), "§eYou §aaccepted §ethe trade request from §7%name%.");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_SENT.toString(), "§e%name% §4declined §eyour trade request.");
		cfg.set(Phrase.TRADE_OFFER_RESULT_MESSAGE_DECLINED_RECEIVED.toString(), "§eYou §4declined §ethe trade request from §7%name%.");
		cfg.set(Phrase.TRADE_OFFER_CANCELLED_SUCCESS.toString(), "§eYou §4cancelled §ethe trade request to §7%name%§e.");
		// Trade Inventory
		cfg.set(Phrase.TRADE_INVENTORY_TITLE.toString(), "§8Trade with %name%");
		cfg.set(Phrase.TRADE_INVENTORY_CONCLUSION_TITLE.toString(), "§8New Items: Trade with %name%");
		cfg.set(Phrase.TRADE_INVENTORY_CLOSE_CANCELLED_MESSAGE.toString(), "§cThe trade has been cancelled.");
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
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_AUTO_COLLECT_ITEMS_LORE.toString(), "Items are automatically added to\nyour inventory post-trade.");
		cfg.set(Phrase.TRADE_SETTINGS_INVENTORY_SET_LANGUAGE_NAME.toString(), "§eSet Language");
		// Language Inventory
		cfg.set(Phrase.TRADE_LANG_SETTINGS_INVENTORY_TITLE.toString(), "§8Language Settings | Page %page%");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_BACK_TO_SETTINGS_NAME.toString(), "§cBack to Settings");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_ADMIN_EDIT_LANGUAGE_NAME.toString(), "§eEdit Language");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_NEXT_PAGE_NAME.toString(), "§7Next Page");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_PREV_PAGE_NAME.toString(), "§7Previous Page");
		cfg.set(Phrase.TRADE_LANG_SETTINGS_CHANGED_LANGUAGE_MESSAGE.toString(), "§eChanged Language to §7%lang%§e.");
		// Edit Language Inventory
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_TITLE.toString(), "§8Editing Language §d§l%lang%");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_NAME.toString(), "§eChange Language Material");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_UPDATE_ITEM_LORE.toString(), "§7Click on this with an Item to change the material.");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_NAME.toString(), "§4§lDelete Language: %langName% | ");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_DELETE_LANG_LORE.toString(), "§c§lThis action cannot be undone.\n§c§lShift-click to delete.");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_NAME.toString(), "§c§lReset Language: %langName% | ");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_RESET_LANG_LORE.toString(), "§e§lThis action cannot be undone.\n§e§lShift-click to reset.");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_NAME.toString(), "§8§lCannot delete language: §c%lang%");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_CANNOT_DELETE_DEFAULT_LANG_LORE.toString(), "§7§lCannot delete the default language.");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_NAME.toString(), "§cBack to Languages");
		cfg.set(Phrase.LANGUAGE_EDIT_INVENTORY_BACK_TO_LANG_SETTINGS_LORE.toString(), "§7Page %page%");
		// Edit Language Messages
		cfg.set(Phrase.LANGUAGE_EDIT_MESSAGE_LANG_DELETED.toString(), "§cDeleted language §4§l%langName% §7(%lang%)§c.");
		cfg.set(Phrase.LANGUAGE_EDIT_MESSAGE_ITEM_UPDATED.toString(), "§eUpdated Item for §d§l%langName%§e.");
		cfg.set(Phrase.LANGUAGE_EDIT_MESSAGE_LANG_RESET.toString(), "§cReset language §e§l%langName% §7(%lang%)§c.");
		// XP-Trading
		
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
		return getLanguage(TradeSettings.getLanguage(uuid)).get(phrase);
	}
	public static String get(HumanEntity p, Phrase phrase, String MentionedPlayerName) { return p == null ? getPhrase(phrase, MentionedPlayerName) : get(p.getUniqueId(), phrase, MentionedPlayerName); }
	public static String get(UUID uuid, Phrase phrase, String MentionedPlayerName)
	{
		if (uuid == null) return getPhrase(phrase, MentionedPlayerName);
		return getLanguage(TradeSettings.getLanguage(uuid)).get(phrase, MentionedPlayerName);
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
		for (Phrase phrase : Phrase.values())
		{
			String s = cfg.getString(phrase.toString());
			if(s == null)
			{
				if(code.equals(DefaultLanguage)) s = DefaultValueString;
				else s = getPhrase(phrase);		// Get Default Language Phrase
				try { cfg.set(phrase.toString(), s); cfg.save(f); } catch (IOException ignored) { }
			}
			Dictionary.put(phrase, s);
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
	
}
