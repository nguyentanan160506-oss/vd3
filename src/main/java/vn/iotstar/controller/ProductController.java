package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model
    ) {
        Page<ProductDTO> productPage = productService.findAll(keyword, PageRequest.of(page, size, Sort.by("id").descending()));
        model.addAttribute("products", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalElements", productPage.getTotalElements());
        return "products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("productDTO")) {
            model.addAttribute("productDTO", new ProductDTO());
        }
        return "products/form";
    }

    @PostMapping("/create")
    public String createProduct(
            @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "products/form";
        }

        try {
            Long userId = userDetails != null ? userDetails.getId() : null;
            productService.createProduct(productDTO, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu sản phẩm: " + e.getMessage());
            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long id, Model model) {
        ProductDTO productDTO = productService.findById(id);
        model.addAttribute("productDTO", productDTO);
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String updateProduct(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        productDTO.setId(id);
        if (bindingResult.hasErrors()) {
            return "products/form";
        }

        try {
            productService.updateProduct(id, productDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return "products/form";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable("id") Long id, Model model) {
        ProductDTO productDTO = productService.findById(id);
        model.addAttribute("product", productDTO);
        return "products/detail";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/delete-image/{imageId}")
    public String deleteImage(
            @PathVariable("imageId") Long imageId,
            @RequestParam("productId") Long productId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            productService.deleteImage(imageId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa ảnh sản phẩm.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa ảnh: " + e.getMessage());
        }
        return "redirect:/products/edit/" + productId;
    }
}
