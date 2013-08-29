package org.jetbrains.kannotator.main

import org.jetbrains.kannotator.index.AnnotationKeyIndex
import org.jetbrains.kannotator.declarations.Method

open class ProgressMonitor {
    open fun processingStarted() {}
    open fun annotationIndexLoaded(index: AnnotationKeyIndex) {}
    open fun methodsProcessingStarted(methodCount: Int) {}
    open fun processingComponentStarted(methods: Collection<Method>) {}
    open fun processingStepStarted(method: Method) {}
    open fun processingStepFinished(method: Method) {}
    open fun processingComponentFinished(methods: Collection<Method>) {}
    open fun processingFinished() {}
}