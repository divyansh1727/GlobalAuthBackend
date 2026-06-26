package com.authapp.projectonauth.auth.services;

import com.authapp.projectonauth.auth.payload.UserDto;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    //create user
    UserDto createUser(UserDto userDto);

    //get user by email
    UserDto getUserByEmail(String email);

    //update user
    UserDto updateUser(UserDto userDto, String userId);

    //delete user
    void deleteUser(String userId);

    //get user by id
    UserDto getUserById(String userId);

    //get all users
    Iterable<UserDto> getAllUsers();

    //to updateprofileimage
    UserDto updateProfileImage(String userId, MultipartFile file) throws IOException;

    // user service se related __


}