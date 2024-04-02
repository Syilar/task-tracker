package com.example.tasktracker;

import com.example.tasktracker.entity.Task;
import com.example.tasktracker.entity.TaskStatus;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.*;

@SpringBootTest
@Testcontainers
@AutoConfigureWebTestClient
public class AbstractTest {

    protected static String FIRST_USER_ID = UUID.randomUUID().toString();

    protected static String SECOND_USER_ID = UUID.randomUUID().toString();

    protected static String OBSERVER_ID = UUID.randomUUID().toString();

    protected static String SECOND_OBSERVER_ID = UUID.randomUUID().toString();

    protected static String FIRST_TASK_ID = UUID.randomUUID().toString();

    protected static String SECOND_TASK_ID = UUID.randomUUID().toString();

    protected static Instant TASK_CREATED = Instant.parse("2024-01-03T10:15:30.00Z");

    protected static Instant TASK_UPDATED = Instant.parse("2024-01-01T10:15:29.00Z");

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.8")
            .withReuse(true);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TaskRepository taskRepository;

    @BeforeEach
    public void setup() {
        User user1 = new User(FIRST_USER_ID, "User 1", "1@gmail.com");
        User user2 = new User(SECOND_USER_ID, "User 2", "2@gmail.com");
        User observer = new User(OBSERVER_ID, "User 3", "3@gmail.com");
        User observer2 = new User(SECOND_OBSERVER_ID, "User 4", "4@gmail.com");
        userRepository.saveAll(List.of(user1, user2, observer, observer2
        )).collectList().block();


        Task task = new Task(FIRST_TASK_ID, "Task 1", "description 1", TASK_CREATED, TASK_UPDATED,
                TaskStatus.TODO, FIRST_USER_ID, SECOND_USER_ID, Collections.singleton(OBSERVER_ID),
                null, null, new HashSet<>());

        taskRepository.saveAll(List.of(task)).collectList().block();
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll().block();
        taskRepository.deleteAll().block();
    }
}
