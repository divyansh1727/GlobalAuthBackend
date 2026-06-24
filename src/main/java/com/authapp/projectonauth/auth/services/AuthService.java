package com.authapp.projectonauth.auth.services;

import com.authapp.projectonauth.auth.payload.UserDto;

public interface AuthService {
    UserDto registerUser(UserDto userDto);
}
