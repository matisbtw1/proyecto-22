package com.proyecto2;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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

    private Auto auto;
    private Lluvia lluvia;
    private Texture autoTex;

    @Override
    public void create () {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        autoTex = new Texture(Gdx.files.internal("bucket.png")); // sprite temporal
        auto = new Auto(autoTex, 800/2f - 32, 20, 64, 64, 0, 800);

        lluvia = new Lluvia();
        lluvia.crear();
    }

    @Override
    public void render () {
        float dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        auto.update(dt);
        lluvia.update(dt);
        lluvia.chequearColision(auto);

        batch.begin();
        auto.render(batch);
        lluvia.render(batch);
        font.draw(batch, "Puntos: " + lluvia.getPuntos(), 10, 470);
        font.draw(batch, "Errores: " + lluvia.getErrores(), 10, 450);
        batch.end();
    }

    @Override
    public void dispose () {
        if (auto != null) auto.dispose();
        if (lluvia != null) lluvia.destruir();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
