package dev.gearhud;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

/** Saved to .minecraft/config/gearhud.json */
public final class HudConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(GearHudClient.MOD_ID);
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static HudConfig instance = new HudConfig();

	public enum NumberMode { DURABILITY, PERCENT, OFF }

	public static class PanelConfig {
		public boolean enabled = true;
		public float scale = 1.0f;
		/** true = offsets are measured from the right edge, false = from the left edge */
		public boolean anchorRight;
		/** true = offsets are measured from the bottom edge, false = from the top edge */
		public boolean anchorBottom;
		public float offX;
		public float offY;

		public void place(boolean right, boolean bottom, float x, float y) {
			anchorRight = right;
			anchorBottom = bottom;
			offX = x;
			offY = y;
		}
	}

	public static class ArmorConfig extends PanelConfig {
		public boolean showMainHand = true;
		public boolean showOffhand = true;
		public boolean showBars = true;
		public boolean colorNumbers = false;
		public NumberMode numbers = NumberMode.DURABILITY;

		public ArmorConfig() {
			place(true, true, 4, 4); // bottom right
		}
	}

	public static class EffectsConfig extends PanelConfig {
		public boolean showIcons = true;
		public boolean showNames = true;
		/** Timer turns red at or below this many seconds (0 = never) */
		public int warnSeconds = 10;

		public EffectsConfig() {
			place(false, false, 4, 4); // top left
		}
	}

	public ArmorConfig armor = new ArmorConfig();
	public EffectsConfig effects = new EffectsConfig();

	public void resetPositions() {
		armor.place(true, true, 4, 4);
		effects.place(false, false, 4, 4);
	}

	public static HudConfig get() {
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(GearHudClient.MOD_ID + ".json");
	}

	public static void load() {
		Path path = path();
		if (!Files.exists(path)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			HudConfig loaded = GSON.fromJson(reader, HudConfig.class);
			if (loaded != null) {
				if (loaded.armor == null) loaded.armor = new ArmorConfig();
				if (loaded.effects == null) loaded.effects = new EffectsConfig();
				instance = loaded;
			}
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Could not read {}, using defaults", path, e);
		}
	}

	public static void save() {
		try (Writer writer = Files.newBufferedWriter(path())) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			LOGGER.warn("Could not save config", e);
		}
	}
}
