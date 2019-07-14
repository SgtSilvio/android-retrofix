package com.github.sgtsilvio.gradle.android.retrofix.transform;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author Silvio Giebl
 */
public class MethodMap extends HashMap<String, MethodMap.Entry> {

    private void put(final @NotNull String methodSignature, final @NotNull String type, final @NotNull String replacement) {
        put(methodSignature, new Entry(type, replacement, get(methodSignature)));
    }

    public @NotNull ForType forType(final @NotNull String type) {
        return new ForType(type);
    }

    public static class Entry {
        public final @NotNull String type;
        public final @NotNull String replacement;
        public final @Nullable Entry next;

        Entry(final @NotNull String type, final @NotNull String replacement, final @Nullable Entry next) {
            this.type = type;
            this.replacement = replacement;
            this.next = next;
        }
    }

    public class ForType {
        final @NotNull String type;

        ForType(final @NotNull String type) {
            this.type = type;
        }

        public @NotNull ForType redirect(
                final @NotNull String method, final @NotNull String signature, final @NotNull String replacement) {

            return redirect(method, signature, replacement, false);
        }

        public @NotNull ForType redirectStatic(
                final @NotNull String method, final @NotNull String signature, final @NotNull String replacement) {

            return redirect(method, signature, replacement, true);
        }

        private @NotNull ForType redirect(
                final @NotNull String method, final @NotNull String signature, @NotNull String replacement,
                final boolean isStatic) {

            if (!signature.endsWith("V")) {
                replacement = "$_ = " + replacement;
            }
            put(method + " " + signature, type, replacement + "." + method + "(" + (isStatic ? "" : "$0,") + "$$);");
            return this;
        }
    }
}
