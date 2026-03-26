package com.coffee.pos.service;

import com.coffee.pos.entity.Product;
import com.coffee.pos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

   
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}