package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(Model model) {
        long totalUsers = userService.countUsers();
        long totalProducts = productService.countProducts();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);

        // Lấy 5 user mới nhất
        model.addAttribute("recentUsers", userService.findAll(null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent());

        // Lấy 5 sản phẩm mới nhất
        model.addAttribute("recentProducts", productService.findAll(null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent());

        return "admin/dashboard";
    }
}
