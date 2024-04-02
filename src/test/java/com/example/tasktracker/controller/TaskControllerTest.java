package com.example.tasktracker.controller;

import com.example.tasktracker.AbstractTest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.dto.UpsertTaskRequest;
import com.example.tasktracker.dto.UserResponse;
import com.example.tasktracker.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TaskControllerTest extends AbstractTest {

    @Test
    public void whenFindAllTasks_thenReturnAllTasks() {
        var expectedData = List.of(
                new TaskResponse(FIRST_TASK_ID, "Task 1", "description 1", TASK_CREATED, TASK_UPDATED,
                        TaskStatus.TODO, FIRST_USER_ID, SECOND_USER_ID, Collections.singleton(OBSERVER_ID),
                        new UserResponse(FIRST_USER_ID, "User 1", "1@gmail.com"),
                        new UserResponse(SECOND_USER_ID, "User 2", "2@gmail.com"),
                        Collections.singleton(new UserResponse(OBSERVER_ID, "User 3", "3@gmail.com"))));

        webTestClient.get().uri("/api/v1/task")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskResponse.class)
                .hasSize(1)
                .contains(expectedData.toArray(TaskResponse[]::new));
    }

    @Test
    public void whenFindTaskById_thenReturnTaskById() {
        var expectedData = new TaskResponse(FIRST_TASK_ID, "Task 1", "description 1", TASK_CREATED,
                TASK_UPDATED, TaskStatus.TODO, FIRST_USER_ID, SECOND_USER_ID, Collections.singleton(OBSERVER_ID),
                new UserResponse(FIRST_USER_ID, "User 1", "1@gmail.com"),
                new UserResponse(SECOND_USER_ID, "User 2", "2@gmail.com"),
                Collections.singleton(new UserResponse(OBSERVER_ID, "User 3", "3@gmail.com")));

        webTestClient.get().uri("/api/v1/task/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenCreateTask_thenReturnNewTask() {
        StepVerifier.create(taskRepository.count())
                .expectNext(1L)
                .expectComplete()
                .verify();

        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("New Task");
        request.setDescription("New description");
        request.setAuthorId(FIRST_USER_ID);
        request.setAssigneeId(SECOND_USER_ID);
        request.setStatus(TaskStatus.TODO);

        webTestClient.post().uri("/api/v1/task")
                .body(Mono.just(request), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertNotNull(taskResponse.getId());
                    assertEquals("New Task", taskResponse.getName());
                    assertEquals("New description", taskResponse.getDescription());
                    assertEquals(FIRST_USER_ID, taskResponse.getAuthor().getId());
                    assertEquals(SECOND_USER_ID, taskResponse.getAssignee().getId());
                    assertEquals(TaskStatus.TODO, taskResponse.getStatus());
                });

        StepVerifier.create(taskRepository.count())
                .expectNext(2L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenAddObserver_thenReturnUpdatedTask() {
        Set<UserResponse> observers = Set.of(
                new UserResponse(OBSERVER_ID, "User 3", "3@gmail.com"),
                new UserResponse(SECOND_OBSERVER_ID, "User 4", "4@gmail.com")
        );

        webTestClient.put().uri("/api/v1/task/observer/{observerId}?taskId={taskId}", SECOND_OBSERVER_ID, FIRST_TASK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertNotNull(taskResponse.getId());
                    assertEquals("Task 1", taskResponse.getName());
                    assertEquals("description 1", taskResponse.getDescription());
                    assertEquals(FIRST_USER_ID, taskResponse.getAuthor().getId());
                    assertEquals(SECOND_USER_ID, taskResponse.getAssignee().getId());
                    assertEquals(TaskStatus.TODO, taskResponse.getStatus());
                    assertTrue(taskResponse.getObservers().containsAll(observers));
                });
    }

    @Test
    public void whenUpdateTask_thenReturnUpdatedTask() {
        UpsertTaskRequest request = new UpsertTaskRequest();
        request.setName("updatedTask");
        request.setDescription("Updated description");
        request.setAuthorId(FIRST_USER_ID);
        request.setAssigneeId(SECOND_USER_ID);
        request.setStatus(TaskStatus.TODO);

        webTestClient.put().uri("/api/v1/task/{id}", FIRST_TASK_ID)
                .body(Mono.just(request), UpsertTaskRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskResponse.class)
                .value(taskResponse -> {
                    assertNotNull(taskResponse.getId());
                    assertEquals("updatedTask", taskResponse.getName());
                    assertEquals("Updated description", taskResponse.getDescription());
                    assertEquals(FIRST_USER_ID, taskResponse.getAuthor().getId());
                    assertEquals(SECOND_USER_ID, taskResponse.getAssignee().getId());
                    assertEquals(TaskStatus.TODO, taskResponse.getStatus());
                });
    }

    @Test
    public void whenDeleteById_thenReturnNoContent() {
        webTestClient.delete().uri("/api/v1/task/{id}", FIRST_TASK_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(taskRepository.count())
                .expectNext(0L)
                .expectComplete()
                .verify();
    }
}
