package dev.gearhud;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Press K in-game: drag the panels to move them, click the buttons to change settings. */
public class HudEditorScreen extends Screen {
	private static final List<Float> SCALES = List.of(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f);
	private static final List<Integer> WARN_SECONDS = List.of(0, 5, 10, 20, 30);
	private static final List<Integer> TILES_PER_ROW = List.of(2, 3, 4, 5, 6, 8);

	private static final int COL_W = 150;
	private static final int GAP = 10;
	private static final int ROW_H = 22;

	private HudPanel dragging;
	private int leftX;
	private int rightX;
	private int topY;

	public HudEditorScreen() {
		super(Component.literal("HUD Editor"));
	}

	@Override
	protected void init() {
		HudConfig cfg = HudConfig.get();
		leftX = this.width / 2 - GAP / 2 - COL_W;
		rightX = this.width / 2 + GAP / 2;
		topY = this.height / 2 - 100;

		// ----- Armor column -----
		int y = topY;
		addToggle(leftX, y, "Armor HUD", () -> cfg.armor.enabled, v -> cfg.armor.enabled = v);
		y += ROW_H;
		addToggle(leftX, y, "Main hand item", () -> cfg.armor.showMainHand, v -> cfg.armor.showMainHand = v);
		y += ROW_H;
		addToggle(leftX, y, "Offhand item", () -> cfg.armor.showOffhand, v -> cfg.armor.showOffhand = v);
		y += ROW_H;
		addCycle(leftX, y, List.of(HudConfig.BarMode.values()),
				() -> cfg.armor.bars, v -> cfg.armor.bars = v,
				v -> "Green bar: " + switch (v) {
					case DAMAGED -> "when damaged";
					case ALWAYS -> "always";
					case OFF -> "off";
				});
		y += ROW_H;
		addCycle(leftX, y, List.of(HudConfig.NumberMode.values()),
				() -> cfg.armor.numbers, v -> cfg.armor.numbers = v,
				v -> "Numbers: " + switch (v) {
					case DURABILITY -> "Durability";
					case PERCENT -> "Percent";
					case OFF -> "Off";
				});
		y += ROW_H;
		addToggle(leftX, y, "Color numbers", () -> cfg.armor.colorNumbers, v -> cfg.armor.colorNumbers = v);
		y += ROW_H;
		addToggle(leftX, y, "Total item count", () -> cfg.armor.showTotalCount, v -> cfg.armor.showTotalCount = v);
		y += ROW_H;
		addCycle(leftX, y, SCALES, () -> cfg.armor.scale, v -> cfg.armor.scale = v, v -> "Scale: " + v + "x");

		// ----- Effects column -----
		y = topY;
		addToggle(rightX, y, "Effect timers", () -> cfg.effects.enabled, v -> cfg.effects.enabled = v);
		y += ROW_H;
		addCycle(rightX, y, List.of(HudConfig.EffectStyle.values()),
				() -> cfg.effects.style, v -> cfg.effects.style = v,
				v -> "Style: " + (v == HudConfig.EffectStyle.TILES ? "Tiles" : "List"));
		y += ROW_H;
		addCycle(rightX, y, TILES_PER_ROW, () -> cfg.effects.tilesPerRow, v -> cfg.effects.tilesPerRow = v,
				v -> "Tiles per row: " + v);
		y += ROW_H;
		addToggle(rightX, y, "Tiles: level", () -> cfg.effects.showLevel, v -> cfg.effects.showLevel = v);
		y += ROW_H;
		addToggle(rightX, y, "List: icons", () -> cfg.effects.showIcons, v -> cfg.effects.showIcons = v);
		y += ROW_H;
		addToggle(rightX, y, "List: names", () -> cfg.effects.showNames, v -> cfg.effects.showNames = v);
		y += ROW_H;
		addCycle(rightX, y, WARN_SECONDS, () -> cfg.effects.warnSeconds, v -> cfg.effects.warnSeconds = v,
				v -> "Red timer: " + (v == 0 ? "never" : "under " + v + "s"));
		y += ROW_H;
		addCycle(rightX, y, SCALES, () -> cfg.effects.scale, v -> cfg.effects.scale = v, v -> "Scale: " + v + "x");

		// ----- Bottom buttons -----
		int by = topY + 8 * ROW_H + 6;
		addRenderableWidget(Button.builder(Component.literal("Reset positions"), b -> cfg.resetPositions())
				.bounds(leftX, by, COL_W, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> this.onClose())
				.bounds(rightX, by, COL_W, 20).build());
	}

	private static Component onOff(String name, boolean value) {
		return Component.literal(name + ": ")
				.append(Component.literal(value ? "ON" : "OFF")
						.withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private void addToggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
		addRenderableWidget(Button.builder(onOff(name, get.getAsBoolean()), btn -> {
			set.accept(!get.getAsBoolean());
			btn.setMessage(onOff(name, get.getAsBoolean()));
		}).bounds(x, y, COL_W, 20).build());
	}

	private <T> void addCycle(int x, int y, List<T> values, Supplier<T> get, Consumer<T> set, Function<T, String> text) {
		addRenderableWidget(Button.builder(Component.literal(text.apply(get.get())), btn -> {
			int i = values.indexOf(get.get());
			set.accept(values.get((i + 1) % values.size()));
			btn.setMessage(Component.literal(text.apply(get.get())));
		}).bounds(x, y, COL_W, 20).build());
	}

	private void centered(GuiGraphics g, Minecraft mc, String text, int cx, int y, int color) {
		g.drawString(mc.font, text, cx - mc.font.width(text) / 2, y, color, true);
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		g.fill(0, 0, this.width, this.height, 0x66000000);

		super.render(g, mouseX, mouseY, partialTick);

		for (HudPanel p : HudRenderer.PANELS) {
			if (!p.render(g, mc, true)) continue;

			boolean active = p == dragging || (dragging == null && p.contains(mouseX, mouseY));
			boolean on = p.cfg().enabled;
			int color = active ? 0xFFFFFF55 : (on ? 0xAAFFFFFF : 0x99FF5555);
			g.renderOutline(p.lastX - 2, p.lastY - 2, p.lastW + 4, p.lastH + 4, color);

			String label = p.label() + (on ? "" : " (off)");
			int ly = p.lastY >= 14 ? p.lastY - 13 : p.lastY + p.lastH + 5;
			g.drawString(mc.font, label, p.lastX, ly, color, true);
		}

		centered(g, mc, "HUD Editor - drag the panels to move them. Press ESC or Done to save.",
				this.width / 2, 6, 0xFFFFFFFF);
		centered(g, mc, "Armor HUD", leftX + COL_W / 2, topY - 12, 0xFFFFFF55);
		centered(g, mc, "Effect timers", rightX + COL_W / 2, topY - 12, 0xFFFFFF55);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		for (HudPanel p : HudRenderer.PANELS) {
			if (p.contains(event.x(), event.y())) {
				dragging = p;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (dragging != null) {
			dragging.dragBy(dx, dy);
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging != null) {
			dragging.snap(this.width, this.height);
			dragging = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void removed() {
		HudConfig.save();
		super.removed();
	}
}
