package com.adrian.sintaxis.security;

import com.adrian.sintaxis.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .map(usuario -> {
                    String rolName = usuario.getRol() != null ? usuario.getRol().name() : "CLIENTE";
                    String authorityName = rolName.startsWith("ROLE_") ? rolName : "ROLE_" + rolName;
                    return new User(
                            usuario.getEmail(),
                            usuario.getPassword(),
                            List.of(new SimpleGrantedAuthority(authorityName))
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
