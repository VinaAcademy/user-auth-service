package vn.vinaacademy.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.auth.entity.Permission;

public interface PrivilegeRepository extends JpaRepository<Permission, Long> {
    Permission findByName(String name);
}
