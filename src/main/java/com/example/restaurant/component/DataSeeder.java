package com.example.restaurant.component;

import com.example.restaurant.model.CustomerOrder;
import com.example.restaurant.model.MenuItem;
import com.example.restaurant.model.RestaurantTable;
import com.example.restaurant.repository.CustomerOrderRepository;
import com.example.restaurant.repository.MenuItemRepository;
import com.example.restaurant.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final CustomerOrderRepository orderRepository;

    public DataSeeder(MenuItemRepository menuItemRepository, RestaurantTableRepository tableRepository,
            CustomerOrderRepository orderRepository) {
        this.menuItemRepository = menuItemRepository;
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (menuItemRepository.count() == 0) {
            menuItemRepository.save(
                    new MenuItem("Bruschetta", "Toasted bread with tomatoes/basil", new BigDecimal("8.50"), "Starter"));
            menuItemRepository
                    .save(new MenuItem("Grilled Salmon", "Fresh salmon with sides", new BigDecimal("24.00"), "Main"));
            menuItemRepository.save(new MenuItem("Steak Frites", "Ribeye with fries", new BigDecimal("32.50"), "Main"));
            menuItemRepository
                    .save(new MenuItem("Tiramisu", "Classic espresso dessert", new BigDecimal("9.00"), "Dessert"));
            menuItemRepository.save(new MenuItem("Iced Tea", "Freshly brewed", new BigDecimal("3.50"), "Drink"));
        }

        if (tableRepository.count() == 0) {
            tableRepository.save(new RestaurantTable("T1", 2, "AVAILABLE"));
            tableRepository.save(new RestaurantTable("T2", 4, "OCCUPIED"));
            tableRepository.save(new RestaurantTable("T3", 4, "AVAILABLE"));
            tableRepository.save(new RestaurantTable("T4", 6, "AVAILABLE"));
        }

        if (orderRepository.count() == 0) {
            RestaurantTable t2 = tableRepository.findAll().stream().filter(t -> t.getTableNumber().equals("T2"))
                    .findFirst().orElse(null);
            if (t2 != null) {
                CustomerOrder order1 = new CustomerOrder();
                order1.setRestaurantTable(t2);
                order1.setOrderTime(LocalDateTime.now().minusMinutes(20));
                order1.setStatus("SERVED");

                MenuItem salmon = menuItemRepository.findAll().stream()
                        .filter(m -> m.getName().equals("Grilled Salmon")).findFirst().orElse(null);
                MenuItem tea = menuItemRepository.findAll().stream().filter(m -> m.getName().equals("Iced Tea"))
                        .findFirst().orElse(null);

                if (salmon != null && tea != null) {
                    order1.getItems().add(new com.example.restaurant.model.OrderItem(order1, salmon, 1));
                    order1.getItems().add(new com.example.restaurant.model.OrderItem(order1, tea, 2));
                }

                orderRepository.save(order1);
            }
        }
    }
}
