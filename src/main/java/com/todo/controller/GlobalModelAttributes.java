package com.todo.controller;

import com.todo.model.User;
import com.todo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds the currently authenticated User to every model so that all
 * templates (especially the navbar fragment) can access it.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private UserService userService;

    @ModelAttribute("currentUser")
    public User currentUser(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                return userService.getCurrentUser();
            } catch (Exception ignored) {
                // User may not exist on first authentication check
            }
        }
        return null;
    }

    @ModelAttribute("currentUserProfileImageUrl")
    public String currentUserProfileImageUrl(Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                return userService.getProfileImageUrl(userService.getCurrentUser());
            } catch (Exception ignored) {
                // Ignore URL failures and fall back to initials.
            }
        }
        return null;
    }
}
