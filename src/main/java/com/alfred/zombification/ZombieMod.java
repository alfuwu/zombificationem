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

import java.util.UUID;

public class ZombieMod implements ModInitializer, EntityComponentInitializer {
	public static final ComponentKey<ZombificationComponent> ZOMBIE = ComponentRegistry.getOrCreate(identifier("zombie"), ZombificationComponent.class);
	public static final Identifier SELECT_SLOT = identifier("select_slot");
	public static final UUID ZOMBIE_SPEED_MODIFIER = UUID.fromString("121C953C-A264-423A-B6fB-C51FC5C040B9");

	@Override
	public void onInitialize() {

	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		//registry.registerFor(LivingEntity.class, ZOMBIE, ZombificationComponent::new);
		registry.beginRegistration(LivingEntity.class, ZOMBIE)
				.impl(ZombificationComponent.class)
				.respawnStrategy(RespawnCopyStrategy.CHARACTER)
				.end(ZombificationComponent::new);
	}

	public static Identifier identifier(String path) {
		return new Identifier("zombification", path);
	}
}