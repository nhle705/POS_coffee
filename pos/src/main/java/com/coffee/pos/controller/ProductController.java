package com.coffee.pos.controller;

import com.coffee.pos.entity.Product;
import com.coffee.pos.repository.OrderRepository;
import com.coffee.pos.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;
import com.coffee.pos.entity.Order;
@Controller
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private OrderRepository orderRepository; 

    @GetMapping("/")
    public String viewHomePage(Model model) {
        model.addAttribute("listProducts", productService.getAllProducts());
        return "index"; 
    }


@GetMapping("/showNewProductForm")
public String showNewProductForm(Model model) {
    Product product = new Product();
    model.addAttribute("product", product);
    return "new_product"; 
}


@PostMapping("/saveProduct")
public String saveProduct(@ModelAttribute("product") Product product) {
    productService.saveProduct(product);
    return "redirect:/";
}

@GetMapping("/deleteProduct/{id}")
public String deleteProduct(@PathVariable(value = "id") Long id) {
    this.productService.deleteProductById(id);
    return "redirect:/";
}

@GetMapping("/showFormForUpdate/{id}")
public String showFormForUpdate(@PathVariable(value = "id") Long id, Model model) {
    Product product = productService.getProductById(id);
    model.addAttribute("product", product);
    return "update_product"; 
}  

@GetMapping("/pos")
public String showPosPage(Model model) {
   model.addAttribute("listProducts", productService.getAllProducts());
    return "pos_page";
}

@GetMapping("/order-history")
public String viewOrderHistory(Model model) {
    List<Order> listOrders = orderRepository.findAll();
    model.addAttribute("listOrders", listOrders);
    return "order_history";
}

}