/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.security.service

import org.springframework.security.core.Authentication

interface IAuthenticationFacade {
    fun getAuthentication(): Authentication?
}
