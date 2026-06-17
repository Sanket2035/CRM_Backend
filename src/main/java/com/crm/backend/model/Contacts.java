package com.crm.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contacts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String company;
    private String email;
    private String phone;
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "crm_contact_tags", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "crm_contact_activities", joinColumns = @JoinColumn(name = "contact_id"))
    @Builder.Default
    private List<ActivityLog> activities = new ArrayList<>();

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityLog {
        private String date;
        private String type;
        @Column(name = "description", length = 1000)
        private String desc;
    }
}
