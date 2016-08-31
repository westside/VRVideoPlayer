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
    private File file;
    private CanvasRenderer canvasRenderer;
    private MediaPlayer mediaPlayer;
    private SceneObject canvas;
    private SceneObject video;
    private Animator fadeInVideo, fadeOutCanvas;
    private ObjectLookingStateDetector detector;

    UnityTactosyManager unityTactosyManager;
    private boolean playing;

    protected VideoPlayerApp(Meganekko meganekko, MainActivity activity) {
        super(meganekko);
        this.activity = activity;

        file = new File(Environment.getExternalStorageDirectory(), getContext().getString(R.string.video_path_from_sdcard));

        activity.showGazeCursor();
        setSceneFromXML(R.xml.scene);

        // get scene objects
        canvas = getScene().findObjectById(R.id.canvas);
        canvasRenderer = new CanvasRenderer(getContext());
        canvas.getRenderData().getMaterial().getTexture().set(canvasRenderer);

        video = getScene().findObjectById(R.id.video);

        PermissionUtils.verifyStoragePermissions(activity);

        unityTactosyManager = UnityTactosyManager.instance(activity.getApplicationContext());

        // setup animations
        this.fadeInVideo = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_in);
        fadeInVideo.setTarget(video);
        this.fadeOutCanvas = AnimatorInflater.loadAnimator(getContext(), R.animator.fade_out);
        fadeOutCanvas.setTarget(canvas);

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

                if (!notified && canvasRenderer.getSweepFraction() >= 1.0f) {
                    notified = true;
                    startPlaying();
                }
            }

            @Override
            public void onLookEnd(SceneObject sceneObject, Frame frame) {
                canvasRenderer.setLooking(false);
            }
        });
    }

    @Override
    public void update() {
        if (!playing) {
            detector.update(getFrame());
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

    private void jsonFileRead() {
        try {
            File jsonFile = new File(Environment.getExternalStorageDirectory(),"/VrVideoPlayer/video2.tactosy");
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

                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(mediaPlayer.isPlaying()){
                                int time = mediaPlayer.getCurrentPosition();
                                Log.i(TAG, "run: " + time);
                                time = time / 20;
                                time = time * 20;

                                if (time > tactosyFile.getDurationMillis()) {
                                    timer.cancel();
                                }

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

    private void startPlaying() {
        playing = true;
        activity.hideGazeCursor();

        if (mediaPlayer != null) {
            release();
        }

        if (file.exists()) {
            mediaPlayer = MediaPlayer.create(getContext(), Uri.fromFile(file));

        } else {
//            mediaPlayer = MediaPlayer.create(getContext(), R.raw.video);
//            activity.getApp().showInfoText(3, getContext().getString(R.string.error_default_video));
        }

        if (mediaPlayer != null) {

            jsonFileRead();
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
                                pause();
                            }
                        });
                    }
                });

            } catch (IllegalStateException e) {
                activity.getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
        } else {
//            activity.getApp().showInfoText(3, "media file null");
        }

        if (canvas != null) {
            animate(fadeOutCanvas, new Runnable() {
                @Override
                public void run() {
                    canvas.setVisible(false);
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

        if (video != null) {
            video.setVisible(false);
        }
    }
}
