package com.example.ecommerce.controllers.user;


import com.example.ecommerce.models.Category;
import com.example.ecommerce.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllCategoriesHandler() {
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<Category> getCategoryHandler(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping("/name/{categoryName}")
    public ResponseEntity<Category> getCategoryByNameHandler(@PathVariable String categoryName) {
        Category category = categoryService.getCategoryByName(categoryName);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping("/parent/{id}")
    public ResponseEntity<Category> getParentCategoryHandler(@PathVariable Long id) {
        Category category = categoryService.getParentCategory(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping("/{categoryId}/sub")
    public ResponseEntity<List<Category>> getAllCategoriesHandler(@PathVariable Long categoryId) {
        List<Category> categories = categoryService.getSubCategories(categoryId);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }
}
