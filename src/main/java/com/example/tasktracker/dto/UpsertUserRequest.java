package com.example.tasktracker.dto;

import lombok.Data;

@Data
public class UpsertUserRequest {

    private String userName;

    private String email;
}
