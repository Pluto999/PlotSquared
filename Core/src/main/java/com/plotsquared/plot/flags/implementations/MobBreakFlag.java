package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class MobBreakFlag extends BooleanFlag<MobBreakFlag> {

    public static final MobBreakFlag MOB_BREAK_TRUE = new MobBreakFlag(true);
    public static final MobBreakFlag MOB_BREAK_FALSE = new MobBreakFlag(false);

    private MobBreakFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_MOB_BREAK);
    }

    @Override protected MobBreakFlag flagOf(@NotNull Boolean value) {
        return value ? MOB_BREAK_TRUE : MOB_BREAK_FALSE;
    }

}