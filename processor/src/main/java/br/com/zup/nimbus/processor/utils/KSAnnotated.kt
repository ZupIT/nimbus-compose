/*
 * Copyright 2023 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.nimbus.processor.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation> KSAnnotated.getAnnotation(): T? {
    return this.getAnnotationsByType(T::class).firstOrNull()
}

@OptIn(KspExperimental::class)
inline fun <reified T: Annotation>KSAnnotated.hasAnnotation(): Boolean {
    return this.isAnnotationPresent(T::class)
}

fun KSAnnotated.hasAnnotation(className: ClassName): Boolean {
    return this.annotations.find {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == className.canonicalName
    } != null
}
