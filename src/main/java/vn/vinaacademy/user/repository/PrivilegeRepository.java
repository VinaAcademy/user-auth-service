package vn.vinaacademy.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vinaacademy.user.entity.Permission;

public interface PrivilegeRepository extends JpaRepository<Permission, Long> {
    Permission findByName(String name);
}
