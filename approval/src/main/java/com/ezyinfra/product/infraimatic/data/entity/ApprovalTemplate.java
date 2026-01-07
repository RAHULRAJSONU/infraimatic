package com.ezyinfra.product.infraimatic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "approval_templates")
public class ApprovalTemplate extends AbstractPersistable{
    private String name;
    private String description;
    private boolean active = true;
    // template is either single-level or multi-level using levels list
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("levelOrder ASC")
    @JsonIgnore
    private List<ApprovalLevel> levels = new ArrayList<>();
}