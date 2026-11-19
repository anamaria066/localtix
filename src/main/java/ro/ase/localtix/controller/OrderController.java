package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ro.ase.localtix.model.Order;
import ro.ase.localtix.repository.OrderRepository;

import java.util.Objects;

/**
 * Order confirmation page (US-11): shown after a successful payment.
 * A customer can only see their own orders.
 */
@Controller
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders/{id}")
    public String showOrder(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(ProfileController.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login";
        }
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            return "redirect:/events";
        }
        model.addAttribute("order", order);
        return "order-confirmation";
    }
}
