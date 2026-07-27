package event_management.config;

import event_management.model.Category;
import event_management.model.RolesEntity;
import event_management.model.Users;
import event_management.repository.CategoryRepository;
import event_management.repository.RolesRepository;
import event_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RolesRepository rolesRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner init() {
        return args -> {
            if (!rolesRepository.existsByNome("ROLE_ADMIN")) {
                rolesRepository.save(new RolesEntity("ROLE_ADMIN"));
            }
            if (!rolesRepository.existsByNome("ROLE_USER")) {
                rolesRepository.save(new RolesEntity("ROLE_USER"));
            }

            if (!userRepository.existsByEmail("admin@email.com")) {
                RolesEntity adminRole = rolesRepository.findByNome("ROLE_ADMIN")
                        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN não encontrada"));
                Users admin = Users.builder()
                        .nome("Admin")
                        .email("admin@email.com")
                        .senha(passwordEncoder.encode("123456"))
                        .roles(Set.of(adminRole))
                        .build();
                userRepository.save(admin);
            }

            if (!categoryRepository.existsByNome("Palestra")) {
                categoryRepository.save(Category.builder().nome("Palestra").descricao("Palestras e talks").build());
            }
            if (!categoryRepository.existsByNome("Workshop")) {
                categoryRepository.save(Category.builder().nome("Workshop").descricao("Workshops práticos").build());
            }
            if (!categoryRepository.existsByNome("Meetup")) {
                categoryRepository.save(Category.builder().nome("Meetup").descricao("Encontros e networking").build());
            }
            if (!categoryRepository.existsByNome("Conferência")) {
                categoryRepository.save(Category.builder().nome("Conferência").descricao("Conferências e congressos").build());
            }
        };
    }
}
