package ru.netology.cloudstorage.service;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloudstorage.dao.entity.File;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dao.repository.CloudStorageRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.utils.SecurityUtils;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Log
public class CloudStorageService {
    CloudStorageRepository cloudStorageRepository;
    UserRepository userRepository;

    public void save(String filename, MultipartFile file) throws IOException {
        User currentUser = userRepository.findByLogin(SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("Unable to save. User not found."));
        cloudStorageRepository.save(new File(filename,
                file.getBytes(), currentUser));
        log.info("User [" + currentUser.getLogin() + "] added a file with name " + filename);
    }

    public void delete(String fileName) {
        cloudStorageRepository.delete(
                cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                        .orElseThrow(() -> new NoSuchElementException("File not found."))
        );
    }

    public byte[] getFile(String fileName) {
        log.info("User [" + SecurityUtils.getCurrentUser() + "] try to download file with name " + fileName);
        return cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("File not found."))
                .getFile();
    }

    public void updateFile(String fileName, String newFilename) {
        File file = cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        file.setFileName(newFilename);
        cloudStorageRepository.save(file);

        log.info("User [" + SecurityUtils.getCurrentUser() + "] updated filename from " + fileName + " to " + newFilename);
    }

    public List<File> getAllFiles(int limit) {
        log.info("User [" + SecurityUtils.getCurrentUser() + "] requested all files");
        return cloudStorageRepository.findAllByUser_Login(SecurityUtils.getCurrentUser(),
                        PageRequest.of(0, limit, Sort.by("fileName")))
                .getContent();
    }
}