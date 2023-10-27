package ru.netology.cloudstorage.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.netology.cloudstorage.dao.entity.Session;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dao.repository.SessionRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.dto.JwtRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private AuthService authService;

    private final String LOGIN = "Test";
    private final String PASSWORD = "Test";
    private final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4Iiwicm9sZXMiOltdLCJpYXQiOjE2OT" +
            "c0NzkwMTh9.DiAVzgQYVTVIQiSJy9mXIfxE8267iQT6CyQv-obyqwM";
    private User user;

    @BeforeEach
    public void beforeEach() {
        user = userService.createNewUser(new User(LOGIN, PASSWORD));
    }
    @AfterEach
    public void afterEach() {
        sessionRepository.deleteByUserId(user.getId());
        userRepository.delete(user);
    }

    @Test
    void login() {
        String result = authService.login(new JwtRequest(LOGIN, PASSWORD));

        assertNotNull(result);
        List<Session> sessions = sessionRepository.findByUserId(user.getId());

        assertTrue(sessions.stream()
                .anyMatch(session ->
                        session.getAuthToken().equals(result)));
    }

    @Test
    void logout() {
        Session session = new Session();
        session.setUser(user);
        session.setAuthToken(TOKEN);
        sessionRepository.save(session);

        authService.logout(TOKEN);

        List<Session> sessions = sessionRepository.findByUserId(user.getId());

        assertTrue(sessions.stream().noneMatch(s -> s.getAuthToken().equals(TOKEN)));
    }
}