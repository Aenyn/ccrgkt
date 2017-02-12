package fr.panicot.ccrg.security

import fr.panicot.ccrg.security.authentication.CCRGAuthenticationProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter


/**
 * Created by William on 11/02/2017.
 */

@Configuration
@EnableWebSecurity
open class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/js/**").permitAll()
                .antMatchers("/css/**").permitAll()
                .antMatchers("/messages/**").permitAll()
                .anyRequest()/*.permitAll()*/.authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
    }

    @Autowired
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth
                .authenticationProvider(authenticationProvider())
//                .inMemoryAuthentication()
//                .withUser("user").password("f66c19ab77a62ab4f5589e01a4faca11da1a18e7ae3a0d1e6ef17331be4c172a868b4cfd99e7b636f8344cb9a55f1b2f1cddf51c6ed1c5f62240a4a682cd2998").roles("USER")
//                .and()
//                .withUser("user2").password("570cb608fe3b37234530eed50db9a5be46ea0cc255576e1692010992001a8f043dea014ad0ee3e7769a3e26bd972ad1ba68c5d5f4634cee526afac6033b42ba2").roles("USER")
    }

    @Bean
    open fun authenticationProvider(): AuthenticationProvider {
        return CCRGAuthenticationProvider()
    }
}