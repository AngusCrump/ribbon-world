package technochip.ribbonworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkStatusTasks.class)
public class VoidTerrainMixin {
    @Inject(method = "isLighted", at = @At(value = "HEAD"))
    private static void EditChunks(
            ChunkAccess chunk, CallbackInfoReturnable<Boolean> cir
    ) {
        ChunkPos pos = chunk.getPos();
        int x = pos.x();
        int z = pos.z();

        int minz;
        int maxz;

        if (chunk.getMaxY() == 319) {
            minz = -12;
            maxz = 13;
        } else {
            minz = -8;
            maxz = 9;
        }

        if (z >= maxz || z <= minz) {
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState barrier = Blocks.BARRIER.defaultBlockState();

            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = chunk.getMinY(); by < chunk.getMaxY(); by++) {
                        BlockPos blockPos = new BlockPos(x + bx, by, z + bz);
                        if (z == minz || z == maxz) {
                            chunk.setBlockState(blockPos, barrier);
                        }
                        else {
                            chunk.setBlockState(blockPos, air);
                        }
                    }
                }
            }
        }
    }
}

