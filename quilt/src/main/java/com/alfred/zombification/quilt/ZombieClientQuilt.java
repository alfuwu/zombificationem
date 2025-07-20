package com.alfred.zombification.quilt;

import com.alfred.zombification.ZombieClient;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ZombieClientQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        ZombieClient.init();
    }
}
