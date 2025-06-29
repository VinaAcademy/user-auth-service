package vn.vinaacademy.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import vn.vinaacademy.common.entity.BaseEntity;

import java.util.Set;

@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permission")
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private Set<Role> roles;
}
