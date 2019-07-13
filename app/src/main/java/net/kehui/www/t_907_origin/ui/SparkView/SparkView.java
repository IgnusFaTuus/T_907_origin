/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kehui.www.t_907_origin.ui.SparkView;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.application.MyApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Gong
 * @date 2018/12/23
 */
public class SparkView extends View implements ScrubGestureDetector.ScrubListener {
    private static final String TAG = "Spark";
    /**
     * styleable values
     */
    @ColorInt
    private int lineColor;
    private float lineWidth;
    private float cornerRadius;
    private boolean fill;
    @ColorInt
    private int baseLineColor;
    private float baseLineWidth;
    @ColorInt
    private int scrubLineColor;
    private float scrubLineWidth;
    private boolean scrubEnabled;
    private boolean animateChanges;

    /**
     * the onDraw data
     */
    private final Path renderPath = new Path();
    private final Path renderPath2 = new Path();
    private final Path sparkPath = new Path();
    private final Path sparkPath2 = new Path();
    private final Path baseLinePath = new Path();
    private final Path scrubLinePath = new Path();
    private final Path scrubLinePath2 = new Path();

    /**
     * adapter
     */
    private BaseSparkAdapter adapter;

    /**
     * misc fields
     */
    private ScaleHelper scaleHelper;
    private Paint sparkLinePaint;
    private Paint baseLinePaint;
    private Paint scrubLinePaint;
    private Paint scrubLinePaint2;

    private OnScrubListener scrubListener;
    private ScrubGestureDetector scrubGestureDetector;
    private List<Float> xPoints;
    private ValueAnimator pathAnimator;
    private final RectF contentRect = new RectF();

    private static int shortAnimationTime;

    private float scX;
    public int startPoint;
    /**
     * //GC20190629
     */
    private boolean startMove;

    public SparkView(Context context) {
        super(context);
        init(context, null, R.attr.spark_SparkViewStyle,
                R.style.spark_SparkView);
    }

