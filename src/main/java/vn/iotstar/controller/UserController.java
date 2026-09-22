package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping({"/users", "/admin/users"})
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<UserDTO> userPage = userService.findAll(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("users", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalElements", userPage.getTotalElements());
        return "admin/users/list";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        UserDTO userDTO = userService.findById(id);
        model.addAttribute("userDTO", userDTO);
        model.addAttribute("roles", userService.findAllRoles());
        return "admin/users/form";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("userDTO") UserDTO userDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        userDTO.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", userService.findAllRoles());
            return "admin/users/form";
        }

        try {
            userService.update(userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công!");
            return "redirect:/users";
        } catch (Exception e) {
            model.addAttribute("roles", userService.findAllRoles());
            model.addAttribute("errorMessage", e.getMessage());
            return "admin/users/form";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái kích hoạt của người dùng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa người dùng thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng: " + e.getMessage());
        }
        return "redirect:/users";
    }
}
