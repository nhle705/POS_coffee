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

    // 1. Hàm lấy danh sách (phục vụ trang chủ)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 2. Hàm lưu (phục vụ Thêm và Sửa)
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    // 3. Hàm xóa
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    // 4. Hàm tìm theo ID (phục vụ nút Sửa - Phải nằm riêng thế này)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}