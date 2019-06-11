import ar.ferman.needle.*
import kotlin.reflect.KParameter

fun main(args: Array<String>) {

    Needle.init(
        Actions,
        Services,
        Repositories
    )

    (0 until 2).forEach {
        println(Actions.action)
    }
    println(Needle)
//    Needle.eagerInit()


    //definir una funcion que resuelva los parametros y valide el tipo sin necesidad de usar un mapa.
    val foo = Foo(hashMapOf("a" to 55))
}

interface DynamicInitializer<T>

inline operator fun <reified C : Any, T : DynamicInitializer<C>> T.invoke(args: Map<String, Any>): C {

    val constructor = C::class.constructors.first()!!
    val argmap = HashMap<KParameter, Any?>().apply {
        constructor.parameters.forEach { if (it.name in args) put(it, args[it.name]) }
    }
    return constructor.callBy(argmap)
}

data class Foo(val a: Int = 10, val b: Int = 100) {
    companion object : DynamicInitializer<Foo>
}




object Repositories : NeedleConfiguration({
    singleton("repository1") { Repository() }
    singleton("repository2") { Repository() }
})


object Services : NeedleConfiguration({
    singleton("service") { Service(Needle) }
})

object Actions : NeedleConfiguration({
//    singleton("action") { Action(Needle) }

    on { activeProfile("DEV") } define {
        singleton("action") { DevAction(Needle) }
    }

    on { activeProfile("TEST") } define {
        singleton("action") { TestAction(Needle) }
    }

    on { activeProfile("PROD") } define {
        singleton("action") { Action(Needle) }
    }
}) {
    val action: Action by needle("action")
}


object Profile {
    val current
        get() = "TEST"
}
