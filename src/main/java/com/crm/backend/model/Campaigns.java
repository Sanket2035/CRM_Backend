package com.crm.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaigns {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String status;
    private Long budget;
    private Long revenue;
    private Long reach;
    private Long conversions;

}
