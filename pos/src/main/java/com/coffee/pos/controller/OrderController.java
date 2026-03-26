package com.coffee.pos.controller;

import com.coffee.pos.entity.Order;
import com.coffee.pos.entity.OrderItem;
import com.coffee.pos.repository.OrderRepository;
import com.coffee.pos.service.MailService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController 
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private MailService mailService; 

    
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            order.setOrderDate(LocalDateTime.now());
            if (order.getStatus() == null) {
                order.setStatus(Order.OrderStatus.PENDING);
            }
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    item.setOrder(order);
                }
            }
            order.setTotalAmount(calculateTotal(order));
            orderRepository.save(order);
            return ResponseEntity.ok("Lưu đơn thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi lưu đơn: " + e.getMessage());
        }
    }

    
    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendEmailInvoice(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new Exception("Không thấy đơn"));
            String customerEmail = payload.get("email");
            
           
            String qrUrl = "https://img.vietqr.io/image/MBBank-098722228888-compact2.png?amount=" 
                            + order.getTotalAmount() + "&addInfo=Thanh%20toan%20Huy%20Coffee%20" + id;

            

StringBuilder html = new StringBuilder();
html.append("<div style='background-color:#f1f5f9; padding:20px; font-family:sans-serif;'>");
html.append("<div style='max-width:500px; margin:auto; background:#fff; border-radius:15px; padding:30px; box-shadow:0 4px 6px rgba(0,0,0,0.1);'>");
html.append("<h1 style='color:#fbbf24; text-align:center; background:#0f172a; padding:20px; border-radius:10px; margin-bottom:20px;'>HUY COFFEE</h1>");


html.append("<h3 style='border-bottom:2px solid #f1f5f9; padding-bottom:10px;'>Chi tiết đơn hàng:</h3>");
html.append("<table style='width:100%; border-collapse:collapse; margin-bottom:20px;'>");
html.append("<thead><tr style='text-align:left; color:#64748b; font-size:14px;'><th>Món</th><th>SL</th><th>Giá</th></tr></thead>");
html.append("<tbody>");


if (order.getItems() != null) {
    for (OrderItem item : order.getItems()) {
        html.append("<tr style='border-bottom:1px solid #f1f5f9;'>");
        html.append("<td style='padding:10px 0;'>").append(item.getProductName()).append("</td>");
        html.append("<td style='padding:10px 0;'>x").append(item.getQuantity()).append("</td>");
        html.append("<td style='padding:10px 0; text-align:right;'>").append(String.format("%,.0f", item.getPrice())).append(" đ</td>");
        html.append("</tr>");
    }
}
html.append("</tbody></table>");


html.append("<div style='background:#f8fafc; padding:15px; border-radius:10px; text-align:center;'>");
html.append("<p style='margin:0; color:#64748b;'>Quét mã để thanh toán nhanh:</p>");
html.append("<img src='").append(qrUrl).append("' width='200' style='margin:15px auto; border-radius:10px;' />");
html.append("<h2 style='margin:10px 0; color:#10b981;'>TỔNG: ").append(String.format("%,.0f", order.getTotalAmount())).append(" đ</h2>");
html.append("</div>");

html.append("<p style='text-align:center; color:#94a3b8; font-size:12px; margin-top:20px;'>Cảm ơn quý khách đã ủng hộ Huy Coffee!</p>");
html.append("</div></div>");


