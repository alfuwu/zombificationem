package com.alfred.zombification;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ZombieClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ZombieMod.SELECT_SLOT, (client, networkHandler, buf, sender) ->
			client.player.getInventory().selectedSlot = buf.readShort()
		);
	}
}