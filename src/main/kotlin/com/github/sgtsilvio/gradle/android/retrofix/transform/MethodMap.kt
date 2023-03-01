package com.github.sgtsilvio.gradle.android.retrofix.transform

/**
 * @author Silvio Giebl
 */
class MethodMap : HashMap<String, MethodMap.Entry>() {

    fun put(methodSignature: String, type: String, replacement: String) {
        put(methodSignature, Entry(type, replacement, get(methodSignature)))
    }

    fun forType(type: String) = ForType(type)

    class Entry(
        val type: String,
        val replacement: String,
        val next: Entry?,
    )

    inner class ForType(private val type: String) {

        fun redirect(method: String, signature: String, replacement: String) =
            redirect(method, signature, replacement, method, false)

        fun redirectStatic(method: String, signature: String, replacement: String) =
            redirect(method, signature, replacement, method, true)

        fun redirectStatic(method: String, signature: String, replacement: String, newMethod: String) =
            redirect(method, signature, replacement, newMethod, true)

        private fun redirect(
            method: String,
            signature: String,
            replacement: String,
            newMethod: String,
            isStatic: Boolean,
        ): ForType {
            val r = if (signature.endsWith("V")) replacement else "\$_ = $replacement"
            put("$method $signature", type, r + "." + newMethod + "(" + (if (isStatic) "" else "$0,") + "$$);")
            return this
        }
    }
}
