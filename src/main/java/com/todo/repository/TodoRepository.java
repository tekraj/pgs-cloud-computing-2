package com.todo.repository;

import com.todo.model.Todo;
import com.todo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByUserOrderByCreatedAtDesc(User user);

    List<Todo> findByUserAndCompletedOrderByCreatedAtDesc(User user, boolean completed);

    List<Todo> findByUserAndPriorityOrderByCreatedAtDesc(User user, Todo.Priority priority);

    Optional<Todo> findByIdAndUser(Long id, User user);

    long countByUser(User user);

    long countByUserAndCompleted(User user, boolean completed);
}
