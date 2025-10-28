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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {
    // Mundo vertical “tipo celular”
    public static final int VW = 540;   // ancho virtual (puedes usar 720)
    public static final int VH = 960;   // alto virtual  (o 1280)

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;

    private Texture texRoad, texPlayer;

    private Road road;
    private Auto auto;
    private Lluvia lluvia;

    private enum State { RUN, GAME_OVER }
    private State state = State.RUN;

    @Override
    public void create () {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VW, VH, camera);
        viewport.apply(true);

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // ⬇️ Fondo nuevo de 6 carriles
        texRoad   = new Texture(Gdx.files.internal("road_6lanes.png"));
        texPlayer = new Texture(Gdx.files.internal("player_lambo.png"));

        // Scroll de la carretera
        road = new Road(texRoad, VW, VH, 420f);

        // ⬇️ límites de carretera extraídos de Lluvia (coinciden con el PNG)
        float roadLeft  = Lluvia.roadLeft(VW);
        float roadRight = Lluvia.roadRight(VW);

        // Jugador (más grande)
        auto = new Auto(
                texPlayer,
                VW/2f - 64, 36,   // x, y
                128, 280,         // w, h
                roadLeft + 6, roadRight - 6
        );

        // ⬇️ crear spawner con tamaño del mundo
        lluvia = new Lluvia();
        lluvia.crear(VW, VH);

        state = State.RUN;
    }

    private void reset() {
        float roadLeft  = Lluvia.roadLeft(VW);
        float roadRight = Lluvia.roadRight(VW);
        auto = new Auto(texPlayer, VW/2f - 64, 36, 128, 280, roadLeft + 6, roadRight - 6);
        lluvia.crear(VW, VH);
        state = State.RUN;
    }

    @Override
    public void render () {
        float dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (state == State.RUN) {
            road.update(dt);
            auto.update(dt);
            lluvia.update(dt);
            lluvia.chequearColision(auto);

            if (lluvia.getErrores() >= 1) state = State.GAME_OVER;
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset();
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        road.draw(batch);
        auto.render(batch);
        lluvia.render(batch);

        font.draw(batch, "Puntos: " + lluvia.getPuntos(), 16, VH - 16);
        font.draw(batch, "Choques: " + lluvia.getErrores(), 16, VH - 40);
        if (state == State.GAME_OVER) {
            font.draw(batch, "GAME OVER - Presiona R para reiniciar", VW/2f - 170, VH/2f);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose () {
        if (auto != null) auto.dispose();
        if (lluvia != null) lluvia.destruir();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (texRoad != null) texRoad.dispose();
        if (texPlayer != null) texPlayer.dispose();
    }
}
