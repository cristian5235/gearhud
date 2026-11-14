package dev.gearhud;

import java.util.List;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class HudRenderer {
	static final ArmorPanel ARMOR = new ArmorPanel();
	static final EffectsPanel EFFECTS = new EffectsPanel();
	static final List<HudPanel> PANELS = List.of(ARMOR, EFFECTS);

	private HudRenderer() {}

	public static void render(GuiGraphics g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;
		// the editor draws its own preview of the panels
		if (mc.screen instanceof HudEditorScreen) return;

		for (HudPanel panel : PANELS) {
			panel.render(g, mc, false);
		}
	}
}
