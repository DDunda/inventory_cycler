package net.fabricmc.inventory_swapper;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.client.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;

@Environment(EnvType.CLIENT)
public class SwapperModClient implements ClientModInitializer {
	private static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		SwapperMod.swapPlayerInventory(client.player);
	}

	@Override
	public void onInitializeClient() {
		KeyBinding binding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Swap Inventory", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "Inventory Swapper"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while(binding.wasPressed()) {
				if (client.getNetworkHandler() != null) {
					ClientPlayNetworking.send(SwapperMod.PACKET_ID, PacketByteBufs.empty());
				}
			}
		});

		ClientPlayConnectionEvents.INIT.register((handler, server) -> {
			ClientPlayNetworking.registerReceiver(SwapperMod.RETURN_ID, SwapperModClient::receive);
		});
	}
}
