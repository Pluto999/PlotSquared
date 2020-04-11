package com.plotsquared.generator;

import com.plotsquared.PlotSquared;
import com.plotsquared.location.Location;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotAreaTerrainType;
import com.plotsquared.plot.PlotAreaType;
import com.plotsquared.plot.PlotManager;
import com.plotsquared.queue.DelegateLocalBlockQueue;
import com.plotsquared.queue.GlobalBlockQueue;
import com.plotsquared.queue.LocalBlockQueue;
import com.plotsquared.queue.ScopedLocalBlockQueue;
import com.plotsquared.util.RegionUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class AugmentedUtils {

    private static boolean enabled = true;

    public static void bypass(boolean bypass, Runnable run) {
        enabled = bypass;
        run.run();
        enabled = true;
    }

    public static boolean generate(@Nullable Object chunkObject, @NotNull final String world, final int chunkX, final int chunkZ,
        LocalBlockQueue queue) {
        if (!enabled) {
            return false;
        }

        final int blockX = chunkX << 4;
        final int blockZ = chunkZ << 4;
        CuboidRegion region = RegionUtil.createRegion(blockX, blockX + 15, blockZ, blockZ + 15);
        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(world, region);
        if (areas.isEmpty()) {
            return false;
        }
        boolean toReturn = false;
        for (final PlotArea area : areas) {
            if (area.getType() == PlotAreaType.NORMAL) {
                return false;
            }
            if (area.getTerrain() == PlotAreaTerrainType.ALL) {
                continue;
            }
            IndependentPlotGenerator generator = area.getGenerator();
            // Mask
            if (queue == null) {
                queue = GlobalBlockQueue.IMP.getNewQueue(world, false);
                queue.setChunkObject(chunkObject);
            }
            LocalBlockQueue primaryMask;
            // coordinates
            int bxx;
            int bzz;
            int txx;
            int tzz;
            // gen
            if (area.getType() == PlotAreaType.PARTIAL) {
                bxx = Math.max(0, area.getRegion().getMinimumPoint().getX() - blockX);
                bzz = Math.max(0, area.getRegion().getMinimumPoint().getZ() - blockZ);
                txx = Math.min(15, area.getRegion().getMaximumPoint().getX() - blockX);
                tzz = Math.min(15, area.getRegion().getMaximumPoint().getZ() - blockZ);
                primaryMask = new DelegateLocalBlockQueue(queue) {
                    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
                        if (area.contains(x, z)) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBiome(int x, int z, BiomeType biome) {
                        if (area.contains(x, z)) {
                            return super.setBiome(x, z, biome);
                        }
                        return false;
                    }
                };
            } else {
                bxx = bzz = 0;
                txx = tzz = 15;
                primaryMask = queue;
            }
            LocalBlockQueue secondaryMask;
            BlockState air = BlockTypes.AIR.getDefaultState();
            if (area.getTerrain() == PlotAreaTerrainType.ROAD) {
                PlotManager manager = area.getPlotManager();
                final boolean[][] canPlace = new boolean[16][16];
                boolean has = false;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        int rx = x + blockX;
                        int rz = z + blockZ;
                        boolean can = manager.getPlotId(rx, 0, rz) == null;
                        if (can) {
                            for (int y = 1; y < 128; y++) {
                                queue.setBlock(rx, y, rz, air);
                            }
                            canPlace[x][z] = true;
                            has = true;
                        }
                    }
                }
                if (!has) {
                    continue;
                }
                toReturn = true;
                secondaryMask = new DelegateLocalBlockQueue(primaryMask) {
                    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
                        if (canPlace[x - blockX][z - blockZ]) {
                            return super.setBlock(x, y, z, id);
                        }
                        return false;
                    }

                    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
                        try {
                            if (canPlace[x - blockX][z - blockZ]) {
                                return super.setBlock(x, y, z, id);
                            }
                        } catch (final Exception e) {
                            PlotSquared.debug(String.format("Failed to set block at: %d;%d;%d (to = %s) with offset %d;%d."
                                + " Translated to: %d;%d", x, y, z, id, blockX, blockZ, x - blockX, z - blockZ));
                            throw e;
                        }
                        return false;
                    }

                    @Override public boolean setBlock(int x, int y, int z, Pattern pattern) {
                        final BlockVector3 blockVector3 = BlockVector3.at(x + blockX, y, z + blockZ);
                        return this.setBlock(x, y, z, pattern.apply(blockVector3));
                    }

                    @Override public boolean setBiome(int x, int y, BiomeType biome) {
                        return super.setBiome(x, y, biome);
                    }
                };
            } else {
                secondaryMask = primaryMask;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        for (int y = 1; y < 128; y++) {
                            queue.setBlock(blockX + x, y, blockZ + z, air);
                        }
                    }
                }
                toReturn = true;
            }
            primaryMask.setChunkObject(chunkObject);
            primaryMask.setForceSync(true);
            secondaryMask.setChunkObject(chunkObject);
            secondaryMask.setForceSync(true);

            ScopedLocalBlockQueue scoped = new ScopedLocalBlockQueue(secondaryMask, new Location(world, blockX, 0, blockZ),
                new Location(world, blockX + 15, 255, blockZ + 15));
            generator.generateChunk(scoped, area);
            generator.populateChunk(scoped, area);
        }
        if (queue != null) {
            queue.setForceSync(true);
            queue.flush();
        }
        return toReturn;
    }
}