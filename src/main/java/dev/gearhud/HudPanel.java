package dev.gearhud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/** A draggable block on the HUD. Content is drawn at (0,0); the panel handles position and scale. */
abstract class HudPanel {
	/** Unscaled content size, set by prepare() */
	protected int width;
	protected int height;

	/** Where it was last drawn on screen (used by the editor for dragging) */
	int lastX, lastY, lastW, lastH;

	abstract HudConfig.PanelConfig cfg();

	abstract String label();

	/** Collect data and set width/height. Return false if there is nothing to draw. */
	abstract boolean prepare(Minecraft mc, LocalPlayer player, boolean preview);

	/** Draw at (0,0) in local (unscaled) coordinates. */
	abstract void draw(GuiGraphics g, Minecraft mc);

	private float scale() {
		return Math.max(0.25f, Math.min(4f, cfg().scale));
	}

	int scaledW() {
		return Math.max(1, Math.round(width * scale()));
	}

	int scaledH() {
		return Math.max(1, Math.round(height * scale()));
	}

	int posX(int screenW) {
		HudConfig.PanelConfig c = cfg();
		int x = Math.round(c.anchorRight ? screenW - scaledW() - c.offX : c.offX);
		return Math.max(0, Math.min(x, Math.max(0, screenW - scaledW())));
	}

	int posY(int screenH) {
		HudConfig.PanelConfig c = cfg();
		int y = Math.round(c.anchorBottom ? screenH - scaledH() - c.offY : c.offY);
		return Math.max(0, Math.min(y, Math.max(0, screenH - scaledH())));
	}

	boolean render(GuiGraphics g, Minecraft mc, boolean preview) {
		LocalPlayer player = mc.player;
		if (player == null) return false;
		HudConfig.PanelConfig c = cfg();
		if (!c.enabled && !preview) return false;
		if (!prepare(mc, player, preview)) return false;

		int x = posX(g.guiWidth());
		int y = posY(g.guiHeight());
		lastX = x;
		lastY = y;
		lastW = scaledW();
		lastH = scaledH();

		g.pose().pushMatrix();
		g.pose().translate((float) x, (float) y);
		g.pose().scale(scale(), scale());
		draw(g, mc);
		g.pose().popMatrix();
		return true;
	}

	boolean contains(double mx, double my) {
		return mx >= lastX && mx < lastX + lastW && my >= lastY && my < lastY + lastH;
	}

	void dragBy(double dx, double dy) {
		HudConfig.PanelConfig c = cfg();
		c.offX += (float) (c.anchorRight ? -dx : dx);
		c.offY += (float) (c.anchorBottom ? -dy : dy);
	}

	/** After dropping: attach to the nearest corner so it stays there on other window sizes. */
	void snap(int screenW, int screenH) {
		HudConfig.PanelConfig c = cfg();
		int w = scaledW();
		int h = scaledH();
		int x = posX(screenW);
		int y = posY(screenH);
		c.anchorRight = x + w / 2 > screenW / 2;
		c.anchorBottom = y + h / 2 > screenH / 2;
		c.offX = c.anchorRight ? screenW - w - x : x;
		c.offY = c.anchorBottom ? screenH - h - y : y;
	}
}
