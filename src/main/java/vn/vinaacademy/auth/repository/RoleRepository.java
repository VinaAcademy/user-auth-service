package vn.vinaacademy.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByCode(String name);
}
