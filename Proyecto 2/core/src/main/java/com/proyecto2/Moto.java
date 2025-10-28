package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Moto extends Vehiculo {
    private float velocidad = 320f;
    private float inerciaX = 0f, inerciaY = 0f;
    private float factorDerrape = 0.90f;

    // límites horizontales: usa los mismos del asfalto
    private final float roadMinX = Lluvia.ROAD_LEFT;
    private final float roadMaxX = Lluvia.ROAD_RIGHT;

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

        dx += inerciaX; dy += inerciaY;
        bounds.x += dx; bounds.y += dy;

        inerciaX = dx * factorDerrape;
        inerciaY = dy * factorDerrape;

        // clamp al asfalto (horizontal) y a la pantalla (vertical)
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX) bounds.x = roadMaxX - bounds.width;
        if (bounds.y < 0) bounds.y = 0;
        if (bounds.y + bounds.height > 480) bounds.y = 480 - bounds.height;
    }
}
