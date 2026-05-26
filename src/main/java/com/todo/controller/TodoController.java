package com.todo.controller;

import com.todo.dto.TodoFormDto;
import com.todo.model.Todo;
import com.todo.model.User;
import com.todo.service.TodoService;
import com.todo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listTodos(@RequestParam(required = false) String filter,
                            @RequestParam(required = false) String priority,
                            Model model) {
        User user = userService.getCurrentUser();
        List<Todo> todos;

        if ("completed".equals(filter)) {
            todos = todoService.getTodosByStatus(user, true);
        } else if ("active".equals(filter)) {
            todos = todoService.getTodosByStatus(user, false);
        } else if (priority != null && !priority.isBlank()) {
            try {
                Todo.Priority p = Todo.Priority.valueOf(priority.toUpperCase());
                todos = todoService.getTodosByPriority(user, p);
            } catch (IllegalArgumentException e) {
                todos = todoService.getTodosForUser(user);
            }
        } else {
            todos = todoService.getTodosForUser(user);
        }

        model.addAttribute("todos", todos);
        model.addAttribute("totalCount", todoService.countTotal(user));
        model.addAttribute("completedCount", todoService.countCompleted(user));
        model.addAttribute("pendingCount", todoService.countPending(user));
        model.addAttribute("activeFilter", filter);
        model.addAttribute("activePriority", priority);
        return "todos/list";
    }

    @GetMapping("/new")
    public String newTodoForm(Model model) {
        model.addAttribute("todoForm", new TodoFormDto());
        model.addAttribute("priorities", Todo.Priority.values());
        model.addAttribute("pageTitle", "New TODO");
        return "todos/form";
    }

    @PostMapping
    public String createTodo(@Valid @ModelAttribute("todoForm") TodoFormDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("priorities", Todo.Priority.values());
            model.addAttribute("pageTitle", "New TODO");
            return "todos/form";
        }
        User user = userService.getCurrentUser();
        todoService.createTodo(user, dto);
        redirectAttributes.addFlashAttribute("message", "TODO created successfully!");
        return "redirect:/todos";
    }

    @GetMapping("/{id}/edit")
    public String editTodoForm(@PathVariable Long id, Model model) {
        User user = userService.getCurrentUser();
        try {
            Todo todo = todoService.getTodoByIdAndUser(id, user);
            TodoFormDto dto = new TodoFormDto();
            dto.setTitle(todo.getTitle());
            dto.setDescription(todo.getDescription());
            dto.setPriority(todo.getPriority());
            dto.setDueDate(todo.getDueDate());
            model.addAttribute("todoForm", dto);
            model.addAttribute("todoId", id);
            model.addAttribute("priorities", Todo.Priority.values());
            model.addAttribute("pageTitle", "Edit TODO");
            return "todos/form";
        } catch (IllegalArgumentException e) {
            return "redirect:/todos";
        }
    }

    @PostMapping("/{id}")
    public String updateTodo(@PathVariable Long id,
                             @Valid @ModelAttribute("todoForm") TodoFormDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("todoId", id);
            model.addAttribute("priorities", Todo.Priority.values());
            model.addAttribute("pageTitle", "Edit TODO");
            return "todos/form";
        }
        User user = userService.getCurrentUser();
        try {
            todoService.updateTodo(id, user, dto);
            redirectAttributes.addFlashAttribute("message", "TODO updated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/todos";
    }

    @PostMapping("/{id}/toggle")
    public String toggleTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        try {
            Todo todo = todoService.toggleTodo(id, user);
            redirectAttributes.addFlashAttribute("message",
                    todo.isCompleted() ? "Marked as completed!" : "Marked as active!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/todos";
    }

    @PostMapping("/{id}/delete")
    public String deleteTodo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        try {
            todoService.deleteTodo(id, user);
            redirectAttributes.addFlashAttribute("message", "TODO deleted.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/todos";
    }
}
