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
    private static final int ENEMIGO = 0; // autos que debemos esquivar
    private static final int BONUS   = 1; // objetos buenos (por ahora dan puntos)

    private final Array<Rectangle> objetos = new Array<Rectangle>();
    private final Array<Integer> tipo = new Array<Integer>();

    private Texture texEnemigo;
    private Texture texBonus;
    private Sound dropSound;
    private Music rainMusic;

    private long lastSpawnTime = 0L;
    private float speed = 220f;
    private int puntos = 0;
    private int errores = 0;

    // === Carriles (4 carriles) ===
    private static final float ROAD_LEFT  = 80f;
    private static final float ROAD_RIGHT = 720f;
    private static final int   LANES = 4;
    private static final float OBJ_W = 64f;
    private static final float OBJ_H = 64f;
    private int lastLane = -1;

    // === Probabilidades y frecuencia ===
    private float probBonus = 0.15f;     // solo 15% buenos
    private long spawnIntervalMs = 650;  // tiempo entre spawns

    public void crear() {
        texEnemigo = new Texture(Gdx.files.internal("autoMalo.png")); // 👈 asset del enemigo
        texBonus   = new Texture(Gdx.files.internal("drop.png"));     // 👈 asset del bonus

        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        rainMusic.setLooping(true);
        rainMusic.play();

        spawnObjeto();
    }

    private void spawnObjeto() {
        Rectangle r = new Rectangle();
        r.y = 480;
        r.width = OBJ_W;
        r.height = OBJ_H;

        // seleccionar carril
        float laneWidth = (ROAD_RIGHT - ROAD_LEFT) / (float) LANES;
        int lane;
        do {
            lane = MathUtils.random(0, LANES - 1);
        } while (lane == lastLane && LANES > 1);
        lastLane = lane;

        float laneCenterX = ROAD_LEFT + laneWidth * (lane + 0.5f);
        r.x = laneCenterX - OBJ_W / 2f;

        // decidir tipo
        int t = MathUtils.randomBoolean(probBonus) ? BONUS : ENEMIGO;
        objetos.add(r);
        tipo.add(t);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnObjeto();

        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            r.y -= speed * dt;
            if (r.y + r.height < 0) {
                objetos.removeIndex(i);
                tipo.removeIndex(i);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < objetos.size; i++) {
            Texture t = (tipo.get(i) == ENEMIGO) ? texEnemigo : texBonus;
            Rectangle r = objetos.get(i);
            batch.draw(t, r.x, r.y, r.width, r.height);
        }
    }

    public void chequearColision(Auto auto) {
        for (int i = objetos.size - 1; i >= 0; i--) {
            if (auto.getBounds().overlaps(objetos.get(i))) {
                if (tipo.get(i) == ENEMIGO) {
                    errores += 1; // chocar = error
                } else {
                    puntos += 10; // bonus = puntaje
                }
                dropSound.play();
                objetos.removeIndex(i);
                tipo.removeIndex(i);
            }
        }
    }

    public int getPuntos() { return puntos; }
    public int getErrores() { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (texBonus != null)   texBonus.dispose();
        if (dropSound != null)  dropSound.dispose();
        if (rainMusic != null)  rainMusic.dispose();
    }
}
