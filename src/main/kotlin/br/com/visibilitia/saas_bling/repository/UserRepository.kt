package br.com.visibilitia.saas_bling.repository

import br.com.visibilitia.saas_bling.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: JpaRepository<UserEntity, String> {

    fun findByEmail(email: String): UserEntity?
}