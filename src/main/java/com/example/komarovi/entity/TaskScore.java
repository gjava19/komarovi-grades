package com.example.komarovi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@Entity
@Table(
        name = "tasks_score",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_total_task", columnNames = {"total_score_id","task_number"})
        }
)
public class TaskScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="total_score", nullable = false)
    private TotalScore totalScore;

    @Column(name="task_number", nullable = false)
    private Integer taskNumber;

    @Column(name="task_points", nullable = false, precision = 6, scale = 2)
    private BigDecimal taskPoints;
}
