package test.compiler

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolutionException
import org.junit.jupiter.api.extension.ParameterResolver

class CompilerResolver : ParameterResolver {
    @Throws(ParameterResolutionException::class)
    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext?
    ): Boolean {
        return parameterContext.parameter.type.equals(TestCompiler::class.java)
    }

    @Throws(ParameterResolutionException::class)
    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Any {
        return TestCompiler(extensionContext)
    }
}
