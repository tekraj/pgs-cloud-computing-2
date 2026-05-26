package com.todo.controller;

import com.todo.dto.ProfileUpdateDto;
import com.todo.model.User;
import com.todo.service.TodoService;
import com.todo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TodoService todoService;

    @GetMapping
    public String viewProfile(Model model) {
        User user = userService.getCurrentUser();
        model.addAttribute("profileUser", user);
        model.addAttribute("profileImageUrl", userService.getProfileImageUrl(user));
        model.addAttribute("totalCount", todoService.countTotal(user));
        model.addAttribute("completedCount", todoService.countCompleted(user));
        model.addAttribute("pendingCount", todoService.countPending(user));
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        User user = userService.getCurrentUser();
        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setBio(user.getBio());
        model.addAttribute("profileDto", dto);
        model.addAttribute("profileUser", user);
        model.addAttribute("profileImageUrl", userService.getProfileImageUrl(user));
        return "profile/edit";
    }

    @PostMapping
    public String updateProfile(@Valid @ModelAttribute("profileDto") ProfileUpdateDto dto,
                                BindingResult result,
                                @RequestParam(value = "profilePicFile", required = false) MultipartFile profilePic,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        User user = userService.getCurrentUser();
        if (result.hasErrors()) {
            model.addAttribute("profileUser", user);
            model.addAttribute("profileImageUrl", userService.getProfileImageUrl(user));
            return "profile/edit";
        }
        try {
            userService.updateProfile(user, dto, profilePic);
            redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("profileUser", user);
            model.addAttribute("profileImageUrl", userService.getProfileImageUrl(user));
            return "profile/edit";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload profile picture. Please try again.");
            model.addAttribute("profileUser", user);
            model.addAttribute("profileImageUrl", userService.getProfileImageUrl(user));
            return "profile/edit";
        }
    }
}
