package com.eje_c.vrvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;

import com.eje_c.meganekko.ObjectLookingStateDetector;
import com.eje_c.meganekko.SceneObject;
import com.eje_c.meganekko.VrContext;
import com.eje_c.meganekko.VrFrame;
import com.eje_c.meganekko.scene_objects.CanvasSceneObject;
import com.eje_c.meganekko.scene_objects.VideoSceneObject;

import java.io.File;

public class MainActivity extends BaseActivity {

    private File file;
    private CanvasRenderer canvasRenderer;
    private MediaPlayer mediaPlayer;
    private CanvasSceneObject canvas;
    private VideoSceneObject video;
    private Animator fadeInVideo, fadeOutCanvas;
    private ObjectLookingStateDetector detector;

    @Override
    protected void oneTimeInit(VrContext context) {

        file = new File(Environment.getExternalStorageDirectory(), getString(R.string.video_path_from_sdcard));

        showGazeCursor();

        parseAndSetScene(R.xml.scene);

        // get scene objects
        canvas = (CanvasSceneObject) findObjectById(R.id.canvas);
        canvasRenderer = new CanvasRenderer(this);
        canvas.setOnDrawListener(canvasRenderer);

        video = (VideoSceneObject) findObjectById(R.id.video);

        // setup animations
        this.fadeInVideo = loadAnimator(R.animator.fade_in, video, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                video.setVisible(true);
            }
        });
        this.fadeOutCanvas = loadAnimator(R.animator.fade_out, canvas, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                canvas.setVisible(false);
            }
        });

        // animation while looking at start button
        detector = new ObjectLookingStateDetector(canvas, new ObjectLookingStateDetector.ObjectLookingStateListener() {
            boolean notified;

            @Override
            public void onLookStart(SceneObject sceneObject, VrFrame vrFrame) {
                canvasRenderer.setLooking(true);
                notified = false;
            }

            @Override
            public void onLooking(SceneObject sceneObject, VrFrame vrFrame) {
                canvasRenderer.update(vrFrame);

                if (!notified && canvasRenderer.getSweepFraction() >= 1.0f) {
                    notified = true;
                    startPlaying();
                }
            }

            @Override
            public void onLookEnd(SceneObject sceneObject, VrFrame vrFrame) {
                canvasRenderer.setLooking(false);
            }
        });
        onFrame(detector);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void oneTimeShutDown(VrContext context) {
        release();
    }

    private void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
            mediaPlayer = null;
        }
    }

    private void startPlaying() {
        hideGazeCursor();
        offFrame(detector);

        if (mediaPlayer != null) {
            release();
        }

        if (file.exists()) {
            mediaPlayer = MediaPlayer.create(this, Uri.fromFile(file));
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.video);
            getApp().showInfoText(3, getString(R.string.error_default_video));
        }

        if (mediaPlayer != null) {
            try {
                mediaPlayer.start();
                video.setMediaPlayer(mediaPlayer);

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
                getApp().showInfoText(1, "error");
                e.printStackTrace();
            }
        }

        if (canvas != null) {
            animate(fadeOutCanvas);
        }

        if (video != null) {
            animate(fadeInVideo);
        }
    }

    private void pause() {
        showGazeCursor();

        try {
            onFrame(detector);
        } catch (RuntimeException e) {
            // ignore to prevent crash
        }

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            } catch (IllegalStateException e) {
                getApp().showInfoText(1, "error");
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
