package com.github.intellectualsites.plotsquared.events;

import com.github.intellectualsites.plotsquared.commands.Claim;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;

import javax.annotation.Nullable;

public class PlayerClaimPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private Result eventResult;
    private String schematic;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player    Player that claimed the plot
     * @param plot      Plot that was claimed
     * @param schematic The schematic defined or null
     */
    public PlayerClaimPlotEvent(PlotPlayer player, Plot plot, @Nullable String schematic) {
        super(player, plot);
        this.schematic = schematic;
    }

    /**
     * Obtain the schematic string as used by the {@link Claim} command or null.
     *
     * @return schematic string
     */
    @Nullable public String getSchematic() {
        return this.schematic;
    }

    /**
     * Set the schematic string used in the claim.
     */
    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}