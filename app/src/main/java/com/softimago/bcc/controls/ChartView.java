package com.softimago.bcc.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.softimago.bcc.R;
import com.softimago.bcc.engine.bo.RealChartCompilation;

public class ChartView extends RelativeLayout
{

    RealChartCompilation _compilationData;

    private LinearLayout mainLayout;

    private int layoutHeight;



    private Paint paint = new Paint();

    private int axisYPadding;
    private int axisXPadding;

    // workspace
    private int wpLeft;
    private int wpTop;
    private int wpRight;
    private int wpBottom;
    private int wpWidth;
    private int wpHeight;


    private final static int LABEL_PADDING_X = 10;
    private final static int LABEL_PADDING_Y = 10;
    private final static float MARKER_AXIS_SIZE = 10;


    public ChartView(Context context)
    {
        this(context, null, 0);
    }

    public ChartView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ChartView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        setWillNotDraw(false);
        setBackgroundColor(Color.TRANSPARENT);

        _compilationData = null;

        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ChartView);

        axisYPadding = attributes.getInt(R.styleable.ChartView_axisPaddingY, 160);
        axisXPadding = attributes.getInt(R.styleable.ChartView_axisPaddingX, 80);

        layoutHeight = attributes.getDimensionPixelSize(R.styleable.ChartView_bottomLabelHeight, 0);


        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, layoutHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        mainLayout = new LinearLayout(context);
        mainLayout.setLayoutParams(layoutParams);
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Add label views
        //addView(mLeftLabelLayout);
        addView(mainLayout);

    }


    // Label adapters
    public void setChartCompilationData(RealChartCompilation compilationData)
    {
        _compilationData = compilationData;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        wpLeft = left;
        wpTop = top;
        wpRight = right;
        wpBottom = bottom;
        wpWidth = right - left;
        wpHeight = bottom - top;

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        drawAxisY(canvas);
        drawAxisX(canvas);
        drawChart(canvas);
    }

    public float calculateScrollFactor(float distance)
    {
        return distance / wpWidth;
    }

    private void drawChart(Canvas canvas)
    {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        if(_compilationData != null && _compilationData.points != null && _compilationData.points.size() > 0)
        {
            float xStart = wpLeft + axisYPadding;
            float yStart = wpBottom - axisXPadding;
            float chartWidth = (float)(wpWidth - axisYPadding);
            float chartHeight = (float)(wpHeight - axisXPadding);


            for(int i = 1; i < _compilationData.points.size(); i++)
            {
                float x1 = xStart + chartWidth * _compilationData.points.get(i-1).x;
                float x2 = xStart + chartWidth * _compilationData.points.get(i).x;

                float y1 = yStart - chartHeight * _compilationData.points.get(i-1).y;
                float y2 = yStart - chartHeight * _compilationData.points.get(i).y;

                canvas.drawLine(x1, y1, x2, y2, paint);
            }

        }


    }

    private void drawAxisY(Canvas canvas)
    {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        float x1 = wpLeft + axisYPadding;
        float x2 = x1;
        float y1 = wpTop;
        float y2 = wpBottom - axisXPadding;

        canvas.drawLine(x1, y1, x2, y2, paint);

        paint.setTextSize(40);

        if(_compilationData != null && _compilationData.averageYLabeles != null && _compilationData.averageYLabeles.size() > 0)
        {
            float deltaY = ((float)wpHeight - axisXPadding) / _compilationData.averageYLabeles.size();

            for(int i = 0; i < _compilationData.averageYLabeles.size(); i++)
            {
                canvas.drawLine(x1, y2 - deltaY * i, x1 + MARKER_AXIS_SIZE, y2 - deltaY * i, paint);

                String textToShow = _compilationData.averageYLabeles.get(i);

                Rect bounds = new Rect();
                paint.getTextBounds(textToShow, 0, textToShow.length(), bounds);
                int textWidth = bounds.width();
                int textHeight = bounds.height();
                float correctionY = 0;

                if(i == 0)
                {
                    correctionY = 0;
                }
                else
                {
                    correctionY = deltaY * i - textHeight / 2;
                }

                canvas.drawText(textToShow, x1 - textWidth - LABEL_PADDING_X, y2 - correctionY, paint);

            }
        }
    }



    private void drawAxisX(Canvas canvas)
    {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);

        float x1 = wpLeft + axisYPadding;
        float x2 = wpRight;
        float y1 = wpBottom - axisXPadding;
        float y2 = y1;

        canvas.drawLine(x1, y1, x2, y2, paint);

        paint.setTextSize(40);

        if(_compilationData != null && _compilationData.averageXLabels != null && _compilationData.averageXLabels.size() > 0)
        {
            for(int i = 0; i < _compilationData.averageXLabels.size(); i++)
            {
                float deltaX = (float)(wpWidth - axisYPadding) * _compilationData.averageXLabels.get(i).normalizedPosition;

                canvas.drawLine(x1 + deltaX, y1, x1 + deltaX, y1 - MARKER_AXIS_SIZE, paint);

                String textToShow = _compilationData.averageXLabels.get(i).valueToShow;
                Rect bounds = new Rect();
                paint.getTextBounds(textToShow, 0, textToShow.length(), bounds);
                int textWidth = bounds.width();
                int textHeight = bounds.height();
                float correctionX = 0;

                if(i == _compilationData.averageXLabels.size() - 1)
                {
                    // last mark -> move text to left
                    correctionX = textWidth + LABEL_PADDING_X;
                }
                else if(i == 0)
                {
                    // first mark -> move text to right
                    correctionX = 0;
                }
                else
                {
                    correctionX = textWidth / 2;
                }

                // here would be good to measure width of text and calculate
                canvas.drawText(textToShow, x1 + deltaX - correctionX, y1 + textHeight + LABEL_PADDING_Y, paint);
            }
        }
    }

}
