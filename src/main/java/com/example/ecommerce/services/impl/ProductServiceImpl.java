package com.example.ecommerce.services.impl;

import com.example.ecommerce.exceptions.product.ProductNotFoundException;
import com.example.ecommerce.models.Category;
import com.example.ecommerce.models.Product;
import com.example.ecommerce.models.Seller;
import com.example.ecommerce.models.Variant;
import com.example.ecommerce.repositories.ProductRepository;
import com.example.ecommerce.requests.ProductFilterRequest;
import com.example.ecommerce.response.FilterResponse;
import com.example.ecommerce.services.CategoryService;
import com.example.ecommerce.services.ProductService;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;

import org.springframework.util.CollectionUtils;


@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryService categoryService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(CategoryService categoryService,
                              ProductRepository productRepository
    ) {
        this.categoryService = categoryService;
        this.productRepository = productRepository;
    }

    public double calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice < 0) {
            throw new IllegalArgumentException("Actual price must be grater than 0");
        }

        double discount = mrpPrice - sellingPrice;
        return (discount / mrpPrice) * 100;
    }

    @Override
    public Product createProduct(Seller seller, Long categoryId, Product product) {


        Category category = categoryService.getCategoryById(categoryId);

        product.setCategory(category);

        product.setSeller(seller);


        if (product.getVariants() != null) {
            for (Variant variant : product.getVariants()) {
                variant.setProduct(product);
                double discount = calculateDiscountPercentage(
                        variant.getMrpPrice(),
                        variant.getSellingPrice());
                variant.setDiscount(discount);
            }
        }

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) {

        Product existingProduct = this.getProductById(productId);


        if (product.getName() != null && !product.getName().trim().isEmpty())
            existingProduct.setName(product.getName());

        if (product.getDescription() != null && !product.getDescription().trim().isEmpty())
            existingProduct.setDescription(product.getDescription());

        if (product.getCategory() != null) existingProduct.setCategory(product.getCategory());

        if (product.isActive() != existingProduct.isActive()) existingProduct.setActive(product.isActive());

        if (product.isFeatured() != existingProduct.isFeatured()) existingProduct.setFeatured(product.isFeatured());

        if (product.isBestSeller() != existingProduct.isBestSeller())
            existingProduct.setBestSeller(product.isBestSeller());

        return productRepository.save(existingProduct);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue();
    }

    @Override
    public List<Product> getBestSellerProducts() {
        return productRepository.findByIsBestSellerTrue();
    }

    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductsByCategoryName(String categoryName) {
        return productRepository.findByCategory_Name(categoryName);
    }

    @Override
    public List<Product> sortProducts(String sortBy, boolean ascending) {

        if (sortBy.equals("sellingPrice")) {
            return ascending
                    ? productRepository.sortBySellingPriceAsc()
                    : productRepository.sortBySellingPriceDesc();
        }

        // fall-back for normal fields
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return productRepository.findAll(sort);
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProductsSorted(keyword);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = this.getProductById(productId);

        productRepository.delete(product);
    }

    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
    }

    @Override
    public List<Product> getSimilarProducts(Long productId) {

        Product product = this.getProductById(productId);

        Long categoryId = product.getCategory().getId();

        return productRepository.findSimilarProducts(categoryId, productId);
    }

    @Override
    public FilterResponse getFilters(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);

        // Collect all variants
        List<Variant> variants = products.stream()
                .flatMap(p -> p.getVariants().stream())
                .toList();

        // Colors
        List<String> colors = variants.stream()
                .map(Variant::getColor)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Sizes
        List<String> sizes = variants.stream()
                .map(Variant::getSize)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Brands (From Seller name)
        List<String> brands = products.stream()
                .map(p -> p.getSeller().getUser().getFullName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Price range
        List<Long> prices = variants.stream()
                .map(Variant::getSellingPrice)
                .toList();

        int minPrice = prices.stream().mapToInt(Long::intValue).min().orElse(0);
        int maxPrice = prices.stream().mapToInt(Long::intValue).max().orElse(0);

        // Discount list
        List<Integer> discounts = Arrays.asList(10, 20, 30, 40, 50, 60);

        // Specifications (Map<string, list>)
        Map<String, List<String>> specs = new HashMap<>();

        variants.forEach(v -> {
            if (v.getSpecifications() != null) {
                v.getSpecifications().forEach((key, value) -> {
                    specs.computeIfAbsent(key, k -> new ArrayList<>());
                    if (!specs.get(key).contains(value)) {
                        specs.get(key).add(value);
                    }
                });
            }
        });

        return new FilterResponse(
                colors,
                sizes,
                brands,
                minPrice,
                maxPrice,
                discounts,
                specs
        );
    }

    @Override
    public List<Product> getFilteredProducts(ProductFilterRequest filterRequest) {

        System.out.println("Incoming Discount Filters: " + filterRequest.getDiscounts());


        Specification<Product> spec = (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            // Join variants ONCE
            Join<Product, Variant> variantJoin = root.join("variants", JoinType.INNER);

            // -------------------- COLOR FILTER --------------------
            if (!CollectionUtils.isEmpty(filterRequest.getColors())) {
                predicate = cb.and(predicate,
                        variantJoin.get("color").in(filterRequest.getColors()));
            }

// -------------------- DISCOUNT FILTER (nearby-range logic) --------------------
            if (!CollectionUtils.isEmpty(filterRequest.getDiscounts())) {

                List<Predicate> discountPreds = new ArrayList<>();

                double range = 5.0;

                for (Double d : filterRequest.getDiscounts()) {

                    double lower = d - range;
                    double upper = d + range;

                    discountPreds.add(
                            cb.between(variantJoin.get("discount"), lower, upper)
                    );
                }

                predicate = cb.and(predicate,
                        cb.or(discountPreds.toArray(new Predicate[0])));
            }




            // -------------------- PRICE FILTER --------------------
            if (filterRequest.getMinPrice() != null) {
                predicate = cb.and(predicate,
                        cb.ge(variantJoin.get("sellingPrice"), filterRequest.getMinPrice()));
            }

            if (filterRequest.getMaxPrice() != null) {
                predicate = cb.and(predicate,
                        cb.le(variantJoin.get("sellingPrice"), filterRequest.getMaxPrice()));
            }

            // -------------------- SPECIFICATION FILTER --------------------
            Map<String, List<String>> specs = filterRequest.getSpecifications();

            if (specs != null && !specs.isEmpty()) {

                // Join only once
                MapJoin<Variant, String, String> specJoin =
                        variantJoin.joinMap("specifications", JoinType.INNER);

                List<Predicate> specPredicates = new ArrayList<>();

                // Each spec key (ex: RAM, Storage)
                for (Map.Entry<String, List<String>> entry : specs.entrySet()) {

                    String specKey = entry.getKey();
                    List<String> specValues = entry.getValue();

                    if (!CollectionUtils.isEmpty(specValues)) {

                        // (spec_key = "RAM") AND (spec_value IN [8GB, 12GB])
                        Predicate p = cb.and(
                                cb.equal(specJoin.key(), specKey),
                                specJoin.value().in(specValues)
                        );

                        specPredicates.add(p);
                    }
                }

                // Add all spec conditions
                if (!specPredicates.isEmpty()) {
                    predicate = cb.and(predicate,
                            cb.or(specPredicates.toArray(new Predicate[0])));
                    // Using OR → product variant can match ANY selected spec option
                }
            }

            query.distinct(true);
            return predicate;
        };

        return productRepository.findAll(spec);
    }

}
