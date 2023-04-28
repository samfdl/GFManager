/*
 * Copyright (C) 2013-2017, Shenzhen Huiding Technology Co., Ltd.
 * All Rights Reserved.
 */

package com.goodix.gftest.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.goodix.gftest.R;

public class PixelMapView extends View {

    private int mMaxPixel;

    private float mMaxPixelWidth;
    private int mMaxPixelPerLine;

    private float mPixelBorderWidth;
    private int mPixelBorderColor;

    private int mDefaultPixelColor;
    private int mUsedPixelColor;
    private int mUnusedPixelColor;

    private byte[] mPixelStatus;

    /**
     * true if not all properties are set. then the view isn't drawn and there are no errors in the
     * LayoutEditor
     */
    private boolean mShowScales = true;
    private float mScaleLength;
    private float mXPixelsPerScale;
    private float mYPixelsPerScale;
    private float mTextSize;
    private float mTextGap;

    private float mPixelMapWidth;

    private Paint mPixelFillPaint = new Paint();
    private Paint mPixelStrokePaint = new Paint();
    private Paint mTextPaint = new Paint();

    public PixelMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PixelMapView(Context context) {
        this(context, null);
    }

    public PixelMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context
                .obtainStyledAttributes(attrs, R.styleable.PixelMapView,
                        defStyleAttr, 0);

        if (attributes != null) {
            try {
                setMaxPixel(attributes.getInt(R.styleable.PixelMapView_max_pixels, 4096));
                setMaxPixelPerLine(attributes.getInt(R.styleable.PixelMapView_max_pixel_per_line, 64));
                setMaxPixelWidth(attributes.getDimensionPixelSize(R.styleable.PixelMapView_max_pixel_width, 20));
                setPixelBorderWidth((int) attributes.getDimension(R.styleable.PixelMapView_pixel_border_width, 2));
                setPixelBorderColor(attributes.getColor(R.styleable.PixelMapView_pixel_border_color, Color.BLACK));
                setDefaultPixelColor(attributes.getColor(R.styleable.PixelMapView_default_pixel_color, Color.GRAY));
                setUsedPixelColor(attributes.getColor(R.styleable.PixelMapView_used_pixel_color, Color.RED));
                setUnusedPixelColor(attributes.getColor(R.styleable.PixelMapView_unused_pixel_color, Color.GREEN));
                setTextSize(attributes.getDimension(R.styleable.PixelMapView_text_size, 30));
                setTextGap(attributes.getDimension(R.styleable.PixelMapView_text_gap, 5));
                setScaleLength(attributes.getDimension(R.styleable.PixelMapView_scale_length, 5));
                setXPixelsPerScale(attributes.getInteger(R.styleable.PixelMapView_max_xaxis_scales, 16));
                setYPixelsPerScale(attributes.getInteger(R.styleable.PixelMapView_max_yaxis_scales, 16));
                setShowScales(attributes.getBoolean(R.styleable.PixelMapView_show_scales, true));
            } finally {
                attributes.recycle();
            }
        }

