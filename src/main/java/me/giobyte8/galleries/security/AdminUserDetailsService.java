package me.giobyte8.galleries.security;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username
                ));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .disabled(!user.isEnabled())
                .authorities(mapAuthorities(user.getRoles()))
                .build();
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<String> roles
    ) {
        if (roles == null) {
            return java.util.List.of();
        }

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}

