package net.justonedeveloper.plugins.trading.main;

import net.justonedeveloper.plugins.trading.language.Language;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GlobalConfig {
	
	static File f = new File(TradingMain.main.getDataFolder(), "config.yml");
	
	public static boolean EnableXPTrading;
	public static Material DefaultLanguageItem;
	
	private static final String DefaultLanguage = "en-US";
	private static final Material DefaultMaterial = Material.DARK_OAK_SIGN;
	private static final boolean DefaultXPTradingEnabled = true;
	
	public static void LoadConfig()
	{
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
		if(!f.exists())
		{
			cfg.options().copyDefaults(true);
			cfg.addDefault("Default Language", "en-US");
			cfg.addDefault("Default Language Item", Material.DARK_OAK_SIGN.toString());
			cfg.addDefault("Enable XP-Trading", true);
			saveCfg(cfg);
			Language.DefaultLanguage = DefaultLanguage;
			DefaultLanguageItem = DefaultMaterial;
			EnableXPTrading = DefaultXPTradingEnabled;
			return;
		}
		
		Map<String, Object> updateThese = new HashMap<>();
		
		Language.DefaultLanguage = getOrDefaultString("Default Language", cfg, updateThese);
		DefaultLanguageItem = getOrDefaultMaterial("Default Language Item", cfg, updateThese);
		EnableXPTrading = getOrDefaultBoolean("Enable XP-Trading", cfg, updateThese);
		
		if (updateThese.isEmpty()) return;
		cfg = YamlConfiguration.loadConfiguration(f);	// Reload config
		for (Map.Entry<String, Object> entry : updateThese.entrySet()) {
			cfg.set(entry.getKey(), entry.getValue());
		}
		saveCfg(cfg);
	}
	
	private static String getOrDefaultString(String key, YamlConfiguration cfg, Map<String, Object> updateThese)
	{
		String result = cfg.getString(key);
		if(result == null || Language.isInvalidLanguageCode(key))
		{
			result = DefaultLanguage;
			updateThese.put(key, result);
		}
		return result;
	}
	
	private static Material getOrDefaultMaterial(String key, YamlConfiguration cfg, Map<String, Object> updateThese)
	{
		String mat = cfg.getString(key);
		try {
			return Material.valueOf(mat);
		} catch (Exception any) {
			updateThese.put(key, DefaultMaterial.toString());
			return DefaultMaterial;
		}
	}
	
	private static boolean getOrDefaultBoolean(String key, YamlConfiguration cfg, Map<String, Object> updateThese)
	{
		if (cfg.isSet(key)) return cfg.getBoolean(key);
		updateThese.put(key, DefaultXPTradingEnabled);
		return DefaultXPTradingEnabled;
	}
	
	public static void saveCfg(YamlConfiguration cfg)
	{
		try {
			cfg.save(f);
		} catch(IOException ignored) { }
	}
	
}
