package technochip.ribbonworld;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RibbonWorld implements ModInitializer {
	public static final String MOD_ID = "ribbon-world";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerTickEvents.START_LEVEL_TICK.register(level -> {
			// Only run on the server side

			if (!level.isClientSide()) {
				try {
					ArrayList<Entity> toKill = new ArrayList<>();
					for (Entity entity : level.getAllEntities()) {
						if (entity != null && entity.isAlive() && !entity.isRemoved()) {
							if (level.dimension() == Level.OVERWORLD) {
								if (!(entity instanceof Player) && (entity.getZ() < -176 || entity.getZ() > 208)) {
									toKill.add(entity);
								}
							} else {
								if (!(entity instanceof Player) && (entity.getZ() < -112 || entity.getZ() > 144)) {
									toKill.add(entity);
								}
							}
						}
					}
					for (Entity entity : toKill) {
						entity.kill(level);
					}
				} finally {

				}
			}
		});
	}
}