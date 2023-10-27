package ru.netology.cloudstorage.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.netology.cloudstorage.dao.entity.Session;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dao.repository.SessionRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.dto.JwtRequest;
import ru.netology.cloudstorage.service.AuthService;
import ru.netology.cloudstorage.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @Test
    void loginComplete() {

        User user = prepareTestData("Test", "Test");

        try {
            mockMvc.perform(
                            post("/cloud/login")
                                    .content("{\"login\": \"" + user.getLogin() + "\"," +
                                            " \"password\": \"Test\"}")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk());

            List<Session> sessions = sessionRepository.findByUserId(user.getId());
            assertEquals(1, sessions.size());
            System.out.println(sessions);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanLoginTestData(user);
        }
    }

    @Test
    void loginFailed() throws Exception {
        mockMvc.perform(
                        post("/cloud/login")
                                .content("{\"login\": \"1234\", \"password\": \"1234\"}")
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is(401));
    }

    @Test
    void logout() {
        User user = prepareTestData("Logout", "Logout");

        try {
            authService.login(new JwtRequest(user.getLogin(), "Logout"));

            List<Session> sessions = sessionRepository.findByUserId(user.getId());

            assertEquals(1, sessions.size());

            mockMvc.perform(
                            post("/cloud/logout")
                                    .header("auth-token", sessions.get(0).getAuthToken()))
                    .andDo(print())
                    .andExpect(status().isOk());

            List<Session> actualSessions = sessionRepository.findByUserId(user.getId());

            assertEquals(0, actualSessions.size());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanLoginTestData(user);
        }
    }

    private User prepareTestData(String login, String password) {
        User user = new User(login, password);
        return userService.createNewUser(user);
    }

    private void cleanLoginTestData(User user) {
        sessionRepository.deleteByUserId(user.getId());
        userRepository.deleteById(user.getId());
    }
}