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
    private final Array<Rectangle> objetos = new Array<>();
    private Texture texEnemigo;
    private Sound sPoint, sCrash;
    private Music bgm;

    private long lastSpawnTime = 0L;
    private float baseSpeed = 520f;
    private int puntos = 0;
    private int errores = 0;

    private int VW, VH; // tamaño de mundo

    // === Carretera por porcentaje ===
    // (ajusta si tu PNG tiene márgenes distintos)
    public static float roadLeft(float vw)  { return vw * 0.12f; }  // antes 0.16
    public static float roadRight(float vw) { return vw * 0.88f; }  // antes 0.84
    private static final int LANES = 6; // ← 3 por sentido

    // Tamaños (ajústalos si tu sprite es distinto)
    private float OBJ_W = 120f;
    private float OBJ_H = 260f;

    private int lastLane = -1;
    private long spawnIntervalMs = 650;

    // rect temporal para colisiones
    private final Rectangle tmp = new Rectangle();

    public void crear(int worldW, int worldH) {
        this.VW = worldW; this.VH = worldH;
        objetos.clear();
        puntos = 0; errores = 0;

        if (texEnemigo == null) texEnemigo = new Texture(Gdx.files.internal("police_explorer.png"));
        try { if (sPoint == null) sPoint = Gdx.audio.newSound(Gdx.files.internal("point.wav")); } catch (Exception ignored) {}
        try { if (sCrash == null) sCrash = Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")); }  catch (Exception ignored) {}
        try {
            if (bgm == null) { bgm = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3")); bgm.setLooping(true); bgm.play(); }
        } catch (Exception ignored) {}

        spawnObjeto();
    }

    private void spawnObjeto() {
        Rectangle r = new Rectangle();
        r.y = VH + 10;
        r.width = OBJ_W;
        r.height = OBJ_H;

        float left  = roadLeft(VW);
        float right = roadRight(VW);
        float laneWidth = (right - left) / (float) LANES;

        int lane;
        do { lane = MathUtils.random(0, LANES - 1); }
        while (lane == lastLane && LANES > 1);
        lastLane = lane;

        float laneCenterX = left + laneWidth * (lane + 0.5f);
        r.x = laneCenterX - OBJ_W / 2f;

        objetos.add(r);
        lastSpawnTime = TimeUtils.millis();
    }

    public void update(float dt) {
        float speed = baseSpeed * (1f + puntos / 300f);
        if (TimeUtils.timeSinceMillis(lastSpawnTime) > spawnIntervalMs) spawnObjeto();

        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            r.y -= speed * dt;
            if (r.y + r.height < 0) {
                objetos.removeIndex(i);
                puntos += 10;
                if (sPoint != null) sPoint.play(0.2f);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (Rectangle r : objetos) {
            batch.draw(texEnemigo, r.x - 6, r.y - 6, r.width + 12, r.height + 12);
        }
    }

    public void chequearColision(Auto auto) {
        // hitboxes reducidos (80%)
        Rectangle hbPlayer = auto.getHitbox(0.8f, 0.8f);
        for (int i = objetos.size - 1; i >= 0; i--) {
            Rectangle r = objetos.get(i);
            Rectangle hbEnemy = shrink(r, 0.78f, 0.78f);
            if (hbEnemy.overlaps(hbPlayer)) {
                errores += 1;
                if (sCrash != null) sCrash.play(0.35f);
                objetos.removeIndex(i);
            }
        }
    }

    private Rectangle shrink(Rectangle src, float sx, float sy) {
        float nx = src.x + (1f - sx) * 0.5f * src.width;
        float ny = src.y + (1f - sy) * 0.5f * src.height;
        tmp.set(nx, ny, src.width * sx, src.height * sy);
        return tmp;
    }

    public int getPuntos()  { return puntos; }
    public int getErrores() { return errores; }

    public void destruir() {
        if (texEnemigo != null) texEnemigo.dispose();
        if (sPoint != null) sPoint.dispose();
        if (sCrash != null) sCrash.dispose();
        if (bgm != null) bgm.dispose();
        texEnemigo = null; sPoint = null; sCrash = null; bgm = null;
    }
}