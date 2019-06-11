package ar.ferman.needle

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

    fun on(condition: ConditionConfigurator.() -> Unit): ConditionalContext {
        val conditionConfigurator = ConditionConfigurator()
        condition.invoke(conditionConfigurator)

        return ConditionalContext(this.needle, conditionConfigurator.condition)
    }
}

@NeedleDsl
class ConditionConfigurator {
    lateinit var condition: NeedleCondition

    fun activeProfile(name: String){
        this.condition = ProfileCondition(name)
    }

}

@NeedleDsl
class ConditionalContext(private val needle: Needle, private val condition: NeedleCondition) {
    infix fun define(context: NeedleConfigurator.() -> Unit) {
        if(condition()) {
            context.invoke(NeedleConfigurator(needle))
        }
    }
}