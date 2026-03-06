package com.coffee.pos.controller;

import com.coffee.pos.entity.Order;
import com.coffee.pos.entity.OrderItem;
import com.coffee.pos.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController 
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;


    @DeleteMapping("/delete/{id}")
public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
    try {
        orderRepository.deleteById(id);
        return ResponseEntity.ok("Đã xóa đơn hàng");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Lỗi khi xóa: " + e.getMessage());
    }
}
    @PostMapping("/save")
    public ResponseEntity<?> saveOrder(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Lưu hóa đơn chính
            Order order = new Order();
            Object totalObj = payload.get("totalAmount");
            order.setTotalAmount(Double.valueOf(totalObj.toString()));

            // 2. Lấy danh sách món từ 'cart' gửi lên
          @SuppressWarnings("unchecked")
            List<Map<String, Object>> cartItems = (List<Map<String, Object>>) payload.get("cart");
            if (cartItems != null) {
                // Đảm bảo danh sách items không bị null
                if (order.getItems() == null) {
                    order.setItems(new ArrayList<>());
                }

                for (Map<String, Object> itemMap : cartItems) {
                    OrderItem detail = new OrderItem();
                    detail.setProductName(itemMap.get("name").toString());
                    
                    // Chuyển đổi số lượng an toàn
                    Object qtyObj = itemMap.get("qty");
                    detail.setQuantity(Integer.valueOf(qtyObj.toString())); 
                    
                    detail.setPrice(Double.valueOf(itemMap.get("price").toString()));
                    detail.setOrder(order);
                    order.getItems().add(detail);
                }
            }

            orderRepository.save(order);
            return ResponseEntity.ok("Thành công");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi Controller: " + e.getMessage());
        }

    
        
    }
}