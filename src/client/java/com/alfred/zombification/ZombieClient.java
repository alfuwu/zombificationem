package com.alfred.zombification;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.mob.*;
import net.minecraft.util.Identifier;

public class ZombieClient implements ClientModInitializer {
	public static final Identifier ZOMBIE_VISION = ZombieMod.identifier("shaders/post/zombie_vision.json");
	private static boolean wasZombified = false;

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ZombieMod.SELECT_SLOT, (client, networkHandler, buf, sender) ->
			client.player.getInventory().selectedSlot = buf.readShort()
		);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean isZombified = (client.getCameraEntity() != null && (ZombieMod.ZOMBIE.get(client.getCameraEntity()).isZombified() || client.getCameraEntity() instanceof ZombieEntity || client.getCameraEntity() instanceof ZombieHorseEntity)) || (client.getCameraEntity() == null && client.player != null && ZombieMod.ZOMBIE.get(client.player).isZombified());
			if (!wasZombified && isZombified) {
				client.gameRenderer.disablePostProcessor();
				client.gameRenderer.loadPostProcessor(ZOMBIE_VISION);
			} else if (wasZombified && !isZombified && !(client.getCameraEntity() instanceof CreeperEntity || client.getCameraEntity() instanceof SpiderEntity || client.getCameraEntity() instanceof EndermanEntity)) {
				client.gameRenderer.disablePostProcessor();
			}
			wasZombified = isZombified;
		});
	}
}