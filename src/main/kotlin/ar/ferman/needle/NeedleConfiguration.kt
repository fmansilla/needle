package ar.ferman.needle

open class NeedleConfiguration(initializer: NeedleConfigurator.() -> Unit) {
    init {
        initializer.invoke(NeedleConfigurator(Needle))
    }
}

@DslMarker
annotation class NeedleDsl

@NeedleDsl
class NeedleConfigurator(private val needle: Needle) {
    fun <T : Any> singleton(name: String, beanCreator: BeanCreator<T>) {
        needle.singleton(name, beanCreator)
    }

    fun <T : Any> prototype(name: String, beanCreator: BeanCreator<T>) {
        needle.prototype(name, beanCreator)
    }


    inline fun <reified T : Any> singleton(noinline beanCreator: BeanCreator<T>) {
        val name = T::class.java.simpleName.decapitalize()
        singleton(name, beanCreator)
    }


    inline fun <reified T : Any> prototype(noinline beanCreator: BeanCreator<T>) {
        val name = T::class.java.simpleName.decapitalize()
        prototype(name, beanCreator)
    }


    fun on(condition: ConditionConfigurator.() -> Unit): ConditionalContext {
        val conditionConfigurator = ConditionConfigurator()
        condition.invoke(conditionConfigurator)

        return ConditionalContext(this.needle, conditionConfigurator.condition)
    }
}

typealias NeedleCondition = () -> Boolean

@NeedleDsl
class ConditionConfigurator {
    lateinit var condition: NeedleCondition

    fun condition(condition: NeedleCondition){
        this.condition = condition
    }

}

@NeedleDsl
class ConditionalContext(private val needle: Needle, private val condition: NeedleCondition) {
    infix fun define(context: NeedleConfigurator.() -> Unit) {
        if (condition()) {
            context.invoke(NeedleConfigurator(needle))
        }
    }
}