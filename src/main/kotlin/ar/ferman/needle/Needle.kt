package ar.ferman.needle

import ar.ferman.needle.Needle.Beans.register
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

typealias BeanCreator<T> = () -> T

object Needle {
    private val logger = LoggerFactory.getLogger(Needle::class.java)

    object Beans : AbstractMap<String, Any>() {
        private var beanCreators: MutableMap<String, BeanCreator<*>> = mutableMapOf()

        override val entries: Set<Map.Entry<String, Any>>
            get() {
                checkInitialization()
                return beanCreators.keys.map { it to get(it) }.toMap().entries
            }

        override operator fun get(key: String): Any {
            checkInitialization()
            return beanCreators[key]?.invoke() ?: throw BeanNotFoundException("Bean not found with name [$key]")
        }

        override fun containsKey(key: String): Boolean {
            checkInitialization()
            return key in beanCreators
        }

        override val size: Int
            get() {
                checkInitialization()
                return beanCreators.size
            }

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Any> get(key: String, type: KClass<T>): T? {
            checkInitialization()
            val bean = get(key)
            if (!type.isInstance(bean)) {
                throw InvalidBeanTypeException("Bean [$key] is not a [$type]")
            }
            return bean as T?
        }

        internal fun <T> register(name: String, beanCreator: BeanCreator<T>) {
            if (name in beanCreators) {
                throw DuplicatedBeanException("Already exists a bean with name '$name'")
            }
            beanCreators[name] = beanCreator
        }

        fun eagerInit() {
            checkInitialization()
            beanCreators.keys.forEach { get(it) }
        }

        internal fun clear() {
            beanCreators = mutableMapOf()
        }

        override fun toString(): String {
            return "Needle (${beanCreators.size} registered beans)"
        }
    }

    var initialized = false
        private set

    fun init(vararg configurations: NeedleConfiguration) {
        initialized = true
        logger.debug("${configurations.size} initialized")
    }

    fun init(initializer: NeedleConfigurator.() -> Unit) {
        initializer.invoke(NeedleConfigurator(this))
        initialized = true
    }

    fun stop() {
        Beans.clear()
        initialized = false
    }

    internal fun <T : Any> prototype(name: String, beanCreator: BeanCreator<T>) {
        logger.debug("Registering prototype: $name")
        register(name, beanCreator)
    }

    internal fun <T : Any> singleton(name: String, beanCreator: BeanCreator<T>) {
        logger.debug("Registering singleton: $name")
        register(name, SingletonBeanCreator(name, beanCreator))
    }

    private fun checkInitialization() {
        if (!initialized) throw NeedleException("Needle is not initialized")
    }

    private class SingletonBeanCreator<T : Any>(name: String, creator: BeanCreator<T>) : BeanCreator<T> {
        private var count = 0
        private val cached: T by lazy {
            if (count++ > 0) throw CyclicDependencyException(listOf(name))

            creator.invoke()
        }

        override fun invoke(): T = cached
    }

    open class NeedleException(msg: String, cause: Throwable?) : RuntimeException(msg, cause) {
        constructor(msg: String) : this(msg, null)
    }


    open class BeanCreationException(msg: String, cause: Throwable?) : NeedleException(msg, cause) {
        constructor(msg: String) : this(msg, null)
    }

    class CyclicDependencyException(val dependencies: List<String> = emptyList()) :
        BeanCreationException(dependencies.joinToString(separator = " -> ", prefix = "[", postfix = "]"))

    class BeanNotFoundException(msg: String) : NeedleException(msg)

    class InvalidBeanTypeException(msg: String) : NeedleException(msg)

    class DuplicatedBeanException(msg: String) : NeedleException(msg)
}

fun <T : Any> needle(name: String): NeedleBeanProperty<T> {
    return NeedleBeanProperty(name)
}

fun <T : Any> needle(): NeedleBeanProperty<T> {
    return NeedleBeanProperty()
}

@Suppress("UNCHECKED_CAST")
class NeedleBeanProperty<T : Any> internal constructor(private val name: String? = null) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return Needle.Beans[name ?: property.name] as T
    }
}