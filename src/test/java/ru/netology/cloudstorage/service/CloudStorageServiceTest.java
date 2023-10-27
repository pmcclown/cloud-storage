package ru.netology.cloudstorage.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import ru.netology.cloudstorage.dao.entity.File;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dto.FileDTO;
import ru.netology.cloudstorage.dto.FileInfoDTO;
import ru.netology.cloudstorage.dao.repository.CloudStorageRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.utils.SecurityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CloudStorageServiceTest {

    @Autowired
    private CloudStorageService cloudStorageService;
    @Autowired
    private CloudStorageRepository cloudStorageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    private final byte[] file = {1, 2, 3};
    private final MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class);
    private final String LOGIN = "Test";
    private User user;
    private final String filename = "TestFile.txt";

    @BeforeEach
    public void beforeEach() {
        user = userService.createNewUser(new User(LOGIN, "Test"));
        cloudStorageRepository.save(new File(filename, file, user));
        securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(LOGIN);
    }

    @AfterEach
    public void afterEach() {
        Optional<File> f = cloudStorageRepository
                .findFileByFileNameAndUser_Login("Test.txt", LOGIN);
        f.ifPresent(value -> cloudStorageRepository.delete(value));
        Optional<File> fi = cloudStorageRepository
                .findFileByFileNameAndUser_Login(filename, LOGIN);
        fi.ifPresent(value -> cloudStorageRepository.delete(value));
        securityUtils.close();
        userRepository.delete(user);
    }

    @Test
    void save() throws IOException {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFile(new MockMultipartFile("Test.txt", "Test.txt", "byte[]", file));

        cloudStorageService.save("Test.txt",fileDTO);

        File file = cloudStorageRepository.findFileByFileNameAndUser_Login("Test.txt", LOGIN)
                .orElse(new File());

        assertNotNull(file);
        assertEquals("Test.txt", file.getFileName());
        assertArrayEquals(file.getFile(), this.file);
    }

    @Test
    void delete() {
        cloudStorageService.delete(filename);

        File result = cloudStorageRepository
                .findFileByFileNameAndUser_Login(filename, LOGIN).orElse(null);

        assertNull(result);
    }

    @Test
    void getFile() {
        byte[] result = cloudStorageService.getFile(filename);
        assertNotNull(result);
        assertArrayEquals(file, result);
    }

    @Test
    void updateFile() {
        FileInfoDTO fileInfoDTO = new FileInfoDTO("newTest.txt", 3);
        cloudStorageService.updateFile(filename, fileInfoDTO);

        File file = cloudStorageRepository.findFileByFileNameAndUser_Login("newTest.txt", LOGIN)
                .orElse(new File());

        assertNotNull(file);
        assertArrayEquals(this.file, file.getFile());
        cloudStorageRepository.delete(file);
    }

    @Test
    void getAllFiles() {
        List<File> files = cloudStorageService.getAllFiles(3);

        assertNotNull(files);
        assertEquals(1, files.size());
        assertEquals(filename, files.get(0).getFileName());
        assertArrayEquals(file, files.get(0).getFile());

    }
}