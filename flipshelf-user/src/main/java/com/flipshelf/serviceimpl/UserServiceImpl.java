package com.flipshelf.serviceimpl;

import com.flipshelf.model.Cart;
import com.flipshelf.model.Product;
import com.flipshelf.model.Purchase;
import com.flipshelf.repository.CartRepository;
import com.flipshelf.repository.PurchaseRepository;
import com.flipshelf.service.AuthClient;
import com.flipshelf.service.MailService;
import com.flipshelf.service.ProductClient;
import com.flipshelf.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private AuthClient authClient;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private MailService mailService;

    @Override
    public ResponseEntity<?> viewAllProducts(String token) {
        return productClient.getAllProducts(token);
    }

    @Override
    @Transactional
    public Cart addToCart(Cart cart) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Adding to cart for user: {}, product ID: {}", userEmail, cart.getProductId());
        cart.setUserEmail(userEmail);
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Purchase purchaseItems(String token, Purchase purchase) {
        logger.info("Creating order for product ID: {}", purchase.getProductId());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        purchase.setUserEmail(userEmail);
        Product product = productClient.updateStock(token, purchase.getProductId(), purchase.getQuantity());
        purchase.setName(product.getName());
        purchase.setTotalPrice(purchase.getQuantity() * product.getPrice());
        purchase.setOrderDate(LocalDateTime.now());
        Optional<Cart> cartItem = cartRepository.findByUserEmailAndProductId(userEmail, purchase.getProductId());
        if (cartItem.isPresent()) {
            logger.info("Deleting CartItem for user: {}, product Id: {}", userEmail, purchase.getProductId());
            cartRepository.delete(cartItem.get());
        } else {
            logger.info("No Cart Item found...");
        }
        Purchase saved = purchaseRepository.save(purchase);
        mailService.sendPurchaseConfirmation(userEmail, saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Purchase> getPurchaseHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Fetching order history for user: {}", email);
        return purchaseRepository.findByUserEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cart> getSavedCart() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Fetching cart for user: {}", userEmail);
        return cartRepository.findByUserEmail(userEmail);
    }

    @Override
    @Transactional
    public void removeFromCart(Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug("Removing cart item ID: {} for user: {}", id, email);
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found with ID: " + id));
        if (!cart.getUserEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot delete another user's cart item");
        }
        cartRepository.delete(cart);
    }
}
