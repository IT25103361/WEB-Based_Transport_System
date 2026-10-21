package com.webtrans.foodgrocery.config;

import com.webtrans.foodgrocery.entity.*;
import com.webtrans.foodgrocery.enums.FoodRole;
import com.webtrans.foodgrocery.enums.Gender;
import com.webtrans.foodgrocery.enums.VendorType;
import com.webtrans.foodgrocery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FoodDataSeeder implements CommandLineRunner {

    private final FoodUserRepository userRepository;
    private final FoodVendorRepository vendorRepository;
    private final FoodShopRepository shopRepository;
    private final FoodProductRepository productRepository;
    private final FoodStockRepository stockRepository;
    private final FoodDeliveryPersonRepository deliveryPersonRepository;

    @Override
    public void run(String... args) {
        // Only seed Food module if food_users table is empty
        if (userRepository.count() == 0) {
            // 1. Food Administrator
            FoodUser admin = new FoodUser();
            admin.setFullName("Food Module Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword("admin");
            admin.setPhoneNumber("0771234567");
            admin.setAddress("Colombo, Sri Lanka");
            admin.setPostalCode("00100");
            admin.setGender(Gender.OTHER);
            admin.setRole(FoodRole.ADMIN);
            admin.setActive(true);
            userRepository.save(admin);

            // 2. Food Vendor Manager
            FoodUser manager = new FoodUser();
            manager.setFullName("Vendor Manager Kasun");
            manager.setEmail("manager@food.com");
            manager.setPassword("manager");
            manager.setPhoneNumber("0779876543");
            manager.setAddress("Kandy, Sri Lanka");
            manager.setPostalCode("20000");
            manager.setGender(Gender.MALE);
            manager.setRole(FoodRole.VENDOR_MANAGER);
            manager.setActive(true);
            userRepository.save(manager);

            // 3. Food Delivery Partner
            FoodUser partner = new FoodUser();
            partner.setFullName("Rider Nuwan");
            partner.setEmail("partner@food.com");
            partner.setPassword("partner");
            partner.setPhoneNumber("0712345678");
            partner.setAddress("Colombo, Sri Lanka");
            partner.setPostalCode("00100");
            partner.setGender(Gender.MALE);
            partner.setRole(FoodRole.DELIVERY_PARTNER);
            partner.setActive(true);
            FoodUser savedPartner = userRepository.save(partner);

            FoodDeliveryPerson dp = new FoodDeliveryPerson();
            dp.setUser(savedPartner);
            dp.setName(savedPartner.getFullName());
            dp.setPhone(savedPartner.getPhoneNumber());
            dp.setVehicleType("Motorbike");
            dp.setAvailable(true);
            deliveryPersonRepository.save(dp);
        }

        // Seed initial shops and products if food_shops is empty
        if (shopRepository.count() == 0) {
            FoodVendor vendor1 = new FoodVendor();
            vendor1.setName("Golden Feast Restaurant");
            vendor1.setDescription("Authentic Sri Lankan and Asian cuisine");
            vendor1.setAddress("123 Galle Road, Colombo 03");
            vendor1.setPhone("0112345678");
            vendor1.setType(VendorType.FOOD);
            vendor1.setRating(4.8);
            vendor1.setDeliveryTimeMinutes(25);
            vendor1.setDeliveryFee(150.0);
            vendor1.setActive(true);
            vendor1 = vendorRepository.save(vendor1);

            FoodShop shop1 = new FoodShop();
            shop1.setName("Golden Feast - Colombo Central");
            shop1.setDescription("Top-rated dine-in and delivery branch");
            shop1.setAddress("123 Galle Road");
            shop1.setCity("Colombo");
            shop1.setPhone("0112345678");
            shop1.setEmail("central@goldenfeast.lk");
            shop1.setType(VendorType.FOOD);
            shop1.setRating(4.8);
            shop1.setDeliveryTimeMinutes(25);
            shop1.setDeliveryFee(150.0);
            shop1.setOpeningHours("08:00 AM - 10:00 PM");
            shop1.setActive(true);
            shop1 = shopRepository.save(shop1);

            FoodShop shop2 = new FoodShop();
            shop2.setName("FreshMart Grocery");
            shop2.setDescription("Organic fruits, vegetables and household essentials");
            shop2.setAddress("45 Peradeniya Road");
            shop2.setCity("Kandy");
            shop2.setPhone("0812345678");
            shop2.setEmail("support@freshmart.lk");
            shop2.setType(VendorType.GROCERY);
            shop2.setRating(4.6);
            shop2.setDeliveryTimeMinutes(35);
            shop2.setDeliveryFee(200.0);
            shop2.setOpeningHours("07:00 AM - 09:00 PM");
            shop2.setActive(true);
            shop2 = shopRepository.save(shop2);

            // Sample products
            FoodProduct p1 = new FoodProduct();
            p1.setName("Chicken Kottu Roti");
            p1.setDescription("Spicy shredded godamba roti with tender chicken and aromatic curry sauce");
            p1.setPrice(950.0);
            p1.setCategory("Meals");
            p1.setAvailable(true);
            p1.setPopular(true);
            p1.setStockQuantity(50);
            p1.setUnit("portion");
            p1.setVendor(vendor1);
            p1.setShop(shop1);
            p1 = productRepository.save(p1);

            FoodStock s1 = new FoodStock();
            s1.setProduct(p1);
            s1.setShop(shop1);
            s1.setQuantity(50);
            s1.setMinStockLevel(10);
            s1.setUnit("portions");
            s1.setNotes("Freshly prepared daily");
            stockRepository.save(s1);

            FoodProduct p2 = new FoodProduct();
            p2.setName("Fresh Organic Apples (1kg)");
            p2.setDescription("Crisp and sweet Fuji apples directly from hill country farms");
            p2.setPrice(850.0);
            p2.setCategory("Fruits");
            p2.setAvailable(true);
            p2.setPopular(true);
            p2.setStockQuantity(100);
            p2.setUnit("kg");
            p2.setShop(shop2);
            p2 = productRepository.save(p2);

            FoodStock s2 = new FoodStock();
            s2.setProduct(p2);
            s2.setShop(shop2);
            s2.setQuantity(100);
            s2.setMinStockLevel(15);
            s2.setUnit("kg");
            s2.setNotes("Cold-stored fresh lot");
            stockRepository.save(s2);
        }
    }
}
