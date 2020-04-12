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
package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import lombok.NonNull;

import java.util.Map;

public final class BlockUtil {
    private static ParserContext PARSER_CONTEXT = new ParserContext();
    private static InputParser<BaseBlock> PARSER;

    static {
        PARSER_CONTEXT.setRestricted(false);
        PARSER_CONTEXT.setPreferringWildcard(false);
        PARSER_CONTEXT.setTryLegacy(true);
        PARSER = WorldEdit.getInstance().getBlockFactory().getParsers().get(0);
    }

    private BlockUtil() {
    }

    public static BlockState get(int id) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id);
    }

    public static BlockState get(int id, int data) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id, data);
    }

    public static BlockState get(String id) {
        if (id.length() == 1 && id.charAt(0) == '*') {
            return FuzzyBlockState.builder().type(BlockTypes.AIR).build();
        }
        id = id.toLowerCase();
        BlockType type = BlockTypes.get(id);
        if (type != null) {
            return type.getDefaultState();
        }
        if (Character.isDigit(id.charAt(0))) {
            String[] split = id.split(":");
            if (MathMan.isInteger(split[0])) {
                if (split.length == 2) {
                    if (MathMan.isInteger(split[1])) {
                        return get(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    }
                } else {
                    return get(Integer.parseInt(split[0]));
                }
            }
        }
        try {
            BaseBlock block = PARSER.parseFromInput(id, PARSER_CONTEXT);
            return block.toImmutableState();
        } catch (InputParseException e) {
            return null;
        }
    }

    public static BlockState[] parse(String commaDelimited) {
        String[] split = commaDelimited.split(",(?![^\\(\\[]*[\\]\\)])");
        BlockState[] result = new BlockState[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = get(split[i]);
        }
        return result;
    }

    public static BlockState deserialize(@NonNull final Map<String, Object> map) {
        if (map.containsKey("material")) {
            final Object object = map.get("material");
            return get(object.toString());
        }
        return null;
    }

}