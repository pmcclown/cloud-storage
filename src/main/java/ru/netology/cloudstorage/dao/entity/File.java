package ru.netology.cloudstorage.dao.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "file")
@Data
@NoArgsConstructor
@Getter
@Setter
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String fileName;
    private byte[] file;

    public File(String fileName, byte[] file, User user) {
        this.fileName = fileName;
        this.user = user;
        this.file = file;
    }

    public File setFile(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public File setUser(User user) {
        this.user = user;
        return this;
    }
}