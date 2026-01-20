package br.com.visibilitia.saas_bling.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    val name: String,

    val email: String,

    val hashedPassword: String,

    val createdAt: Instant,

    val updatedAt: Instant

    ): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?>? = null
    override fun getPassword(): String = this.hashedPassword

    override fun getUsername(): String = this.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
