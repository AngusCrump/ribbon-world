package technochip.ribbonworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Comparator;
import java.util.Optional;

@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {
    @Shadow
    private ServerLevel level;

    @Shadow
    abstract boolean canPortalReplaceBlock(final BlockPos.MutableBlockPos pos);
    @Shadow
    abstract boolean canHostFrame(final BlockPos origin, final BlockPos.MutableBlockPos mutable, final Direction direction, final int offset);


    /**
     * @author technochip
     * @reason Force portals generated in overworld to be inside strip
     */
    @Overwrite
    public Optional<BlockPos> findClosestPortalPosition(final BlockPos approximateExitPos, final boolean toNether, final WorldBorder worldBorder) {
        PoiManager poiManager = this.level.getPoiManager();
        int radius = toNether ? 16 : 128;

        WorldBorder tempBorder = new WorldBorder();
        if (!toNether) {
            tempBorder.setCenter(approximateExitPos.getX(), 14);
            tempBorder.setSize(380);
        }
        poiManager.ensureLoadedAndValid(this.level, approximateExitPos, radius);
        return poiManager.getInSquare(type -> type.is(PoiTypes.NETHER_PORTAL), approximateExitPos, radius, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter((toNether ? worldBorder : tempBorder)::isWithinBounds)
                .filter(pos -> this.level.getBlockState(pos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
                .min(Comparator.<BlockPos>comparingDouble(p -> p.distSqr(approximateExitPos)).thenComparingInt(Vec3i::getY));
    }

    /**
     * @author technochip
     * @reason Force portals generated in overworld to be inside strip
     */
    @Overwrite
    public Optional<BlockUtil.FoundRectangle> createPortal(final BlockPos origin, final Direction.Axis portalAxis) {
        Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, portalAxis);
        double closestFullDistanceSqr = -1.0;
        BlockPos closestFullPosition = null;
        double closestPartialDistanceSqr = -1.0;
        BlockPos closestPartialPosition = null;
        WorldBorder worldBorder;
        if (level.dimension() == Level.OVERWORLD) {
            worldBorder = new WorldBorder();
            worldBorder.setCenter(origin.getX(), 14);
            worldBorder.setSize(380);
        } else {
            worldBorder = this.level.getWorldBorder();
        }

        int maxPlaceableY = Math.min(this.level.getMaxY(), this.level.getMinY() + this.level.getLogicalHeight() - 1);
        int edgeDistance = 1;
        BlockPos.MutableBlockPos mutable = origin.mutable();

        for (BlockPos.MutableBlockPos columnPos : BlockPos.spiralAround(origin, 16, Direction.EAST, Direction.SOUTH)) {
            int height = Math.min(maxPlaceableY, this.level.getHeight(Heightmap.Types.MOTION_BLOCKING, columnPos.getX(), columnPos.getZ()));
            if (worldBorder.isWithinBounds(columnPos) && worldBorder.isWithinBounds(columnPos.move(direction, 1))) {
                columnPos.move(direction.getOpposite(), 1);

                for (int y = height; y >= this.level.getMinY(); y--) {
                    columnPos.setY(y);
                    if (this.canPortalReplaceBlock(columnPos)) {
                        int firstEmptyY = y;

                        while (y > this.level.getMinY() && this.canPortalReplaceBlock(columnPos.move(Direction.DOWN))) {
                            y--;
                        }

                        if (y + 4 <= maxPlaceableY) {
                            int deltaY = firstEmptyY - y;
                            if (deltaY <= 0 || deltaY >= 3) {
                                columnPos.setY(y);
                                if (this.canHostFrame(columnPos, mutable, direction, 0)) {
                                    double distance = origin.distSqr(columnPos);
                                    if (this.canHostFrame(columnPos, mutable, direction, -1)
                                            && this.canHostFrame(columnPos, mutable, direction, 1)
                                            && (closestFullDistanceSqr == -1.0 || closestFullDistanceSqr > distance)) {
                                        closestFullDistanceSqr = distance;
                                        closestFullPosition = columnPos.immutable();
                                    }

                                    if (closestFullDistanceSqr == -1.0 && (closestPartialDistanceSqr == -1.0 || closestPartialDistanceSqr > distance)) {
                                        closestPartialDistanceSqr = distance;
                                        closestPartialPosition = columnPos.immutable();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (closestFullDistanceSqr == -1.0 && closestPartialDistanceSqr != -1.0) {
            closestFullPosition = closestPartialPosition;
            closestFullDistanceSqr = closestPartialDistanceSqr;
        }

        if (closestFullDistanceSqr == -1.0) {
            int minStartY = Math.max(this.level.getMinY() - -1, 70);
            int maxStartY = maxPlaceableY - 9;
            if (maxStartY < minStartY) {
                return Optional.empty();
            }

            closestFullPosition = new BlockPos(
                    origin.getX() - direction.getStepX() * 1, Mth.clamp(origin.getY(), minStartY, maxStartY), origin.getZ() - direction.getStepZ() * 1
            )
                    .immutable();
            closestFullPosition = worldBorder.clampToBounds(closestFullPosition);
            Direction clockWise = direction.getClockWise();

            for (int box = -1; box < 2; box++) {
                for (int width = 0; width < 2; width++) {
                    for (int height = -1; height < 3; height++) {
                        BlockState blockState = height < 0 ? Blocks.OBSIDIAN.defaultBlockState() : Blocks.AIR.defaultBlockState();
                        mutable.setWithOffset(
                                closestFullPosition, width * direction.getStepX() + box * clockWise.getStepX(), height, width * direction.getStepZ() + box * clockWise.getStepZ()
                        );
                        this.level.setBlockAndUpdate(mutable, blockState);
                    }
                }
            }
        }

        for (int width = -1; width < 3; width++) {
            for (int height = -1; height < 4; height++) {
                if (width == -1 || width == 2 || height == -1 || height == 3) {
                    mutable.setWithOffset(closestFullPosition, width * direction.getStepX(), height, width * direction.getStepZ());
                    this.level.setBlock(mutable, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }

        BlockState portalBlockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, portalAxis);

        for (int width = 0; width < 2; width++) {
            for (int heightx = 0; heightx < 3; heightx++) {
                mutable.setWithOffset(closestFullPosition, width * direction.getStepX(), heightx, width * direction.getStepZ());
                this.level.setBlock(mutable, portalBlockState, 18);
            }
        }

        return Optional.of(new BlockUtil.FoundRectangle(closestFullPosition.immutable(), 2, 3));
    }


}
