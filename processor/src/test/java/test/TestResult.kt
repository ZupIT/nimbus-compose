package test

import test.utils.CompilationResult

interface GenericTestResult {
    fun get(): Any?
    fun getAll(): List<Any?>
    fun clear(): Unit
}

object TestResult: GenericTestResult {
    private var values = mutableListOf<Any?>()

    fun add(vararg value: Any?) = values.addAll(value)
    override fun get() = values.lastOrNull()
    override fun getAll(): List<Any?> = values
    override fun clear() {
        values = mutableListOf()
    }

    fun fromCompilation(compilation: CompilationResult): GenericTestResult {
        val clazz = compilation.loadClass(this::class.qualifiedName)

        return object: GenericTestResult {
            override fun get(): Any? =
                clazz.getDeclaredMethod("get").invoke(clazz.kotlin.objectInstance)
            override fun getAll() =
                clazz.getDeclaredMethod("getAll")
                    .invoke(clazz.kotlin.objectInstance) as List<Any?>
            override fun clear() {
                clazz.getDeclaredMethod("clear").invoke(clazz.kotlin.objectInstance)
            }
        }
    }
}

