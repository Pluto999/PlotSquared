package com.plotsquared.plot.worlds;

import com.plotsquared.PlotSquared;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import com.plotsquared.plot.PlotManager;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.SetupUtils;
import com.plotsquared.util.tasks.TaskManager;
import com.sk89q.worldedit.function.pattern.Pattern;

import java.io.File;
import java.util.List;

public class SinglePlotManager extends PlotManager {
    public SinglePlotManager(PlotArea plotArea) {
        super(plotArea);
    }

    @Override public PlotId getPlotIdAbs(int x, int y, int z) {
        return new PlotId(0, 0);
    }

    @Override public PlotId getPlotId(int x, int y, int z) {
        return new PlotId(0, 0);
    }

    @Override public Location getPlotBottomLocAbs(PlotId plotId) {
        return new Location(plotId.toCommaSeparatedString(), -30000000, 0, -30000000);
    }

    @Override public Location getPlotTopLocAbs(PlotId plotId) {
        return new Location(plotId.toCommaSeparatedString(), 30000000, 0, 30000000);
    }

    @Override public boolean clearPlot(Plot plot, final Runnable whenDone) {
        SetupUtils.manager.unload(plot.getWorldName(), false);
        final File worldFolder =
            new File(PlotSquared.get().IMP.getWorldContainer(), plot.getWorldName());
        TaskManager.IMP.taskAsync(() -> {
            MainUtil.deleteDirectory(worldFolder);
            if (whenDone != null) {
                whenDone.run();
            }
        });
        return true;
    }

    @Override public boolean claimPlot(Plot plot) {
        // TODO
        return true;
    }

    @Override public boolean unClaimPlot(Plot plot, Runnable whenDone) {
        if (whenDone != null) {
            whenDone.run();
        }
        return true;
    }

    @Override public Location getSignLoc(Plot plot) {
        return null;
    }

    @Override public String[] getPlotComponents(PlotId plotId) {
        return new String[0];
    }

    @Override public boolean setComponent(PlotId plotId, String component, Pattern blocks) {
        return false;
    }

    @Override public boolean createRoadEast(Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouth(Plot plot) {
        return false;
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadEast(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouth(Plot plot) {
        return false;
    }

    @Override public boolean removeRoadSouthEast(Plot plot) {
        return false;
    }

    @Override public boolean startPlotMerge(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean startPlotUnlink(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotMerge(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean finishPlotUnlink(List<PlotId> plotIds) {
        return false;
    }

    @Override public boolean regenerateAllPlotWalls() {
        return false;
    }
}