mailService.sendEmailAsync(customerEmail, "☕ Hóa đơn Huy Coffee - #" + id, html.toString());

          
            return ResponseEntity.ok("Hệ thống đang gửi mail ngầm, quý khách vui lòng kiểm tra sau ít giây.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi xử lý yêu cầu: " + e.getMessage());
        }
    }

 
    @GetMapping("/revenue")
    public ResponseEntity<?> getTotalRevenue() {
        try {
            List<Order> orders = orderRepository.findAll();
            double total = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PAID)
                .mapToDouble(Order::getTotalAmount).sum();
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(0.0);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/pay/{id}")
    public ResponseEntity<?> payOrder(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy đơn"));
            order.setPaymentMethod(payload.get("method"));
            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);
            return ResponseEntity.ok("Thanh toán thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Order order = orderRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy đơn"));
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setCancelReason(payload.getOrDefault("reason", "Khách hủy đơn"));
            orderRepository.save(order);
            return ResponseEntity.ok("Đã hủy đơn");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<?> mergeOrders(@RequestBody List<Long> orderIds) {
        try {
            List<Order> ordersToMerge = orderRepository.findAllById(orderIds);
            if (ordersToMerge.isEmpty()) return ResponseEntity.badRequest().body("Không có đơn để gộp");

            Order mainOrder = new Order();
            mainOrder.setTax(ordersToMerge.get(0).getTax());
            mainOrder.setStatus(Order.OrderStatus.PENDING);
            mainOrder.setOrderDate(LocalDateTime.now());
            mainOrder.setItems(new ArrayList<>());
            
            for (Order oldOrder : ordersToMerge) {
                for (OrderItem item : oldOrder.getItems()) {
                    OrderItem newItem = new OrderItem();
                    newItem.setProductName(item.getProductName());
                    newItem.setQuantity(item.getQuantity());
                    newItem.setPrice(item.getPrice());
                    newItem.setOrder(mainOrder);
                    mainOrder.getItems().add(newItem);
                }
                oldOrder.setStatus(Order.OrderStatus.CANCELLED);
                oldOrder.setCancelReason("Đã gộp vào đơn mới");
            }
            
            mainOrder.setTotalAmount(calculateTotal(mainOrder));
            orderRepository.save(mainOrder);
            orderRepository.saveAll(ordersToMerge);
            return ResponseEntity.ok("Gộp đơn thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi gộp: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/split")
    public ResponseEntity<?> splitOrder(@RequestBody Map<String, Object> payload) {
        try {
            Long originalId = Long.valueOf(payload.get("originalOrderId").toString());
            List<Map<String, Object>> splitItems = (List<Map<String, Object>>) payload.get("items");
            Order originalOrder = orderRepository.findById(originalId).orElseThrow(() -> new Exception("Không thấy đơn"));
            
            Order newOrder = new Order();
            newOrder.setTax(originalOrder.getTax());
            newOrder.setStatus(Order.OrderStatus.PENDING);
            newOrder.setOrderDate(LocalDateTime.now());
            newOrder.setItems(new ArrayList<>());

            for (Map<String, Object> sItem : splitItems) {
                String name = sItem.get("name").toString();
                int qtySplit = Integer.parseInt(sItem.get("qty").toString());
                
                Iterator<OrderItem> it = originalOrder.getItems().iterator();
                while (it.hasNext()) {
                    OrderItem oItem = it.next();
                    if (oItem.getProductName().equals(name)) {
                        OrderItem newItem = new OrderItem();
                        newItem.setProductName(name);
                        newItem.setQuantity(qtySplit);
                        newItem.setPrice(oItem.getPrice());
                        newItem.setOrder(newOrder);
                        newOrder.getItems().add(newItem);
                        
                        if (qtySplit >= oItem.getQuantity()) {
                            it.remove(); 
                        } else {
                            oItem.setQuantity(oItem.getQuantity() - qtySplit);
                        }
                        break;
                    }
                }
            }
            
            originalOrder.setTotalAmount(calculateTotal(originalOrder));
            newOrder.setTotalAmount(calculateTotal(newOrder));
            
            orderRepository.save(originalOrder);
            orderRepository.save(newOrder);
            
            return ResponseEntity.ok("Tách đơn thành công");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi tách: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getDailyStats(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime start = localDate.atStartOfDay();
            LocalDateTime end = localDate.atTime(23, 59, 59);

            List<Order> dayOrders = orderRepository.findByOrderDateBetweenAndStatus(start, end, Order.OrderStatus.PAID);

            List<Order> morning = dayOrders.stream().filter(o -> o.getOrderDate().getHour() >= 6 && o.getOrderDate().getHour() < 14).toList();
            List<Order> afternoon = dayOrders.stream().filter(o -> o.getOrderDate().getHour() >= 14 && o.getOrderDate().getHour() < 22).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("totalDayRevenue", dayOrders.stream().mapToDouble(Order::getTotalAmount).sum());
            
            Map<String, Object> mShift = new HashMap<>();
            mShift.put("count", morning.size());
            mShift.put("revenue", morning.stream().mapToDouble(Order::getTotalAmount).sum());
            
            Map<String, Object> aShift = new HashMap<>();
            aShift.put("count", afternoon.size());
            aShift.put("revenue", afternoon.stream().mapToDouble(Order::getTotalAmount).sum());

            response.put("morningShift", mShift);
            response.put("afternoonShift", aShift);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi thống kê: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try {
            orderRepository.deleteById(id);
            return ResponseEntity.ok("Đã xóa vĩnh viễn đơn hàng #" + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi xóa đơn: " + e.getMessage());
        }
    }

    @GetMapping("/revenue-7days")
    public ResponseEntity<?> getRevenueLast7Days() {
        try {
            List<Map<String, Object>> stats = new ArrayList<>();
            LocalDate today = LocalDate.now();

            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.atTime(23, 59, 59);

                double dailyRev = orderRepository.findByOrderDateBetweenAndStatus(start, end, Order.OrderStatus.PAID)
                        .stream().mapToDouble(Order::getTotalAmount).sum();

                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date.format(DateTimeFormatter.ofPattern("dd/MM")));
                dayData.put("revenue", dailyRev);
                stats.add(dayData);
            }
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private double calculateTotal(Order o) {
        if (o.getItems() == null || o.getItems().isEmpty()) return 0.0;
        double base = o.getItems().stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        double taxRate = (o.getTax() != null) ? o.getTax() : 10.0;
        double discountRate = (o.getDiscount() != null) ? o.getDiscount() : 0.0;
        return base * (1 + taxRate/100) * (1 - discountRate/100);
    }
}