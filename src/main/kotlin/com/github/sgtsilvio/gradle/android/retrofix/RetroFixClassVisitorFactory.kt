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
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper

/**
 * @author Silvio Giebl
 */
abstract class RetroFixClassVisitorFactory : AsmClassVisitorFactory<RetroFixClassVisitorFactory.Parameters> {

    interface Parameters : InstrumentationParameters {

    }

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
//        print("cCV ")
//        System.out.flush()
//        println(classContext.loadClassData("java.util.HashMap"))
//        println(classContext)
//        if (classContext.currentClassData.className == "com.hivemq.client.hivemq_mqtt_client_app.MainActivity") {
////            println("HEY")
//            return RetroFixClassVisitor(classContext, TypeMap(), instrumentationContext.apiVersion.get(), nextClassVisitor)
//        }
        val typeMap = TypeMap()
        val methodMap = MethodMap()
        FutureBackport().apply(typeMap, methodMap)
        StreamsBackport().apply(typeMap, methodMap)
        TimeBackport().apply(typeMap, methodMap)
        if (typeMap.values.contains(classContext.currentClassData.className)) { // TODO not working, typeMap.values not enough
            return nextClassVisitor
        }
        val classRemapper = ClassRemapper(nextClassVisitor, RetroFixRemapper(typeMap))
        return RetroFixClassVisitor(classContext, methodMap, instrumentationContext.apiVersion.get(), classRemapper)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
//        print("iI ")
//        System.out.flush()
//        return true
        return !classData.className.startsWith("java9.") && !classData.className.startsWith("org.threeten.bp.")
    }
}
