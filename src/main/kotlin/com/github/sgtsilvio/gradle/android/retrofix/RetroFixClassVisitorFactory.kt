package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.github.sgtsilvio.gradle.android.retrofix.backport.Backport
import com.github.sgtsilvio.gradle.android.retrofix.transform.ClassMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper

/**
 * @author Silvio Giebl
 */
abstract class RetroFixClassVisitorFactory : AsmClassVisitorFactory<RetroFixClassVisitorFactory.Parameters> {

    interface Parameters : InstrumentationParameters {
        @get:Input
        val backportList: ListProperty<Backport>
    }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        val backportList = parameters.get().backportList.get()
        val classMap = ClassMap()
        val methodMap = MethodMap()
        for (backport in backportList) {
            if (backport.isInstrumentable(classContext.currentClassData.className.replace('.', '/'))) {
                backport.apply(classMap, methodMap)
            }
        }
        val classMapper = ClassRemapper(nextClassVisitor, RetroFixClassMapper(classMap))
        return RetroFixClassVisitor(classContext, methodMap, instrumentationContext.apiVersion.get(), classMapper)
    }

    override fun isInstrumentable(classData: ClassData) = true
}
