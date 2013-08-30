package org.jetbrains.kannotator.main

import java.io.File
import java.util.HashMap
import kotlinlib.*
import org.jetbrains.kannotator.asm.util.createMethodNodeStub
import org.jetbrains.kannotator.declarations.*
import org.jetbrains.kannotator.index.DeclarationIndex
import org.jetbrains.kannotator.index.DeclarationIndexImpl
import org.objectweb.asm.tree.MethodNode
import java.util.HashSet
import org.jetbrains.kannotator.index.ClassSource
import org.jetbrains.kannotator.controlFlow.builder.analysis.*
import org.jetbrains.kannotator.runtime.annotations.AnalysisType
import org.jetbrains.kannotator.ErrorHandler
import org.jetbrains.kannotator.index.AnnotationKeyIndex
import java.io.FileReader

data class LoadedAnnotations<K: AnalysisType>(
        val declarationIndex: DeclarationIndex,
        val inferenceResult: InferenceResult<K>,
        val methodNodes: MutableMap<Method, MethodNode>,
        val resultingAnnotationsMap: Map<K, AnnotationsImpl<Any>>
)

fun <K: AnalysisType> loadAnnotations(
        classSource: ClassSource,
        existingAnnotationFiles: Collection<File>,
        inferrers: Map<K, AnnotationInferrer<Any, Qualifier>>,
        progressMonitor: ProgressMonitor = ProgressMonitor(),
        errorHandler: ErrorHandler,
        existingAnnotations: Map<K, Annotations<Any>>,
        existingPositionsToExclude: Map<K, Set<AnnotationPosition>>
): LoadedAnnotations<K> {
    val methodNodes = HashMap<Method, MethodNode>()
    val declarationIndex = DeclarationIndexImpl(classSource, {
        method ->
        val methodNode = method.createMethodNodeStub()
        methodNodes[method] = methodNode
        methodNode
    })

    progressMonitor.annotationIndexLoaded(declarationIndex)

    val loadedAnnotationsMap =  loadExternalAnnotations(loadMethodAnnotationsFromByteCode(methodNodes, inferrers),
            existingAnnotationFiles map { {FileReader(it)} }, declarationIndex, inferrers, errorHandler)
    val filteredLoadedAnnotationsMap = loadedAnnotationsMap.mapValues {(key, loadedAnn) ->
        val positionsToExclude = existingPositionsToExclude[key]

        if (positionsToExclude == null || positionsToExclude.empty) loadedAnn
        else {
            val newAnn = AnnotationsImpl<Any>(loadedAnn.delegate)

            loadedAnn.forEach {(pos, ann) ->
                if (pos !in positionsToExclude) newAnn[pos] = ann
            }

            newAnn
        }
    }

    val resultingAnnotationsMap = filteredLoadedAnnotationsMap.mapValues {(key, ann) -> AnnotationsImpl<Any>(ann) }
    for (key in inferrers.keySet()) {
        val inferrerExistingAnnotations = existingAnnotations[key]
        if (inferrerExistingAnnotations != null) {
            resultingAnnotationsMap[key]!!.copyAllChanged(inferrerExistingAnnotations)
        }
    }

    return LoadedAnnotations(
            declarationIndex,
            InferenceResult(
                    inferrers.mapValues {(key, inferrer) ->
                        InferenceResultGroup<Any>(
                                loadedAnnotationsMap[key]!!,
                                resultingAnnotationsMap[key]!!,
                                HashSet<AnnotationPosition>()
                        )
                    }
            ),
            methodNodes,
            resultingAnnotationsMap)

}
