/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.security.service

import com.enigmastation.streampack.security.entity.RouterUser
import com.enigmastation.streampack.security.repository.UserRepository
import java.util.Optional
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(val repository: UserRepository) : UserDetailsManager {
    val users: User.UserBuilder = User.withDefaultPasswordEncoder()

    @Transactional
    fun findByCloak(cloak: String): Optional<RouterUser> {
        return repository.findByCloakIgnoreCase(cloak)
    }

    @Transactional
    override fun createUser(user: UserDetails) {
        repository.save(
            RouterUser(
                username = user.username,
                password = user.password,
                enabled = true,
                cloak = null,
                roles = "USER"
            )
        )
    }

    @Transactional override fun updateUser(user: UserDetails?) {}

    @Transactional
    override fun deleteUser(username: String?) {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun changePassword(oldPassword: String?, newPassword: String?) {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun userExists(username: String?): Boolean {
        TODO("Not yet implemented")
    }

    @Transactional
    override fun loadUserByUsername(username: String?): UserDetails? {
        TODO("Not yet implemented")
    }
}
