package com.alfred.zombification.forge;

import com.alfred.zombification.ZombieExpectPlatform;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ZombieExpectPlatformImpl {
    /**
     * This is our actual method to {@link ZombieExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
