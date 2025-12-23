package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.category.CategoryNotFoundException;
import com.example.ecommerce.models.Category;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.repositories.CategoryRepository;
import com.example.ecommerce.repositories.ProductRepository;
import com.example.ecommerce.services.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Category createCategory(Category category) {
        if (category.getParentCategory() != null && category.getParentCategory().getId() != null) {
            Category parent = categoryRepository.findById(category.getParentCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));

            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        return categoryRepository.save(category);
    }


    @Override
    public Category updateCategory(Long categoryId, Category category) {
        Category existingCategory = this.getCategoryById(categoryId);

        // Parent Category update (safe check)
        if (category.getParentCategory() != null && category.getParentCategory().getId() != null) {
            Category parent = categoryRepository.findById(category.getParentCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            existingCategory.setParentCategory(parent);
        } else {
            existingCategory.setParentCategory(null); // remove parent if none sent
        }

        // Update name
        if (category.getName() != null && !category.getName().trim().isEmpty()) {
            existingCategory.setName(category.getName().trim());
        }

        // Update description
        if (category.getDescription() != null && !category.getDescription().trim().isEmpty()) {
            existingCategory.setDescription(category.getDescription().trim());
        }

        // Update image
        if (category.getImage() != null && !category.getImage().trim().isEmpty()) {
            existingCategory.setImage(category.getImage().trim());
        }

        return categoryRepository.save(existingCategory);
    }


    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = this.getCategoryById(categoryId);

        List<Product> products = productRepository.findByCategoryId(categoryId);
        productRepository.deleteAll(products);

        categoryRepository.deleteAll(category.getSubcategories());

        categoryRepository.delete(category);
    }


    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId));
    }

    @Override
    public Category getCategoryByName(String name) {
        Category category = categoryRepository.findCategoryByName(name);

        if (category == null) {
            throw new CategoryNotFoundException("Category not found with name: " + name);
        }

        return category;
    }

    @Override
    public Category getParentCategory(Long categoryId) {
        Category category = this.getCategoryById(categoryId);
        Category parentCategory = category.getParentCategory();

        if (parentCategory == null) {
            throw new CategoryNotFoundException("No parent category for category id: " + categoryId);
        }

        return parentCategory;
    }

    @Override
    public List<Category> getSubCategories(Long categoryId) {
        Category category = this.getCategoryById(categoryId);
        return category.getSubcategories() != null ? category.getSubcategories() : List.of();
    }
}
