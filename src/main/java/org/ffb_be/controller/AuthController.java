package org.ffb_be.controller;

import lombok.RequiredArgsConstructor;
import org.ffb_be.config.security.security.JwtTokenUtils;
import org.ffb_be.dto.auth.Login;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userService;
    private final JwtTokenUtils jwtTokenUtils;

    @PostMapping(value = "/login")
    public ResponseEntity<String> get(@RequestBody Login login){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
        UserDetails userDetails= userService.loadUserByUsername(login.getUsername());
        String jwt = jwtTokenUtils.generateToken(userDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        return new ResponseEntity<>(jwt, headers, HttpStatus.OK);
    }
}
