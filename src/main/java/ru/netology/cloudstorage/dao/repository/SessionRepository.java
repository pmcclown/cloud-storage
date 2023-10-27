package ru.netology.cloudstorage.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.netology.cloudstorage.dao.entity.Session;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Modifying
    @Transactional
    void deleteByAuthToken(String authToken);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    Session findByAuthToken(String authToken);

    List<Session> findByUserId(Long userId);
}
