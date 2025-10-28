package com.proyecto2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;

    private Road road;
    private Lluvia lluvia;
    private Vehiculo vehiculo;

    private Texture autoTex;
    private Texture motoTex;

    private boolean juegoIniciado = false;
    private boolean gameOver = false;

    // jugador más grande
    private static final float PLAYER_W = 95f;
    private static final float PLAYER_H = 170f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        road = new Road(); // dibuja "carretera.png"
        autoTex = new Texture(Gdx.files.internal("player_lambo.png"));
        motoTex = new Texture(Gdx.files.internal("MotoRoja.png"));

        lluvia = new Lluvia();
        lluvia.crear();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        if (juegoIniciado && !gameOver) {
            vehiculo.update(dt);
            lluvia.update(dt);
            lluvia.chequearColision(vehiculo);
            if (lluvia.getErrores() >= 3) gameOver = true;
        }

        batch.begin();
        road.render(batch);

        if (!juegoIniciado) {
            font.draw(batch, "Selecciona tu vehiculo:", 300, 300);
            font.draw(batch, "Presiona 1 para AUTO", 320, 260);
            font.draw(batch, "Presiona 2 para MOTO", 320, 240);

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                // Auto limitado al asfalto usando los mismos bordes que la lluvia
                vehiculo = new Auto(autoTex,
                        (Lluvia.ROAD_LEFT + Lluvia.ROAD_RIGHT) / 2f - PLAYER_W / 2f,
                        20, PLAYER_W, PLAYER_H,
                        Lluvia.ROAD_LEFT, Lluvia.ROAD_RIGHT);
                juegoIniciado = true;
                gameOver = false;
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                vehiculo = new Moto(motoTex,
                        (Lluvia.ROAD_LEFT + Lluvia.ROAD_RIGHT) / 2f - PLAYER_W / 2f,
                        100, PLAYER_W, PLAYER_H);
                juegoIniciado = true;
                gameOver = false;
            }
        } else if (gameOver) {
            font.draw(batch, "GAME OVER", 360, 270);
            font.draw(batch, "Presiona R para reiniciar", 310, 240);
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) resetGame();
        } else {
            lluvia.render(batch);
            vehiculo.render(batch);
            font.draw(batch, "Puntos: "  + lluvia.getPuntos(), 10, 470);
            font.draw(batch, "Errores: " + lluvia.getErrores() + " / 3", 10, 450);
        }

        batch.end();
    }

    private void resetGame() {
        lluvia.destruir();
        lluvia = new Lluvia();
        lluvia.crear();
        vehiculo = null;
        juegoIniciado = false;
        gameOver = false;
    }

    @Override
    public void dispose() {
        if (vehiculo != null) vehiculo.dispose();
        if (lluvia != null) lluvia.destruir();
        if (road != null) road.dispose();
        if (autoTex != null) autoTex.dispose();
        if (motoTex != null) motoTex.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
