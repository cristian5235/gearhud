package dev.gearhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

final class EffectsPanel extends HudPanel {
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFAAAAAA;
	private static final int RED = 0xFFFF5555;

	// tile style
	private static final int TILE = 26;
	private static final int TILE_GAP = 2;
	private static final int TILE_BORDER = 0xFF5A5A5A;
	private static final int TILE_BG = 0xFF1E1E1E;

	private final List<MobEffectInstance> list = new ArrayList<>();
	private static List<MobEffectInstance> sample;

	@Override
	HudConfig.PanelConfig cfg() {
		return HudConfig.get().effects;
	}

	@Override
	String label() {
		return "Effect timers";
	}

	private static List<MobEffectInstance> sampleEffects() {
		if (sample == null) {
			sample = List.of(
					new MobEffectInstance(MobEffects.SPEED, 2400, 1),
					new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0),
					new MobEffectInstance(MobEffects.NIGHT_VISION, 140, 0),
					new MobEffectInstance(MobEffects.REGENERATION, 3000, 0));
		}
		return sample;
	}

	private static String levelOf(MobEffectInstance e) {
		int amp = e.getAmplifier();
		if (amp <= 0) return "";
		return amp < 10
				? Component.translatable("enchantment.level." + (amp + 1)).getString()
				: String.valueOf(amp + 1);
	}

	private static String nameOf(MobEffectInstance e) {
		String name = e.getEffect().value().getDisplayName().getString();
		String level = levelOf(e);
		return level.isEmpty() ? name : name + " " + level;
	}

	private static int secondsOf(MobEffectInstance e) {
		return (e.getDuration() + 19) / 20;
	}

	/** m:ss, used by the list style */
	private static String timeOf(MobEffectInstance e) {
		if (e.isInfiniteDuration()) return "**:**";
		int s = secondsOf(e);
		return String.format("%d:%02d", s / 60, s % 60);
	}

	/** 45s / 3m / 1h, used by the tile style */
	private static String shortTimeOf(MobEffectInstance e) {
		if (e.isInfiniteDuration()) return "**";
		int s = secondsOf(e);
		if (s >= 3600) return (s / 3600) + "h";
		if (s >= 60) return (s / 60) + "m";
		return s + "s";
	}

	private static boolean isWarn(HudConfig.EffectsConfig c, MobEffectInstance e) {
		return c.warnSeconds > 0 && !e.isInfiniteDuration() && secondsOf(e) <= c.warnSeconds;
	}

	private int rowHeight(HudConfig.EffectsConfig c) {
		return c.showNames ? 22 : (c.showIcons ? 20 : 12);
	}

	private static void drawIcon(GuiGraphics g, MobEffectInstance e, int x, int y) {
		var key = BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value());
		if (key != null) {
			Identifier sprite = Identifier.fromNamespaceAndPath(key.getNamespace(), "mob_effect/" + key.getPath());
			g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);
		}
	}

	@Override
	boolean prepare(Minecraft mc, LocalPlayer player, boolean preview) {
		HudConfig.EffectsConfig c = HudConfig.get().effects;

		list.clear();
		list.addAll(player.getActiveEffects());
		if (list.isEmpty() && preview) list.addAll(sampleEffects());
		if (list.isEmpty()) return false;

		// shortest time first, infinite effects last
		list.sort(Comparator.comparingInt(e -> e.isInfiniteDuration() ? Integer.MAX_VALUE : e.getDuration()));

		if (c.style == HudConfig.EffectStyle.TILES) {
			int perRow = Math.max(1, c.tilesPerRow);
			int cols = Math.min(perRow, list.size());
			int rows = (list.size() + perRow - 1) / perRow;
			width = cols * TILE + (cols - 1) * TILE_GAP;
			height = rows * TILE + (rows - 1) * TILE_GAP;
			return true;
		}

		int iconW = c.showIcons ? 22 : 0;
		int maxW = 0;
		for (MobEffectInstance e : list) {
			int tw = mc.font.width(timeOf(e));
			if (c.showNames) tw = Math.max(tw, mc.font.width(nameOf(e)));
			maxW = Math.max(maxW, iconW + tw);
		}
		width = maxW;
		height = list.size() * rowHeight(c) - 2;
		return true;
	}

	@Override
	void draw(GuiGraphics g, Minecraft mc) {
		HudConfig.EffectsConfig c = HudConfig.get().effects;
		if (c.style == HudConfig.EffectStyle.TILES) {
			drawTiles(g, mc, c);
		} else {
			drawList(g, mc, c);
		}
	}

	// ---------- Tiles: small square boxes with the icon and a short time ----------

	private void drawTiles(GuiGraphics g, Minecraft mc, HudConfig.EffectsConfig c) {
		int perRow = Math.max(1, c.tilesPerRow);
		for (int i = 0; i < list.size(); i++) {
			MobEffectInstance e = list.get(i);
			int x = (i % perRow) * (TILE + TILE_GAP);
			int y = (i / perRow) * (TILE + TILE_GAP);

			// box with rounded corners
			g.fill(x + 1, y, x + TILE - 1, y + TILE, TILE_BORDER);
			g.fill(x, y + 1, x + TILE, y + TILE - 1, TILE_BORDER);
			g.fill(x + 2, y + 1, x + TILE - 2, y + TILE - 1, TILE_BG);
			g.fill(x + 1, y + 2, x + TILE - 1, y + TILE - 2, TILE_BG);

			drawIcon(g, e, x + 4, y + 2);

			String time = shortTimeOf(e);
			int timeColor = isWarn(c, e) ? RED : WHITE;
			g.drawString(mc.font, time, x + (TILE - mc.font.width(time)) / 2, y + TILE - 10, timeColor, true);

			if (c.showLevel) {
				String level = levelOf(e);
				if (!level.isEmpty()) {
					g.drawString(mc.font, level, x + TILE - 3 - mc.font.width(level), y + 3, WHITE, true);
				}
			}
		}
	}

	// ---------- List: icon, name and m:ss ----------

	private void drawList(GuiGraphics g, Minecraft mc, HudConfig.EffectsConfig c) {
		boolean mirror = c.anchorRight; // icon on the right, text right-aligned to its left
		int iconW = c.showIcons ? 22 : 0;
		int rowH = rowHeight(c);

		int y = 0;
		for (MobEffectInstance e : list) {
			if (c.showIcons) {
				drawIcon(g, e, mirror ? width - 18 : 0, y);
			}

			String time = timeOf(e);
			int timeColor = isWarn(c, e) ? RED : GRAY;
			int baseX = mirror ? width - iconW : iconW; // mirror: right edge of text area

			if (c.showNames) {
				String name = nameOf(e);
				int nx = mirror ? baseX - mc.font.width(name) : baseX;
				g.drawString(mc.font, name, nx, y + 1, WHITE, true);
				int tx = mirror ? baseX - mc.font.width(time) : baseX;
				g.drawString(mc.font, time, tx, y + 11, timeColor, true);
			} else {
				int ty = c.showIcons ? y + 5 : y + 1;
				int tx = mirror ? baseX - mc.font.width(time) : baseX;
				g.drawString(mc.font, time, tx, ty, timeColor, true);
			}
			y += rowH;
		}
	}
}
