package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.ClassContext
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import org.objectweb.asm.ClassVisitor

/**
 * @author Silvio Giebl
 */
class RetroFixClassVisitor(
    private val classContext: ClassContext,
    private val methodMap: MethodMap,
    api: Int,
    nextClassVisitor: ClassVisitor,
) : ClassVisitor(api, nextClassVisitor) {

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ) = RetroFixMethodVisitor(
        classContext,
        methodMap,
        api,
        super.visitMethod(access, name, descriptor, signature, exceptions),
    )
}
