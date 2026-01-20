package br.com.visibilitia.saas_bling.mapper

import br.com.visibilitia.saas_bling.dto.RegisterDTO
import br.com.visibilitia.saas_bling.dto.RegisterResponseDTO
import br.com.visibilitia.saas_bling.entity.UserEntity
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class UserMapper {

    // Map RegisterDTO to UserEntity
    fun RegisterDTO.toEntity(hashedPassword: String): UserEntity = UserEntity(
        id = null,
        name = this.name,
        email = this.email,
        hashedPassword = hashedPassword,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    fun UserEntity.toResponseDTO(): RegisterResponseDTO = RegisterResponseDTO(
        id = this.id!!,
        message = "Usuário '${this.name}' criado com sucesso"
    )
}