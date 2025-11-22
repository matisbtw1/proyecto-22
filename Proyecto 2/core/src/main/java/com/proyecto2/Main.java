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
    private GestorObjetos gestor;
    private Vehiculo vehiculo;

    // referencias a las texturas del singleton
    private Texture autoTex;
    private Texture motoTex;

    private boolean juegoIniciado = false;
    private boolean gameOver = false;

    private float playerSpeedMul = 1f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        font  = new BitmapFont();
        font.setColor(Color.WHITE);
        shape = new ShapeRenderer();

        // Inicializamos el singleton de assets
        AssetsJuego assets = AssetsJuego.get();

        // Fondo 
        road = new Road();

        // Texturas del jugador
        autoTex = assets.texAutoJugador;
        motoTex = assets.texMotoJugador;

        // Gestor de objetos dinámicos (enemigos, bonus, malus)
        gestor = new GestorObjetos();
        gestor.crear();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        if (juegoIniciado && !gameOver) {
            playerSpeedMul = gestor.isTurboActivo() ? gestor.getBonusVelocidad() : 1f;

         // sincronizamos el estado de controles invertidos al vehículo
            vehiculo.setInvertControls(gestor.isControlsInverted());
            
            // Si está el malus de frenos activo, el jugador no se actualiza
            if (!gestor.isFrenoActivo()) {
                vehiculo.update(dt * playerSpeedMul);
            }

            gestor.update(dt);
            gestor.chequearColision(vehiculo);
            if (gestor.getErrores() >= 3) gameOver = true;
        }



        // Fondo
        batch.begin();
        road.render(batch);
        batch.end();

        // ==========================
        //  SELECCIÓN DE VEHÍCULO
        // ==========================
        if (!juegoIniciado) {
            batch.begin();
            font.draw(batch, "Selecciona tu vehiculo:", 300, 300);
            font.draw(batch, "Presiona 1 para AUTO",     320, 260);
            font.draw(batch, "Presiona 2 para MOTO",     320, 240);
            batch.end();

            float centerX = (GestorObjetos.roadMinX() + GestorObjetos.roadMaxX()) / 2f;
            float laneW   = GestorObjetos.laneWidth();

            // Parámetros de escala
            final float AUTO_W_FRAC  = 0.58f;
            final float MOTO_W_FRAC  = 0.46f;
            final float AUTO_FLAT    = 0.80f;
            final float MOTO_FLAT    = 0.90f;
            final float MAX_AR_AUTO  = 1.85f;
            final float MAX_AR_MOTO  = 1.60f;

            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) ||
                Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {

                // ===== AUTO =====
                float w     = laneW * AUTO_W_FRAC;
                float texAR = autoTex.getHeight() / (float) autoTex.getWidth();
                float usedAR = Math.min(texAR, MAX_AR_AUTO);
                float h     = w * usedAR * AUTO_FLAT;

                float startX = Math.round(centerX - w / 2f);

                vehiculo = new Auto(
                        autoTex,
                        startX,
                        20f,
                        w, h,
                        GestorObjetos.roadMinX(),
                        GestorObjetos.roadMaxX()
                );
                juegoIniciado = true;
                gameOver = false;

            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) ||
                       Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {

                // ===== MOTO =====
                float w      = laneW * MOTO_W_FRAC;
                float texAR  = motoTex.getHeight() / (float) motoTex.getWidth();
                float usedAR = Math.min(texAR, MAX_AR_MOTO);
                float h      = w * usedAR * MOTO_FLAT;

                float startX = centerX - w / 2f;

                vehiculo = new Moto(
                        motoTex,
                        startX,
                        100f,
                        w, h
                );
                juegoIniciado = true;
                gameOver = false;
            }
            return;
        }

        // ==========================
        //        GAME OVER
        // ==========================
        if (gameOver) {
            batch.begin();
            font.draw(batch, "GAME OVER", 360, 270);
            font.draw(batch, "Presiona R para reiniciar", 310, 240);
            batch.end();

            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) resetGame();
            return;
        }

        // ==========================
        //   RENDER PRINCIPAL
        // ==========================
        batch.begin();
        gestor.render(batch);
        vehiculo.render(batch);
        batch.end();

        // ==========================
        //  EFECTOS VISUALES BONUS
        // ==========================
        float cx = vehiculo.getBounds().x + vehiculo.getBounds().width / 2f;
        float cy = vehiculo.getBounds().y + vehiculo.getBounds().height / 2f;
        float r  = Math.max(vehiculo.getBounds().width, vehiculo.getBounds().height) * 0.65f;

        if (gestor.isEscudoActivo() || gestor.isTurboActivo() || gestor.justPickedVida()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shape.begin(ShapeRenderer.ShapeType.Filled);

            if (gestor.isEscudoActivo() && !gestor.isTurboActivo() && !gestor.justPickedVida())
                shape.setColor(0f, 0.8f, 1f, 0.25f);
            else if (gestor.isTurboActivo() && !gestor.isEscudoActivo() && !gestor.justPickedVida())
                shape.setColor(1f, 0.3f, 0f, 0.25f);
            else if (gestor.justPickedVida() && !gestor.isEscudoActivo() && !gestor.isTurboActivo())
                shape.setColor(0f, 1f, 0f, 0.25f);
            else
                shape.setColor(0.7f, 0.5f, 1f, 0.25f);

            shape.circle(cx, cy, r);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            if (gestor.isEscudoActivo() && !gestor.isTurboActivo() && !gestor.justPickedVida())
                shape.setColor(0f, 0.9f, 1f, 0.9f);
            else if (gestor.isTurboActivo() && !gestor.isEscudoActivo() && !gestor.justPickedVida())
                shape.setColor(1f, 0.4f, 0f, 0.9f);
            else if (gestor.justPickedVida() && !gestor.isEscudoActivo() && !gestor.isTurboActivo())
                shape.setColor(0f, 1f, 0f, 0.8f);
            else
                shape.setColor(0.8f, 0.6f, 1f, 0.9f);

            shape.circle(cx, cy, r);
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        // ==========================
        //           HUD
        // ==========================
        batch.begin();
        if (gestor.isEscudoActivo()) {
            font.setColor(Color.CYAN);
            font.draw(batch, "ESCUDO ACTIVO!", 340, 470);
        } else if (gestor.isTurboActivo()) {
            font.setColor(Color.ORANGE);
            font.draw(batch, "TURBO ACTIVADO!", 330, 470);
        } else if (gestor.justPickedVida()) {
            font.setColor(Color.GREEN);
            font.draw(batch, "REPARADO!", 360, 470);
        } else if (gestor.isControlsInverted()) {
            font.setColor(Color.RED);
            font.draw(batch, "CONTROLES INVERTIDOS!", 290, 470);
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Puntos: "  + gestor.getPuntos(),           10, 470);
        font.draw(batch, "Errores: " + gestor.getErrores() + " / 3", 10, 450);
        batch.end();
    }

    private void resetGame() {
        gestor.destruir();
        gestor = new GestorObjetos();
        gestor.crear();
        vehiculo = null;
        juegoIniciado = false;
        gameOver = false;
        playerSpeedMul = 1f;
    }

    @Override
    public void dispose() {
        if (vehiculo != null) vehiculo.dispose();
        if (gestor   != null) gestor.destruir();
        if (road     != null) road.dispose();
        if (batch    != null) batch.dispose();
        if (shape    != null) shape.dispose();
        if (font     != null) font.dispose();

        // liberar TODOS los assets cargados por el singleton
        AssetsJuego.get().dispose();
    }
}
