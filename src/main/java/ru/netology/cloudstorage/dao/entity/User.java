package ru.netology.cloudstorage.dao.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "user", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String login;
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User() {

    }
}
