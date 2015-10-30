package com.hzpd.custorm;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.Timer;
import java.util.TimerTask;

public class TopPicViewPager extends ViewPager {
    private float mX;
    private float mY;


    public TopPicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TopPicViewPager(Context context) {
        super(context);
        init();
    }

    public void init() {
        addOnPageChangeListener(pagerChangeListener);
    }


    private OnPageChangeListener pagerChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_IDLE:
                    startSwitchPager();
                    break;
                case ViewPager.SCROLL_STATE_SETTLING:
                case ViewPager.SCROLL_STATE_DRAGGING:
                    stopSwitchPager();
                    break;
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mX = ev.getX();
                mY = ev.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (Math.abs(ev.getX() - mX) > Math.abs(ev.getY() - mY)) {
                    if (ev.getX() - mX > 10) {//left-->right
                        int c = getCurrentItem();
                        if (0 == c) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    } else if (ev.getX() - mX < -10) {
                        int c = getCurrentItem();
                        int total = getAdapter().getCount();
                        if (total == c + 1) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
            break;
        }
        if (this.getChildCount() < 1) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }


    private Timer timer = null;
    private PageSwitchTask adSwitchTask = null;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startSwitchPager();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopSwitchPager();
    }

    private class PageSwitchTask extends TimerTask {

        @Override
        public void run() {
            post(new Runnable() {
                @Override
                public void run() {
                    if (getAdapter() != null) {
                        int count = getAdapter().getCount();
                        int newItem = (getCurrentItem() + 1) % count;
                        setCurrentItem(newItem);
                    }
                }
            });

        }
    }

    private static final int SWITCH_PERIOD = 4000;

    private void startSwitchPager() {
        if (getAdapter() != null && getAdapter().getCount() > 1) {
            if (adSwitchTask == null || timer == null) {
                adSwitchTask = new PageSwitchTask();
                timer = new Timer();
                timer.schedule(adSwitchTask, SWITCH_PERIOD, SWITCH_PERIOD);
            }
        }
    }

    private void stopSwitchPager() {
        if (adSwitchTask != null) {
            adSwitchTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = null;
        adSwitchTask = null;
    }

}