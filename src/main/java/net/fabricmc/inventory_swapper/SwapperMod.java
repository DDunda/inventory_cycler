package net.fabricmc.inventory_swapper;

import net.minecraft.item.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class SwapperMod implements ModInitializer {
    public static Identifier PACKET_ID = new Identifier("inventory-swapper","swapper_press");
    public static Identifier RETURN_ID = new Identifier("inventory-swapper","swapper_return");

    static void swapPlayerInventory(PlayerEntity player) {
        var i = player.getInventory();

        if(i.isEmpty()) return;

        int o = PlayerInventory.getHotbarSize();

        boolean hotbarEmpty = true;

        do {
            for (int x = 0; x < o; x++) {
                ItemStack tmp = i.getStack(x).copy();
                i.setStack(x, i.getStack(x + o * 3));
                i.setStack(x + o * 3, i.getStack(x + o * 2));
                i.setStack(x + o * 2, i.getStack(x + o));
                i.setStack(x + o, tmp);
                if(!i.getStack(x).isEmpty()) hotbarEmpty = false;
            }
        } while(hotbarEmpty);
    }

	private static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		server.execute(() -> {
            swapPlayerInventory(player);
            ServerPlayNetworking.send(player, RETURN_ID, PacketByteBufs.empty());
		});
	}

    @Override
	public void onInitialize() {
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, PACKET_ID, SwapperMod::receive);
		});
	}
}