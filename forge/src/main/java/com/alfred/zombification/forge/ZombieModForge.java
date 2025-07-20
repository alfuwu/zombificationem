package com.alfred.zombification.forge;

import com.alfred.zombification.ZombieClient;
import dev.architectury.platform.forge.EventBuses;
import com.alfred.zombification.ZombieMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ZombieMod.MOD_ID)
public class ZombieModForge {
    public ZombieModForge() {
        EventBuses.registerModEventBus(ZombieMod.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ZombieMod.init();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ZombieClient::init);
    }
}
