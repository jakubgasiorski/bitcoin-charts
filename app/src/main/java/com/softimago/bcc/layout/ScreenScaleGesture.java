package com.softimago.bcc.layout;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


public class ScreenScaleGesture implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener
{

    private View view;
    private ScaleGestureDetector gestureScale;
    private final GestureDetector gestureDetector;
    private float scaleFactor = 1;

    private OnScaleListener _listener;

    public interface OnScaleListener
    {
        void onZoom(float scaleFactor);
        void onSwipe(float distance);
    }

    public ScreenScaleGesture(Context context, OnScaleListener listener)
    {
        gestureScale = new ScaleGestureDetector(context, this);

        gestureDetector = new GestureDetector(context, new GestureListener());

        _listener = listener;

    }

    @Override
    public boolean onTouch(View view, MotionEvent event)
    {
        this.view = view;
        gestureScale.onTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        float gotScaleFactor = detector.getScaleFactor();
        if(gotScaleFactor != 0)
        {
            scaleFactor *= 1.0f/detector.getScaleFactor();
            scaleFactor = (scaleFactor > 1 ? 1 : scaleFactor);
        }
        _listener.onZoom(scaleFactor);

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector)
    {
        _listener.onZoom(scaleFactor);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e)
        {

            return true;
        }

        // do animation here if necessary

//        @Override
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
//        {
//            float distanceX = e2.getX() - e1.getX();
//            _listener.onSwipe(distanceX);
//            return true;
//        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
        {
            //float distanceX = e2.getX() - e1.getX();
            _listener.onSwipe(distanceX);
            return true;
        }

    }
}
