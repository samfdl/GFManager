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
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;

import com.goodix.gftest.R;

import java.util.ArrayList;
import java.util.List;

public class ColumnDiagramView extends View {

    private int mDefaultColumnColor;

    private ColumnData mData = new ColumnData();

    private float mMinXAxisLength;
    private float mMinYAxisLength;

    private int mMaxYAxisScale;
    private int mMaxXAxisScale;

    private float mScaleLength;
    private float mAxisStrokeWidth;

    private float mTextSize;
    private float mXAxisTextGap;
    private float mYAxisTextGap;

    private List<Integer> mColorList = new ArrayList<Integer>();

    private int mMaxValue;

    private boolean mShowColumnText = true;

    private Paint mAxisPaint = new Paint();
    private Paint mColumnPaint = new Paint();
    private Paint mTextPaint = new Paint();

    public ColumnDiagramView(Context context) {
        this(context, null);
    }

    public ColumnDiagramView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColumnDiagramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray attributes = context
                .obtainStyledAttributes(attrs, R.styleable.GFColumnDiagram,
                        defStyleAttr, 0);

        if (attributes != null) {
            try {
                setMaxValue(attributes.getInteger(R.styleable.GFColumnDiagram_max_height, 100));
                setDefaultColumnColor(attributes.getColor(R.styleable.GFColumnDiagram_column_color, Color.RED));
                setMaxXAxisScale(attributes.getInteger(R.styleable.GFColumnDiagram_max_scales_xaxis, 5));
                setMaxYAxisScale(attributes.getInteger(R.styleable.GFColumnDiagram_max_scales_yaxis, 10));
                setMinYAxisLength(attributes.getDimension(R.styleable.GFColumnDiagram_min_width, 320));
                setMinXAxisLength(attributes.getDimension(R.styleable.GFColumnDiagram_max_height, 220));
                setScaleLength(attributes.getDimension(R.styleable.GFColumnDiagram_scale_length, 10));
                setAxisStrokeWidth(attributes.getDimension(R.styleable.GFColumnDiagram_scale_stroke_width, 1));
                setTextSize(attributes.getDimension(R.styleable.GFColumnDiagram_text_size, 20));
                setXAxisTextGap(attributes.getDimension(R.styleable.GFColumnDiagram_text_gap_xaxis, 12));
                setYAxisTextGap(attributes.getDimension(R.styleable.GFColumnDiagram_text_gap_yaxis, 12));
            } finally {
                attributes.recycle();
            }
        }

