package com.finance.core.entity;

import com.finance.core.enums.TransactionType;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper=false)
@Entity
@Table(name = "transactions")
public class TransactionEntity extends AuditableEntity{

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "category", nullable = false, length = 100)
    private String category;


    @Column(name = "date", nullable = false)
    private LocalDate date;


    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnore
    private UserEntity createdByUser;
}
