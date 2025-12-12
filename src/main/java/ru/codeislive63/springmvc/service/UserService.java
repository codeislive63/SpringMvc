package ru.codeislive63.springmvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.RoleType;
import ru.codeislive63.springmvc.domain.entity.Role;
import ru.codeislive63.springmvc.domain.entity.UserAccount;
import ru.codeislive63.springmvc.repository.RoleRepository;
import ru.codeislive63.springmvc.repository.UserAccountRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccount getUser(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public UserAccount getByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    public UserAccount register(String email, String rawPassword, String fullName, RoleType roleType) {
        if (userAccountRepository.existsByEmail(email)) {
            return userAccountRepository.findByEmail(email).orElseThrow();
        }
        Role role = roleRepository.findByCode(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType)));
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRoles(Set.of(role));
        return userAccountRepository.save(user);
    }
}

