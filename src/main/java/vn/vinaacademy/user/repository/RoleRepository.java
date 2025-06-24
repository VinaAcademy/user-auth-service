package vn.vinaacademy.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.user.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByCode(String name);
}
