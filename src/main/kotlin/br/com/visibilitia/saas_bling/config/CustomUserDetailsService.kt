package br.com.visibilitia.saas_bling.config

import br.com.visibilitia.saas_bling.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service


@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails? {
        return userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found with email: $username")
    }
}