package ar.ferman.needle

import org.assertj.core.api.BDDAssertions.catchThrowable
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class NeedleConfigurationWithProfileTest {

    private lateinit var bean: Any
    private var throwable: Throwable? = null

    @AfterEach
    fun tearDown() {
        Needle.stop()
    }

    @Test
    fun `bean defined in an inactive is not found`() {
        Needle.init(NeedleConfiguration {
            on { activeProfile("PROFILE_A") } define {
                singleton("myRepository") {
                    create<Repository>()
                }
            }
        })

        whenGetBean(name = "myRepository")

        then(throwable).isInstanceOf(Needle.BeanNotFoundException::class.java)
    }

    @Test
    fun `get proper bean by profile`() {
        Needle.activateProfiles("PROFILE_B")
        Needle.init(NeedleConfiguration {
            on { activeProfile("PROFILE_A") } define {
                singleton("myRepository") {
                    create<Repository>()
                }
            }
            on { activeProfile("PROFILE_B") } define {
                singleton("myRepository") {
                    create<OtherRepository>()
                }
            }
        })

        whenGetBean(name = "myRepository")
        then(bean).isInstanceOf(OtherRepository::class.java)
    }

    @Test
    fun `profile activation after Needle init is forbidden`() {
        Needle.init(NeedleConfiguration {})

        throwable = catchThrowable {
            Needle.activateProfiles("PROFILE_B")
        }

        then(throwable).isInstanceOf(CannotActivateProfile::class.java)
    }

    private fun whenGetBean(name: String) {
        throwable = catchThrowable {
            bean = Needle.Beans[name]
        }
    }

    class Repository
    class OtherRepository
}
