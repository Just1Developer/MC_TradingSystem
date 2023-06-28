package net.justonedeveloper.plugins.trading.language;

import net.justonedeveloper.plugins.trading.main.TradingMain;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class Language {
	
	public static final String DefaultLanguage = "en-US";
	private static final HashMap<String, Language> Languages = new HashMap<>();
	private static final HashMap<UUID, String> LanguagesPerPlayer = new HashMap<>();
	
	public static void Init()
	{
		File folder = new File(TradingMain.main.getDataFolder(), "Languages/");
		// Import all languages
		if(!folder.exists()) return;
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
		cfg.set(Phrase.TRADE_DENY_WITH_SELF.toString(), "§cYou cannot trade with yourself.");
		cfg.set(Phrase.TRADE_DENY_NO_ACCEPT.toString(), "§cThis player does not allow trade requests.");
		// Items
		cfg.set(Phrase.ITEM_NAME_CONFIRM_TRADE.toString(), "§c§lConfirm Trade");
		cfg.set(Phrase.ITEM_NAME_TRADE_CONFIRMED.toString(), "§a§lTrade confirmed");
		cfg.set(Phrase.ITEM_LORE_CLICK_TO_RESCIND.toString(), "§cClick to rescind offer");
		cfg.set(Phrase.ITEM_NAME_PARTNER_NOT_CONFIRMED.toString(), "§c§lPartner has not confirmed Trade");
		cfg.set(Phrase.ITEM_NAME_PARTNER_CONFIRMED.toString(), "§a§lPartner has confirmed Trade");
		cfg.set(Phrase.ITEM_NAME_PROCESSING_TRADE.toString(), "§b§lTrading in progress...");
		// ToDo all the others....
	}
	
	public static String get(Player p, Phrase phrase)
	{
		UUID uuid = p.getUniqueId();
		if(LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getLanguage(DefaultLanguage).get(phrase);
	}
	public static String get(Player p, Phrase phrase, String MentionedPlayerName)
	{
		UUID uuid = p.getUniqueId();
		if(LanguagesPerPlayer.containsKey(uuid)) return getLanguage(LanguagesPerPlayer.get(uuid)).get(phrase, MentionedPlayerName);
		LanguagesPerPlayer.put(uuid, DefaultLanguage);
		return getLanguage(DefaultLanguage).get(phrase, MentionedPlayerName);
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
		File f = new File(TradingMain.main.getDataFolder(), "Languages/" + code + ".yml");
		if(!f.exists()) return;
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
