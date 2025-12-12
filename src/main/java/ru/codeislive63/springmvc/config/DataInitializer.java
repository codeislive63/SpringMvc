package ru.codeislive63.springmvc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.codeislive63.springmvc.domain.RoleType;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.service.AdminService;
import ru.codeislive63.springmvc.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final UserService userService;

    @Override
    public void run(String... args) {
        userService.register("admin@example.com", "admin123", "Администратор", RoleType.ADMIN);
        userService.register("customer@example.com", "cust123", "Пассажир", RoleType.CUSTOMER);

        Station minsk = adminService.createStation("MSK", "Минск");
        Station gomel = adminService.createStation("GML", "Гомель");
        Station vitebsk = adminService.createStation("VTB", "Витебск");

        Train t1 = adminService.createTrain("TRAIN-1", "Скорый 001", 120);
        Train t2 = adminService.createTrain("TRAIN-2", "Региональный 045", 80);

        Route route1 = adminService.createRoute(minsk.getId(), gomel.getId(), 320, "Минск — Гомель");
        Route route2 = adminService.createRoute(minsk.getId(), vitebsk.getId(), 280, "Минск — Витебск");

        adminService.createTrip(route1.getId(), t1.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(8), BigDecimal.valueOf(35));
        adminService.createTrip(route2.getId(), t2.getId(),
                LocalDateTime.now().plusHours(6), LocalDateTime.now().plusHours(10), BigDecimal.valueOf(28));
    }
}
