package ru.netology.cloudstorage.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "session", schema = "public")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private User user;
    private String authToken;
}