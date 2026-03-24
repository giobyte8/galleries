package me.giobyte8.galleries.security;

import me.giobyte8.galleries.persistence.models.User;
import me.giobyte8.galleries.persistence.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_shouldMapUser() {
        var user = User.builder()
                .username("dev_admin")
                .password("hashed")
                .roles(List.of("ROLE_ADMIN"))
                .enabled(true)
                .build();

        when(userRepository.findByUsername("dev_admin"))
                .thenReturn(Optional.of(user));

        var details = userDetailsService.loadUserByUsername("dev_admin");

        assertThat(details.getUsername()).isEqualTo("dev_admin");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowWhenMissing() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}

