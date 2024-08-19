package com.clds.bottletalk.product.controller;

import com.clds.bottletalk.product.service.ProductService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/v1/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping("")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String search,
            @RequestHeader(value = "X-User-ID", required = false) String userId
    ) throws IOException {
        if (search != null && !search.isEmpty()) {
            if(userId != null && !userId.isEmpty()) {
                return productService.searchProducts(search, userId);
            }else {
                userId = null;
                return productService.searchProducts(search, userId);
            }
        }
        return productService.getAllProducts();
    }


    @GetMapping("/top5Keywords")
    public ResponseEntity<?> getTop5Keywords(@RequestHeader(value = "X-User-ID", required = false) String userId) throws IOException {
        if(userId != null && !userId.isEmpty()) {
            return productService.getTop5Keywords(userId);
        }else{
              userId = null;
        return productService.getTop5Keywords(userId);
        }
    }


    @GetMapping("/top5Products")
    public ResponseEntity<?> getTop5Products(@RequestHeader(value = "X-User-ID", required = false) String userId) throws IOException {
        if (userId != null && !userId.isEmpty()) {
            return  productService.getTop5Products(userId);
        }else{
            userId = null;
            return  productService.getTop5Products(userId);
        }
    }


    @GetMapping("{productId}")
    public ResponseEntity<?> getProductById(@PathVariable String productId, @RequestHeader(value = "X-User-ID", required = false) String userId) throws IOException {
        if (userId != null && !userId.isEmpty()) {
            return productService.getProduct(productId, userId);
        } else {
            return productService.getProduct(productId, null);
        }
    }

}

