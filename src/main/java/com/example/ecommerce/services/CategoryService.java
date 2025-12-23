package com.example.ecommerce.services;

import com.example.ecommerce.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category createCategory(Category category);

    Category updateCategory(Long categoryId, Category category);

    void deleteCategory(Long categoryId);

    List<Category> getAllCategories();

    Category getCategoryById(Long categoryId);

    Category getCategoryByName(String name);

    Category getParentCategory(Long categoryId);

    List<Category> getSubCategories(Long categoryId);
}
