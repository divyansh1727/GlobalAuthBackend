package com.authapp.projectonauth.auth.services.impl;

import com.authapp.projectonauth.auth.config.AppConstants;
import com.authapp.projectonauth.auth.payload.ChangePasswordRequest;
import com.authapp.projectonauth.auth.payload.DeleteAccountRequest;
import com.authapp.projectonauth.auth.payload.UserDto;
import com.authapp.projectonauth.auth.entities.Provider;
import com.authapp.projectonauth.auth.entities.Role;
import com.authapp.projectonauth.auth.entities.User;
import com.authapp.projectonauth.auth.repositories.RefreshTokenRepository;
import com.authapp.projectonauth.exceptions.ResourceNotFoundException;
import com.authapp.projectonauth.auth.helpers.UserHelper;
import com.authapp.projectonauth.auth.repositories.RoleRepository;
import com.authapp.projectonauth.auth.repositories.UserRepository;
import com.authapp.projectonauth.auth.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with given email already exists");
        }

        // DTO -> Entity
        User user = modelMapper.map(userDto, User.class);

        if (user.getEnable() == null) {
            user.setEnable(true);
        }

        // Default provider
        user.setProvider(
                userDto.getProvider() != null
                        ? userDto.getProvider()
                        : Provider.LOCAL
        );

        // Prevent NPE from ModelMapper
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Assign default role
        Role role = roleRepository
                .findByName("ROLE_" + AppConstants.GUEST_ROLE)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Default role ROLE_" + AppConstants.GUEST_ROLE + " not found"
                        ));

        user.getRoles().add(role);

        // Save user
        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public UserDto getUserByEmail(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given email id "));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        //we are not going to change email id for this project.
        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getProvider() != null) existingUser.setProvider(userDto.getProvider());
        //TODO: change password updation logic...
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        existingUser.setEnable(userDto.getEnable());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    @Transactional
    public UserDto updateProfileImage(String userId, MultipartFile file) throws IOException {

        UUID uId = UserHelper.parseUUID(userId);

        User user = userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(file);

        user.setImage(imageUrl);

        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserDto.class);
    }
    @Override
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {

        UUID uId = UserHelper.parseUUID(userId);

        User user = userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getProvider() != Provider.LOCAL) {
            throw new IllegalArgumentException(
                    "Password cannot be changed for OAuth accounts");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }
    @Override
    @Transactional
    public void deleteAccount(String userId, DeleteAccountRequest request) {

        UUID uId = UserHelper.parseUUID(userId);

        User user = userRepository.findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // LOCAL account -> verify password
        if (user.getProvider() == Provider.LOCAL) {

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Incorrect password");
            }
        }

        // TODO: Delete Cloudinary image (next step)

        // TODO: Delete refresh tokens
        refreshTokenRepository.deleteByUser(user);

        // Delete user
        userRepository.delete(user);
    }
}