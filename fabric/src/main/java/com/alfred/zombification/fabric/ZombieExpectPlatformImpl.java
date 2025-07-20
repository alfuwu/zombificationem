package com.alfred.zombification.fabric;

import com.alfred.zombification.ZombieExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ZombieExpectPlatformImpl {
    /**
     * This is our actual method to {@link ZombieExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