        updateColumnColor();
        updateAxisPaint();
        updateTextPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        updateColumnDiagram(canvas);
    }

    private void updateAxisPaint() {
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setStyle(Paint.Style.STROKE);
        mAxisPaint.setStrokeWidth(mAxisStrokeWidth);

        invalidate();
    }

    private void updateTextPaint() {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);

        invalidate();
    }

    private void updateColumnPaint() {
        mColumnPaint = new Paint();
        mColumnPaint.setAntiAlias(true);
        mColumnPaint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    private void updateColumnColor() {
        mColorList.add(0, mDefaultColumnColor);
        mColorList.add(1, Color.GREEN);
        mColorList.add(2, Color.YELLOW);

        updateColumnPaint();
    }

    private void drawTextCenter(String text, float x, float y, Canvas canvas) {
        FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;

        float offY = fontHeight / 2 - fontMetrics.bottom;

        canvas.drawText(text, x, y + offY, mTextPaint);
    }

    private void updateColumnDiagram(Canvas canvas) {

        float yAxisTextLength = mTextPaint.measureText(String.valueOf(mMaxValue));

        float xAxisLength = getMeasuredWidth() - yAxisTextLength - mScaleLength - mYAxisTextGap;
        float yAxisLength = getMeasuredHeight() - mTextSize * 2 - mXAxisTextGap * 2;

        float originX = getMeasuredWidth() - xAxisLength;
        float originY = getMeasuredHeight() - mTextSize - mXAxisTextGap;

        // draw X/Y Axis
        canvas.drawLine(originX, originY, originX + xAxisLength, originY, mAxisPaint);
        canvas.drawLine(originX, originY, originX, originY - yAxisLength, mAxisPaint);

        int yScale = mMaxValue / mMaxYAxisScale;
        float yAxisScalesWidth = (float) (yScale * yAxisLength) / mMaxValue;
        int yScaleCount = mMaxValue % mMaxYAxisScale == 0 ? mMaxYAxisScale + 1 : mMaxYAxisScale;

        // draw Y Axis scales
        for (int i = 0; i < yScaleCount; i++) {
            float startY = originY - i * yAxisScalesWidth;
            float startX = originX - mTextPaint.measureText(String.valueOf(i * yScale)) - mYAxisTextGap - mScaleLength;

            canvas.drawLine(originX, startY, originX - mScaleLength, startY, mAxisPaint);
            // draw Y Axis scale text
            drawTextCenter(String.valueOf(i * yScale), startX, startY, canvas);
        }

        // draw X Axis scales & column
        int xScaleCount = mData.getSize();
        float xAxisScalesWidth = xAxisLength / xScaleCount;
        float columnWidth = mData.getGroupSize() > 1 ? xAxisScalesWidth * (float) 0.6 / mData.getGroupSize() : xAxisScalesWidth * (float) 0.6;
        for (int i = 0; i < xScaleCount; i++) {
            ColumnGroup group = mData.getColumnGroup(i);
            float startX = originX + i * xAxisScalesWidth;

            // draw X Axis scale
            canvas.drawLine(startX, originY, startX, originY + mScaleLength, mAxisPaint);
            // draw X Axis scale text
            float textSize = mTextPaint.measureText(group.getDescription());
            drawTextCenter(group.getDescription(), startX + (xAxisScalesWidth - textSize) / 2, originY + mXAxisTextGap + mTextSize / 2, canvas);

            // draw column
            int color = mDefaultColumnColor;
            for (int j = 0; j < group.getUnitSize(); j++) {

                float startUnitX = startX + xAxisScalesWidth * (float) 0.2 + j * columnWidth;

                ColumnUnitData data = group.getColumnUnitData(j);
                mColumnPaint.setColor(data.getColor());
                float height = (data.getValue() * yAxisLength / mMaxValue);

                canvas.drawRect(startUnitX, originY - height, startUnitX + columnWidth, originY, mColumnPaint);

                if (mShowColumnText) {
                    String value = String.valueOf(data.getValue());
                    textSize = mTextPaint.measureText(value);
                    drawTextCenter(value, startUnitX + (columnWidth - textSize) / 2,
                            originY - height - mYAxisTextGap - mTextSize / 2, canvas);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public float getAxisStrokeWidth() {
        return mAxisStrokeWidth;
    }

    public void setAxisStrokeWidth(float axisStrokeWidth) {
        this.mAxisStrokeWidth = axisStrokeWidth;
    }

    public int getDefaultColumnColor() {
        return mDefaultColumnColor;
    }

    public void setDefaultColumnColor(int defaultColumnColor) {
        this.mDefaultColumnColor = defaultColumnColor;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;

        postInvalidate();
    }

    public int getMaxXAxisScale() {
        return mMaxXAxisScale;
    }

    public void setMaxXAxisScale(int maxXAxisScale) {
        this.mMaxXAxisScale = maxXAxisScale;
    }

    public int getMaxYAxisScale() {
        return mMaxYAxisScale;
    }

    public void setMaxYAxisScale(int maxYAxisScale) {
        this.mMaxYAxisScale = maxYAxisScale;
    }

    public float getScaleLength() {
        return mScaleLength;
    }

    public void setScaleLength(float scaleLength) {
        this.mScaleLength = scaleLength;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    public float getMinXAxisLength() {
        return this.mMinXAxisLength;
    }

    public void setMinXAxisLength(float minXAxisLength) {
        this.mMinXAxisLength = minXAxisLength;
    }

    public float getXAxisTextGap() {
        return mXAxisTextGap;
    }

    public void setXAxisTextGap(float xAxisTextGap) {
        this.mXAxisTextGap = xAxisTextGap;
    }

    public float getMinYAxisLength() {
        return mMinYAxisLength;
    }

    public void setMinYAxisLength(float minYAxisLength) {
        this.mMinYAxisLength = minYAxisLength;
    }

    public float getYAxisTextGap() {
        return mYAxisTextGap;
    }

    public void setYAxisTextGap(float yAxisTextGap) {
        this.mYAxisTextGap = yAxisTextGap;
    }

    public ColumnData getColumnData() {
        return mData;
    }

    public void setColumnData(ColumnData data) {
        mData = data;
        postInvalidate();
    }

    public static class ColumnData {

        private List<ColumnGroup> mData = new ArrayList<ColumnGroup>();

        public void addColumnGroup(ColumnGroup data) {
            mData.add(data);
        }

        public void removeColumnGroup(ColumnGroup group) {
            mData.remove(group);
        }

        public void setColumnGroup(ColumnGroup group, int groupIndex) {
            if (groupIndex < mData.size()) {
                mData.remove(groupIndex);
            }
            mData.add(groupIndex, group);
        }

        public ColumnGroup getColumnGroup(int groupIndex) {
            if (groupIndex < mData.size()) {
                return mData.get(groupIndex);
            }

            return null;
        }

        public int getSize() {
            return mData.size();
        }

        public int getGroupSize() {
            int size = 0;
            for (ColumnGroup group : mData) {
                size = group.getUnitSize() > size ? group.getUnitSize() : size;
            }
            return size;
        }

        public int getMaxValue() {
            int max = 0;
            for (ColumnGroup group : mData) {
                max = group.getMaxValue() > max ? group.getMaxValue() : max;
            }
            return max;
        }
    }

    public static class ColumnGroup {

        private String mDescription = null;
        private int mMaxUnitSize = 0;

        private List<ColumnUnitData> mData = new ArrayList<ColumnUnitData>();

        public ColumnGroup(String description, int maxSize) {
            mDescription = description;
            mMaxUnitSize = maxSize;
        }

        public void addColumnData(ColumnUnitData data) {
            if (mData.size() < mMaxUnitSize && data != null) {
                mData.add(data);
            }
        }

        public void removeColumnData(ColumnUnitData data) {
            mData.remove(data);
        }

        public void setColumnData(ColumnUnitData data, int index) {
            if (index < mData.size()) {
                mData.remove(index);
            }
            mData.add(index, data);
        }

        public ColumnUnitData getColumnUnitData(int index) {
            if (index < mData.size()) {
                return mData.get(index);
            }

            return null;
        }

        public int getUnitSize() {
            return mData.size();
        }

        public String getDescription() {
            return mDescription;
        }

        public int getMaxValue() {
            int max = 0;
            for (ColumnUnitData data : mData) {
                max = data.getValue() > max ? data.getValue() : max;
            }
            return max;
        }
    }

    public static class ColumnUnitData {
        private String mComment;
        private int mValue;
        private int mColor;

        public ColumnUnitData(String comment, int value, int color) {
            this.mComment = comment;
            this.mValue = value;
            this.mColor = color;
        }

        public ColumnUnitData(String comment, int value) {
            this(comment, value, 0);
        }

        public String getComment() {
            return mComment;
        }

        public int getValue() {
            return mValue;
        }

        public int getColor() {
            return mColor;
        }
    }

}
