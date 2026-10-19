package ru.kubsu.dekanat.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "user_totp")
@Getter
@Setter
@NoArgsConstructor
public class UserTotp {

    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Convert(converter = TotpSecretConverter.class)
    @Column(nullable = false)
    private String secret;

    @Column(nullable = false)
    private boolean enabled;

    private Long lastUsedStep;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
