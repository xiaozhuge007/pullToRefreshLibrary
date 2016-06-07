package com.xiaozhuge007;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 解决嵌套viewPager滑动冲突的问题
 */
public class PullableScrollView extends ScrollView implements Pullable {
    public static final int INIT = 0;
    public static final int LOADING = 1;
    private int state = INIT;
    private boolean canLoad = true;
    private GestureDetector mGestureDetector;
    private int Scroll_height = 0;
    private int view_height = 0;
    protected Field scrollView_mScroller;
    private static final String TAG = "CustomScrollView";
    boolean ret;
    boolean ret2;

    public PullableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            stopAnim();
        }

        ret = super.onInterceptTouchEvent(ev);
        ret2 = mGestureDetector.onTouchEvent(ev);
        return ret && ret2;
    }

    // Return false if we're scrolling in the x direction
    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceY) > Math.abs(distanceX)) {
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 按下的时候禁止自动加载
                canLoad = false;
                break;
            case MotionEvent.ACTION_UP:
                // 松开手判断是否自动加载
                canLoad = true;
                checkLoad();
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        boolean stop = false;
        if (Scroll_height - view_height == t) {
            stop = true;
        }

        if (t == 0 || stop == true) {
            try {
                if (scrollView_mScroller == null) {
                    scrollView_mScroller = getDeclaredField(this, "mScroller");
                }

                Object ob = scrollView_mScroller.get(this);
                if (ob == null || !(ob instanceof Scroller)) {
                    return;
                }
                Scroller sc = (Scroller) ob;
                sc.abortAnimation();

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        super.onScrollChanged(l, t, oldl, oldt);
        checkLoad();
    }

    private void stopAnim() {
        try {
            if (scrollView_mScroller == null) {
                scrollView_mScroller = getDeclaredField(this, "mScroller");
            }

            Object ob = scrollView_mScroller.get(this);
            if (ob == null) {
                return;
            }
            Method method = ob.getClass().getMethod("abortAnimation");
            method.invoke(ob);
        } catch (Exception ex) {
        }
    }

    @Override
    protected int computeVerticalScrollRange() {
        Scroll_height = super.computeVerticalScrollRange();
        return Scroll_height;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed == true) {
            view_height = b - t;
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (focused != null && focused instanceof WebView) {
            return;
        }
        super.requestChildFocus(child, focused);
    }

    /**
     * 获取一个对象隐藏的属性，并设置属性为public属性允许直接访问
     *
     * @return {@link Field} 如果无法读取，返回null；返回的Field需要使用者自己缓存，本方法不做缓存�?
     */
    public static Field getDeclaredField(Object object, String field_name) {
        Class<?> cla = object.getClass();
        Field field = null;
        for (; cla != Object.class; cla = cla.getSuperclass()) {
            try {
                field = cla.getDeclaredField(field_name);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {

            }
        }
        return null;
    }

    /**
     * 判断是否满足自动加载条件
     */
    private void checkLoad() {
        if (reachBottom() && mOnLoadListener != null && state != LOADING
                && canLoad) {
            mOnLoadListener.onLoad(this);
            changeState(LOADING);
        }
    }

    /**
     * 完成加载
     */
    public void finishLoading() {
        changeState(INIT);
    }

    private boolean reachBottom() {
        if (getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight())) {
            return true;
        } else {
            return false;
        }
    }

    private void changeState(int state) {
        this.state = state;
        switch (state) {
            case INIT:
                canLoad = true;
                break;

            case LOADING:
                canLoad = false;
                break;
        }
    }

    @Override
    public boolean canPullDown() {
        if (getScrollY() == 0 && ret && ret2) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canPullUp() {
        if (getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight())) {
            return true;
        } else {
            return false;
        }
    }

    private OnLoadListener mOnLoadListener;

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }

    public interface OnLoadListener {
        void onLoad(PullableScrollView pullableScrollView);
    }
}
