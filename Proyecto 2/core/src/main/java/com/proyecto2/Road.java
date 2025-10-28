package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Road {
    private final Texture tex;

    public Road() {
        tex = new Texture(Gdx.files.internal("carretera.png")); // fondo nuevo
    }

    public void render(SpriteBatch batch) {
        // Dibuja el fondo escalado a toda la pantalla
        batch.draw(tex, 0, 0, 800, 480);
    }

    public void dispose() {
        tex.dispose();
    }
}
