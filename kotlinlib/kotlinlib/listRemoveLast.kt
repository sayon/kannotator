package kotlinlib

fun <T> MutableList<T>.removeLast(): T = remove(lastIndex)