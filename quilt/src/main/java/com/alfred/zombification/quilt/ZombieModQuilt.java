package com.alfred.zombification.quilt;

import com.alfred.zombification.fabriclike.ZombieModFabricLike;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ZombieModQuilt implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        ZombieModFabricLike.init();
    }
}
