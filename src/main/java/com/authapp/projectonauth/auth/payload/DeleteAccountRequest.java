package com.authapp.projectonauth.auth.payload;

import lombok.Data;

@Data
public class DeleteAccountRequest {
    private String password;
}
