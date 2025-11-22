package com.proyecto2;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class Road {

    public Road() {

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
    }
}
