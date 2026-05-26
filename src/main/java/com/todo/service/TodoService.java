package com.todo.service;

import com.todo.dto.TodoFormDto;
import com.todo.model.Todo;
import com.todo.model.User;
import com.todo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public List<Todo> getTodosForUser(User user) {
        return todoRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Todo> getTodosByStatus(User user, boolean completed) {
        return todoRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, completed);
    }

    @Transactional(readOnly = true)
    public List<Todo> getTodosByPriority(User user, Todo.Priority priority) {
        return todoRepository.findByUserAndPriorityOrderByCreatedAtDesc(user, priority);
    }

    public Todo createTodo(User user, TodoFormDto dto) {
        Todo todo = new Todo();
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        todo.setPriority(dto.getPriority() != null ? dto.getPriority() : Todo.Priority.MEDIUM);
        todo.setDueDate(dto.getDueDate());
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    @Transactional(readOnly = true)
    public Todo getTodoByIdAndUser(Long id, User user) {
        return todoRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("TODO not found or access denied"));
    }

    public Todo updateTodo(Long id, User user, TodoFormDto dto) {
        Todo todo = getTodoByIdAndUser(id, user);
        todo.setTitle(dto.getTitle());
        todo.setDescription(dto.getDescription());
        todo.setPriority(dto.getPriority() != null ? dto.getPriority() : Todo.Priority.MEDIUM);
        todo.setDueDate(dto.getDueDate());
        return todoRepository.save(todo);
    }

    public Todo toggleTodo(Long id, User user) {
        Todo todo = getTodoByIdAndUser(id, user);
        todo.setCompleted(!todo.isCompleted());
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id, User user) {
        Todo todo = getTodoByIdAndUser(id, user);
        todoRepository.delete(todo);
    }

    @Transactional(readOnly = true)
    public long countTotal(User user) {
        return todoRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public long countCompleted(User user) {
        return todoRepository.countByUserAndCompleted(user, true);
    }

    @Transactional(readOnly = true)
    public long countPending(User user) {
        return todoRepository.countByUserAndCompleted(user, false);
    }
}
