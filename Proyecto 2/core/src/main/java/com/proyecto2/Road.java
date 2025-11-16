package com.proyecto2;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Clase Road:
 * - Ya NO carga texturas (usa AssetsJuego).
 * - Ya NO libera texturas (assets globales).
 * - Solo dibuja la carretera.
 */
public class Road {

    public Road() {
        // Nada que cargar aquí
    }

    /** Renderiza el fondo completo */
    public void render(SpriteBatch batch) {
        batch.draw(
                AssetsJuego.get().texCarretera,
                0, 0,
                800, 480
        );
    }

    /** Renderiza la carretera alineada al ancho real del asfalto */
    public void renderAlineado(SpriteBatch batch) {
        float roadWidth = GestorObjetos.ROAD_RIGHT - GestorObjetos.ROAD_LEFT;

        batch.draw(
                AssetsJuego.get().texCarretera,
                GestorObjetos.ROAD_LEFT,
                0,
                roadWidth,
                480
        );
    }

    /** Road NO debe liberar la textura */
    public void dispose() {
        // Intencionalmente vacío
        // Los assets se liberan en AssetsJuego.dispose()
    }
}
