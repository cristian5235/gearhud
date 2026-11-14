package dev.gearhud;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class GearHudClient implements ClientModInitializer {
	public static final String MOD_ID = "gearhud";

	private static KeyMapping openEditorKey;

	@Override
	public void onInitializeClient() {
		HudConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath(MOD_ID, "main"));
		openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.gearhud.open_editor",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_K,
				category));

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(MOD_ID, "panels"),
				HudRenderer::render);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openEditorKey.consumeClick()) {
				if (client.player != null && client.screen == null) {
					client.setScreen(new HudEditorScreen());
				}
			}
		});
	}
}
