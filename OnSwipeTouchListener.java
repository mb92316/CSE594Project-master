package com.example.android.cse594project;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

//This class is used to listen for a touch from the user.
public class OnSwipeTouchListener implements OnTouchListener {

    ListView list;
    private GestureDetector gestureDetector;
    private Context context;

    public OnSwipeTouchListener(Context ctx, Context con, ListView list) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        context = ctx;
        this.list = list;
    }

    //This class registers a touch from the user.
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    //If the user swipes right, the position and side the user swiped is sent to main's delete.
    public void onSwipeRight(int pos) {
        MainActivity main = (MainActivity) context;
        main.delete(pos, 1);
    }

    //If the user swipes left, the position and side the user swiped is sent to main's delete.
    public void onSwipeLeft(int pos) {
        MainActivity main = (MainActivity) context;
        main.delete(pos, 2);
    }

    //This class is used to listen for a gesture from the user
    private final class GestureListener extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        //This function gets the positon of the gesture.
        private int getPostion(MotionEvent motion) {
            return list.pointToPosition((int) motion.getX(), (int) motion.getY());
        }

        /*
        This function takes the start and end point of the users gesture. If the velocity of the gesture
        is fast enough and if the start and end point are above a certain distance, the onSwipeRight or
        onSwipeLeft functions are called.
         */
        @Override
        public boolean onFling(MotionEvent motion1, MotionEvent motion2, float velX, float velY) {
            float distanceX = motion2.getX() - motion1.getX();
            float distanceY = motion2.getY() - motion1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > 100 && Math.abs(velX) > 100) {
                if (distanceX > 50)
                    onSwipeRight(getPostion(motion1));
                else
                    onSwipeLeft(getPostion(motion1));
                return true;
            }
            return false;
        }

    }
}