package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.config.Captions;
import com.plotsquared.database.DBFunc;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.tasks.RunnableVal2;
import com.plotsquared.util.tasks.RunnableVal3;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "add",
    description = "Allow a user to build in a plot while the plot owner is online.",
    usage = "/plot add <player|*>",
    category = CommandCategory.SETTINGS,
    permission = "plots.add",
    requiredType = RequiredType.PLAYER)
public class Add extends Command {

    public Add() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.NO_PLOT_PERMS);
        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
        final Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        checkTrue(!uuids.isEmpty(), Captions.INVALID_PLAYER, args[0]);
        Iterator<UUID> iterator = uuids.iterator();
        int size = plot.getTrusted().size() + plot.getMembers().size();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (uuid == DBFunc.EVERYONE && !(
                Permissions.hasPermission(player, Captions.PERMISSION_TRUST_EVERYONE) || Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST))) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (plot.getMembers().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            size += plot.getTrusted().contains(uuid) ? 0 : 1;
        }
        checkTrue(!uuids.isEmpty(), null);
        checkTrue(size <= plot.getArea().getMaxPlotMembers() || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.PLOT_MAX_MEMBERS);
        // Success
        confirm.run(this, () -> {
            for (UUID uuid : uuids) {
                if (uuid != DBFunc.EVERYONE) {
                    if (!plot.removeTrusted(uuid)) {
                        if (plot.getDenied().contains(uuid)) {
                            plot.removeDenied(uuid);
                        }
                    }
                }
                plot.addMember(uuid);
                PlotSquared.get().getEventDispatcher().callMember(player, plot, uuid, true);
                MainUtil.sendMessage(player, Captions.MEMBER_ADDED);
            }
        }, null);

        return CompletableFuture.completedFuture(true);
    }
}