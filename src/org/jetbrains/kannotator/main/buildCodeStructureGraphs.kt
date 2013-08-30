package org.jetbrains.kannotator.main

import kotlinlib.*
import org.jetbrains.kannotator.declarations.*
import org.jetbrains.kannotator.index.FieldDependencyInfo
import org.jetbrains.kannotator.index.ClassSource
import org.jetbrains.kannotator.classHierarchy.buildClassHierarchyGraph
import org.jetbrains.kannotator.classHierarchy.buildMethodHierarchy
import org.jetbrains.kannotator.funDependecy.*
import org.jetbrains.kannotator.graphs.dependencyGraphs.PackageDependencyGraphBuilder
import org.jetbrains.kannotator.graphs.removeGraphNodes
import org.jetbrains.kannotator.classHierarchy.HierarchyGraph
import org.jetbrains.kannotator.runtime.annotations.AnalysisType
import org.jetbrains.kannotator.graphs.Graph

data class CodeStructureGraphs(
        val packageGraph: Graph<Package, Nothing?>,
        val methodGraph: Graph<Method, String>,
        val methodHierarchy: HierarchyGraph<Method>
)

private fun <K: AnalysisType> buildCodeStructureGraphs(loaded: LoadedAnnotations<K>,
                                                       classSource: ClassSource,
                                                       fieldToDependencyInfos: Map<Field, FieldDependencyInfo>,
                                                       packageIsInteresting: (String) -> Boolean,
                                                       onMissingDependency: (ClassMember) -> ClassMember?
): CodeStructureGraphs {
    val methodGraphBuilder = FunDependencyGraphBuilder(loaded.declarationIndex, classSource, fieldToDependencyInfos, onMissingDependency)

    val methodGraph = methodGraphBuilder.build()

    val packageGraphBuilder = PackageDependencyGraphBuilder(methodGraph)
    val packageGraph = packageGraphBuilder.build()

    val nonInterestingNodes = packageGraph.nodes subtract packageGraph.getTransitivelyInterestingNodes { packageIsInteresting(it.data.name) }
    packageGraphBuilder.removeGraphNodes { it in nonInterestingNodes }

    val methodHierarchy = buildMethodHierarchy(buildClassHierarchyGraph(classSource))

    packageGraphBuilder.extendWithHierarchy(methodHierarchy)

    methodGraphBuilder.removeGraphNodes { packageGraph.findNode(Package(it.data.packageName)) == null }
    return CodeStructureGraphs(packageGraph, methodGraph, methodHierarchy)
}
