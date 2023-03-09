package com.github.sgtsilvio.gradle.android.retrofix

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.github.sgtsilvio.gradle.android.retrofix.backport.FutureBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.StreamsBackport
import com.github.sgtsilvio.gradle.android.retrofix.backport.TimeBackport
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper

/**
 * @author Silvio Giebl
 */
abstract class RetroFixClassVisitorFactory : AsmClassVisitorFactory<RetroFixClassVisitorFactory.Parameters> {

    interface Parameters : InstrumentationParameters {
        @get:Input
        val classList: SetProperty<String>
    }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        val classList = parameters.get().classList.get()
        val typeMap = TypeMap()
        val methodMap = MethodMap()
        for (backport in listOf(FutureBackport(), StreamsBackport(), TimeBackport())) {
            if (classList.contains(backport.indicatorClass) &&
                backport.isInstrumentable(classContext.currentClassData.className.replace('.', '/'))
            ) {
                backport.apply(typeMap, methodMap)
            }
        }
        val classRemapper = ClassRemapper(nextClassVisitor, RetroFixRemapper(typeMap))
        return RetroFixClassVisitor(classContext, methodMap, instrumentationContext.apiVersion.get(), classRemapper)
    }

    override fun isInstrumentable(classData: ClassData) = true
}
