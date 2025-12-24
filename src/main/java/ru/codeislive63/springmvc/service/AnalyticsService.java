package ru.codeislive63.springmvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.repository.TripRepository;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;

    /** Заполняемость по каждому рейсу: считаем занятые места и делим на вместимость. */
    public List<TripOccupancy> getTripOccupancies() {
        List<Trip> allTrips = tripRepository.findAll();
        List<TripOccupancy> result = new ArrayList<>();
        for (Trip trip : allTrips) {
            int capacity = trip.getTrain().getSeatCapacity();
            long booked = ticketRepository.countByTripIdAndStatusIn(
                    trip.getId(),
                    List.of(TicketStatus.BOOKED, TicketStatus.PAID)
            );
            double occupancy = (double) booked / (double) capacity * 100.0;
            result.add(new TripOccupancy(trip, capacity, (int) booked, occupancy));
        }
        return result;
    }

    /** Количество проданных и забронированных билетов по датам отправления (от today‑N до today). */
    public Map<LocalDate, Long> getDemandByDate(int daysBack) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(daysBack);
        // Забираем все билеты и группируем по дате отправления рейса
        var tickets = ticketRepository.findAll();
        Map<LocalDate, Long> counts = new TreeMap<>();
        tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BOOKED || t.getStatus() == TicketStatus.PAID)
                .forEach(t -> {
                    LocalDate date = t.getTrip().getDepartureTime().toLocalDate();
                    if (!date.isBefore(start) && !date.isAfter(today)) {
                        counts.merge(date, 1L, Long::sum);
                    }
                });
        // Заполняем пропущенные даты нулями
        for (int i = 0; i <= daysBack; i++) {
            LocalDate d = start.plusDays(i);
            counts.putIfAbsent(d, 0L);
        }
        return counts;
    }

    /** Распределение билетов по статусам. */
    public Map<TicketStatus, Long> getTicketStatusDistribution() {
        Map<TicketStatus, Long> dist = new EnumMap<>(TicketStatus.class);
        var tickets = ticketRepository.findAll();
        for (var t : tickets) {
            dist.merge(t.getStatus(), 1L, Long::sum);
        }
        return dist;
    }

    /** Выручка по маршрутам (оплаченные билеты). */
    public Map<String, BigDecimal> getRevenueByRoute() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.PAID)
                .forEach(t -> {
                    String key = routeLabel(t.getTrip());
                    BigDecimal price = Optional.ofNullable(t.getPrice()).orElse(BigDecimal.ZERO);
                    result.merge(key, price, BigDecimal::add);
                });
        return result;
    }

    /** Отмены/возвраты по маршрутам. */
    public Map<String, Long> getCancellationsByRoute() {
        Map<String, Long> result = new LinkedHashMap<>();
        ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED || t.getStatus() == TicketStatus.REFUNDED)
                .forEach(t -> result.merge(routeLabel(t.getTrip()), 1L, Long::sum));
        return result;
    }

    /** Популярность направлений по количеству бронирований/покупок. */
    public Map<String, Long> getRouteDemand() {
        Map<String, Long> result = new LinkedHashMap<>();
        ticketRepository.findAll().forEach(t -> result.merge(routeLabel(t.getTrip()), 1L, Long::sum));
        return result;
    }

    /** XLSX отчёт с основными срезами. */
    public byte[] buildReport() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeOccupancySheet(wb);
            writeDemandSheet(wb);
            writeRevenueSheet(wb);
            writeStatusesSheet(wb);
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось сформировать отчёт", e);
        }
    }

    private void writeOccupancySheet(Workbook wb) {
        Sheet sheet = wb.createSheet("Заполняемость");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Маршрут");
        header.createCell(1).setCellValue("Рейс");
        header.createCell(2).setCellValue("Свободно/вместимость");
        header.createCell(3).setCellValue("Заполнено (%)");
        int rowIdx = 1;
        for (TripOccupancy o : getTripOccupancies()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(o.routeLabel());
            row.createCell(1).setCellValue(o.trip().getId());
            row.createCell(2).setCellValue(o.booked() + " / " + o.capacity());
            row.createCell(3).setCellValue(BigDecimal.valueOf(o.occupancy()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
    }

    private void writeDemandSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("Спрос (14 дней)");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Дата");
        header.createCell(1).setCellValue("Билетов");
        int rowIdx = 1;
        var demand = getDemandByDate(14);
        for (var entry : demand.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey().toString());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    private void writeRevenueSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("Выручка");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Маршрут");
        header.createCell(1).setCellValue("Выручка (BYN)");
        int rowIdx = 1;
        for (var entry : getRevenueByRoute().entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue().doubleValue());
        }
    }

    private void writeStatusesSheet(Workbook wb) {
        Sheet sheet = wb.createSheet("Статусы");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Статус");
        header.createCell(1).setCellValue("Кол-во");
        int rowIdx = 1;
        for (var entry : getTicketStatusDistribution().entrySet()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(entry.getKey().getLabel());
            row.createCell(1).setCellValue(entry.getValue());
        }
    }

    private String routeLabel(Trip trip) {
        if (trip == null || trip.getRoute() == null) return "Маршрут";
        var r = trip.getRoute();
        return (r.getOrigin() != null ? r.getOrigin().getName() : "?")
                + " — "
                + (r.getDestination() != null ? r.getDestination().getName() : "?");
    }

    /** DTO для графика заполняемости. */
    public record TripOccupancy(Trip trip, int capacity, int booked, double occupancy) {
        public String routeLabel() {
            return trip.getRoute().getOrigin().getName() + " — " + trip.getRoute().getDestination().getName();
        }
        public String formattedDeparture() {
            return trip.getDepartureTime().toString();
        }
    }
}
