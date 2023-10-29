package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudstorage.dto.FileDTO;
import ru.netology.cloudstorage.dto.FileInfoDTO;
import ru.netology.cloudstorage.service.CloudStorageService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Log
@RestController
@RequiredArgsConstructor
@RequestMapping("/cloud")
@CrossOrigin(originPatterns = "http://localhost**", allowCredentials = "true")
public class CloudStorageController {

    private final CloudStorageService cloudStorageService;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@ModelAttribute FileDTO file, @RequestParam("filename") String filename) throws IOException {
        cloudStorageService.save(filename, file.getFile());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public void delete(@RequestParam("filename") String fileName) {
        cloudStorageService.delete(fileName);
    }

    @GetMapping(value = "/file", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> download(@RequestParam("filename") String fileName) {
        byte[] file = cloudStorageService.getFile(fileName);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.MULTIPART_FORM_DATA).body(file);
    }

    @PutMapping("/file")
    public ResponseEntity<?> edit(@RequestParam("filename") String fileName, @RequestBody FileInfoDTO fileInfoDTO) {
        cloudStorageService.updateFile(fileName, fileInfoDTO.getFilename());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FileInfoDTO> list(@RequestParam("limit") int limit) {
        return cloudStorageService.getAllFiles(limit).stream()
                .map(file -> new FileInfoDTO(file.getFileName(), file.getFile().length))
                .collect(Collectors.toList());
    }
}