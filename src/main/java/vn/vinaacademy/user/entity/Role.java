package vn.vinaacademy.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import vn.vinaacademy.common.entity.BaseEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new HashSet<>();

    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (StringUtils.isBlank(this.getCode())) {
            return Set.of();
        }
        if (this.permissions == null || this.permissions.isEmpty()) {
            return Set.of(new SimpleGrantedAuthority("ROLE_" + this.getCode()));
        }
        Stream<String> role = Stream.of("ROLE_" + this.getCode());
        Stream<String> permissions = this.getPermissions().stream().map(Permission::getCode);
        return Stream.concat(role, permissions)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

//    @ManyToMany(mappedBy = "roles")
//    private Set<User> users = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        // So sánh dựa trên ID hoặc một thuộc tính duy nhất
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        // Nên dựa vào ID
        return (id == null) ? 0 : id.hashCode();
    }
}
