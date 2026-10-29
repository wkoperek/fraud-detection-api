package com.koperek.risk;

public enum RiskLevel {
    VERY_LOW(1),
    LOW(2),
    MEDIUM(3),
    HIGH(4),
    VERY_HIGH(5);

    private final int score;

    RiskLevel(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}