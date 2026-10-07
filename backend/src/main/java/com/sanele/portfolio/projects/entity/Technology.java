package com.sanele.portfolio.projects.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "technologies")
public class Technology {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 120) private String name;
    @Column(nullable = false, unique = true, length = 140) private String slug;
    @ManyToMany(mappedBy = "technologyEntities") private Set<Project> projects = new HashSet<>();
    protected Technology() { }
    public Technology(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
}