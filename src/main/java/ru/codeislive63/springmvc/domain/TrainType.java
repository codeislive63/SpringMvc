package ru.codeislive63.springmvc.domain;

/**
 * Тип поезда для фильтрации и отображения.
 */
public enum TrainType {
    HIGH_SPEED("Скоростной"),
    EXPRESS("Скорый"),
    REGIONAL("Региональный");

    private final String label;

    TrainType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
