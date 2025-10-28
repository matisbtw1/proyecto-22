package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Road {
    private final Texture tex;

    // Constructor sin parámetros
    public Road() {
        tex = new Texture(Gdx.files.internal("road_6lanes.png"));
    }

    public void render(SpriteBatch batch) {
        // Dibuja el fondo de carretera (igual que antes)
        batch.draw(tex, 0, 0, 800, 480);
    }

    public void dispose() {
        tex.dispose();
    }
}
