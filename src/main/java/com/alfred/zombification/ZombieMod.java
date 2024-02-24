package com.alfred.zombification;

import com.alfred.zombification.cca.ZombificationComponent;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ZombieMod implements ModInitializer, EntityComponentInitializer {
	public static final ComponentKey<ZombificationComponent> ZOMBIE = ComponentRegistry.getOrCreate(identifier("zombie"), ZombificationComponent.class);
	public static final Identifier SELECT_SLOT = identifier("select_slot");

	@Override
	public void onInitialize() {

	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.beginRegistration(LivingEntity.class, ZOMBIE)
				.impl(ZombificationComponent.class)
				.respawnStrategy(RespawnCopyStrategy.CHARACTER)
				.end(ZombificationComponent::new);
	}

	public static Identifier identifier(String path) {
		return new Identifier("zombification", path);
	}
}