package com.example.tasktracker.service;

import com.example.tasktracker.entity.Task;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;


    @Override
    public Flux<Task> findAll() {
        return taskRepository.findAll()
                .flatMap(this::getFullTask);
    }

    @Override
    public Mono<Task> findById(String id) {
        return taskRepository.findById(id)
                .flatMap(this::getFullTask)
                .onErrorResume(ex -> {
                    log.error("Error in findById!", ex);
                    return Mono.error(ex);
                });
    }

    @Override
    public Mono<Task> save(Task task) {
        task.setId(UUID.randomUUID().toString());
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        return taskRepository.save(task);
    }

    @Override
    public Mono<Task> update(Task task) {
        return taskRepository.findById(task.getId())
                .flatMap(updatedTask -> {
                    BeanUtils.nonNullPropertiesCopy(task, updatedTask);
                    updatedTask.setUpdatedAt(Instant.now());
                    return taskRepository.save(updatedTask);
                });
    }

    @Override
    public Mono<Task> addObserver(String observerId, String taskId) {
        return findById(taskId)
                .flatMap(updatedTask -> {
                    updatedTask.setUpdatedAt(Instant.now());
                   if (updatedTask.getObserverIds() == null) {
                       updatedTask.setObserverIds(new HashSet<>());
                   }
                   updatedTask.getObserverIds().add(observerId);

                    return taskRepository.save(updatedTask);
                });
    }


    @Override
    public Mono<Void> deleteById(String id) {
        return taskRepository.deleteById(id);
    }

    public Mono<Task> getFullTask(Task task) {
        Mono<User> author = task.getAuthorId() != null ? userRepository.findById(task.getAuthorId()) : Mono.error(new RuntimeException());
        Mono<User> assignee = task.getAssigneeId() != null ? userRepository.findById(task.getAssigneeId()) : Mono.error(new RuntimeException());

        List<Mono<User>> users = new ArrayList<>();
        users.add(author);
        users.add(assignee);

        if (task.getObserverIds() != null) {
            task.getObserverIds().forEach(userId -> users.add(userRepository.findById(userId)));
        }
//        Mono<Tuple2<User, User>> authorAndAssignee = Mono.zip(author, assignee);

       return Mono.zip(users, task::setUsers);
    }


//    public Mono<Task> getFullTask(Task task) {
//        List<Mono<User>> users = new ArrayList<>();
//
//        users.add(task.getAuthorId() == null ? Mono.just(new User()) : userRepository.findById(task.getAuthorId())
//                .defaultIfEmpty(new User(task.getAuthorId(), null, null)));
//
//        users.add(task.getAssigneeId() == null ? Mono.just(new User()) : userRepository.findById(task.getAssigneeId())
//                .defaultIfEmpty(new User(task.getAssigneeId(), null, null)));
//
//        if (task.getObserverIds() != null) {
//            task.getObserverIds().forEach(observerId -> users.add(userRepository.findById(observerId)
//                    .defaultIfEmpty(new User(observerId, null, null))));
//        }
//        return Mono.zip(users, task::setUsers);
//    }
}
