package ru.netology.cloudstorage.service;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.netology.cloudstorage.dao.entity.Session;
import ru.netology.cloudstorage.dao.repository.SessionRepository;
import ru.netology.cloudstorage.dto.JwtRequest;
import ru.netology.cloudstorage.utils.JwtUtils;
import ru.netology.cloudstorage.utils.SecurityUtils;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Log
public class AuthService {
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final SessionRepository sessionRepository;

    public String login(JwtRequest authRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword()));
        UserDetails userDetails = userService.loadUserByUsername(authRequest.getLogin());
        String token = jwtUtils.generateToken(userDetails);
        Session session = new Session();
        session.setUser(userService.findByLogin(authRequest.getLogin())
                .orElseThrow(() -> new NoSuchElementException("User not found.")));
        session.setAuthToken(token);
        sessionRepository.save(session);
        log.info("User [" +userDetails.getUsername()+"] has been authenticated with a auth-token - " + token);
        return token;
    }

    public void logout(String authToken) {
        sessionRepository.deleteByAuthToken(authToken.replace("Bearer ", ""));
        log.info("User [" + SecurityUtils.getCurrentUser() + "] has logged out");
    }
}