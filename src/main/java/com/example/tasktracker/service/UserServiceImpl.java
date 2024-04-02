package com.example.tasktracker.service;

import com.example.tasktracker.entity.User;
import com.example.tasktracker.exception.EntityNotFoundException;
import com.example.tasktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public Flux<User> findAll() {
        return userRepository.findAll()
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Список пользователей пуст!")));
    }

    @Override
    public Mono<User> findById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(() -> new EntityNotFoundException("Пользователь с ID " + id + " не найден!")));

    }

    @Override
    public Mono<User> save(User user) {
        user.setId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    @Override
    public Mono<User> update(String id, User user) {
        return findById(id).flatMap(userForUpdate -> {
            if (StringUtils.hasText(user.getUserName())) {
                userForUpdate.setUserName(user.getUserName());
            }
            if (StringUtils.hasText(user.getEmail())) {
                userForUpdate.setEmail(user.getEmail());
            }
            return userRepository.save(userForUpdate);
        });
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return userRepository.deleteById(id);
    }
}
