package org.jetbrains.kannotator.main

import kotlinlib.*
import org.jetbrains.kannotator.annotations.io.parseAnnotations
import org.jetbrains.kannotator.declarations.*
import org.jetbrains.kannotator.index.AnnotationKeyIndex
import org.jetbrains.kannotator.controlFlow.builder.analysis.*
import org.jetbrains.kannotator.runtime.annotations.AnalysisType
import org.jetbrains.kannotator.ErrorHandler
import java.io.Reader

public fun <K : AnalysisType> loadExternalAnnotations(
        delegatingAnnotations: Map<K, Annotations<Any>>,
        annotationsInXml: Collection<() -> Reader>,
        keyIndex: AnnotationKeyIndex,
        inferrers: Map<K, AnnotationInferrer<Any, Qualifier>>,
        errorHandler: ErrorHandler
): Map<K, MutableAnnotations<Any>> {
    val externalAnnotationsMap = inferrers.mapValues {(key, inferrer) -> AnnotationsImpl<Any>(delegatingAnnotations[key]) }

    for (xml in annotationsInXml) {
        xml() use {
            parseAnnotations(it, {
                key, annotations ->
                val position = keyIndex.findPositionByAnnotationKeyString(key)
                if (position != null) {
                    val classNames = annotations.toMap({ data -> data.annotationClassFqn to data })
                    for ((inferrerKey, inferrer) in inferrers) {
                        val externalAnnotations = externalAnnotationsMap[inferrerKey]!!
                        val annotation = inferrer.resolveAnnotation(classNames)
                        if (annotation != null) {
                            externalAnnotations[position] = annotation
                        }
                    }
                } else {
                    errorHandler.error("Position not found for $key")
                }
            }, errorHandler)
        }
    }

    return externalAnnotationsMap
}