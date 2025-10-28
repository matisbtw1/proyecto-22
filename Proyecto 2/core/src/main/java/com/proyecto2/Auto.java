package com.proyecto2;
//HOLA
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;

public class Auto extends Vehiculo {
    private float roadMinX;
    private float roadMaxX;
    private float speed = 300f; // píxeles por segundo

    public Auto(Texture texture, float x, float y, float width, float height, float roadMinX, float roadMaxX) {
        super(texture, x, y, width, height);
        this.roadMinX = roadMinX;
        this.roadMaxX = roadMaxX;
    }

    @Override
    public void update(float dt) {
        float dx = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            dx -= speed * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            dx += speed * dt;
        }

        bounds.x += dx;

        // Limita el movimiento a la carretera
        if (bounds.x < roadMinX) bounds.x = roadMinX;
        if (bounds.x + bounds.width > roadMaxX) bounds.x = roadMaxX - bounds.width;
    }
}
