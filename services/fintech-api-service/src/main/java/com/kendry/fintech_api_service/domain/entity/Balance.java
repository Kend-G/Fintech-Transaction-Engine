package com.kendry.fintech_api_service.domain.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "balances")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Balance {

    @Id
    private Long accountId;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void updateTimeStamp(){
        this.updatedAt = Instant.now();
    }
}
