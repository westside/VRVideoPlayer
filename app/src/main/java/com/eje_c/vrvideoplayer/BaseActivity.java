package com.eje_c.vrvideoplayer;

import android.animation.Animator;
import android.animation.AnimatorInflater;

import com.eje_c.meganekko.MeganekkoActivity;

import ovr.KeyCode;

public class BaseActivity extends MeganekkoActivity {

    // If you intent to prevent unpredictable user control, such as returning to Oculus Home,
    // disabling back button is effective option.
    // But such behavior is contrary to the Oculus guideline.
    @Override
    public boolean onKeyDown(int keyCode, int repeatCount) {

        if (keyCode == KeyCode.OVR_KEY_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, repeatCount);
    }

    @Override
    public boolean onKeyShortPress(int keyCode, int repeatCount) {

        if (keyCode == KeyCode.OVR_KEY_BACK) {
            return true;
        }

        return super.onKeyShortPress(keyCode, repeatCount);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, int repeatCount) {

        if (keyCode == KeyCode.OVR_KEY_BACK) {
            return true;
        }

        return super.onKeyLongPress(keyCode, repeatCount);
    }

    /**
     * Loading animator utility method.
     *
     * @param animatorRes Animator resource ID
     * @param target      Target object
     * @param listeners   Optional listeners
     * @return Animator
     */
    public Animator loadAnimator(int animatorRes, Object target, Animator.AnimatorListener... listeners) {

        final Animator animator = AnimatorInflater.loadAnimator(this, animatorRes);
        animator.setTarget(target);

        if (listeners != null) {
            for (Animator.AnimatorListener listener : listeners) {
                animator.addListener(listener);
            }
        }

        return animator;
    }
}
