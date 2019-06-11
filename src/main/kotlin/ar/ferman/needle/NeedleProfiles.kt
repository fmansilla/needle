package ar.ferman.needle

typealias NeedleCondition = () -> Boolean

//class ConditionalNeedleInitialization {
//
//    private val initializerByProfile = mutableMapOf<String, Needle.() -> Unit>()
//
//    fun on(condition: NeedleCondition, initializer: Needle.() -> Unit) {
//        if (condition()) {
//            initializer.invoke(Needle)
//        }
//    }
//}

//fun NeedleConfigurator.on(condition: NeedleCondition, initializer: Needle.() -> Unit) {
//    if (condition()) {
//        initializer.invoke(this.needle)
//    }
//}

class ProfileCondition(private val name: String) : NeedleCondition{
    override fun invoke(): Boolean {
        return Profile.current == name
    }
}