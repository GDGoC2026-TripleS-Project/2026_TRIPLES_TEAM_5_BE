package com.triples.team5be.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "situation_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SituationTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private Boolean isActive;
}