package org.jetbrains.jaiftools

import annotations.el.AScene
import annotations.el.AElement
import annotations.el.AClass
import annotations.io.IndexFileWriter
import annotations.io.IndexFileParser

/**
 * Created by user on 8/8/13.
 */
fun main(args: Array<String>)
{
    val ir =  AScene();
    IndexFileParser.parseFile("./jaifComparer/testData/julia-asm-debug-all-4.0.jaif",ir)
}
