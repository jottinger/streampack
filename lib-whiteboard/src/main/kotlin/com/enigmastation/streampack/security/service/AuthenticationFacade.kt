/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.security.service

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class AuthenticationFacade : IAuthenticationFacade {
    override fun getAuthentication(): Authentication? {
        return SecurityContextHolder.getContext().authentication
    }
}
