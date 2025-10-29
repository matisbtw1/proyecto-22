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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shape;

    private Road road;
    private Lluvia lluvia;
    private Vehiculo vehiculo;

    private Texture autoTex; // player_lambo.png
    private Texture motoTex; // MotoRoja.png

    private boolean juegoIniciado = false;
    private boolean gameOver = false;

    // (Antes: constantes fijas de tamaño del jugador; ahora calculamos por carril)
    // private static final float PLAYER_W = 95f;
    // private static final float PLAYER_H = 170f;

    // Multiplicador de velocidad para mover al jugador (afectado por turbo)
    private float playerSpeedMul = 1f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        font  = new BitmapFont();
        font.setColor(Color.WHITE);
        shape = new ShapeRenderer();

        road   = new Road(); // fondo "carretera.png"
        autoTex = new Texture(Gdx.files.internal("player_lambo.png"));
        motoTex = new Texture(Gdx.files.internal("MotoRoja.png"));

        // Suavizado de texturas del jugador (evita dientes al escalar)
        autoTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        motoTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

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
        shape.setProjectionMatrix(camera.combined);

        // --- UPDATE ---
        if (juegoIniciado && !gameOver) {
            playerSpeedMul = lluvia.isTurboActivo() ? lluvia.getBonusVelocidad() : 1f;
            vehiculo.update(dt * playerSpeedMul);
            lluvia.update(dt);
            lluvia.chequearColision(vehiculo);
            if (lluvia.getErrores() >= 3) gameOver = true;
        }

        // --- RENDER FONDO ---
        batch.begin();
        road.render(batch);
        batch.end();

        // --- MENÚ DE SELECCIÓN ---
        if (!juegoIniciado) {
            batch.begin();
            font.draw(batch, "Selecciona tu vehiculo:", 300, 300);
            font.draw(batch, "Presiona 1 para AUTO", 320, 260);
            font.draw(batch, "Presiona 2 para MOTO", 320, 240);
            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                // AUTO: ~70% del ancho del carril
            	float laneW   = Lluvia.laneWidth();
            	float w       = laneW * 0.70f;
            	float aspect  = autoTex.getHeight() / (float) autoTex.getWidth();
            	final float AUTO_H_FACTOR = 0.85f;              // <- acorta 15% la altura
            	float h       = w * aspect * AUTO_H_FACTOR;
                float centerX = (Lluvia.ROAD_LEFT + Lluvia.ROAD_RIGHT) / 2f;

                vehiculo = new Auto(
                        autoTex,
                        centerX - w / 2f,
                        20f,
                        w, h,
                        Lluvia.ROAD_LEFT, Lluvia.ROAD_RIGHT
                );
                juegoIniciado = true;
                gameOver = false;

            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                // MOTO: ~55% del ancho del carril
            	float laneW   = Lluvia.laneWidth();
            	float w       = laneW * 0.55f;
            	float aspect  = motoTex.getHeight() / (float) motoTex.getWidth();
            	final float MOTO_H_FACTOR = 0.90f;              // <- acorta 10% la altura
            	float h       = w * aspect * MOTO_H_FACTOR;
                float centerX = (Lluvia.ROAD_LEFT + Lluvia.ROAD_RIGHT) / 2f;

                vehiculo = new Moto(
                        motoTex,
                        centerX - w / 2f,
                        100f,
                        w, h
                );
                juegoIniciado = true;
                gameOver = false;
            }
            return; // no sigas dibujando HUD/halos si aún no inicia
        }

        // --- GAME OVER ---
        if (gameOver) {
            batch.begin();
            font.draw(batch, "GAME OVER", 360, 270);
            font.draw(batch, "Presiona R para reiniciar", 310, 240);
            batch.end();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) resetGame();
            return;
        }

        // --- JUEGO EN CURSO: OBJETOS + JUGADOR ---
        batch.begin();
        lluvia.render(batch);
        vehiculo.render(batch);
        batch.end();

        // --- HALOS DE ESTADO ---
        float cx = vehiculo.getBounds().x + vehiculo.getBounds().width / 2f;
        float cy = vehiculo.getBounds().y + vehiculo.getBounds().height / 2f;
        float r = Math.max(vehiculo.getBounds().width, vehiculo.getBounds().height) * 0.58f;


        if (lluvia.isEscudoActivo() || lluvia.isTurboActivo() || lluvia.justPickedVida()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);

            // Relleno
            shape.begin(ShapeRenderer.ShapeType.Filled);
            if (lluvia.isEscudoActivo() && !lluvia.isTurboActivo() && !lluvia.justPickedVida()) {
                shape.setColor(0f, 0.8f, 1f, 0.25f);      // azul
            } else if (lluvia.isTurboActivo() && !lluvia.isEscudoActivo() && !lluvia.justPickedVida()) {
                shape.setColor(1f, 0.3f, 0f, 0.25f);      // naranja
            } else if (lluvia.justPickedVida() && !lluvia.isEscudoActivo() && !lluvia.isTurboActivo()) {
                shape.setColor(0f, 1f, 0f, 0.25f);        // verde
            } else {
                shape.setColor(0.7f, 0.5f, 1f, 0.25f);    // mezcla si coinciden
            }
            shape.circle(cx, cy, r);
            shape.end();

            // Contorno
            shape.begin(ShapeRenderer.ShapeType.Line);
            if (lluvia.isEscudoActivo() && !lluvia.isTurboActivo() && !lluvia.justPickedVida()) {
                shape.setColor(0f, 0.9f, 1f, 0.9f);
            } else if (lluvia.isTurboActivo() && !lluvia.isEscudoActivo() && !lluvia.justPickedVida()) {
                shape.setColor(1f, 0.4f, 0f, 0.9f);
            } else if (lluvia.justPickedVida() && !lluvia.isEscudoActivo() && !lluvia.isTurboActivo()) {
                shape.setColor(0f, 1f, 0f, 0.8f);
            } else {
                shape.setColor(0.8f, 0.6f, 1f, 0.9f);
            }
            shape.circle(cx, cy, r);
            shape.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // --- HUD ---
        batch.begin();
        if (lluvia.isEscudoActivo()) {
            font.setColor(Color.CYAN);
            font.draw(batch, "ESCUDO ACTIVO!", 340, 470);
        } else if (lluvia.isTurboActivo()) {
            font.setColor(Color.ORANGE);
            font.draw(batch, "TURBO ACTIVADO!", 330, 470);
        } else if (lluvia.justPickedVida()) {
            font.setColor(Color.GREEN);
            font.draw(batch, "REPARADO!", 360, 470);
        }
        font.setColor(Color.WHITE);
        font.draw(batch, "Puntos: "  + lluvia.getPuntos(), 10, 470);
        font.draw(batch, "Errores: " + lluvia.getErrores() + " / 3", 10, 450);
        batch.end();
    }

    private void resetGame() {
        lluvia.destruir();
        lluvia = new Lluvia();
        lluvia.crear();
        vehiculo = null;
        juegoIniciado = false;
        gameOver = false;
        playerSpeedMul = 1f;
    }

    @Override
    public void dispose() {
        if (vehiculo != null) vehiculo.dispose();
        if (lluvia   != null) lluvia.destruir();
        if (road     != null) road.dispose();
        if (autoTex  != null) autoTex.dispose();
        if (motoTex  != null) motoTex.dispose();
        if (batch    != null) batch.dispose();
        if (shape    != null) shape.dispose();
        if (font     != null) font.dispose();
    }
}
