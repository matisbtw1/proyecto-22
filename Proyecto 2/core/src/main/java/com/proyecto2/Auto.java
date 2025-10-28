package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Auto extends Vehiculo {
    private float roadMinX, roadMaxX;
    private float speed = 520f;
    private final Rectangle hb = new Rectangle(); // hitbox reducido

    public Auto(Texture texture, float x, float y, float width, float height, float roadMinX, float roadMaxX) {
        super(texture, x, y, width, height);
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    @Override
    public void update(float dt) {
        float dx = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) dx -= speed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) dx += speed * dt;

        bounds.x += dx;
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX) bounds.x = roadMaxX - bounds.width;
    }

    /** Hitbox escalado para evitar colisiones injustas (por defecto 80%). */
    public Rectangle getHitbox(float sx, float sy) {
        float nx = bounds.x + (1f - sx) * 0.5f * bounds.width;
        float ny = bounds.y + (1f - sy) * 0.5f * bounds.height;
        hb.set(nx, ny, bounds.width * sx, bounds.height * sy);
        return hb;
    }
}
