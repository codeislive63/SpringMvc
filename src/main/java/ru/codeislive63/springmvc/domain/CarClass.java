package ru.codeislive63.springmvc.domain;

/**
 * Класс обслуживания вагона.
 */
public enum CarClass {
    ECONOMY("Эконом"),
    COMFORT("Комфорт"),
    BUSINESS("Бизнес");

    private final String label;

    CarClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
