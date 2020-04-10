package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.database.DBFunc;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

@CommandDeclaration(command = "debugloadtest",
    permission = "plots.debugloadtest",
    description = "This debug command will force the reload of all plots in the DB",
    usage = "/plot debugloadtest",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.CONSOLE)
public class DebugLoadTest extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        PlotSquared.get().plots_tmp = DBFunc.getPlots();
        return true;
    }
}