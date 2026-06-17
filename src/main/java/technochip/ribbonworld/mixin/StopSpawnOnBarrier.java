package technochip.ribbonworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerSpawnFinder.class)
public class StopSpawnOnBarrier {
    /**
     * @author technochip
     * @reason Rewrite to return null if topY == 318,
     * as this means spawn is on barrier roof.
     */
    @Overwrite
    protected static BlockPos getLevelRespawnPos(final ServerLevel level, final int x, final int z) {
        boolean caveWorld = level.dimensionType().hasCeiling();
        LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int topY = caveWorld ? level.getChunkSource().getGenerator().getSpawnHeight(level) : chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
        if (topY < level.getMinY() || topY == 318) {
            return null;
        }

        int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
        if (surface <= topY && surface > chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x & 15, z & 15)) {
            return null;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int y = topY + 1; y >= level.getMinY(); y--) {
            pos.set(x, y, z);
            BlockState blockState = level.getBlockState(pos);
            if (!blockState.getFluidState().isEmpty()) {
                break;
            }

            if (Block.isFaceFull(blockState.getCollisionShape(level, pos), Direction.UP)) {
                return pos.above().immutable();
            }
        }

        return null;

    }

}
