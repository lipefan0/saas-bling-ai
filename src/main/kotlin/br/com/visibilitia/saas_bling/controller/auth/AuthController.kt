package br.com.visibilitia.saas_bling.controller.auth

import br.com.visibilitia.saas_bling.config.JwtTokenProvider
import br.com.visibilitia.saas_bling.dto.RegisterDTO
import br.com.visibilitia.saas_bling.dto.RegisterResponseDTO
import br.com.visibilitia.saas_bling.entity.UserEntity
import br.com.visibilitia.saas_bling.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwt: JwtTokenProvider
) {

    @PostMapping("/register")
    fun register(@RequestBody payload: RegisterDTO): ResponseEntity<RegisterResponseDTO> {
        val response = userService.createUser(payload)
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @PostMapping("/login")
    fun login(email: String, password: String): ResponseEntity<Map<String, String>> {

        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(email, password)
        )
        SecurityContextHolder.getContext().authentication = authentication

        val user = authentication.principal as UserEntity
        val token = jwt.generateToken(user)
        return ResponseEntity.ok(mapOf("access_token" to token))
    }
}