package com.eje_c.vrvideoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

import com.eje_c.meganekko.Frame;
import com.eje_c.meganekko.Texture;

public class CanvasRenderer implements Texture.CanvasRenderer {

    private static final float SWEEP_TIME = 3.0f;
    private static final Interpolator interpolator = PathInterpolatorCompat.create(0.4f, 0.0f, 0.2f, 1.0f); // Material design interpolator
    private final Paint mSweepPaint = new Paint();
    private final RectF mSweepRect = new RectF(11, 11, 500, 500);
    private Bitmap mStartButton;
    private boolean mLooking;
    private float mSweepFraction;
    private boolean mDirty = false;

    public CanvasRenderer(Context context) {
        mStartButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.start_button);

        mSweepPaint.setColor(ContextCompat.getColor(context, R.color.lineColor));
        mSweepPaint.setStyle(Paint.Style.STROKE);
        mSweepPaint.setStrokeWidth(12);
    }

    @Override
    public void render(Canvas canvas, Frame frame) {
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);

        canvas.drawBitmap(mStartButton, 0, 0, null);

        // Draw sweep while looking
        if (mLooking) {
            float mappedFraction = interpolator.getInterpolation(mSweepFraction);
            float sweepAngle = mappedFraction * 360.0f;
            canvas.drawArc(mSweepRect, -90, sweepAngle, false, mSweepPaint);

        } else {
            mDirty = false;
        }
    }

    @Override
    public int getWidth() {
        return 512;
    }

    @Override
    public int getHeight() {
        return 512;
    }

    @Override
    public boolean isDirty() {
        return mDirty;
    }

    /**
     * @param looking Whether user is looking at start button.
     */
    public void setLooking(boolean looking) {
        this.mLooking = looking;
        this.mSweepFraction = 0.0f;
        this.mDirty = true;
    }

    /**
     * Call this while user is looking at start button.
     *
     * @param frame
     */
    public void update(Frame frame) {
        if (mSweepFraction < 1.0f) {
            mSweepFraction += frame.getDeltaSeconds() / SWEEP_TIME;
        }
    }

    /**
     * @return 0.0: No sweep is drawing. 1.0: Sweep is fully drawing.
     */
    public float getSweepFraction() {
        return mSweepFraction;
    }
}
