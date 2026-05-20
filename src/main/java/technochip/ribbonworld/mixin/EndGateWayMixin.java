package technochip.ribbonworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import static com.mojang.text2speech.Narrator.LOGGER;

@Mixin(TheEndGatewayBlockEntity.class)
abstract class EndGateWayMixin {
    /**
     * @author me
     * @reason cos
     */
    @Overwrite
    private static Vec3 findExitPortalXZPosTentative(final ServerLevel level, final BlockPos endGatewayPos) {
        Vec3 teleportXZDirectionVector = new Vec3(endGatewayPos.getX(), 0.0, 0.0).normalize();
        int teleportDistance = 1024;
        Vec3 exitPortalXZPosTentative = teleportXZDirectionVector.scale(1024.0);

        for (int chunkLimit = 16;
             !isChunkEmpty(level, exitPortalXZPosTentative) && chunkLimit-- > 0;
             exitPortalXZPosTentative = exitPortalXZPosTentative.add(teleportXZDirectionVector.scale(-16.0))
        ) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", exitPortalXZPosTentative);
        }

        for (int var6 = 16;
             isChunkEmpty(level, exitPortalXZPosTentative) && var6-- > 0;
             exitPortalXZPosTentative = exitPortalXZPosTentative.add(teleportXZDirectionVector.scale(16.0))
        ) {
            LOGGER.debug("Skipping forward past empty chunk at {}", exitPortalXZPosTentative);
        }

        LOGGER.debug("Found chunk at {}", exitPortalXZPosTentative);
        return exitPortalXZPosTentative;
    }

    @Shadow
    private static boolean isChunkEmpty(ServerLevel level, Vec3 xzPos) {
        return false;
    }
}
