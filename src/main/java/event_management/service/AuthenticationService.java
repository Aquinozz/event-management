package event_management.service;

import event_management.config.TokenBlacklist;
import event_management.config.TokenProvider;
import event_management.dto.LoginRequestDto;
import event_management.dto.RegisterRequestDto;
import event_management.dto.TokenResponseDto;
import event_management.enums.RolesType;
import event_management.model.RolesEntity;
import event_management.model.Users;
import event_management.repository.RolesRepository;
import event_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final TokenBlacklist tokenBlacklist;
    private final long expiration = 86400000;

    public void register(RegisterRequestDto dto) throws BadRequestException {
        log.info("Tentando registrar usuário com email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email já cadastrado: {}", dto.getEmail());
            throw new BadRequestException("Usuário já cadastrado");
        }

        RolesEntity role = rolesRepository.findByNome(RolesType.ROLE_USER.name())
                .orElseGet(() -> rolesRepository.save(
                        RolesEntity.builder()
                                .nome(RolesType.ROLE_USER.name())
                                .build()
                ));

        userRepository.save(
                Users.builder()
                        .nome(dto.getNome())
                        .email(dto.getEmail())
                        .senha(passwordEncoder.encode(dto.getSenha()))
                        .roles(Set.of(role))
                        .build()
        );

        log.info("Usuário registrado com sucesso: {}", dto.getEmail());
    }

    public TokenResponseDto login(LoginRequestDto dto) throws Exception {
        log.info("Tentativa de login para email: {}", dto.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
            );
            String token = tokenProvider.gerarToken(authentication);
            log.info("Login realizado para: {}", dto.getEmail());
            return new TokenResponseDto(token, expiration);
        } catch (BadCredentialsException e) {
            log.warn("Credenciais inválidas para: {}", dto.getEmail());
            throw new BadRequestException("Credenciais inválidas");
        }
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            tokenBlacklist.invalidate(token.substring(7));
        }
        log.info("Logout realizado com sucesso");
    }
}
