package com.alfred.zombification;

import com.alfred.zombification.access.ZombifiableEntity;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieClient {
    public static final ResourceLocation ZOMBIE_VISION = new ResourceLocation("zombification", "shaders/post/zombie_vision.json");
    private static boolean wasZombified = false;

    public static void init() {
        EntityEvent.ADD.register(((entity, world) -> {
            if (entity instanceof AbstractClientPlayer player)
                NetworkManager.sendToServer(ZombieMod.SYNC_PACKET, new FriendlyByteBuf(Unpooled.buffer()).writeUUID(player.getUUID()));
            return EventResult.pass();
        }));
        System.out.println("client initialized");
    }

    public static void tick(Minecraft client) {
        Entity cam = client.getCameraEntity();
        boolean zombified = cam != null && (cam instanceof ZombifiableEntity zomb && zomb.isZombified() || cam instanceof Zombie || cam instanceof ZombieHorse) || cam == null && client.player != null && ((ZombifiableEntity) client.player).isZombified();
        if (!wasZombified && zombified) {
            client.gameRenderer.shutdownEffect();
            client.gameRenderer.loadEffect(ZOMBIE_VISION);
        } else if (wasZombified && !zombified && !(cam instanceof Creeper || cam instanceof Spider || cam instanceof EnderMan)) {
            client.gameRenderer.shutdownEffect();
        }
    }
}
