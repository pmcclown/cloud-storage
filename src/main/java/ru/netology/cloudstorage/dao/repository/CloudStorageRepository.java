package ru.netology.cloudstorage.dao.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.netology.cloudstorage.dao.entity.File;

import java.util.Optional;

@Repository
public interface CloudStorageRepository extends JpaRepository<File, Long> {
    Optional<File> findFileByFileNameAndUser_Login(String fileName, String login);

    Slice<File> findAllByUser_Login(String login, Pageable pageable);
}