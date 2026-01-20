package br.com.visibilitia.saas_bling.service

import br.com.visibilitia.saas_bling.dto.RegisterDTO
import br.com.visibilitia.saas_bling.entity.UserEntity
import br.com.visibilitia.saas_bling.mapper.UserMapper
import br.com.visibilitia.saas_bling.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {
    fun createUser(payload: RegisterDTO): UserEntity {
        // Implementation for creating a user
        if (userRepository.findByEmail(payload.email) != null) {
            throw IllegalArgumentException("Email already in use")
        }

        val hashedPassword = passwordEncoder.encode(payload.password)
        val userEntity = with(userMapper) {
            payload.toEntity(hashedPassword)
        }
        return userRepository.save(userEntity)
    }
}