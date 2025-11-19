package com.proyecto2;

public interface DificultadStrategy {

    long calcularSpawnIntervalMs(long baseIntervalMs, int puntos);

    float calcularSpeed(float baseSpeed, int puntos);

    int calcularMaxEnemigos(int baseMaxEnemigos, int puntos);
}
