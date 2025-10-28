package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Moto extends Vehiculo {
    private float velocidad = 300f;     // velocidad base
    private float inerciaX = 0f, inerciaY = 0f;
    private float factorDerrape = 0.90f; // 0.85 = más derrape, 0.95 = menos

    public Moto(Texture textura, float x, float y, float w, float h) {
        super(textura, x, y, w, h);
    }

    @Override
    public void update(float dt) {
        float dx = 0f, dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)  || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= velocidad * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += velocidad * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)    || Gdx.input.isKeyPressed(Input.Keys.W)) dy += velocidad * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)  || Gdx.input.isKeyPressed(Input.Keys.S)) dy -= velocidad * dt;

        // aplicar inercia (derrape)
        dx += inerciaX;
        dy += inerciaY;

        bounds.x += dx;
        bounds.y += dy;

        // actualizar inercia
        inerciaX = dx * factorDerrape;
        inerciaY = dy * factorDerrape;

        // límites de pantalla (800x480)
        if (bounds.x < 0) bounds.x = 0;
        if (bounds.x + bounds.width  > 800) bounds.x = 800 - bounds.width;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.y + bounds.height > 480) bounds.y = 480 - bounds.height;
    }
}
