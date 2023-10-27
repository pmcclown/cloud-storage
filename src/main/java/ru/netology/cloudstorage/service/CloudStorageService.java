package ru.netology.cloudstorage.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.netology.cloudstorage.dao.entity.File;
import ru.netology.cloudstorage.dao.entity.User;
import ru.netology.cloudstorage.dao.repository.CloudStorageRepository;
import ru.netology.cloudstorage.dao.repository.UserRepository;
import ru.netology.cloudstorage.dto.FileDTO;
import ru.netology.cloudstorage.dto.FileInfoDTO;
import ru.netology.cloudstorage.utils.SecurityUtils;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class CloudStorageService {
    CloudStorageRepository cloudStorageRepository;
    UserRepository userRepository;

    public void save(String filename, FileDTO file) throws IOException {
        User currentUser = userRepository.findByLogin(SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("Unable to save. User not found."));
        cloudStorageRepository.save(new File(filename,
                file.getFile().getBytes(), currentUser));
    }

    public void delete(String fileName) {
        cloudStorageRepository.delete(
                cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                        .orElseThrow(() -> new NoSuchElementException("File not found."))
        );
    }

    public byte[] getFile(String fileName) {
        return cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("File not found."))
                .getFile();
    }

    public void updateFile(String fileName, FileInfoDTO fileInfoDTO) {
        File file = cloudStorageRepository.findFileByFileNameAndUser_Login(fileName, SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        file.setFileName(fileInfoDTO.getFilename());
        cloudStorageRepository.save(file);
    }

    public List<File> getAllFiles(int limit) {
        return cloudStorageRepository.findAllByUser_Login(SecurityUtils.getCurrentUser(),
                        PageRequest.of(0, limit, Sort.by("fileName")))
                .getContent();
    }
}