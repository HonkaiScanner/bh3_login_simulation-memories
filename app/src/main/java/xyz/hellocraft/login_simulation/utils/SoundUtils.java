package xyz.hellocraft.login_simulation.utils;

import static xyz.hellocraft.login_simulation.data.Constant.DFA;
import static xyz.hellocraft.login_simulation.data.Constant.FA;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.github.haocen2004.login_simulation.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SoundUtils {

    private static volatile SoundUtils instance;
    private final String TAG = "SoundUtils";
    private final SoundPool soundPool;
    private final MediaPlayer mediaPlayer;
    private final Map<Integer, String> loadingSoundMap = new HashMap<>();
    private final Map<String, Integer> soundMap = new HashMap<>();
    private final int chance = new Random().nextInt(10);
    private boolean looping = false;

    public SoundUtils(Context context) {
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                .setMaxStreams(10)
                .build();
        if (loadingSoundMap.isEmpty() || loadingSoundMap.size() != soundMap.size())
            initRes(context);
        if (chance > 8) {
            mediaPlayer = MediaPlayer.create(context, R.raw.loop3);
        } else {
            if (new Random().nextBoolean()) {
                mediaPlayer = MediaPlayer.create(context, R.raw.loop1);
            } else {
                mediaPlayer = MediaPlayer.create(context, R.raw.loop2);
            }
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(0.05f, 0.05f);
        mediaPlayer.setLooping(true);
        Logger.d(TAG, "SoundUtils loaded.");
    }

    public static SoundUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SoundUtils.class) {
                if (instance == null) {
                    instance = new SoundUtils(context);
                }
            }
        }
        return instance;
    }

    private void initRes(Context context) {
        if (!loadingSoundMap.isEmpty() && loadingSoundMap.size() == soundMap.size()) return;

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            if (status == 0) {
                String key = loadingSoundMap.get(sampleId);
                Logger.d(TAG, key + " loaded.");
                soundMap.put(key, sampleId);
                if (soundMap.size() == loadingSoundMap.size()) {
                    Logger.d(TAG, "All sounds loaded.");
                }
            }
        });

        Logger.d(TAG, "loading sounds ...");
//        loadingSoundMap.put(soundPool.load(context, R.raw.dxstart, 1), "dxStart");
//        loadingSoundMap.put(soundPool.load(context, R.raw.start, 1), "Start");
//        loadingSoundMap.put(soundPool.load(context, R.raw.dxlogin, 1), "dxLogin");
//        loadingSoundMap.put(soundPool.load(context, R.raw.login, 1), "Login");
//        loadingSoundMap.put(soundPool.load(context, R.raw.dxscan, 1), "dxScan");
//        loadingSoundMap.put(soundPool.load(context, R.raw.scan, 1), "Scan");
//        loadingSoundMap.put(soundPool.load(context, R.raw.dxscansucc, 1), "dxScanSucc");
//        loadingSoundMap.put(soundPool.load(context, R.raw.scansucc, 1), "ScanSucc");
//        loadingSoundMap.put(soundPool.load(context, R.raw.failed, 1), "Failed");


    }

    public void playSound(String key) {
        String finalKey = key;
        if (soundMap.size() != loadingSoundMap.size()) {
            String finalKey1 = finalKey;
            new Thread() {
                @Override
                public void run() {
                    while (soundMap.size() != loadingSoundMap.size()) {
                        try {
                            Logger.d(TAG, "waiting for loading.");
                            Thread.sleep(500);
                        } catch (InterruptedException ignore) {
                        }
                    }
                    playSound(finalKey1);
                    super.run();
                }
            }.start();
            return;
        }

        if (!FA) return;
        if (DFA) return;
        if (key.equals("Loop") && looping) return;

        if (chance > 8) {
            finalKey = "dx" + key;
        }
        if (finalKey.equals("dxFailed")) finalKey = "Failed";

        if (key.contains("Loop")) {
            mediaPlayer.start();
            looping = true;
        } else if (soundMap.containsKey(finalKey)) {
            soundPool.play(soundMap.get(finalKey), 0.3f, 0.3f, 0, 0, 1);
            Logger.d(TAG, key + " played.");
        } else {
            Logger.w(TAG, "Sound " + finalKey + " NOT found!");
        }
    }

    public void stop() {
        soundPool.release();
        mediaPlayer.stop();
        mediaPlayer.release();
    }
}
