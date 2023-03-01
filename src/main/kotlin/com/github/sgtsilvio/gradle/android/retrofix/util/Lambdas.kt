package com.github.sgtsilvio.gradle.android.retrofix.util

import javassist.expr.ExprEditor
import javassist.expr.MethodCall

fun methodEditor(consumer: (MethodCall, Int) -> Unit): ExprEditor {
    return object : ExprEditor() {
        private var c = 0

        override fun edit(m: MethodCall) {
            try {
                consumer.invoke(m, c)
                c++
            } catch (throwable: Throwable) {
                throw RuntimeException(throwable)
            }
        }
    }
}
