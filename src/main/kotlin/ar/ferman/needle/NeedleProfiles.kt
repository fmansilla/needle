package ar.ferman.needle

typealias Profile = String

private object NeedleProfiles {
    private var activeProfiles = setOf<Profile>()

    fun isActive(profile: Profile): Boolean {
        return profile in activeProfiles
    }

    fun activate(vararg profiles: Profile) {
        activeProfiles = activeProfiles + profiles.toSet()
    }
}

fun Needle.activateProfiles(vararg profiles: Profile): Needle {
    if (initialized) throw CannotActivateProfile("Needle was previously initialized")

    NeedleProfiles.activate(*profiles)

    return this
}

fun ConditionConfigurator.activeProfile(name: String) {
    this.condition = ProfileCondition(name)
}

class ProfileCondition(private val profile: Profile) : NeedleCondition {

    override fun invoke(): Boolean {
        return NeedleProfiles.isActive(profile)
    }
}

class CannotActivateProfile(message: String) : RuntimeException(message)
