package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.RoleType;
import ru.codeislive63.springmvc.domain.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(RoleType code);
}
