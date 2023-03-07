package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.ClassContext
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

/**
 * @author Silvio Giebl
 */
class RetroFixClassVisitor(
    private val classContext: ClassContext,
    private val methodMap: MethodMap,
    api: Int,
    classVisitor: ClassVisitor,
) : ClassVisitor(api, classVisitor) {

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?,
    ): MethodVisitor {
//        println("visitMethod $access $name $descriptor $signature ${exceptions.contentToString()}")

        return RetroFixMethodVisitor(
            classContext,
            methodMap,
            api,
            super.visitMethod(access, name, descriptor, signature, exceptions),
        )
    }
}
