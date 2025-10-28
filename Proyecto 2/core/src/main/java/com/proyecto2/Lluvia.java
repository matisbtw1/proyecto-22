package com.proyecto2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Lluvia {
    private final Array<Rectangle> enemigos = new Array<>();
    private Texture texEnemigo;
    private Sound dropSound;
    private Music rainMusic;

    private long lastSpawnTime = 0L;
    private long spawnIntervalMs = 650;
    private float speed = 270f;

    private int errores = 0;
    private int puntos = 0;
    private float puntosFrac = 0f;
    private float puntosPorSegundo = 10f;

    // ======== CARRILES (ajusta estos si hace falta) ========
    // Asfalto más ancho: reduce márgenes laterales
    public static final float ROAD_LEFT  = 180f;          // antes ~235
    public static final float ROAD_RIGHT = 800f - 180f;   // 620
    public static final int   LANES = 4;

    // Tamaño enemigo más grande (anchura del carril ~110px → coche ~90px)
    private static final float EN_W = 90f;
    private static final float EN_H = 160f;

    private int lastLane = -1;

    public void crear() {
        texEnemigo = new Texture(Gdx.files.internal("police_explorer.png"));
        dropSound  = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic  = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();
        spawnEnemigo();
    }

    private void spawnEnemigo() {
        Rectangle r = new Rectangle();
        r.y = 480;
        r.width  = EN_W;
        r.height = EN_H;

        float laneWidth = (ROAD_RIGHT - ROAD_LEFT) / (float) LANES;
        int lane;
        do { lane = MathUtils.random(0, LANES - 1); }
        while (LANES > 1 && lane == lastLane);
        lastLane = lane;

        float laneCenterX = ROAD_LEFT + laneWidth * (lane + 0.5f);
        r.x = laneCenterX - EN_W / 2f;

        enemigos.add(r);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnEnemigo();

        for (int i = enemigos.size - 1; i >= 0; i--) {
            Rectangle r = enemigos.get(i);
            r.y -= speed * dt;
            if (r.y + r.height < 0) enemigos.removeIndex(i);
        }

        puntosFrac += puntosPorSegundo * dt;
        if (puntosFrac >= 1f) {
            int inc = (int) puntosFrac;
            puntos += inc;
            puntosFrac -= inc;
        }
    }

    public void render(SpriteBatch batch) {
        for (Rectangle r : enemigos) batch.draw(texEnemigo, r.x, r.y, r.width, r.height);
    }

    public void chequearColision(Vehiculo vehiculo) {
        for (int i = enemigos.size - 1; i >= 0; i--) {
            if (vehiculo.getBounds().overlaps(enemigos.get(i))) {
                errores++;
                if (dropSound != null) dropSound.play();
                enemigos.removeIndex(i);
            }
        }
    }

    public int getPuntos()  { return puntos; }
    public int getErrores() { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (dropSound  != null) dropSound.dispose();
        if (rainMusic  != null) rainMusic.dispose();
    }
}