    public SparkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.spark_SparkViewStyle, R.style.spark_SparkView);
    }

    public SparkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.spark_SparkView);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        //GC20190629
        startMove = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.spark_SparkView, defStyleAttr, defStyleRes);
        lineColor = a.getColor(R.styleable.spark_SparkView_spark_lineColor, 0);
        lineWidth = a.getDimension(R.styleable.spark_SparkView_spark_lineWidth, 0);
        cornerRadius = a.getDimension(R.styleable.spark_SparkView_spark_cornerRadius, 0);
        fill = a.getBoolean(R.styleable.spark_SparkView_spark_fill, false);
        baseLineColor = a.getColor(R.styleable.spark_SparkView_spark_baseLineColor, 0);
        baseLineWidth = a.getDimension(R.styleable.spark_SparkView_spark_baseLineWidth, 0);
        scrubEnabled = a.getBoolean(R.styleable.spark_SparkView_spark_scrubEnabled, true);
        scrubLineColor = a.getColor(R.styleable.spark_SparkView_spark_scrubLineColor, baseLineColor);
        scrubLineWidth = a.getDimension(R.styleable.spark_SparkView_spark_scrubLineWidth, lineWidth);
        animateChanges = a.getBoolean(R.styleable.spark_SparkView_spark_animateChanges, false);
        a.recycle();

        sparkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkLinePaint.setColor(lineColor);
        sparkLinePaint.setStrokeWidth(lineWidth);
        sparkLinePaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
        sparkLinePaint.setStrokeCap(Paint.Cap.ROUND);
        if (cornerRadius != 0) {
            sparkLinePaint.setPathEffect(new CornerPathEffect(cornerRadius));
        }

        baseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baseLinePaint.setStyle(Paint.Style.STROKE);
        baseLinePaint.setColor(Color.WHITE);
        baseLinePaint.setStrokeWidth(lineWidth);

        scrubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrubLinePaint.setStyle(Paint.Style.STROKE);
        scrubLinePaint.setStrokeWidth(scrubLineWidth);
        scrubLinePaint.setColor(Color.RED);
        scrubLinePaint.setStrokeCap(Paint.Cap.ROUND);

        scrubLinePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        scrubLinePaint2.setStyle(Paint.Style.STROKE);
        scrubLinePaint2.setStrokeWidth(scrubLineWidth);
        scrubLinePaint2.setColor(Color.parseColor("#ff00ff"));
        //GC20190628 变为虚光标
        scrubLinePaint2.setAntiAlias(true);
        scrubLinePaint2.setStrokeCap(Paint.Cap.SQUARE);
        scrubLinePaint2.setPathEffect(new DashPathEffect(new float[]{6,10},0));

        final Handler handler = new Handler();
        final float touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scrubGestureDetector = new ScrubGestureDetector(this, handler, touchSlop);
        scrubGestureDetector.setEnabled(scrubEnabled);
        setOnTouchListener(scrubGestureDetector);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        updateContentRect();
        populatePath();

    }

    /**
     * Populates the {@linkplain #sparkPath} with points
     */
    private void populatePath() {
        if (adapter == null) {
            return;
        }
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        //GC20181227
//        final int adapterCount = adapter.getMax();
        final int adapterCount = adapter.getCount();

        // to draw anything, we need 2 or more points
        if (adapterCount < 2) {
            clearData();
            return;
        }

        scaleHelper = new ScaleHelper(adapter, contentRect, lineWidth, fill);

        // xPoints is only used in scrubbing, skip if disabled
        if (xPoints == null) {
            xPoints = new ArrayList<>(adapterCount);
        } else {
            xPoints.clear();
        }

        // make our main graph path
        sparkPath.reset();
        for (int i = 0; i < adapterCount; i++) {
            final float x = scaleHelper.getX(adapter.getX(i));
            final float y = scaleHelper.getY(adapter.getY(i));

            if (i == 0) {
                sparkPath.moveTo(x, y);
            } else {
                sparkPath.lineTo(x, y);
            }
            xPoints.add(x);
        }

        // if we're filling the graph in, close the path's circuit
        if (fill) {
            //GC20181227
//            float lastX = scaleHelper.getX(adapter.getMax() - 1);
            float lastX = scaleHelper.getX(adapter.getCount() - 1);
            float bottom = getHeight() - getPaddingBottom();
            // line straight down to the bottom of the view
            sparkPath.lineTo(lastX, bottom);
            // line straight left to far edge of the view
            sparkPath.lineTo(getPaddingStart(), bottom);
            // line straight up to meet the first point
            sparkPath.close();
        }

        //画第二条线
        Log.e("QQQ", "adapter" + ".getIsShowCompare()" + adapter.getCompare());
        if (adapter.getCompare()) {
            sparkPath2.reset();
            for (int i = 0; i < adapterCount; i++) {
                final float x = scaleHelper.getX(adapter.getX(i));
                final float y = scaleHelper.getY(adapter.getY1(i));

                if (i == 0) {
                    sparkPath2.moveTo(x, y);
                } else {
                    sparkPath2.lineTo(x, y);
                }
                xPoints.add(x);
            }

            // if we're filling the graph in, close the path's circuit
            if (fill) {
                //GC20181227
//                float lastX = scaleHelper.getX(adapter.getMax() - 1);
                float lastX = scaleHelper.getX(adapter.getCount() - 1);
                float bottom = getHeight() - getPaddingBottom();
                // line straight down to the bottom of the view
                sparkPath2.lineTo(lastX, bottom);
                // line straight left to far edge of the view
                sparkPath2.lineTo(getPaddingStart(), bottom);
                // line straight up to meet the first point
                sparkPath2.close();
            }
            renderPath2.reset();
            renderPath2.addPath(sparkPath2);

        } else {
            renderPath2.reset();

        }

        // make our base line path
        baseLinePath.reset();
        if (adapter.hasBaseLine()) {
            float scaledBaseLine = scaleHelper.getY(adapter.getBaseLine());
            baseLinePath.moveTo(0, scaledBaseLine);
            baseLinePath.lineTo(getWidth(), scaledBaseLine);
        }

        renderPath.reset();
        renderPath.addPath(sparkPath);

        invalidate();
    }

    /**
     * Get the scaled (pixel) coordinate of your given x value. If no scale is currently computed
     * (for instance {@link BaseSparkAdapter} has not been set or has less than 2 points of data). This
     * mode will return the unscaled value.
     *
     * @param x the value to scale (should be the same units as your graph's data points)
     * @return the pixel coordinates of where this point is located in SparkView's bounds
     */
    public float getScaledX(float x) {
        if (scaleHelper == null) {
            Log.w(TAG, "getScaledX() - no scale available yet.");
            return x;
        }
        return scaleHelper.getX(x);
    }

    /**
     * Get the scaled (pixel) coordinate of your given y value. If no scale is currently computed
     * (for instance {@link BaseSparkAdapter} has not been set or has less than 2 points of data). This
     * mode will return the unscaled value.
     *
     * @param y the value to scale (should be the same units as your graph's data points)
     * @return the pixel coordinates of where this point is located in SparkView's bounds
     */
    public float getScaledY(float y) {
        if (scaleHelper == null) {
            Log.w(TAG, "getScaledX() - no scale available yet.");
            return y;
        }
        return scaleHelper.getY(y);
    }

    /**
     * Gets a copy of the sparkLine path
     */
    public Path getSparkLinePath() {
        return new Path(sparkPath);
    }

    /**
     * @param x  实光标位置（监听触摸位置）
     */
    public void setScrubLineRealMove(float x) {
        scrubLinePath.reset();
        scrubLinePath.moveTo(x, getPaddingTop());
        scrubLinePath.lineTo(x, getHeight() - getPaddingBottom());
        invalidate();
    }

    /**
     * @param x 虚光标位置（监听触摸位置）
     */
    public void setScrubLineVirtualMove(float x) {
        scrubLinePath2.reset();
        scrubLinePath2.moveTo(x, getPaddingTop());
        scrubLinePath2.lineTo(x, getHeight() - getPaddingBottom());
        invalidate();
    }

    /**
     * @param position 实光标位置（设置固定值）
     */
    public void setScrubLineReal(int position) {
        //GC2019629
        startMove = true;
        scrubLinePath.reset();
        scrubLinePath.moveTo(xPoints.get(position), getPaddingTop());
        scrubLinePath.lineTo(xPoints.get(position), getHeight() - getPaddingBottom());
        invalidate();
    }

    public void setScrubLineRealState() {
        scrubLinePath.reset();
        invalidate();
    }

    /**
     * @param position 虚光标位置（设置固定值）
     */
    public void setScrubLineVirtual(int position) {
        //GC20190629
        startMove = true;
        scrubLinePath2.reset();
        scrubLinePath2.moveTo(xPoints.get(position), getPaddingTop());
        scrubLinePath2.lineTo(xPoints.get(position), getHeight() - getPaddingBottom());
        invalidate();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateContentRect();
        populatePath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(baseLinePath, baseLinePaint);
        canvas.drawPath(renderPath, sparkLinePaint);
        canvas.drawPath(renderPath2, baseLinePaint);
        canvas.drawPath(scrubLinePath2, scrubLinePaint2);
        canvas.drawPath(scrubLinePath, scrubLinePaint);

        if(!startMove) {
            setScrubLineReal(0);
            setScrubLineVirtual(255);
        }

        //setScrubLineReal(0);
        //setScrubLineVirtual(255);

        //setScrubLine(50);
        /*drawTria(canvas, xPoints.get(startPoint), getHeight() - getPaddingBottom(),
                xPoints.get(startPoint), getPaddingTop(), 30, 10);*/

    }

    public void setStartPoint(int startPoint) {
        this.startPoint = startPoint;
    }

    /*protected void drawTria(Canvas canvas, float fromX, float fromY, float toX, float toY, int heigth, int bottom) {
        // heigth和bottom分别为三角形的高与底的一半,调节三角形大小
        canvas.drawLine(xPoints.get(startPoint), getHeight() - getPaddingBottom(), xPoints.get(startPoint), getPaddingTop(), arrowLinePaint);
        float juli = (float) Math.sqrt((toX - fromX) * (toX - fromX)
                + (toY - fromY) * (toY - fromY));// 获取线段距离
        float juliX = toX - fromX;// 有正负，不要取绝对值
        float juliY = toY - fromY;// 有正负，不要取绝对值
        float dianX = toX - (heigth / juli * juliX);
        float dianY = toY - (heigth / juli * juliY);
        float dian2X = fromX + (heigth / juli * juliX);
        float dian2Y = fromY + (heigth / juli * juliY);
        //终点的箭头
        Path path = new Path();
        path.moveTo(toX, toY);// 此点为三边形的起点
        path.lineTo(dianX + (bottom / juli * juliY), dianY
                - (bottom / juli * juliX));
        path.lineTo(dianX - (bottom / juli * juliY), dianY
                + (bottom / juli * juliX));
        path.close(); // 使这些点构成封闭的三边形
        canvas.drawPath(path, arrowLinePaint);
    }*/

    /**
     * Get the color of the sparkline
     */
    @ColorInt
    public int getLineColor() {
        return lineColor;
    }

    /**
     * Set the color of the sparkline
     */
    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        sparkLinePaint.setColor(lineColor);
        invalidate();
    }

    /**
     * Get the width in pixels of the sparkline's stroke
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Set the width in pixels of the sparkline's stroke
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        sparkLinePaint.setStrokeWidth(lineWidth);
        invalidate();
    }

    /**
     * Get the corner radius in pixels used when rounding the sparkline's segments.
     */
    public float getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Set the corner radius in pixels to use when rounding the sparkline's segments. Passing 0
     * indicates that corners should not be rounded.
     */
    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        if (cornerRadius != 0) {
            sparkLinePaint.setPathEffect(new CornerPathEffect(cornerRadius));
        } else {
            sparkLinePaint.setPathEffect(null);
        }
        invalidate();
    }

    /**
     * Whether or not this view animates changes to its data.
     */
    public boolean getAnimateChanges() {
        return animateChanges;
    }

    /**
     * Whether or not this view should animate changes to its data.
     */
    public void setAnimateChanges(boolean animate) {
        this.animateChanges = animate;
    }

    /**
     * Get the {@link Paint} used to draw the scrub line. Any custom modifications to this
     * {@link Paint} will not reflect until the next call to {@link #invalidate()}
     */
    public Paint getScrubLinePaint() {
        return scrubLinePaint;
    }

    /**
     * Set the {@link Paint} to be used to draw the scrub line. Warning: setting a paint other than
     * the instance returned by {@link #getScrubLinePaint()} may result in loss of style attributes
     * specified on this view.
     */
    public void setScrubLinePaint(Paint scrubLinePaint) {
        this.scrubLinePaint = scrubLinePaint;
        invalidate();
    }

    /**
     * Return whether or not this sparkline should fill the area underneath.
     */
    public boolean isFill() {
        return fill;
    }

    /**
     * Set whether or not this sparkline should fill the area underneath.
     */
    public void setFill(boolean fill) {
        if (this.fill != fill) {
            this.fill = fill;
            sparkLinePaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            populatePath();
        }
    }

    /**
     * Get the {@link Paint} used to draw the sparkline. Any modifications to this {@link Paint}
     * will not reflect until the next call to {@link #invalidate()}
     */
    public Paint getSparkLinePaint() {
        return sparkLinePaint;
    }

    /**
     * Set the {@link Paint} to be used to draw the sparkline. Warning: setting a paint other than
     * the instance returned by {@link #getSparkLinePaint()} may result in loss of style attributes
     * specified on this view.
     */
    public void setSparkLinePaint(Paint pathPaint) {
        this.sparkLinePaint = pathPaint;
        invalidate();
    }

    /**
     * Get the color of the base line
     */
    @ColorInt
    public int getBaseLineColor() {
        return baseLineColor;
    }

    /**
     * Set the color of the base line
     */
    public void setBaseLineColor(@ColorInt int baseLineColor) {
        this.baseLineColor = baseLineColor;
        baseLinePaint.setColor(baseLineColor);
        invalidate();
    }

    /**
     * Get the width in pixels of the base line's stroke
     */
    public float getBaseLineWidth() {
        return baseLineWidth;
    }

    /**
     * Set the width in pixels of the base line's stroke
     */
    public void setBaseLineWidth(float baseLineWidth) {
        this.baseLineWidth = baseLineWidth;
        baseLinePaint.setStrokeWidth(baseLineWidth);
        invalidate();
    }

    /**
     * Get the {@link Paint} used to draw the base line. Any modifications to this {@link Paint}
     * will not reflect until the next call to {@link #invalidate()}
     */
    public Paint getBaseLinePaint() {
        return baseLinePaint;
    }

    /**
     * Set the {@link Paint} to be used to draw the base line. Warning: setting a paint other than
     * the instance returned by {@link #getBaseLinePaint()} ()} may result in loss of style
     * attributes specified on this view.
     */
    public void setBaseLinePaint(Paint baseLinePaint) {
        this.baseLinePaint = baseLinePaint;
        invalidate();
    }

    /**
     * Get the color of the scrub line
     */
    @ColorInt
    public int getScrubLineColor() {
        return scrubLineColor;
    }

    /**
     * Set the color of the scrub line
     */
    public void setScrubLineColor(@ColorInt int scrubLineColor) {
        this.scrubLineColor = scrubLineColor;
        scrubLinePaint.setColor(scrubLineColor);
        invalidate();
    }

    /**
     * Get the width in pixels of the scrub line's stroke
     */
    public float getScrubLineWidth() {
        return scrubLineWidth;
    }

    /**
     * Set the width in pixels of the scrub line's stroke
     */
    public void setScrubLineWidth(float scrubLineWidth) {
        this.scrubLineWidth = scrubLineWidth;
        scrubLinePaint.setStrokeWidth(scrubLineWidth);
        invalidate();
    }

    /**
     * Return true if scrubbing is enabled on this view
     */
    public boolean isScrubEnabled() {
        return scrubEnabled;
    }

    /**
     * Set whether or not to enable scrubbing on this view.
     */
    public void setScrubEnabled(boolean scrubbingEnabled) {
        this.scrubEnabled = scrubbingEnabled;
        scrubGestureDetector.setEnabled(scrubbingEnabled);
        invalidate();
    }

    /**
     * Get the current {@link com.robinhood.spark.SparkView.OnScrubListener}
     */
    public OnScrubListener getScrubListener() {
        return scrubListener;
    }

    /**
     * Set a {@link com.robinhood.spark.SparkView.OnScrubListener} to be notified of the user's
     * scrubbing gestures.
     */
    public void setScrubListener(OnScrubListener scrubListener) {
        this.scrubListener = scrubListener;
    }

    /**
     * Get the backing {@link BaseSparkAdapter}
     */
    public BaseSparkAdapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the backing {@link BaseSparkAdapter} to generate the points to be graphed
     */
    public void setAdapter(BaseSparkAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        if (this.adapter != null) {
            this.adapter.registerDataSetObserver(dataSetObserver);
        }
        populatePath();
    }

    private void doPathAnimation() {
        if (pathAnimator != null) {
            pathAnimator.cancel();
        }

        if (shortAnimationTime == 0) {
            shortAnimationTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        }

        final PathMeasure pathMeasure = new PathMeasure(sparkPath, false);

        float endLength = pathMeasure.getLength();
        if (endLength == 0) {
            return;
        }

        pathAnimator = ValueAnimator.ofFloat(0, endLength);
        pathAnimator.setDuration(shortAnimationTime);
        pathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedPathLength = (Float) animation.getAnimatedValue();
                renderPath.reset();
                pathMeasure.getSegment(0, animatedPathLength, renderPath, true);
                renderPath.rLineTo(0, 0);
                invalidate();
            }
        });
        pathAnimator.start();
    }

    private void clearData() {
        scaleHelper = null;
        renderPath.reset();
        sparkPath.reset();
        baseLinePath.reset();
        invalidate();
    }

    /**
     * Helper class for handling scaling logic.
     */
    static class ScaleHelper {
        // the width and height of the view
        final float width, height;
        final int size;
        // the scale factor for the Y values
        final float xScale, yScale;
        // translates the Y values back into the bounding rect after being scaled
        final float xTranslation, yTranslation;

        ScaleHelper(BaseSparkAdapter adapter, RectF contentRect, float lineWidth, boolean fill) {
            final float leftPadding = contentRect.left;
            final float topPadding = contentRect.top;

            // subtract lineWidth to offset for 1/2 of the line bleeding out of the content box on
            // either side of the view
            final float lineWidthOffset = fill ? 0 : lineWidth;
            this.width = contentRect.width() - lineWidthOffset;
            this.height = contentRect.height() - lineWidthOffset;
            //GC20181227
//            this.size = adapter.getMax();
            this.size = adapter.getCount();

            // get data bounds from adapter
            RectF bounds = adapter.getDataBounds();

            // if data is a line (which technically has no size), expand bounds to center the data
            bounds.inset(bounds.width() == 0 ? -1 : 0, bounds.height() == 0 ? -1 : 0);

            final float minX = bounds.left;
            final float maxX = bounds.right;
            final float minY = bounds.top;
            final float maxY = bounds.bottom;

            // xScale will compress or expand the min and dataMax x values to be just inside the view
            this.xScale = width / (maxX - minX);
            // xTranslation will move the x points back between 0 - width
            this.xTranslation = leftPadding - (minX * xScale) + (lineWidthOffset / 2);
            // yScale will compress or expand the min and dataMax y values to be just inside the view
            this.yScale = height / (maxY - minY);
            // yTranslation will move the y points back between 0 - height
            this.yTranslation = minY * yScale + topPadding + (lineWidthOffset / 2);
        }

        /**
         * Given the 'raw' X value, scale it to fit within our view.
         */
        float getX(float rawX) {
            return rawX * xScale + xTranslation;
        }

        /**
         * Given the 'raw' Y value, scale it to fit within our view. This mode also 'flips' the
         * value to be ready for drawing.
         */
        float getY(float rawY) {
            return height - (rawY * yScale) + yTranslation;
        }
    }

    @Override
    public int getPaddingStart() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
                ? super.getPaddingStart()
                : getPaddingLeft();
    }

    @Override
    public int getPaddingEnd() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1
                ? super.getPaddingEnd()
                : getPaddingRight();
    }

    /**
     * Gets the rect representing the 'content area' of the view. This is essentially the bounding
     * rect minus any padding.
     */
    private void updateContentRect() {
        if (contentRect == null) {
            return;
        }

        contentRect.set(
                getPaddingStart(),
                getPaddingTop(),
                getWidth() - getPaddingEnd(),
                getHeight() - getPaddingBottom()
        );
    }

    /**
     * returns the nearest index (into {@link #adapter}'s data) for the given x coordinate.
     */
    static int getNearestIndex(List<Float> points, float x) {
        int index = Collections.binarySearch(points, x);

        // if binary search returns positive, we had an exact match, return that index
        if (index >= 0) {
            return index;
        }

        // otherwise, calculate the binary search's specified insertion index
        index = -1 - index;

        // if we're inserting at 0, then our guaranteed nearest index is 0
        if (index == 0) {
            return index;
        }

        // if we're inserting at the very end, then our guaranteed nearest index is the final one
        if (index == points.size()) {
            return --index;
        }

        // otherwise we need to check which of our two neighbors we're closer to
        final float deltaUp = points.get(index) - x;
        final float deltaDown = x - points.get(index - 1);
        if (deltaUp > deltaDown) {
            // if the below neighbor is closer, decrement our index
            index--;
        }

        return index;
    }

    @Override
    public void onScrubbed(float x, float y) {
        //GC2019629
        startMove = true;
        scX = x;
        //GC20181227
//        if (adapter == null || adapter.getMax()== 0) {
        if (adapter == null || adapter.getCount()== 0) {
            return;
        }
        if (scrubListener != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
            int index = getNearestIndex(xPoints, x);
            if (scrubListener != null) {
                scrubListener.onScrubbed(adapter.getItem(index));
            }
        }
        //GC20181223 光标切换
        Log.i("QQQ", "adapter" + ".selectCursor()" + adapter.getCursorState());
        //GC20190628 光标位置限制，触摸有效范围
        if(x >= 8.9929 & x <= 1111.1304) {
            if (adapter.getCursorState()){
                setScrubLineRealMove(x);
            }else{
                setScrubLineVirtualMove(x);
            }
        }
        Log.i("position", String.valueOf(x));
    }

    public float getScX() {
        return scX;
    }

    @Override
    public void onScrubEnded() {
        scrubLinePath.reset();
        if (scrubListener != null) {
            scrubListener.onScrubbed(null);
        }
        invalidate();
    }

    /**
     * Listener for a user scrubbing (dragging their finger along) the graph.
     */
    public interface OnScrubListener {
        /**
         * Indicates the user is currently scrubbing over the given value. A null value indicates
         * that the user has stopped scrubbing.
         *
         * @param value //触摸位置
         */
        void onScrubbed(Object value);
    }

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            populatePath();

            if (animateChanges) {
                doPathAnimation();
            }
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            clearData();
        }
    };
}
