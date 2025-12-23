package ru.codeislive63.springmvc.domain;

public enum TicketStatus {
    BOOKED,
    PAID,
    CANCELLED,
    REFUNDED;

    /**
     * Возвращает локализованное название статуса.
     *
     * @return строковое представление статуса
     */
    public String getLabel() {
        return switch (this) {
            case BOOKED -> "ЗАБРОНИРОВАНО";
            case PAID -> "ОПЛАЧЕНО";
            case CANCELLED -> "ОТМЕНЕНО";
            case REFUNDED -> "ВОЗВРАЩЕНО";
        };
    }
}
