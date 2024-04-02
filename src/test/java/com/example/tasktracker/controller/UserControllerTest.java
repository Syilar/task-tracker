package com.example.tasktracker.controller;

import com.example.tasktracker.AbstractTest;
import com.example.tasktracker.dto.UpsertUserRequest;
import com.example.tasktracker.dto.UserResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserControllerTest extends AbstractTest {

    @Test
    public void whenGetAllUsers_thenReturnListOfUsersFromDatabase() {
        var expectedData = List.of(
                new UserResponse(FIRST_USER_ID, "User 1", "1@gmail.com"),
                new UserResponse(SECOND_USER_ID, "User 2", "2@gmail.com"),
                new UserResponse(OBSERVER_ID, "User 3", "3@gmail.com"),
                new UserResponse(SECOND_OBSERVER_ID, "User 4", "4@gmail.com")
        );

        webTestClient.get().uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponse.class)
                .hasSize(4)
                .contains(expectedData.toArray(UserResponse[]::new));
    }

    @Test
    public void whenGetUserById_thenReturnUserById() {
        var expectedData = new UserResponse(FIRST_USER_ID, "User 1", "1@gmail.com");

        webTestClient.get().uri("/api/v1/user/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .isEqualTo(expectedData);
    }

    @Test
    public void whenCreateUser_thenReturnNewUser() {
        StepVerifier.create(userRepository.count())
                .expectNext(4L)
                .expectComplete()
                .verify();

        UpsertUserRequest request = new UpsertUserRequest();
        request.setUserName("Test User");
        request.setEmail("test@gmail.com");

        webTestClient.post().uri("/api/v1/user")
                .body(Mono.just(request), UpsertUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals("Test User", response.getUserName());
                    assertEquals("test@gmail.com", response.getEmail());
                });

        StepVerifier.create(userRepository.count())
                .expectNext(5L)
                .expectComplete()
                .verify();
    }

    @Test
    public void whenUpdateUser_thenReturnUpdatedUser() {
        UpsertUserRequest request = new UpsertUserRequest();
        request.setUserName("New User");
        request.setEmail("New@gmail.com");

//        UserResponse expectedResponse = new UserResponse(FIRST_USER_ID, "New User", "New@gmail.com");

        webTestClient.put().uri("/api/v1/user/{id}", FIRST_USER_ID)
                .body(Mono.just(request), UpsertUserRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
//                .isEqualTo(expectedResponse);
                .value(response -> {
                    assertNotNull(response.getId());
                    assertEquals("New User", response.getUserName());
                    assertEquals("New@gmail.com", response.getEmail());
                });
    }

    @Test
    public void whenDeleteUserById_thenReturnNoContent() {
        webTestClient.delete().uri("/api/v1/user/{id}", FIRST_USER_ID)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(userRepository.count())
                .expectNext(3L)
                .expectComplete()
                .verify();
    }
}
