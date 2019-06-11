
open class Action(registeredBeans: Map<String, Any>) {
    private val service: Service by registeredBeans

    init {
        //Forces eager initialization
        requireNotNull(service)
    }

    override fun toString(): String {
        return "${super.toString()} -> $service"
    }
}


class DevAction(registeredBeans: Map<String, Any>) : Action(registeredBeans) {
    init {
        println("Creating: DevAction")
    }
}

class TestAction(registeredBeans: Map<String, Any>) : Action(registeredBeans) {
    init {
        println("Creating: TestAction")
    }
}


open class Service(beans: Map<String, Any>) {
    private val repository1: Repository by beans
    private val repository2: Repository by beans

    init {
        requireNotNull(repository1)
        requireNotNull(repository2)
        println("Creating: Service")
    }

    override fun toString(): String {
        return "${super.toString()} -> $repository1, $repository2"
    }
}

open class Repository {
    init {
        println("Creating: Repository")
    }
}