        updatePixelPaint();
        updateTextPaint();
    }

    private void updatePixelPaint() {

        mPixelFillPaint = new Paint();
        mPixelFillPaint.setAntiAlias(true);
        mPixelFillPaint.setStyle(Paint.Style.FILL);

        mPixelStrokePaint = new Paint();
        mPixelStrokePaint.setAntiAlias(true);
        mPixelStrokePaint.setStyle(Paint.Style.STROKE);
        mPixelStrokePaint.setColor(mPixelBorderColor);
        mPixelStrokePaint.setStrokeWidth(mPixelBorderWidth);

        invalidate();
    }

    private void updateTextPaint() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStrokeWidth(2);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updatePixelsMap(canvas);
    }

    private void updatePixelsMap(Canvas canvas) {

        float width = mPixelMapWidth / mMaxPixelPerLine < mMaxPixelWidth ? mPixelMapWidth / mMaxPixelPerLine : mMaxPixelWidth;

        int cols = (int) (mPixelMapWidth / width);
        int rows = mMaxPixel / cols;
        int extra_col = mMaxPixel % cols;

        float originX = (getMeasuredWidth() - mPixelMapWidth) / 2;
        float originY = 0;

        float startX = originX;
        float startY = originY;

        if (mShowScales) {

            originX = 0;
            originY = 0;

            startX = originX + mTextPaint.measureText(String.valueOf(mMaxPixel / 1024) + " MB") + mTextGap + mScaleLength;
            startY = originY + mTextSize + mTextGap + mScaleLength;

            // draw origin point
            drawTextCenter(String.valueOf(0), startX - mTextPaint.measureText("0") - mTextGap - mScaleLength, startY - mScaleLength, canvas);

            // draw XAxis scales
            for (int i = 1; i <= cols; i++) {
                if (i % mXPixelsPerScale == 0) {
                    String text = String.valueOf(i) + " KB";
                    float textLength = mTextPaint.measureText(text);

                    canvas.drawLine(startX - 1 + i * width, startY, startX - 1 + i * width, startY - mScaleLength, mPixelStrokePaint);
                    drawTextCenter(text, startX - 1 + i * width - textLength / 2, startY - mScaleLength - mTextGap - mTextSize / 2, canvas);
                }
            }

            // draw YAxis scales
            for (int i = 1; i <= rows; i++) {

                String text = String.valueOf(i * cols / 1024) + " MB";
                float textLength = mTextPaint.measureText(text);

                if (i % mYPixelsPerScale == 0) {
                    canvas.drawLine(startX - mScaleLength, startY - 1 + i * width, startX, startY - 1 + i * width, mTextPaint);
                    drawTextCenter(text, startX - textLength - mTextGap - mScaleLength, startY - 1 + i * width, canvas);
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (mPixelStatus == null || (i * cols + j >= mPixelStatus.length && i * cols + j < mMaxPixel)) {
                    mPixelFillPaint.setColor(mDefaultPixelColor);
                } else if ((i * cols + j) < mPixelStatus.length) {
                    if (mPixelStatus[i * cols + j] > 0) {
                        mPixelFillPaint.setColor(mUsedPixelColor);
                    } else {
                        mPixelFillPaint.setColor(mUnusedPixelColor);
                    }
                }
                canvas.drawRect(startX + j * width, startY + i * width,
                        startX + (j + 1) * width, startY + (i + 1) * width, mPixelFillPaint);

                // draw pixel border
                canvas.drawRect(startX + j * width, startY + i * width,
                        startX + (j + 1) * width, startY + (i + 1) * width, mPixelStrokePaint);
            }
        }

        if (extra_col > 0) {
            for (int i = 0; i < extra_col; i++) {
                canvas.drawRect(startX + i * width, startY + rows * width,
                        startX + (i + 1) * width, startY + (rows + 1) * width, mPixelFillPaint);

                canvas.drawRect(startX + i * width, startY + rows * width,
                        startX + (i + 1) * width, startY + (rows + 1) * width, mPixelStrokePaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }

        float pixelWidth = width / mMaxPixelPerLine < mMaxPixelWidth ? width / mMaxPixelPerLine : mMaxPixelWidth;

        int cols = width / (int) pixelWidth;
        int rows = mMaxPixel / cols;
        int extra_col = mMaxPixel % cols;

        int computeHeight = (int) (extra_col > 0 ? (rows + 1) * pixelWidth : rows * pixelWidth);

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = computeHeight < heightSize ? computeHeight : heightSize;
        } else if (heightMode == MeasureSpec.UNSPECIFIED) {
            height = computeHeight;
        }

        if (mShowScales) {
            mPixelMapWidth = width - (mTextPaint.measureText(String.valueOf(mMaxPixel)) - mTextGap - mScaleLength) * 2;
        } else {
            mPixelMapWidth = width;
        }

        setMeasuredDimension(width, height);
    }

    private void drawTextCenter(String text, float x, float y, Canvas canvas) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;

        float offY = fontHeight / 2 - fontMetrics.bottom;

        canvas.drawText(text, x, y + offY, mTextPaint);
    }


    public int getMaxPixel() {
        return mMaxPixel;
    }

    public void setMaxPixel(int maxPixel) {
        if (maxPixel < 0) {
            this.mMaxPixel = 0;
        }

        if (this.mMaxPixel != maxPixel) {
            this.mMaxPixel = maxPixel;
            postInvalidate();
        }
    }

    public float getMaxPixelWidth() {
        return mMaxPixelWidth;
    }

    public void setMaxPixelWidth(int mPixelWidth) {
        this.mMaxPixelWidth = mPixelWidth;
    }

    public int getMaxPixelPerLine() {
        return mMaxPixelPerLine;
    }

    public void setMaxPixelPerLine(int mMaxPixelPerLine) {
        this.mMaxPixelPerLine = mMaxPixelPerLine;
    }

    public float getPixelBorderWidth() {
        return mPixelBorderWidth;
    }

    public void setPixelBorderWidth(float mPixelBorderWidth) {
        this.mPixelBorderWidth = mPixelBorderWidth;

        updatePixelPaint();
    }

    public int getDefaultPixelColor() {
        return mDefaultPixelColor;
    }

    public void setDefaultPixelColor(int mDefaultPixelColor) {
        this.mDefaultPixelColor = mDefaultPixelColor;

        updatePixelPaint();
    }

    public byte[] getPixelStatus() {
        return mPixelStatus;
    }

    public void setPixelStatus(byte[] pixelStatus) {
        this.mPixelStatus = pixelStatus;

        postInvalidate();
    }

    public void setPixelStatus(byte value, int position) {
        if (this.mPixelStatus == null) {
            return;
        }

        if (position < this.mPixelStatus.length) {
            this.mPixelStatus[position] = value;
        }

        postInvalidate();
    }

    public void setPixelStatus(byte value, int from, int to) {
        if (this.mPixelStatus == null) {
            return;
        }

        int sanityFrom = from;
        int sanityTo = to;
        if (from < 0) {
            sanityFrom = 0;
        }

        if (to >= mPixelStatus.length) {
            sanityTo = mPixelStatus.length - 1;
        }

        for (int i = sanityFrom; i <= sanityTo; i++) {
            this.mPixelStatus[i] = value;
        }

        postInvalidate();
    }

    public int getUsedPixelColor() {
        return mUsedPixelColor;
    }

    public void setUsedPixelColor(int mUsedPixelColor) {
        this.mUsedPixelColor = mUsedPixelColor;

        updatePixelPaint();
    }

    public int getUnusedPixelColor() {
        return mUnusedPixelColor;
    }

    public void setUnusedPixelColor(int mUnusedPixelColor) {
        this.mUnusedPixelColor = mUnusedPixelColor;

        updatePixelPaint();
    }

    public int getPixelBorderColor() {
        return mPixelBorderColor;
    }

    public void setPixelBorderColor(int mPixelBorderColor) {
        this.mPixelBorderColor = mPixelBorderColor;

        updatePixelPaint();
    }

    public float getTextGap() {
        return mTextGap;
    }

    public void setTextGap(float textGap) {
        this.mTextGap = textGap;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    public float getXPixelsPerScale() {
        return mXPixelsPerScale;
    }

    public void setXPixelsPerScale(float xPixelsPerScale) {
        this.mXPixelsPerScale = xPixelsPerScale;
    }

    public float getYPixelsPerScale() {
        return mYPixelsPerScale;
    }

    public void setYPixelsPerScale(float yPixelsPerScale) {
        this.mYPixelsPerScale = yPixelsPerScale;
    }

    public float getScaleLength() {
        return mScaleLength;
    }

    public void setScaleLength(float scaleLength) {
        this.mScaleLength = scaleLength;
    }

    public void setShowScales(boolean show) {
        this.mShowScales = show;

        postInvalidate();
    }

    public boolean isShowScales() {
        return this.mShowScales;
    }
}
