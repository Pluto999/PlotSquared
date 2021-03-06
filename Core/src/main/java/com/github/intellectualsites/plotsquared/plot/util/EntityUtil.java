/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Entity related general utility methods
 */
@UtilityClass public final class EntityUtil {

    private static int capNumeral(@NonNull final String flagName) {
        int i;
        switch (flagName) {
            case "mob-cap":
                i = 3;
                break;
            case "hostile-cap":
                i = 2;
                break;
            case "animal-cap":
                i = 1;
                break;
            case "vehicle-cap":
                i = 4;
                break;
            case "misc-cap":
                i = 5;
                break;
            case "entity-cap":
            default:
                i = 0;
        }
        return i;
    }

    public static boolean checkEntity(Plot plot, PlotFlag<Integer, ?>... flags) {
        if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            return true;
        }
        int[] mobs = null;
        for (PlotFlag<Integer, ?> flag : flags) {
            final int i = capNumeral(flag.getName());
            int cap = plot.getFlag(flag);
            if (cap == Integer.MAX_VALUE) {
                continue;
            }
            if (cap == 0) {
                return true;
            }
            if (mobs == null) {
                mobs = plot.countEntities();
            }
            if (mobs[i] >= cap) {
                plot.setMeta("EntityCount", mobs);
                plot.setMeta("EntityCountTime", System.currentTimeMillis());
                return true;
            }
        }
        if (mobs != null) {
            for (PlotFlag<Integer, ?> flag : flags) {
                final int i = capNumeral(flag.getName());
                mobs[i]++;
            }
            plot.setMeta("EntityCount", mobs);
            plot.setMeta("EntityCountTime", System.currentTimeMillis());
        }
        return false;
    }

}
