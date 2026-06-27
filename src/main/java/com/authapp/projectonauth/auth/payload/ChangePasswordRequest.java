package com.authapp.projectonauth.auth.payload;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String currentPassword;

    private String newPassword;

    private String confirmPassword;
}
