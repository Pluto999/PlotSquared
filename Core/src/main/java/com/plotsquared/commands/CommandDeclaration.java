package com.plotsquared.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME)
public @interface CommandDeclaration {

    String command();

    String[] aliases() default {};

    String permission() default "";

    String usage() default "";

    String description() default "";

    RequiredType requiredType() default RequiredType.NONE;

    CommandCategory category() default CommandCategory.INFO;

    boolean confirmation() default false;
}