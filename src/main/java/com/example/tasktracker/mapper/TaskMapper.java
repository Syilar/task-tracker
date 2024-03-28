package com.example.tasktracker.mapper;

import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.dto.UpsertTaskRequest;
import com.example.tasktracker.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = UserMapper.class)
public interface TaskMapper {

    Task requestToTask(UpsertTaskRequest request);

    Task requestToTask(String id, UpsertTaskRequest request);

    TaskResponse taskToResponse(Task task);
}
