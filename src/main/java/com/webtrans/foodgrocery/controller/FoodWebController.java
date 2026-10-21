package com.webtrans.foodgrocery.controller;

import com.webtrans.auth.model.User;
import com.webtrans.foodgrocery.entity.FoodUser;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.service.FoodCustomerBridgeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodWebController {

    private final FoodCustomerBridgeService bridgeService;

    private FoodUser resolveFoodUser(HttpSession session) {
        return bridgeService.resolveCurrentUser(session);
    }

    @GetMapping({"", "/", "/index", "/index.html"})
    public String root() {
        return "redirect:/food/customer";
    }

    @GetMapping({"/customer", "/customer.html"})
    public String customer(Model model, HttpSession session) {
        FoodUser foodUser = resolveFoodUser(session);
        model.addAttribute("foodUser", foodUser);
        return "foodgrocery/customer";
    }

    @GetMapping({"/login", "/login.html", "/register", "/register.html"})
    public String login() {
        return "foodgrocery/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            // Only clear Food identities; preserve global loggedUser, driverUser, and adminUser
            session.removeAttribute("foodUser");
            session.removeAttribute("foodRole");
        }
        return "redirect:/food/login";
    }

    @GetMapping({"/admin", "/admin.html"})
    public String admin(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || user.getRole() != FoodRole.ADMIN) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/admin";
    }

    @GetMapping({"/shops", "/shops.html"})
    public String shops(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.VENDOR_MANAGER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/shops";
    }

    @GetMapping({"/products", "/products.html"})
    public String products(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.VENDOR_MANAGER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/products";
    }

    @GetMapping({"/stock", "/stock.html"})
    public String stock(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.VENDOR_MANAGER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/stock";
    }

    @GetMapping({"/orders", "/orders.html"})
    public String orders(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null) {
            return "redirect:/login?redirect=/food/orders";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/orders";
    }

    @GetMapping({"/deliveries", "/deliveries.html"})
    public String deliveries(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.VENDOR_MANAGER && user.getRole() != FoodRole.DELIVERY_PARTNER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/deliveries";
    }

    @GetMapping({"/tracking", "/tracking.html"})
    public String tracking(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        model.addAttribute("foodUser", user);
        return "foodgrocery/tracking";
    }

    @GetMapping({"/vendor", "/vendor.html"})
    public String vendor(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        model.addAttribute("foodUser", user);
        return "foodgrocery/vendor";
    }

    @GetMapping({"/vendor_manager", "/vendor_manager.html"})
    public String vendorManager(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.VENDOR_MANAGER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/vendor_manager";
    }

    @GetMapping({"/checkout", "/checkout.html"})
    public String checkout(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null) {
            return "redirect:/login?redirect=/food/checkout";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/checkout";
    }

    @GetMapping({"/partner", "/partner.html", "/courier", "/courier.html"})
    public String partner(Model model, HttpSession session) {
        FoodUser user = resolveFoodUser(session);
        if (user == null || (user.getRole() != FoodRole.ADMIN && user.getRole() != FoodRole.DELIVERY_PARTNER)) {
            return "redirect:/food/login?error=auth_required";
        }
        model.addAttribute("foodUser", user);
        return "foodgrocery/partner";
    }
}
