package com.alfred.zombification.fabric;

import com.alfred.zombification.ZombieClient;
import net.fabricmc.api.ClientModInitializer;

public class ZombieClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ZombieClient.init();
    }
}
