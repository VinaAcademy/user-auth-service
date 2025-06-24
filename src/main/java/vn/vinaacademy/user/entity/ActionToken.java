package vn.vinaacademy.user.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.vinaacademy.user.enums.ActionTokenType;
import vn.vinaacademy.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "action_tokens", indexes = {
        @Index(name = "idx_password_reset_token", columnList = "token")
})
public class ActionToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", unique = true)
    private String token;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ActionTokenType type;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
