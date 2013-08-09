package org.jetbrains.jaiftools

import annotations.el.AScene
import annotations.el.AElement
import annotations.io.IndexFileParser

/**
 * Created by user on 8/8/13.
 */

fun <K, V, M : MutableMap<K, V>> M.filterTo(predicate: (K, V)-> Boolean, destination: M): M
{
    for ((key, value) in this)
        if (predicate(key, value)) destination.put(key, value)
    return destination
}

/*
fun AScene.filter(predicate: (AElement)-> Boolean): AScene
{
    val newScene = AScene()
    for((key, value) in this.packages){
        if (predicate(value))
            newScene.packages!!.put(key, value)
    }

    for((key, value) in this.classes)
    {
    }

}

  */