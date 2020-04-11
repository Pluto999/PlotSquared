package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.plot.flags.types.ListFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BlockedCmdsFlag extends ListFlag<String, BlockedCmdsFlag> {

    public static final BlockedCmdsFlag BLOCKED_CMDS_FLAG_NONE =
        new BlockedCmdsFlag(Collections.emptyList());

    protected BlockedCmdsFlag(List<String> valueList) {
        super(valueList, Captions.FLAG_CATEGORY_STRING_LIST,
            Captions.FLAG_DESCRIPTION_BLOCKED_CMDS);
    }

    @Override public BlockedCmdsFlag parse(@NotNull String input) throws FlagParseException {
        return flagOf(Arrays.asList(input.split(",")));
    }

    @Override public String getExample() {
        return "gamemode survival, spawn";
    }

    @Override protected BlockedCmdsFlag flagOf(@NotNull List<String> value) {
        return new BlockedCmdsFlag(value);

    }

}