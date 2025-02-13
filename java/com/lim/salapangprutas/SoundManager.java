package com.lim.salapangprutas;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

public class SoundManager {

    private static SoundManager instance;

    private SoundPool soundPool;
    private int[] fruitSoundIDs;
    private int[] flowerSoundIDs;
    private int[] pestSoundIDs;  // Renamed from wormSoundIDs.
    private int gameOverSoundId;  // Game-over sound effect.
    private int loseLifeSoundId;  // Lose-life sound effect.

    // To track the stream ID of the game-over sound so we can pause it.
    private int gameOverStreamId = 0;

    // Volume variable for pest SFX. Set to 1.0f (100% volume).
    // Adjust this value if the pest SFX are too loud or soft.
    private float pestVolume = 10.5f;

    // Arrays of sound resource IDs.
    private int[] fruitSoundResourceIDs = {
            R.raw.fruit_tap1, R.raw.fruit_tap2, R.raw.fruit_tap3
    };
    private int[] flowerSoundResourceIDs = {
            R.raw.flower_tap1, R.raw.flower_tap2, R.raw.flower_tap3
    };
    // Renamed wormSoundResourceIDs to pestSoundResourceIDs.
    private int[] pestSoundResourceIDs = {
            R.raw.pest_tap1, R.raw.pest_tap2
    };

    private MediaPlayer bgmPlayer;

    // Private constructor for singleton pattern.
    private SoundManager(Context context) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)  // Adjust max streams as needed.
                .setAudioAttributes(audioAttributes)
                .build();

        // Log listener for sound loading.
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                Log.d("SoundManager", "Sound loaded successfully: " + sampleId);
            } else {
                Log.e("SoundManager", "Sound failed to load: " + sampleId);
            }
        });

        // Initialize arrays.
        fruitSoundIDs = new int[fruitSoundResourceIDs.length];
        flowerSoundIDs = new int[flowerSoundResourceIDs.length];
        pestSoundIDs = new int[pestSoundResourceIDs.length];

        // Load fruit tap sounds.
        for (int i = 0; i < fruitSoundResourceIDs.length; i++) {
            fruitSoundIDs[i] = soundPool.load(context, fruitSoundResourceIDs[i], 1);
        }

        // Load flower tap sounds.
        for (int i = 0; i < flowerSoundResourceIDs.length; i++) {
            flowerSoundIDs[i] = soundPool.load(context, flowerSoundResourceIDs[i], 1);
        }

        // Load pest tap sounds.
        for (int i = 0; i < pestSoundResourceIDs.length; i++) {
            pestSoundIDs[i] = soundPool.load(context, pestSoundResourceIDs[i], 1);
        }

        // Load the game-over sound effect.
        gameOverSoundId = soundPool.load(context, R.raw.game_over, 1);
        // Load the lose-life sound effect.
        loseLifeSoundId = soundPool.load(context, R.raw.lose_life, 1);

        // Set up background music. Ensure you have "background_music" in res/raw.
        bgmPlayer = MediaPlayer.create(context, R.raw.background_music);
        bgmPlayer.setLooping(true);
        // Reduce background music volume to 50%.
        bgmPlayer.setVolume(0.5f, 0.5f);
    }

    // Singleton instance getter.
    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    // Play a random fruit tap sound at a lower volume (0.7).
    public void playFruitTap() {
        int index = (int) (Math.random() * fruitSoundIDs.length);
        Log.d("SoundManager", "Playing fruit sound: " + fruitSoundIDs[index]);
        soundPool.play(fruitSoundIDs[index], 0.7f, 0.7f, 1, 0, 1f);
    }

    // Play a random flower tap sound at a lower volume (0.7).
    public void playFlowerTap() {
        int index = (int) (Math.random() * flowerSoundIDs.length);
        Log.d("SoundManager", "Playing flower sound: " + flowerSoundIDs[index]);
        soundPool.play(flowerSoundIDs[index], 0.7f, 0.7f, 1, 0, 1f);
    }

    // Play a random pest tap sound using pestVolume.
    public void playPestTap() {
        int index = (int) (Math.random() * pestSoundIDs.length);
        Log.d("SoundManager", "Playing pest sound: " + pestSoundIDs[index] + " at volume " + pestVolume);
        soundPool.play(pestSoundIDs[index], pestVolume, pestVolume, 1, 0, 1f);
    }

    // Play the lose-life sound effect.
    public void playLoseLifeSfx() {
        Log.d("SoundManager", "Playing lose life sound: " + loseLifeSoundId);
        soundPool.play(loseLifeSoundId, 1f, 1f, 1, 0, 1f);
    }

    // Play the game-over sound effect and store its stream ID.
    public void playGameOverSfx() {
        Log.d("SoundManager", "Playing game over sound: " + gameOverSoundId);
        gameOverStreamId = soundPool.play(gameOverSoundId, 1f, 1f, 1, 0, 1f);
    }

    // Pause (stop) the game-over sound effect if it's playing.
    public void pauseGameOverSfx() {
        if (gameOverStreamId != 0) {
            soundPool.stop(gameOverStreamId);
            gameOverStreamId = 0;
        }
    }

    // Start background music.
    public void startBgm() {
        if (bgmPlayer != null && !bgmPlayer.isPlaying()) {
            bgmPlayer.start();
        }
    }

    // Pause background music.
    public void pauseBgm() {
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
        }
    }

    // Release resources.
    public void release() {
        if (bgmPlayer != null) {
            bgmPlayer.release();
            bgmPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}
