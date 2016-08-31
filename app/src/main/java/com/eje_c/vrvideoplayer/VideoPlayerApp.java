package com.eje_c.vrvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.eje_c.meganekko.Frame;
import com.eje_c.meganekko.Meganekko;
import com.eje_c.meganekko.MeganekkoApp;
import com.eje_c.meganekko.ObjectLookingStateDetector;
import com.eje_c.meganekko.SceneObject;
import com.eje_c.vrvideoplayer.model.FeedbackMode;
import com.eje_c.vrvideoplayer.model.PositionType;
import com.eje_c.vrvideoplayer.model.TactosyFeedback;
import com.eje_c.vrvideoplayer.model.TactosyFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import tactosy.UnityTactosyManager;


public class VideoPlayerApp extends MeganekkoApp {
    public static final String TAG = VideoPlayerApp.class.getSimpleName();

    private final MainActivity activity;
    private File file, file2;
    private CanvasRenderer canvasRenderer, canvasRenderer2;
    private MediaPlayer mediaPlayer;
    private SceneObject canvas, canvas2;
    private SceneObject video;
    private Animator fadeInVideo, fadeOutCanvas, fadeOutCanvas2;
    private ObjectLookingStateDetector detector, detector2;

    UnityTactosyManager unityTactosyManager;
    private boolean playing;

