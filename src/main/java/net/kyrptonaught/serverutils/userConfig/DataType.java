package net.kyrptonaught.serverutils.userConfig;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

public abstract class DataType<T> {

    public final String cmdName;

    public DataType(String cmdName) {
        this.cmdName = cmdName;
    }

    public abstract int compare(T obj1, T obj2);

    public abstract ArgumentType<T> getArgumentType();

    public abstract T getArgument(CommandContext<?> context, final String name);

    public boolean canCompareInequalities() {
        return false;
    }

    public static class IntType extends DataType<Integer> {

        public IntType() {
            super("INT");
        }

        @Override
        public int compare(Integer obj1, Integer obj2) {
            return Integer.compare(obj1, obj2);
        }

        @Override
        public ArgumentType<Integer> getArgumentType() {
            return IntegerArgumentType.integer();
        }

        @Override
        public Integer getArgument(CommandContext<?> context, String name) {
            return IntegerArgumentType.getInteger(context, name);
        }

        @Override
        public boolean canCompareInequalities() {
            return true;
        }
    }

    public static class StringType extends DataType<String> {
        public StringType() {
            super("STRING");
        }

        @Override
        public int compare(String obj1, String obj2) {
            return obj1.equals(obj2) ? 0 : 1;
        }

        @Override
        public ArgumentType<String> getArgumentType() {
            return StringArgumentType.word();
        }

        @Override
        public String getArgument(CommandContext<?> context, String name) {
            return StringArgumentType.getString(context, name);
        }
    }

    public static class BoolType extends DataType<Boolean> {
        public BoolType() {
            super("BOOL");
        }

        @Override
        public int compare(Boolean obj1, Boolean obj2) {
            return obj1.equals(obj2) ? 0 : 1;
        }

        @Override
        public ArgumentType<Boolean> getArgumentType() {
            return BoolArgumentType.bool();
        }

        @Override
        public Boolean getArgument(CommandContext<?> context, String name) {
            return BoolArgumentType.getBool(context, name);
        }
    }
}
