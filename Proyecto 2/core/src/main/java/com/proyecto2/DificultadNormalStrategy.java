package com.proyecto2;

public class DificultadNormalStrategy implements DificultadStrategy {

    @Override
    public long calcularSpawnIntervalMs(long baseIntervalMs, int puntos) {
        // Cada 500 puntos disminuye el intervalo, hasta un mínimo
        long min = 250L;
        long intervalo = (long) (baseIntervalMs - puntos * 0.3f);
        return Math.max(intervalo, min);
    }

    @Override
    public float calcularSpeed(float baseSpeed, int puntos) {
        // La velocidad crece suavemente con el puntaje
        float extra = puntos * 0.03f;
        return baseSpeed + extra;
    }

    @Override
    public int calcularMaxEnemigos(int baseMaxEnemigos, int puntos) {
        int extra = puntos / 800; // cada 800 puntos, 1 enemigo más
        int max = baseMaxEnemigos + extra;
        return Math.min(max, 12); // límite duro para no llenar la pantalla
    }
}
