package com.github.sgtsilvio.gradle.android.retrofix.util;

import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Silvio Giebl
 */
public class Lambdas {

    public interface EConsumer<T> {

        void accept(@NotNull T t) throws Throwable;
    }

    public interface EPredicate<T> {

        boolean test(@NotNull T t) throws Throwable;
    }

    public interface EObjIntConsumer<T> {

        void accept(@NotNull T t, int value) throws Throwable;
    }

    public static <T> @NotNull Consumer<T> consumer(final @NotNull EConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    public static <T> @NotNull Predicate<T> predicate(final @NotNull EPredicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (final Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    public static @NotNull ExprEditor methodEditor(final @NotNull EObjIntConsumer<MethodCall> consumer) {
        return new ExprEditor() {
            private int c;

            @Override
            public void edit(final @NotNull MethodCall m) {
                try {
                    consumer.accept(m, c);
                    c++;
                } catch (final Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        };
    }
}
