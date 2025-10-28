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

    private Texture texEnemigo;     // police_explorer.png
    private Sound dropSound;        // se usa como “impacto”
    private Music rainMusic;

    private long  lastSpawnTime = 0L;
    private long  spawnIntervalMs = 650;       // separación temporal entre autos
    private float speed = 260f;                // velocidad de bajada (px/s)

    private int   errores = 0;                 // 0..3  (game over lo maneja Main)
    private int   puntos  = 0;                 // puntaje entero mostrado en HUD
    private float puntosFrac = 0f;             // acumulador fraccional para sumar por tiempo
    private float puntosPorSegundo = 10f;      // +10 pts / segundo

    // ======== Carriles (ajusta para que calce con tu road_6lanes.png) ========
    private static final int   LANES = 4;
    private static final float ROAD_LEFT  = 80f;  // borde izquierdo de la carretera útil
    private static final float ROAD_RIGHT = 720f; // borde derecho de la carretera útil
    private static final float EN_W = 72f;        // ancho del enemigo (más grande)
    private static final float EN_H = 128f;       // alto del enemigo (más grande)
    private int lastLane = -1;

    public void crear() {
        texEnemigo = new Texture(Gdx.files.internal("police_explorer.png"));
        dropSound  = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic  = new MusicSafe(Gdx.audio.newMusic(Gdx.files.internal("rain.mp3")));
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
        do {
            lane = MathUtils.random(0, LANES - 1);
        } while (LANES > 1 && lane == lastLane);   // evita repetir el mismo carril consecutivo
        lastLane = lane;

        float laneCenterX = ROAD_LEFT + laneWidth * (lane + 0.5f);
        r.x = laneCenterX - EN_W / 2f;

        enemigos.add(r);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        // spawner
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnEnemigo();

        // movimiento
        for (int i = enemigos.size - 1; i >= 0; i--) {
            Rectangle r = enemigos.get(i);
            r.y -= speed * dt;
            if (r.y + r.height < 0) {
                enemigos.removeIndex(i);
            }
        }

        // puntaje por tiempo (suave, sin perder fracción)
        puntosFrac += puntosPorSegundo * dt;
        if (puntosFrac >= 1f) {
            int inc = (int) puntosFrac;
            puntos += inc;
            puntosFrac -= inc;
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < enemigos.size; i++) {
            Rectangle r = enemigos.get(i);
            batch.draw(texEnemigo, r.x, r.y, r.width, r.height);
        }
    }

    public void chequearColision(Vehiculo vehiculo) {
        for (int i = enemigos.size - 1; i >= 0; i--) {
            Rectangle r = enemigos.get(i);
            if (vehiculo.getBounds().overlaps(r)) {
                errores += 1;
                if (dropSound != null) dropSound.play();
                enemigos.removeIndex(i);
            }
        }
    }

    public int  getPuntos()  { return puntos; }
    public int  getErrores() { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (dropSound  != null) dropSound.dispose();
        if (rainMusic  != null) rainMusic.dispose();
    }

    // Wrapper para evitar NPE si cambias música
    private static class MusicSafe implements Music {
        private final Music m;
        MusicSafe(Music m) { this.m = m; }
        public void play(){ m.play(); }
        public void pause(){ m.pause(); }
        public void stop(){ m.stop(); }
        public boolean isPlaying(){ return m.isPlaying(); }
        public void setLooping(boolean b){ m.setLooping(b); }
        public boolean isLooping(){ return m.isLooping(); }
        public void setVolume(float v){ m.setVolume(v); }
        public float getVolume(){ return m.getVolume(); }
        public void setPan(float pan, float volume){ m.setPan(pan, volume); }
        public void setPosition(float p){ m.setPosition(p); }
        public float getPosition(){ return m.getPosition(); }
        public void dispose(){ m.dispose(); }
        public void setOnCompletionListener(OnCompletionListener l){ m.setOnCompletionListener(l); }
    }
}
