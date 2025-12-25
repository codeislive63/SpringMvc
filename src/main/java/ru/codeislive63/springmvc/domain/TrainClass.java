package ru.codeislive63.springmvc.domain;

/**
 * Класс обслуживания вагона.
 */
public enum TrainClass {
    ECONOMY("Эконом"),
    COMFORT("Комфорт"),
    BUSINESS("Бизнес");

    private final String label;

    TrainClass(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
