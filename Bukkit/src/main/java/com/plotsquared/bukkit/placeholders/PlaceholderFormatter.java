package com.plotsquared.bukkit.placeholders;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.config.ChatFormatter;
import com.plotsquared.player.PlotPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderFormatter implements ChatFormatter {

    @Override public void format(final ChatContext context) {
        final PlotPlayer recipient = context.getRecipient();
        if (recipient instanceof BukkitPlayer) {
            if (context.isRawOutput()) {
                context.setMessage(context.getMessage().replace('%', '\u2010'));
            } else {
                final Player player = ((BukkitPlayer) recipient).player;
                context.setMessage(PlaceholderAPI.setPlaceholders(player, context.getMessage()));
            }
         }
    }

}