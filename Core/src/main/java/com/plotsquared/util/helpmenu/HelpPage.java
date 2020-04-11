package com.plotsquared.util.helpmenu;

import com.plotsquared.commands.CommandCategory;
import com.plotsquared.config.Captions;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.StringMan;

import java.util.ArrayList;
import java.util.List;

public class HelpPage {

    private final List<HelpObject> helpObjects;
    private final String header;

    public HelpPage(CommandCategory category, int currentPage, int maxPages) {
        this.helpObjects = new ArrayList<>();
        this.header = Captions.HELP_PAGE_HEADER.getTranslated()
            .replace("%category%", category == null ? "ALL" : category.toString())
            .replace("%current%", (currentPage + 1) + "").replace("%max%", (maxPages + 1) + "");
    }

    public void render(PlotPlayer player) {
        if (this.helpObjects.size() < 1) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_NUMBER, "(0)");
        } else {
            String message =
                Captions.HELP_HEADER.getTranslated() + "\n" + this.header + "\n" + StringMan
                    .join(this.helpObjects, "\n") + "\n" + Captions.HELP_FOOTER.getTranslated();
            MainUtil.sendMessage(player, message, false);
        }
    }

    public void addHelpItem(HelpObject object) {
        this.helpObjects.add(object);
    }
}