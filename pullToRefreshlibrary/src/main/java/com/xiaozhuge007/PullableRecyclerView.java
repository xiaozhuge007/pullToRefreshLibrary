package com.xiaozhuge007;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class PullableRecyclerView extends WrapRecyclerView implements Pullable
{
    public static final String TAG = PullableRecyclerView.class.getSimpleName();
    public int mFirstVisiblePosition = -1;
    public int mLastVisiblePosition = -1;
    private OnScrollUpListener mOnScrollUpListener;
    private int mTempLastVisiblePosition = -1;

    public static final int INIT = 0;
    public static final int LOADING = 1;
    private int state = INIT;
    private boolean canLoad = true;
    public View view;

    public PullableRecyclerView(Context context)
    {
        this(context, null, 0);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public PullableRecyclerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        setOnScrollListener(new OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if (null != mOnScrollUpListener)
                {
//                    Log.i(TAG, "LastVisibleItemPosition:" + getLastVisibleItemPosition());
//                    Log.i(TAG, "position:" + mTempLastVisiblePosition);
//                    Log.i(TAG, "childCount:" + getChildCount());
                    if (getAdapter().getItemCount() - 1 == getLastVisibleItemPosition())
                    {
                        return;
                    }
                    if (mTempLastVisiblePosition < getLastVisibleItemPosition()
                            || mTempLastVisiblePosition - getLastVisibleItemPosition() >= getChildCount())
                    {
                        mTempLastVisiblePosition = getLastVisibleItemPosition();
                        mOnScrollUpListener.onScrollUp(mTempLastVisiblePosition);
                    }
                }
            }
        });
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

    public void changeState(int state) {
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
        LayoutManager lm = getLayoutManager();
        mFirstVisiblePosition = getFirstVisibleItemPosition();
        mLastVisiblePosition = getLastVisibleItemPosition();
        int count = getAdapter().getItemCount();
        if (0 == count)
        {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (mLastVisiblePosition == (count - 1))
        {
            // 滑到底部了
            if (lm.findViewByPosition(count - 1).getBottom() <= getMeasuredHeight())
            {
                return true;
            }
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
    public boolean canPullDown()
    {
        LayoutManager lm = getLayoutManager();
        mFirstVisiblePosition = getFirstVisibleItemPosition();
        View view = lm.findViewByPosition(mFirstVisiblePosition);
        int count = getAdapter().getItemCount();
        if (0 == count)
        {
            // 没有item的时候也可以下拉刷新
            return true;
        } else if (null != view && view.getTop() == 0 && mFirstVisiblePosition == 0)
        {
            // 滑到ListView的顶部了
            return true;
        } else
            return false;
    }

    @Override
    public boolean canPullUp()
    {
        LayoutManager lm = getLayoutManager();
        mFirstVisiblePosition = getFirstVisibleItemPosition();
        mLastVisiblePosition = getLastVisibleItemPosition();
        int count = getAdapter().getItemCount();
        if (0 == count)
        {
            // 没有item的时候也可以上拉加载
            return true;
        } else if (mLastVisiblePosition == (count - 1))
        {
            // 滑到底部了
            if (lm.findViewByPosition(count - 1).getBottom() <= getMeasuredHeight())
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取顶部可见项的位置
     *
     * @return
     */
    public int getFirstVisibleItemPosition()
    {
        LayoutManager lm = getLayoutManager();
        int firstVisibleItemPosition = 0;
        if (lm instanceof GridLayoutManager)
        {
            firstVisibleItemPosition = ((GridLayoutManager) lm).findFirstVisibleItemPosition();
        } else if (lm instanceof LinearLayoutManager)
        {
            firstVisibleItemPosition = ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
        } else if (lm instanceof StaggeredGridLayoutManager)
        {
            int positions[] = new int[1];
            ((StaggeredGridLayoutManager) lm).findFirstVisibleItemPositions(positions);
            firstVisibleItemPosition = positions[0];
        }
        return firstVisibleItemPosition;
    }

    /**
     * 获取底部可见项的位置
     *
     * @return
     */
    public int getLastVisibleItemPosition()
    {
        LayoutManager lm = getLayoutManager();
        int lastVisibleItemPosition = 0;
        if (lm instanceof GridLayoutManager)
        {
            lastVisibleItemPosition = ((GridLayoutManager) lm).findLastVisibleItemPosition();
        } else if (lm instanceof LinearLayoutManager)
        {
            lastVisibleItemPosition = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
        } else if (lm instanceof StaggeredGridLayoutManager)
        {
            int positions[] = new int[1];
            ((StaggeredGridLayoutManager) lm).findLastVisibleItemPositions(positions);
            lastVisibleItemPosition = positions[0];
        }
        return lastVisibleItemPosition;
    }

    public int getFirstVisiblePosition()
    {
        return mFirstVisiblePosition;
    }

    public int getLastVisiblePosition()
    {
        return mLastVisiblePosition;
    }

    public void setOnScrollUpListener(OnScrollUpListener listener)
    {
        mOnScrollUpListener = listener;
    }

    /**
     * 向上滚动监听
     */
    public interface OnScrollUpListener
    {
        void onScrollUp(int position);
    }

    private OnLoadListener mOnLoadListener;

    public void setOnLoadListener(OnLoadListener listener) {
        this.mOnLoadListener = listener;
    }


    public interface OnLoadListener {
        void onLoad(PullableRecyclerView pullableRecyclerView);
    }
}
