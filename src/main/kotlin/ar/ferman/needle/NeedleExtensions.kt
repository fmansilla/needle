package ar.ferman.needle

import ar.ferman.needle.Needle.BeanCreationException
import ar.ferman.needle.Needle.CyclicDependencyException
import ar.ferman.needle.Needle.NeedleException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor

inline fun <reified C : Any> KClass<C>.create(): C {
    return create(Needle.Beans)
}

inline fun <reified C : Any> create(args: Map<String, Any>): C {
    val constructor = C::class.primaryConstructor!!
    val constructorArgs = HashMap<KParameter, Any?>().apply {
        constructor.parameters.forEach {
            val expectedType = it.type.classifier as KClass<*>
            val beanParam = args[it.name]
            if (!beanParam!!::class.isSubclassOf(expectedType)) {
                throw BeanCreationException("Expecting bean named[${it.name}] of type $expectedType, but found was ${beanParam::class}")
            }

            if (it.name in args) put(it, beanParam)
        }
    }
    return constructor.callBy(constructorArgs)
}

inline fun <reified C : Any> create(): C = try {
    val constructor = C::class.primaryConstructor ?: throw BeanCreationException("Missing primary constructor")
    val parameters = constructor.parameters

    if (constructor.isBeanMapBasedConstructor()) {
        constructor.callBy(mapOf(constructor.parameters.single() to Needle.Beans))
    } else {
        constructor.callBy(parameters.toMapOfBeansByNeedle())
    }
} catch (e: CyclicDependencyException) {
    throw CyclicDependencyException(e.dependencies + C::class.simpleName!!)
} catch (e: NeedleException) {
    throw BeanCreationException("Cannot create bean type [${C::class}]", e)
}

fun List<KParameter>.toMapOfBeansByNeedle(): Map<KParameter, Any?> {
    return map { param ->
        param to Needle.Beans[param.name!!, param.type.classifier as KClass<*>]!!
    }.toMap()
}

fun <C> KFunction<C>.isBeanMapBasedConstructor(): Boolean {
    if (parameters.size == 1) {
        return with(parameters.single()) {
            name !in Needle.Beans && (type.classifier as KClass<*>).isSubclassOf(Map::class)
        }
    }

    return false
}
