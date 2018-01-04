package fr.panicot.ccrg.security.authentication

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.security.MessageDigest
import java.util.*
import javax.xml.bind.annotation.adapters.HexBinaryAdapter

/**
 * Created by William on 12/02/2017.
 */

open class CCRGAuthenticationProvider(val defaultPassword: String): AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication? {
        return if(checkPassword(authentication.name, authentication.credentials.toString())) {
            createSuccessAuthentication(authentication)
        } else {
            null
        }
    }

    override fun supports(authentication: Class<*>): Boolean {
        return authentication == UsernamePasswordAuthenticationToken::class.java
    }

    fun checkPassword(user: String, password: String): Boolean {
        if (user.endsWith("Bot")) return false
        val saltedPass = (defaultPassword + user).toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        md.update(saltedPass)
        val hashedPass = md.digest()
        return HexBinaryAdapter().marshal(hashedPass).toLowerCase() == password
    }

    fun createSuccessAuthentication(authentication: Authentication): Authentication {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        val result = UsernamePasswordAuthenticationToken(authentication.principal, authentication.credentials, Arrays.asList(SimpleGrantedAuthority("ROLE_USER")))
        result.details = authentication.details

        return result
    }

    fun createFailAuthentication(authentication: Authentication): Authentication {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        val result = UsernamePasswordAuthenticationToken(authentication.principal, authentication.credentials, Arrays.asList())
        result.details = authentication.details

        return result
    }


}