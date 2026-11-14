package dev.gearhud;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class ArmorPanel extends HudPanel {
	private static final EquipmentSlot[] ARMOR_SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};
	private static final int ROW = 19;
	private static final int WHITE = 0xFFFFFFFF;

	private record Row(ItemStack stack, String text, int color) {}

	private final List<Row> rows = new ArrayList<>();
	private static List<ItemStack> sample;

	@Override
	HudConfig.PanelConfig cfg() {
		return HudConfig.get().armor;
	}

	@Override
	String label() {
		return "Armor HUD";
	}

	private static List<ItemStack> sampleStacks() {
		if (sample == null) {
			sample = new ArrayList<>();
			Item[] items = {
					Items.NETHERITE_SWORD, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
					Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.SHIELD
			};
			for (Item item : items) {
				ItemStack s = new ItemStack(item);
				s.setDamageValue(s.getMaxDamage() / 6);
				sample.add(s);
			}
		}
		return sample;
	}

	@Override
	boolean prepare(Minecraft mc, LocalPlayer player, boolean preview) {
		HudConfig.ArmorConfig c = HudConfig.get().armor;

		List<ItemStack> stacks = new ArrayList<>();
		if (c.showMainHand) stacks.add(player.getMainHandItem());
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			stacks.add(player.getItemBySlot(slot));
		}
		if (c.showOffhand) stacks.add(player.getOffhandItem());
		stacks.removeIf(ItemStack::isEmpty);

		if (stacks.isEmpty() && preview) {
			List<ItemStack> all = sampleStacks();
			for (int i = 0; i < all.size(); i++) {
				if (i == 0 && !c.showMainHand) continue;
				if (i == all.size() - 1 && !c.showOffhand) continue;
				stacks.add(all.get(i));
			}
		}
		if (stacks.isEmpty()) return false;

		rows.clear();
		int maxText = 0;
		for (ItemStack s : stacks) {
			String text = null;
			int color = WHITE;
			if (c.numbers != HudConfig.NumberMode.OFF) {
				if (s.isDamageableItem()) {
					int left = s.getMaxDamage() - s.getDamageValue();
					text = c.numbers == HudConfig.NumberMode.PERCENT
							? Math.round(100f * left / s.getMaxDamage()) + "%"
							: String.valueOf(left);
					if (c.colorNumbers) color = 0xFF000000 | s.getBarColor();
				} else if (s.getCount() > 1) {
					text = String.valueOf(s.getCount());
				}
			}
			if (text != null) maxText = Math.max(maxText, mc.font.width(text));
			rows.add(new Row(s, text, color));
		}

		width = 16 + (maxText > 0 ? 3 + maxText : 0);
		height = rows.size() * ROW - 3;
		return true;
	}

	@Override
	void draw(GuiGraphics g, Minecraft mc) {
		HudConfig.ArmorConfig c = HudConfig.get().armor;
		boolean mirror = c.anchorRight; // icon on the right, numbers to its left

		int y = 0;
		for (Row r : rows) {
			ItemStack s = r.stack();
			int iconX = mirror ? width - 16 : 0;
			g.renderItem(s, iconX, y);

			if (r.text() != null) {
				int tw = mc.font.width(r.text());
				int tx = mirror ? iconX - 3 - tw : 19;
				g.drawString(mc.font, r.text(), tx, y + 4, r.color(), true);
			}

			if (c.showBars && s.isDamageableItem()) {
				int left = s.getMaxDamage() - s.getDamageValue();
				int fill = Math.round(13f * left / s.getMaxDamage());
				g.fill(iconX + 2, y + 16, iconX + 15, y + 18, 0xFF000000);
				g.fill(iconX + 2, y + 16, iconX + 2 + fill, y + 17, 0xFF000000 | s.getBarColor());
			}
			y += ROW;
		}
	}
}
