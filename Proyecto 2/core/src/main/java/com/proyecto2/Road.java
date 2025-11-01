package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Road {
    private final Texture tex;

    public Road() {
        tex = new Texture(Gdx.files.internal("carretera.png")); // fondo
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void render(SpriteBatch batch) {
        // Fondo completo
        batch.draw(tex, 0, 0, 800, 480);
    }

    public void renderAlineado(SpriteBatch batch) {
        float roadWidth = Lluvia.ROAD_RIGHT - Lluvia.ROAD_LEFT;
        // Dibuja solo el asfalto centrado con márgenes reales
        batch.draw(tex, Lluvia.ROAD_LEFT, 0, roadWidth, 480);
    }

    public void dispose() {
        tex.dispose();
    }
}