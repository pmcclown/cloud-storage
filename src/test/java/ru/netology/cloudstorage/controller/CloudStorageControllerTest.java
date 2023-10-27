package ru.netology.cloudstorage.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.netology.cloudstorage.dao.entity.File;
import ru.netology.cloudstorage.dao.entity.Session;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dto.FileInfoDTO;
import ru.netology.cloudstorage.dao.repository.SessionRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.service.CloudStorageService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CloudStorageControllerTest {

    @MockBean
    CloudStorageService cloudStorageService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    UserRepository userRepository;

    private final byte[] TEST_FILE = {1, 2, 3};

    private final String TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4Iiwicm9sZXMiOltdLCJpYXQiOjE2OT" +
            "c0NzkwMTh9.DiAVzgQYVTVIQiSJy9mXIfxE8267iQT6CyQv-obyqwM";

    private User user = new User();
    private Session session = new Session();

    @BeforeEach
    public void beforeEach() {
        user.setLogin("Test");
        user.setPassword("Test");
        user = userRepository.save(user);
        session.setAuthToken(TOKEN);
        session.setUser(user);
        session = sessionRepository.save(session);
    }

    @AfterEach
    public void afterEach() {
        sessionRepository.delete(session);
        userRepository.delete(user);
    }

    @Test
    void postFileTest() throws Exception {
        doNothing().when(cloudStorageService).save(any(),any());

        mockMvc.perform(
                        multipart("/cloud/file")
                                .file("file", TEST_FILE)
                                .param("hash", "123")
                                .queryParam("filename", "file1.dat")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .header("auth-token", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFileTest() throws Exception {
        doNothing().when(cloudStorageService).delete(any());

        mockMvc.perform(
                        delete("/cloud/file")
                                .queryParam("filename", "file1.dat")
                                .header("auth-token", TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void getFileTest() throws Exception {
        when(cloudStorageService.getFile(any())).thenReturn(TEST_FILE);

        MvcResult result = mockMvc.perform(
                        get("/cloud/file")
                                .queryParam("filename", "file1.dat")
                                .header("auth-token", TOKEN))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        byte[] actual = response.getBytes();
        assertArrayEquals(TEST_FILE, actual);
    }

    @Test
    void renameFileTest() throws Exception {
        doNothing().when(cloudStorageService).updateFile(any(), any());

        mockMvc.perform(
                        put("/cloud/file")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"filename\":\"file1renamed.dat\"}")
                                .queryParam("filename", "file1.dat")
                                .header("auth-token", TOKEN))
                .andExpect(status().isOk());

    }

    @Test
    void listFileTest() throws Exception {
        List<File> list = new ArrayList<>();
        list.add(new File("file1", TEST_FILE, user));
        list.add(new File("file2", TEST_FILE, user));
        list.add(new File("file3", TEST_FILE, user));
        when(cloudStorageService.getAllFiles(3)).thenReturn(list);

        List<FileInfoDTO> expectedList = new ArrayList<>();
        expectedList.add(new FileInfoDTO("file1", 3));
        expectedList.add(new FileInfoDTO("file2", 3));
        expectedList.add(new FileInfoDTO("file3", 3));

        String result = mockMvc.perform(
                        get("/cloud/list")
                                .queryParam("limit", "3")
                                .header("auth-token", TOKEN))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        ObjectMapper mapper = new ObjectMapper();
        List<FileInfoDTO> actualList = mapper.readValue(result, new TypeReference<>() {
        });

        assertEquals(expectedList, actualList);
    }
}