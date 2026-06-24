package com.authapp.projectonauth.auth.services.impl;

import com.authapp.projectonauth.auth.payload.UserDto;
import com.authapp.projectonauth.auth.services.AuthService;
import com.authapp.projectonauth.auth.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServicelmpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userService.createUser(userDto);
    }
}