    protected VideoPlayerApp(Meganekko meganekko, MainActivity activity) {
        super(meganekko);
        this.activity = activity;

        file = new File(Environment.getExternalStorageDirectory(), getContext().getString(R.string.video_path_from_sdcard));
        file2 = new File(Environment.getExternalStorageDirectory(), getContext().getString(R.string.video_path_from_sdcard2));

        activity.showGazeCursor();
        setSceneFromXML(R.xml.scene);

        // get scene objects
        canvas = getScene().findObjectById(R.id.canvas);
        canvas2 = getScene().findObjectById(R.id.canvas2);
        canvasRenderer = new CanvasRenderer(getContext(), 1);
        canvasRenderer2 = new CanvasRenderer(getContext(), 2);
        canvas.getRenderData().getMaterial().getTexture().set(canvasRenderer);
        canvas2.getRenderData().getMaterial().getTexture().set(canvasRenderer2);

        video = getScene().findObjectById(R.id.video);

        com.bhaptics.common.PermissionUtils.verifyStoragePermissions(activity);

        unityTactosyManager = UnityTactosyManager.instance(activity.getApplicationContext());

        // setup animations
        this.fadeInVideo = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        fadeInVideo.setTarget(video);
        this.fadeOutCanvas = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_out);
        this.fadeOutCanvas2 = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_out);

        fadeOutCanvas.setTarget(canvas);
        fadeOutCanvas2.setTarget(canvas2);

        // animation while looking at start button
        detector = new ObjectLookingStateDetector(this, canvas, new ObjectLookingStateDetector.ObjectLookingStateListener() {
            boolean notified;

            @Override
            public void onLookStart(SceneObject sceneObject, Frame frame) {
                canvasRenderer.setLooking(true);
            }

            @Override
            public void onLooking(SceneObject sceneObject, Frame frame) {
                canvasRenderer.update(frame);

                if (!playing && canvasRenderer.getSweepFraction() >= 1.0f) {
                    startPlaying(1);
                    canvasRenderer.setLooking(false);
                }
            }

            @Override
            public void onLookEnd(SceneObject sceneObject, Frame frame) {
                canvasRenderer.setLooking(false);
            }
        });

        // animation while looking at start button
        detector2 = new ObjectLookingStateDetector(this, canvas2, new ObjectLookingStateDetector.ObjectLookingStateListener() {
            boolean notified;

            @Override
            public void onLookStart(SceneObject sceneObject, Frame frame) {
                canvasRenderer2.setLooking(true);
            }

            @Override
            public void onLooking(SceneObject sceneObject, Frame frame) {
                canvasRenderer2.update(frame);

                if (!playing && canvasRenderer2.getSweepFraction() >= 1.0f) {
                    startPlaying(2);
                    canvasRenderer.setLooking(false);
                }
            }

            @Override
            public void onLookEnd(SceneObject sceneObject, Frame frame) {
                canvasRenderer2.setLooking(false);
            }
        });
    }

    @Override
    public void update() {
        if (!playing) {
            detector.update(getFrame());
            detector2.update(getFrame());
        }
        super.update();
    }

    @Override
    public void onPause() {
        super.onPause();
        runOnGlThread(new Runnable() {
            @Override
            public void run() {
                pause();
            }
        });
    }

    @Override
    public void shutdown() {
        unityTactosyManager.destroy();

        release();
        super.shutdown();
    }

    private void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }
    Timer timer;
    private void jsonFileRead(final int idx) {
        Log.e(TAG, "jsonFileRead: " + idx);
        try {
            File jsonFile = new File(Environment.getExternalStorageDirectory(), getContext().getString(idx == 1 ? R.string.tactosy_path_from_sdcard : R.string.tactosy_path_from_sdcard2));
            FileInputStream fin;
            String jsonInfo;
            byte[] fileContent;
            if(jsonFile.isFile()) {
                try {
                    fin = new FileInputStream(jsonFile);
                    fileContent = new byte[(int) jsonFile.length()];
                    fin.read(fileContent);
                    jsonInfo = new String(fileContent);

                    ObjectMapper objectMapper = new ObjectMapper();
                    final TactosyFile tactosyFile = objectMapper.readValue(jsonInfo, TactosyFile.class);

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                if(mediaPlayer.isPlaying()){
                                    int time = mediaPlayer.getCurrentPosition();
                                    Log.v(TAG, "run: " + time);
                                    time = time / 20;
                                    time = time * 20;

//                                    if (time > tactosyFile.getDurationMillis()) {
//                                        Log.e(TAG, "timer cancel: " + idx);
//                                        timer.cancel();
//                                    }

                                    TactosyFeedback[] tactosyFeedbacks = tactosyFile.feedback.get(time);

                                    if (tactosyFeedbacks != null) {
                                        Log.i(TAG, "test run: data" + time);
                                        for (TactosyFeedback tactosyFeedback : tactosyFeedbacks) {
                                            if (tactosyFeedback.getMode() == FeedbackMode.DOT_MODE) {
                                                unityTactosyManager.setMotor(tactosyFeedback.getPosition(), tactosyFeedback.getValues());
                                            } else {
                                                unityTactosyManager.setMotorPathMode(tactosyFeedback.getPosition(), tactosyFeedback.getValues());
                                            }
                                        }
                                    } else {
                                        unityTactosyManager.setMotor(PositionType.Left, new byte[20]);
                                        unityTactosyManager.setMotor(PositionType.Right, new byte[20]);
                                    }

                                }
                            } catch (Exception e) {

                                Log.e(TAG, "timer: " + getContext().getString(idx == 1 ? R.string.tactosy_path_from_sdcard : R.string.tactosy_path_from_sdcard2), e);
                            }
                        }
                    },0, 20);


                    Log.i(TAG, "jsonFileRead: " + tactosyFile);
                } catch (IOException e) {
                    Log.e(TAG, "onCreate: jsonFile into FileInputStream fail", e);
                }
            } else {
                Log.e(TAG, "jsonFileRead: " + "file not exist");
            }
        } catch (Exception exxx) {
            Log.e(TAG, "jsonFileRead: ", exxx);
        }

    }

    private void startPlaying(int index) {
        playing = true;
        activity.hideGazeCursor();

        if (mediaPlayer != null) {
            release();
        }

//        if (file.exists()) {
            mediaPlayer = MediaPlayer.create(getContext(), Uri.fromFile(index == 1 ? file : file2));
//        }

        if (mediaPlayer != null) {
            Log.i(TAG, "startPlaying: " + "start");
            jsonFileRead(index);
            try {
                mediaPlayer.start();
                mediaPlayer.pause();

                mediaPlayer.start();


                video.getRenderData().getMaterial().getTexture().set(mediaPlayer);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        runOnGlThread(new Runnable() {
                            @Override
                            public void run() {
                                if (timer != null) {
                                    Log.e(TAG, "timer cancel: ");
                                    timer.cancel();
                                }
                                pause();
                            }
                        });
                    }
                });

            } catch (IllegalStateException e) {
                activity.getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
        }

        if (canvas != null) {
            animate(fadeOutCanvas, new Runnable() {
                @Override
                public void run() {
                    canvas.setVisible(false);
                }
            });
        }

        if (canvas2 != null) {
            animate(fadeOutCanvas2, new Runnable() {
                @Override
                public void run() {
                    canvas2.setVisible(false);
                }
            });
        }

        if (video != null) {
            animate(fadeInVideo, new Runnable() {
                @Override
                public void run() {
                    video.setVisible(true);
                }
            });
        }
    }

    private void pause() {
        playing = false;
        activity.showGazeCursor();

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            } catch (IllegalStateException e) {
                activity.getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
        }

        if (canvas != null) {
            canvas.setVisible(true);
            canvas.setOpacity(1.0f);
        }

        if (canvas2 != null) {
            canvas2.setVisible(true);
            canvas2.setOpacity(1.0f);
        }

        if (video != null) {
            video.setVisible(false);
        }
    }
}
