package com.project.simulation;

public enum GameDifficulty {
    EASY(100_000), MEDIUM(50000), HARD(10000);

    public final int startingMoney;

    GameDifficulty(int startingMoney) {
        this.startingMoney = startingMoney;
    }
}
