package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.database.DBFunc;
import com.plotsquared.database.MySQL;
import com.plotsquared.database.SQLManager;
import com.plotsquared.database.SQLite;
import com.plotsquared.plot.Plot;
import com.plotsquared.plot.PlotArea;
import com.plotsquared.plot.PlotId;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.plot.worlds.SinglePlotArea;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.tasks.TaskManager;
import com.plotsquared.database.Database;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

@CommandDeclaration(command = "database",
    aliases = {"convert"},
    category = CommandCategory.ADMINISTRATION,
    permission = "plots.database",
    description = "Convert/Backup Storage",
    requiredType = RequiredType.CONSOLE,
    usage = "/plot database [area] <sqlite|mysql|import>")
public class DatabaseCommand extends SubCommand {

    public static void insertPlots(final SQLManager manager, final List<Plot> plots,
        final PlotPlayer player) {
        TaskManager.runTaskAsync(() -> {
            try {
                ArrayList<Plot> ps = new ArrayList<>(plots);
                MainUtil.sendMessage(player, "&6Starting...");
                manager.createPlotsAndData(ps, () -> {
                    MainUtil.sendMessage(player, "&6Database conversion finished!");
                    manager.close();
                });
            } catch (Exception e) {
                MainUtil
                    .sendMessage(player, "Failed to insert plot objects, see stacktrace for info");
                e.printStackTrace();
            }
        });
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 1) {
            MainUtil.sendMessage(player, getUsage());
            return false;
        }
        List<Plot> plots;
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        if (area != null) {
            plots = PlotSquared.get().sortPlotsByTemp(area.getPlots());
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            plots = PlotSquared.get().sortPlotsByTemp(PlotSquared.get().getPlots());
        }
        if (args.length < 1) {
            MainUtil.sendMessage(player, getUsage());
            MainUtil.sendMessage(player, "[arg] indicates an optional argument");
            return false;
        }
        try {
            Database implementation;
            String prefix = "";
            switch (args[0].toLowerCase()) {
                case "import":
                    if (args.length < 2) {
                        MainUtil
                            .sendMessage(player, "/plot database import <sqlite file> [prefix]");
                        return false;
                    }
                    File file = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(),
                        args[1].endsWith(".db") ? args[1] : args[1] + ".db");
                    if (!file.exists()) {
                        MainUtil.sendMessage(player, "&6Database does not exist: " + file);
                        return false;
                    }
                    MainUtil.sendMessage(player, "&6Starting...");
                    implementation = new SQLite(file);
                    SQLManager manager =
                        new SQLManager(implementation, args.length == 3 ? args[2] : "", true);
                    HashMap<String, HashMap<PlotId, Plot>> map = manager.getPlots();
                    plots = new ArrayList<>();
                    for (Entry<String, HashMap<PlotId, Plot>> entry : map.entrySet()) {
                        String areaName = entry.getKey();
                        PlotArea pa = PlotSquared.get().getPlotAreaByString(areaName);
                        if (pa != null) {
                            for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                                Plot plot = entry2.getValue();
                                if (pa.getOwnedPlotAbs(plot.getId()) != null) {
                                    if (pa instanceof SinglePlotArea) {
                                        Plot newPlot = pa.getNextFreePlot(null, plot.getId());
                                        if (newPlot != null) {
                                            PlotId newId = newPlot.getId();
                                            PlotId id = plot.getId();
                                            File worldFile =
                                                new File(PlotSquared.imp().getWorldContainer(),
                                                    id.toCommaSeparatedString());
                                            if (worldFile.exists()) {
                                                File newFile =
                                                    new File(PlotSquared.imp().getWorldContainer(),
                                                        newId.toCommaSeparatedString());
                                                worldFile.renameTo(newFile);
                                            }
                                            id.x = newId.x;
                                            id.y = newId.y;
                                            id.recalculateHash();
                                            plot.setArea(pa);
                                            plots.add(plot);
                                            continue;
                                        }
                                    }
                                    MainUtil.sendMessage(player,
                                        "Skipping duplicate plot: " + plot + " | id=" + plot.temp);
                                    continue;
                                }
                                plot.setArea(pa);
                                plots.add(plot);
                            }
                        } else {
                            HashMap<PlotId, Plot> plotMap = PlotSquared.get().plots_tmp
                                .computeIfAbsent(areaName, k -> new HashMap<>());
                            plotMap.putAll(entry.getValue());
                        }
                    }
                    DBFunc.createPlotsAndData(plots,
                        () -> MainUtil.sendMessage(player, "&6Database conversion finished!"));
                    return true;
                case "mysql":
                    if (args.length < 6) {
                        return MainUtil.sendMessage(player,
                            "/plot database mysql [host] [port] [username] [password] [database] {prefix}");
                    }
                    String host = args[1];
                    String port = args[2];
                    String username = args[3];
                    String password = args[4];
                    String database = args[5];
                    if (args.length > 6) {
                        prefix = args[6];
                    }
                    implementation = new MySQL(host, port, database, username, password);
                    break;
                case "sqlite":
                    if (args.length < 2) {
                        return MainUtil.sendMessage(player, "/plot database sqlite [file]");
                    }
                    File sqliteFile =
                        MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), args[1] + ".db");
                    implementation = new SQLite(sqliteFile);
                    break;
                default:
                    return MainUtil.sendMessage(player, "/plot database [sqlite/mysql]");
            }
            try {
                SQLManager manager = new SQLManager(implementation, prefix, true);
                DatabaseCommand.insertPlots(manager, plots, player);
                return true;
            } catch (ClassNotFoundException | SQLException e) {
                MainUtil.sendMessage(player, "$1Failed to save plots, read stacktrace for info");
                MainUtil.sendMessage(player,
                    "&d==== Here is an ugly stacktrace, if you are interested in those things ===");
                e.printStackTrace();
                MainUtil.sendMessage(player, "&d==== End of stacktrace ====");
                MainUtil
                    .sendMessage(player, "$1Please make sure you are using the correct arguments!");
                return false;
            }
        } catch (ClassNotFoundException | SQLException e) {
            MainUtil.sendMessage(player, "$1Failed to open connection, read stacktrace for info");
            MainUtil.sendMessage(player,
                "&d==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            MainUtil.sendMessage(player, "&d==== End of stacktrace ====");
            MainUtil.sendMessage(player, "$1Please make sure you are using the correct arguments!");
            return false;
        }
    }
}