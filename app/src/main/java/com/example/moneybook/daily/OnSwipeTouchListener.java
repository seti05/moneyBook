package com.example.moneybook.daily;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    GestureListener listener;

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        listener = new GestureListener();
    }

    public void onSwipeLeft() {
    }

    public void onSwipeRight() {
    }

    boolean isMoving = false;

    public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_CANCEL:
                    showfabAction();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float eventNum = event.getOrientation();
                    float xNum = event.getX();
                    float yNum = event.getY();
                    hidefabAction();
                    // implement your move codes
                    break;

                case MotionEvent.ACTION_UP:
                    showfabOnScroll();
                    isMoving=false;
                    break;

                default:
                    break;
            }
        return gestureDetector.onTouchEvent(event);
    }



    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onConfirmDelete();
            super.onLongPress(e);
        }



        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onClick();
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isMoving=true;
            showfabOnScroll();


            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
                if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (distanceX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                        return true;
                    }
                }

            return false;
        }
    }

    public void showfabOnScroll() {
    }


    public void hidefabAction() {
    }

    public void showfabAction() {
    }
    public void onClick() {
    }

    public void onConfirmDelete() {
    }
}