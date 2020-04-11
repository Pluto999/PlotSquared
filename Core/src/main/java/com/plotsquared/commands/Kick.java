package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.config.Captions;
import com.plotsquared.database.DBFunc;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;
import com.plotsquared.util.uuid.UUIDHandler;
import com.plotsquared.util.WorldUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "kick",
    aliases = "k",
    description = "Kick a player from your plot",
    permission = "plots.kick",
    usage = "/plot kick <player|*>",
    category = CommandCategory.TELEPORT,
    requiredType = RequiredType.PLAYER)
public class Kick extends SubCommand {

    public Kick() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(player.getUUID())) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_KICK)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        Set<PlotPlayer> players = new HashSet<>();
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.EVERYONE) {
                for (PlotPlayer pp : plot.getPlayersInPlot()) {
                    if (pp == player || Permissions
                        .hasPermission(pp, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                        continue;
                    }
                    players.add(pp);
                }
                continue;
            }
            PlotPlayer pp = UUIDHandler.getPlayer(uuid);
            if (pp != null) {
                players.add(pp);
            }
        }
        players.remove(player); // Don't ever kick the calling player
        if (players.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        for (PlotPlayer player2 : players) {
            if (!plot.equals(player2.getCurrentPlot())) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
                return false;
            }
            if (Permissions.hasPermission(player2, Captions.PERMISSION_ADMIN_ENTRY_DENIED)) {
                Captions.CANNOT_KICK_PLAYER.send(player, player2.getName());
                return false;
            }
            Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
            Captions.YOU_GOT_KICKED.send(player2);
            if (plot.equals(spawn.getPlot())) {
                Location newSpawn = WorldUtil.IMP
                    .getSpawn(PlotSquared.get().getPlotAreaManager().getAllWorlds()[0]);
                if (plot.equals(newSpawn.getPlot())) {
                    // Kick from server if you can't be teleported to spawn
                    player2.kick(Captions.YOU_GOT_KICKED.getTranslated());
                } else {
                    player2.plotkick(newSpawn);
                }
            } else {
                player2.plotkick(spawn);
            }
        }
        return true;
    }
}