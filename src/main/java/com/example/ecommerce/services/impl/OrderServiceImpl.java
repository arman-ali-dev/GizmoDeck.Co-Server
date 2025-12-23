package com.example.ecommerce.services.impl;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.exceptions.address.AddressNotFoundException;
import com.example.ecommerce.exceptions.cart.EmptyCartException;
import com.example.ecommerce.exceptions.order.OrderNotFoundException;
import com.example.ecommerce.models.*;
import com.example.ecommerce.repositories.*;
import com.example.ecommerce.requests.CheckoutRequest;
import com.example.ecommerce.requests.DirectOrderRequest;
import com.example.ecommerce.response.StripeResponse;
import com.example.ecommerce.services.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    final private CartRepository cartRepository;
    final private OrderRepository orderRepository;
    final private AddressRepository addressRepository;
    final private PaymentRepository paymentRepository;
    final private TransactionRepository transactionRepository;
    final private CartService cartService;
    final private ProductService productService;
    final private VariantService variantService;
    final private AddressService addressService;
    final private CouponUsageRepository couponUsageRepository;

    @Value("${stripe.secretKey}")
    private String secretKey;


    @Autowired
    public OrderServiceImpl(CartRepository cartRepository,
                            OrderRepository orderRepository,
                            AddressRepository addressRepository,
                            PaymentRepository paymentRepository,
                            TransactionRepository transactionRepository,
                            CartService cartService,
                            ProductService productService,
                            VariantService variantService,
                            AddressService addressService,
                            CouponUsageRepository couponUsageRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.variantService = variantService;
        this.addressService = addressService;
        this.couponUsageRepository = couponUsageRepository;
    }

    @Override
    @Transactional
    public Order createOrder(CheckoutRequest request, User user) {

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EmptyCartException("Cart is empty! Cannot create order."));

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty!");
        }

        long finalPrice = cart.getTotalPrice();
        long discount = cart.getDiscount();

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException("Invalid address selected"));

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);

        Seller seller = cartItems.getFirst().getProduct().getSeller();
        order.setSeller(seller);

        order.setDiscount((double) discount);
        order.setFinalAmount(finalPrice);
        order.setTotalAmount((double) finalPrice);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);

        // store coupon used here
        CouponCode appliedCoupon = cart.getCouponCode();
        order.setCouponCode(appliedCoupon);

        order.setDeliverDateTime(LocalDateTime.now().plusDays(7));
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(4));

        List<OrderItem> items = new ArrayList<>();

        for (CartItem c : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(c.getProduct());
            item.setVariant(c.getVariant());
            item.setQuantity(c.getQuantity());

            long itemTotal = c.getVariant().getSellingPrice() * c.getQuantity();
            item.setTotalAmount((double) itemTotal);
            item.setDiscount("0");
            item.setFinalAmount((double) itemTotal);

            items.add(item);
        }

        order.setOrderItems(items);

        // STEP 1: SAVE ORDER
        Order savedOrder = orderRepository.save(order);

        // SAVE COUPON USAGE (VERY IMPORTANT)
        if (appliedCoupon != null) {
            CouponUsage usage = new CouponUsage();
            usage.setUser(user);
            usage.setCoupon(appliedCoupon);
            usage.setUsedAt(LocalDateTime.now());
            couponUsageRepository.save(usage);
        }

        //  CLEAR COUPON FROM CART
        cart.setCouponCode(null);
        cart.setDiscount(0L);
        cart.setTotalPrice(cart.getSubTotal());
        cartRepository.save(cart);

        //  CLEAR CART ITEMS
        cartService.clearCart(user.getId());

        return savedOrder;
    }

    @Override
    @Transactional
    public StripeResponse checkoutProducts(CheckoutRequest request, User user) {

        Stripe.apiKey = secretKey;

        // 1. Create Order
        Order order = this.createOrder(request, user);

        long amountInPaise = (long) (order.getFinalAmount() * 100);

        if (amountInPaise < 5000) {
            throw new RuntimeException("Minimum payment amount for Stripe is ₹50.");
        }

        // 2. Stripe Line Item
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("inr")
                                        .setUnitAmount(amountInPaise)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Order #" + order.getId())
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        // 3. Metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", String.valueOf(order.getId()));

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:5173/order-placed")
                        .setCancelUrl("http://localhost:5173/payment-failed")
                        .addLineItem(lineItem)
                        .putAllMetadata(metadata)
                        .build();

        try {
            Session session = Session.create(params);

            // 4. Create Payment (According to your model)
            Payment payment = new Payment();
            payment.setAmount(order.getFinalAmount());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setUser(user);
            payment.getOrders().add(order);
            payment.setPaymentLinkId(session.getId());  // session-id store

            Payment savedPayment = paymentRepository.save(payment);

            // 5. Create Transaction (According to your model)
            Transaction transaction = new Transaction();
            transaction.setCustomer(user);
            transaction.setOrder(order);
            transaction.setSeller(order.getSeller()); // Seller belongs to order
            transaction.setAmount((double) order.getFinalAmount());

            transactionRepository.save(transaction);

            // 6. Response
            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created!")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException ex) {
            throw new RuntimeException("Stripe session creation failed: " + ex.getMessage());
        }
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = this.getOrderById(orderId);
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled.");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Completed orders cannot be cancelled.");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        // if payment was completed then mark as REFUNDED
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));

        order.setOrderStatus(orderStatus);

        if (orderStatus == OrderStatus.CANCELLED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrdersBySeller(Long sellerId) {
        List<Order> allOrders = orderRepository.findAll();

        return allOrders.stream()
                .filter(order -> order.getOrderItems().stream()
                        .anyMatch(item -> item.getProduct().getSeller().getId().equals(sellerId)))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())  // latest first
                .toList();
    }

    @Override
    public Order getOrderBySeller(Long sellerId, Long orderId) throws AccessDeniedException {
        Order order = this.getOrderById(orderId);

        boolean ownsProduct = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(sellerId));

        if (!ownsProduct) {
            throw new AccessDeniedException("You are not authorized to view this order!");
        }

        return order;
    }

    @Override
    public Order directOrder(DirectOrderRequest req, User user) {
        Product product = productService.getProductById(req.getProductId());
        Variant variant = variantService.getVariant(req.getVariantId());

        int qty = req.getQuantity() == null ? 1 : req.getQuantity();
        Address address = addressService.getAddressById(req.getAddressId());

        long itemPrice = variant.getSellingPrice() * qty;

        Order order = new Order();
        order.setUser(user);
        order.setAddress(address);
        order.setSeller(product.getSeller());

        order.setTotalAmount((double) itemPrice);
        order.setFinalAmount(itemPrice);
        order.setDiscount(0.0);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);

        order.setCouponCode(null);

        order.setDeliverDateTime(LocalDateTime.now().plusDays(7));
        order.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(4));

        // Order Item
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setVariant(variant);
        item.setQuantity(qty);

        item.setTotalAmount((double) itemPrice);
        item.setFinalAmount((double) itemPrice);
        item.setDiscount("0");

        order.setOrderItems(List.of(item));

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public StripeResponse directCheckout(DirectOrderRequest req, User user) {

        Stripe.apiKey = secretKey;

        // 1. Create Order
        Order order = this.directOrder(req, user);

        long amountInPaise = (long) (order.getFinalAmount() * 100);

        if (amountInPaise < 5000) {
            throw new RuntimeException("Minimum payment amount for Stripe is ₹50");
        }

        // 2. Stripe Line Item
        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("inr")
                                        .setUnitAmount(amountInPaise)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName("Order #" + order.getId())
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        // 3. Metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", String.valueOf(order.getId()));

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:5173/order-placed")
                        .setCancelUrl("http://localhost:5173/payment-failed")
                        .addLineItem(lineItem)
                        .putAllMetadata(metadata)
                        .build();

        try {
            Session session = Session.create(params);

            // 4. Create payment record
            Payment payment = new Payment();
            payment.setAmount(order.getFinalAmount());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setUser(user);
            payment.getOrders().add(order);
            payment.setPaymentLinkId(session.getId());

            paymentRepository.save(payment);

            // 5. Create transaction
            Transaction transaction = new Transaction();
            transaction.setCustomer(user);
            transaction.setOrder(order);
            transaction.setSeller(order.getSeller());
            transaction.setAmount((double) order.getFinalAmount());

            transactionRepository.save(transaction);

            // 6. Return Stripe session
            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Stripe session created!")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException ex) {
            throw new RuntimeException("Stripe session creation failed: " + ex.getMessage());
        }
    }

}
