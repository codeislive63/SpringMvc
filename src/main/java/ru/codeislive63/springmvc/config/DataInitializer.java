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
        if (adminService.hasAnyData()) {
            return;
        }

        userService.register("admin@example.com", "admin123", "Администратор", RoleType.ADMIN);
        userService.register("customer@example.com", "cust123", "Пассажир", RoleType.CUSTOMER);

        Station minsk = adminService.createStation("MSK", "Минск");
        Station gomel = adminService.createStation("GML", "Гомель");
        Station vitebsk = adminService.createStation("VTB", "Витебск");

        Station orsha = adminService.createStation("ORS", "Орша");
        Station smolensk = adminService.createStation("SML", "Смоленск");

        Train t1 = adminService.createTrain("TRAIN-1", "Скорый 001", 30);
        Train t2 = adminService.createTrain("TRAIN-2", "Региональный 045", 30);

        Route route1 = adminService.createRoute(minsk.getId(), gomel.getId(), 320, "Минск — Гомель");
        Route route2 = adminService.createRoute(minsk.getId(), vitebsk.getId(), 280, "Минск — Витебск");

        Route mskOrsha = adminService.createRoute(minsk.getId(), orsha.getId(), 210, "Минск — Орша");
        Route orshaSml = adminService.createRoute(orsha.getId(), smolensk.getId(), 160, "Орша — Смоленск");

        adminService.createTrip(route1.getId(), t1.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(8), BigDecimal.valueOf(35));
        adminService.createTrip(route2.getId(), t2.getId(),
                LocalDateTime.now().plusHours(6), LocalDateTime.now().plusHours(10), BigDecimal.valueOf(28));

        LocalDateTime dep1 = LocalDateTime.now().withSecond(0).withNano(0).plusHours(2);
        LocalDateTime arr1 = dep1.plusHours(2);

        LocalDateTime dep2 = arr1.plusMinutes(40);
        LocalDateTime arr2 = dep2.plusHours(2);

        adminService.createTrip(mskOrsha.getId(), t2.getId(), dep1, arr1, BigDecimal.valueOf(12));
        adminService.createTrip(orshaSml.getId(), t1.getId(), dep2, arr2, BigDecimal.valueOf(18));

        Route gomelMinsk   = adminService.createRoute(gomel.getId(),   minsk.getId(), 320, "Гомель — Минск");
        Route vitebskMinsk = adminService.createRoute(vitebsk.getId(), minsk.getId(), 280, "Витебск — Минск");
        Route orshaMsk     = adminService.createRoute(orsha.getId(),   minsk.getId(), 210, "Орша — Минск");
        Route smolenskOrsha= adminService.createRoute(smolensk.getId(),orsha.getId(), 160, "Смоленск — Орша");

        adminService.createTrip(gomelMinsk.getId(), t1.getId(),
                LocalDateTime.now().plusHours(4), LocalDateTime.now().plusHours(8), BigDecimal.valueOf(35));
        adminService.createTrip(vitebskMinsk.getId(), t2.getId(),
                LocalDateTime.now().plusHours(6), LocalDateTime.now().plusHours(10), BigDecimal.valueOf(28));

        adminService.createTrip(smolenskOrsha.getId(), t1.getId(), dep2, arr2, BigDecimal.valueOf(18));

        LocalDateTime depReverse = arr2.plusMinutes(20);
        LocalDateTime arrReverse = depReverse.plusHours(2);
        adminService.createTrip(orshaMsk.getId(), t2.getId(), depReverse, arrReverse, BigDecimal.valueOf(12));
    }
}
