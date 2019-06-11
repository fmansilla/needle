package ar.ferman.needle

import kotlin.reflect.KProperty

typealias BeanCreator<T> = () -> T

object Needle : AbstractMap<String, Any>() {
    private val beanCreators: MutableMap<String, BeanCreator<*>> = mutableMapOf()

    override val entries: Set<Map.Entry<String, Any>>
        get() = beanCreators.entries


    override fun get(key: String): Any? {
        return beanCreators[key]?.invoke() ?: throw RuntimeException("Bean not defined: $key")
    }

    fun init(vararg contexts: NeedleConfiguration) {
        println("${contexts.size} initialized")
    }

    fun <T : Any> prototype(name: String, beanCreator: BeanCreator<T>) {
        println("Registering prototype: $name")
        register(name, beanCreator)
    }

    fun <T : Any> singleton(name: String, beanCreator: BeanCreator<T>) {
        println("Registering singleton: $name")
        register(name, SingletonBeanCreator(beanCreator))
    }

    inline fun <reified T : Any> singleton(noinline beanCreator: BeanCreator<T>) {
        val name = T::class.java.simpleName.decapitalize()
        singleton(name, beanCreator)
    }


    inline fun <reified T : Any> prototype(noinline beanCreator: BeanCreator<T>) {
        val name = T::class.java.simpleName.decapitalize()
        prototype(name, beanCreator)
    }

    private fun <T> register(name: String, beanCreator: BeanCreator<T>){
        if(name in beanCreators){
            throw IllegalStateException("Already exists a bean registered with name '$name'")
        }
        beanCreators[name] = beanCreator
    }

    private class SingletonBeanCreator<T : Any>(creator: BeanCreator<T>) : BeanCreator<T> {
        private val cached: T by lazy { creator.invoke() }

        override fun invoke(): T = cached
    }

    fun eagerInit() {
        beanCreators.keys.forEach { get(it) }
    }

    override fun toString(): String {
        return "Needle (${beanCreators.size} registered beans)"
    }
}

abstract class NeedleConfiguration(initializer: NeedleConfigurator.() -> Unit) {
    init {
        initializer.invoke(NeedleConfigurator(Needle))
    }
}


@Suppress("UNCHECKED_CAST")
class NeedleBeanProperty<T : Any>(private val name: String) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return Needle[name] as T
    }
}

fun <T : Any> needle(name: String): NeedleBeanProperty<T> {
    return NeedleBeanProperty(name)
}
