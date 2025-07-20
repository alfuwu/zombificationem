package com.alfred.zombification;

import com.alfred.zombification.access.ZombifiableEntity;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class ZombieMod {
    public static final String MOD_ID = "zombification";
    public static final ResourceLocation SELECT_SLOT = new ResourceLocation("zombification", "select_slot");
    public static final ResourceLocation SYNC_PACKET = new ResourceLocation("zombification", "sync");
    public static final UUID ZOMBIE_SPEED_MODIFIER = UUID.fromString("121C953C-A264-423A-B6fB-C51FC5C040B9");
    
    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SYNC_PACKET, (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            UUID uuid = buf.readUUID();
            MinecraftServer server = context.getPlayer().getServer();
            if (server == null)
                return;
            ServerPlayer target = context.getPlayer().getServer().getPlayerList().getPlayer(uuid);
            if (target == null)
                return;
            NetworkManager.sendToPlayer(player, SYNC_PACKET, PlayerData.toBuf((ZombifiableEntity) target).writeUUID(uuid));
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SYNC_PACKET, (buf, context) -> {
            PlayerData data = PlayerData.fromBuf(buf);
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return;
            ZombifiableEntity player = (ZombifiableEntity) mc.level.getPlayerByUUID(buf.readUUID());
            if (player == null)
                return;
            player.setZombified(data.zombified);
            player.setUnzombifying(data.unzombifying);
            player.setConversionTimer(data.conversionTimer);
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SELECT_SLOT, (buf, context) ->
            context.getPlayer().getInventory().selected = buf.readShort());
    }

    public static void sendToAll(ServerPlayer source) {
        MinecraftServer server = source.getServer();
        if (server == null)
            return;
        server.getPlayerList().getPlayers().forEach(player ->
                NetworkManager.sendToPlayer(player, SYNC_PACKET,
                        PlayerData.toBuf((ZombifiableEntity) player).writeUUID(source.getUUID())));
    }
}
