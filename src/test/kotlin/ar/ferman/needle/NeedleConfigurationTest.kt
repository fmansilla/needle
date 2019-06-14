package ar.ferman.needle

import org.assertj.core.api.BDDAssertions.catchThrowable
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class NeedleConfigurationTest {

    lateinit var bean: Any
    var throwable: Throwable? = null

    @AfterEach
    fun tearDown() {
        Needle.stop()
    }

    @Test
    fun `fail when get unknown bean`() {
        Needle.init(NeedleConfiguration {})

        whenGetBean(name = "myRepository")

        then(throwable).isInstanceOf(Needle.BeanNotFoundException::class.java)
    }

    @Test
    fun `get registered bean`() {
        Needle.init(NeedleConfiguration {
            singleton("myRepository") {
                create<Repository>()
            }
        })

        whenGetBean(name = "myRepository")

        then(bean).isInstanceOf(Repository::class.java)
    }

    @Test
    fun `get registered singleton twice returns same instance`() {
        Needle.init(NeedleConfiguration {
            singleton("myRepository") {
                create<Repository>()
            }
        })

        whenGetBean(name = "myRepository")

        then(bean).isEqualTo(Needle.Beans["myRepository"])
    }

    @Test
    fun `get registered prototype twice returns different instances`() {
        Needle.init(NeedleConfiguration {
            prototype("myRepository") {
                create<Repository>()
            }
        })

        whenGetBean(name = "myRepository")

        then(bean).isNotEqualTo(Needle.Beans["myRepository"])
    }

    @Test
    fun `get registered bean with dependencies`() {
        Needle.init(NeedleConfiguration {
            singleton("myAction") {
                create<Action>()
            }

            singleton("myService") {
                create<Service>()
            }

            singleton("myRepository") {
                create<Repository>()
            }
        })

        whenGetBean(name = "myAction")

        then(bean).isInstanceOf(Action::class.java)
    }

    @Test
    fun `fail when get bean with missing dependency`() {
        Needle.init(NeedleConfiguration {
            singleton("myService") {
                create<Service>()
            }
        })

        whenGetBean(name = "myService")

        then(throwable).hasCauseInstanceOf(Needle.BeanNotFoundException::class.java)
    }


    @Test
    fun `fail when get bean with invalid dependency type`() {
        Needle.init(NeedleConfiguration {
            singleton("myService") {
                create<Service>()
            }

            singleton("myRepository") {
                create<OtherRepository>()
            }
        })

        whenGetBean(name = "myService")

        then(throwable).hasCauseInstanceOf(Needle.InvalidBeanTypeException::class.java)
    }

    @Test
    fun `fail when get bean with cyclic dependency`() {
        Needle.init(NeedleConfiguration {
            singleton("myService") {
                create<Service>()
            }

            singleton("myRepository") {
                create<RepositoryDependingOnService>()
            }
        })

        whenGetBean(name = "myService")

        then(throwable).isInstanceOf(Needle.CyclicDependencyException::class.java)
    }

    private fun whenGetBean(name: String) {
        throwable = catchThrowable {
            bean = Needle.Beans[name]
        }
    }

    class Action(myService: Service)
    class Service(myRepository: Repository)
    class Repository
    class OtherRepository
    class RepositoryDependingOnService(myService: Service)

}
