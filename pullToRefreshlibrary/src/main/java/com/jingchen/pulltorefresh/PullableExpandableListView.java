package com.jingchen.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ExpandableListView;

public class PullableExpandableListView extends ExpandableListView implements Pullable {
    public static final int INIT = 0;
    public static final int LOADING = 1;
    private int state = INIT;
    private boolean canLoad = true;
    public View view;

    public PullableExpandableListView(Context context) {
        super(context);
    }

    public PullableExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PullableExpandableListView(Context context, AttributeSet attrs,
                                      int defStyle) {
        super(context, attrs, defStyle);
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
            case MotionEvent.ACTION_MOVE:
                canLoad = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        // 在滚动中判断是否满足自动加载条件
        checkLoad();
    }


    /**
     * 判断是否满足自动加载条件
     */
    private void checkLoad() {
        if (reachBottom() && mOnLoadListener != null && state != LOADING
                && canLoad) {
            mOnLoadListener.onLoad(this);
            changeState(LOADING);
            canLoad = false;
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

    /**
     * @return footerview可见时返回true，否则返回false
     */
    public boolean reachBottom() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getTop() < getMeasuredHeight())
                return true;
        }
        return false;
    }

    /**
     * 完成加载
     */
    public void finishLoading() {
        changeState(INIT);
        canLoad = false;
//        view.setVisibility(GONE);
    }


    @Override
    public boolean canPullDown() {
        if (getCount() == 0) {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (getFirstVisiblePosition() == 0
                && getChildAt(0).getTop() >= 0) {
            // 滑到顶部了
            return true;
        } else
            return false;
    }

    @Override
    public boolean canPullUp() {
        if (getCount() == 0) {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (getLastVisiblePosition() == (getCount() - 1)) {
            // 滑到底部了
            if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
                    && getChildAt(
                    getLastVisiblePosition()
                            - getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
                return true;
        }
        return false;
    }

    private OnLoadListener mOnLoadListener;

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }


    public interface OnLoadListener {
        void onLoad(PullableExpandableListView listView);
    }
}
