package com.civiclens.api.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "grievances")
public class Grievance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String status;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private String category; // This will store the AI-assigned category

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @ManyToOne // This sets up the relationship: Many grievances can belong to one user
    @JoinColumn(name = "user_id") // This will create a 'user_id' foreign key column
    private User submittedBy;

    @Column(columnDefinition = "integer default 0") // Sets the default value in the database
    private int votes = 0; // Stores the number of upvotes

    @PrePersist // This method runs automatically before the entity is saved
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = "SUBMITTED";
    }
}
