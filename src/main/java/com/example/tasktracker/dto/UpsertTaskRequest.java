package com.example.tasktracker.dto;

import com.example.tasktracker.entity.TaskStatus;
import lombok.Data;

@Data
public class UpsertTaskRequest {

    private String name;

    private String description;

    private TaskStatus status;

    private String authorId;

    private String assigneeId;
}
