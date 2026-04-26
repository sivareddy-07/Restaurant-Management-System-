package com.example.restaurant.controller;

import com.example.restaurant.model.MenuItem;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.CustomerOrderRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.RestaurantTableRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/")
public class DashboardController {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final CustomerOrderRepository orderRepository;

    public DashboardController(MenuItemRepository menuItemRepository, RestaurantTableRepository tableRepository,
            CustomerOrderRepository orderRepository) {
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("menuItemsCount", menuItemRepository.count());
        model.addAttribute("tablesCount", tableRepository.count());
        model.addAttribute("activeOrders", orderRepository.findByStatusNot("COMPLETED").size());
        return "dashboard";
    }

    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("menuItems", menuItemRepository.findAll());
        return "menu";
    }

    @PostMapping("/menu/add")
    public String addMenuItem(@RequestParam String name, @RequestParam String description,
            @RequestParam BigDecimal price, @RequestParam String category) {
        MenuItem item = new MenuItem(name, description, price, category);
        menuItemRepository.save(item);
        return "redirect:/menu";
    }

    @GetMapping("/tables")
    public String tables(Model model) {
        model.addAttribute("tables", tableRepository.findAll());
        return "tables";
    }

    @PostMapping("/tables/add")
    public String addTable(@RequestParam String tableNumber, @RequestParam int capacity) {
        RestaurantTable table = new RestaurantTable(tableNumber, capacity, "AVAILABLE");
        tableRepository.save(table);
        return "redirect:/tables";
    }

    @PostMapping("/tables/delete")
    public String deleteTable(@RequestParam Long id) {
        tableRepository.deleteById(id);
        return "redirect:/tables";
    }

    @PostMapping("/tables/status")
    public String updateTableStatus(@RequestParam Long id, @RequestParam String status) {
        RestaurantTable table = tableRepository.findById(id).orElse(null);
        if (table != null) {
            table.setStatus(status);
            tableRepository.save(table);
        }
        return "redirect:/tables";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("availableTables", tableRepository.findByStatus("AVAILABLE"));
        model.addAttribute("menuItems", menuItemRepository.findAll());
        return "orders";
    }

    @PostMapping("/orders/add")
    public String addOrder(@RequestParam Long tableId, @RequestParam Long menuItemId, @RequestParam int quantity) {
        RestaurantTable table = tableRepository.findById(tableId).orElse(null);
        MenuItem menuItem = menuItemRepository.findById(menuItemId).orElse(null);

        if (table != null && menuItem != null) {
            com.example.restaurant.model.CustomerOrder order = new com.example.restaurant.model.CustomerOrder(table,
                    "PREPARING", java.time.LocalDateTime.now());
            com.example.restaurant.model.OrderItem item = new com.example.restaurant.model.OrderItem(order, menuItem,
                    quantity);
            order.getItems().add(item);

            table.setStatus("OCCUPIED");
            tableRepository.save(table);
            orderRepository.save(order);
        }
        return "redirect:/orders";
    }

    @GetMapping("/billing")
    public String billing(Model model) {
        model.addAttribute("orders", orderRepository.findByStatusNot("COMPLETED"));
        return "billing";
    }

    @PostMapping("/billing/pay")
    public String payOrder(@RequestParam Long orderId) {
        com.example.restaurant.model.CustomerOrder order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus("COMPLETED");
            RestaurantTable table = order.getRestaurantTable();
            if (table != null) {
                table.setStatus("AVAILABLE");
                tableRepository.save(table);
            }
            orderRepository.save(order);
            return "redirect:/billing/receipt?orderId=" + order.getId();
        }
        return "redirect:/billing";
    }

    @GetMapping("/billing/receipt")
    public String receipt(@RequestParam Long orderId, Model model) {
        com.example.restaurant.model.CustomerOrder order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            model.addAttribute("order", order);
            return "receipt";
        }
        return "redirect:/billing";
    }
}
