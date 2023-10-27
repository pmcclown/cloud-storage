package ru.netology.cloudstorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import ru.netology.cloudstorage.dto.FileInfoDTO;
import ru.netology.cloudstorage.dto.JwtRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestContainersTests {
    private static final TestRestTemplate restTemplate = new TestRestTemplate();
    private static String token;
    private static final String appUrl;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final PostgreSQLContainer<?> postgreSQLContainer;
    private static final GenericContainer<?> backendContainer;

    static {
        Network network = Network.newNetwork();
        try {
            postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres")
                    .asCompatibleSubstituteFor("postgres"))
                    .withDatabaseName("cloud")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withNetwork(network)
                    .withInitScript("initdb.sql")
                    .withNetworkAliases("postgres")
                    .withReuse(true);

            postgreSQLContainer.start();

            backendContainer = new GenericContainer<>("backend")
                    .withExposedPorts(8081)
                    .withNetwork(network)
                    .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres/cloud")
                    .dependsOn(postgreSQLContainer);

            backendContainer.start();

            appUrl = "http://" + backendContainer.getHost() + ":" + backendContainer.getMappedPort(8081);
        } finally {

        }
    }

    @Test
    @Order(1)
    void loginTest() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json,*/*");
        HttpEntity<String> request = new HttpEntity<>(
                mapper.writeValueAsString(new JwtRequest("alex", "1234")), headers);

        String response = restTemplate.postForObject(appUrl + "/cloud/login", request, String.class);

        assertNotNull(response);

        token = "Bearer " + response.substring(15, response.length() - 2);
    }

    @Test
    @Order(2)
    void postFileTest() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("Accept", "application/json,*/*");
        headers.add("auth-token", token);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("hash", "123");
        body.add("file", getTestFile());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> entity = restTemplate.postForEntity(
                appUrl + "/cloud/file?filename=file1.a", request, String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        restTemplate.postForEntity(
                appUrl + "/cloud/file?filename=file2.b", request, String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());

        restTemplate.postForEntity(
                appUrl + "/cloud/file?filename=file3.c", request, String.class);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    @Order(3)
    void listTest() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json,*/*");
        headers.add("auth-token", token);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> entity = restTemplate.exchange(appUrl + "/cloud/list?limit=3", HttpMethod.GET, request, String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());

        List<FileInfoDTO> actualList = mapper.readValue(entity.getBody(), new TypeReference<>() {
        });
        assertEquals(3, actualList.size());

        List<FileInfoDTO> expectedList = new ArrayList<>();
        expectedList.add(new FileInfoDTO("file1.a", 20));
        expectedList.add(new FileInfoDTO("file2.b", 20));
        expectedList.add(new FileInfoDTO("file3.c", 20));

        assertEquals(expectedList, actualList);
    }

    @Test
    @Order(4)
    void logoutTest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("auth-token", token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<String> entity = restTemplate.postForEntity(appUrl + "/cloud/logout", request, String.class);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    public static Resource getTestFile() throws IOException {
        Path testFile = Files.createTempFile("test-file", ".txt");
        Files.write(testFile, "This is a test file.".getBytes());
        return new FileSystemResource(testFile.toFile());
    }

}
