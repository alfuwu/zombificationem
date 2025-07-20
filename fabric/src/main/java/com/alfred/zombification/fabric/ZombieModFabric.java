package com.alfred.zombification.fabric;

import com.alfred.zombification.fabriclike.ZombieModFabricLike;
import net.fabricmc.api.ModInitializer;

public class ZombieModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ZombieModFabricLike.init();
    }
}
