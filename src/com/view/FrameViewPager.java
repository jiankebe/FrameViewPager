/**
 * jianchuanli 2015/9/10/ 16:33
 */
package com.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class FrameViewPager extends ViewGroup {
    private final static int STATUS_SCROLLING_NEXT_PAGE = 1;
    private final static int STATUS_SCROLLING_BACK_PAGE = 2;
    private final static int STATUS_SCROLLING_NEXT_PAGE_END = 3;
    private final static int STATUS_SCROLLING_BACK_PAGE_END = 4;
    private final static int STATUS_SCROLLINT_ANIMATION = 5;
    private final static int STATUS_DONE = 0;
    private int state = STATUS_DONE;
    private FrameViewPager myViewPager = this;
    private int mTouchSlop;
    private int downX;
    private int downY;
    private int tempX;
    private int viewWidth;
    private int viewHeight;
    private boolean isSilding;
    private float mScrollPercent;
    private float mScrimOpacity;
    private int curX;
    private int nextScaleX;
    private int nextScaleY;
    private float nextScalePercent;
    private int nextScaleStartX;
    private int nextScaleStartY;
    private int nextScaleEndX;
    private int nextScaleEndY;
    private int backScaleStartX;
    private int backScaleStartY;
    private int backScaleX;
    private int backScaleY;
    private int mCurItem;
    private int count;
    private FramePagerAdapter mAdapter;
    private Map<Integer, ItemInfo> mItemsMap = new HashMap<Integer, ItemInfo>();

    private class ItemInfo {
        int position;
        Object object;
        int gravity;
    }

    public FrameViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mCurItem = 0;
    }

    public FrameViewPager(Context context) {
        super(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mCurItem = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        // populate(mCurItem);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        mHeight = heightSpecSize;
        mWidth = widthSpecSize;

        measureChildren(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
        setMeasuredDimension(mWidth, mHeight);

    }

    void removeAllCache() {
        Iterator<Entry<Integer, ItemInfo>> iter = mItemsMap.entrySet().iterator();
        ItemInfo ii = null;
        while (iter.hasNext()) {
            Map.Entry<Integer, ItemInfo> entry = (Map.Entry<Integer, ItemInfo>) iter.next();
            Object val = entry.getValue();
            ii = (ItemInfo) val;
            if (ii != null) {
                iter.remove();
                mAdapter.destroyItem(this, ii.position, ii.object);
            }
        }
    }

    public void setAdapter(FramePagerAdapter adapter) {
        if (mAdapter != null) {
            removeAllCache();
            mItemsMap.clear();
            mCurItem = 0;
        }
        mAdapter = adapter;
        setCurrentItemInternal(0);
    }

    public void setCurrentItem(int item) {
        if (item == mCurItem) {
            return;
        }
        setCurrentItemInternal(item, true);
    }

    public int getCurrentItem() {
        return mCurItem;
    }

    private void changeView() {
        final int N = mAdapter.getCount();
        final int prePos = Math.max(0, mCurItem - 1);
        final int endPos = Math.min(N - 1, mCurItem + 1);

        switch (state) {
            case STATUS_SCROLLING_BACK_PAGE:
                if (Math.abs(curX) <= viewWidth / 2) {
                    setCurItem(prePos);
                } else {
                    setBackItem(prePos, endPos);
                }
                break;
            case STATUS_SCROLLING_NEXT_PAGE:
                if (Math.abs(curX) <= viewWidth / 2) {
                    setCurItem(endPos);
                } else {
                    setNextItem(endPos);
                }
                break;
            case STATUS_SCROLLING_NEXT_PAGE_END:
                scrollNormal();
                break;
            case STATUS_SCROLLING_BACK_PAGE_END:
                setCurItem(prePos);
                break;
            default:
                break;

        }

    }

    void setCurrentItemInternal(int item, boolean animation) {
        if(state != STATUS_DONE) {
            return;
        }
        if (mAdapter == null && mAdapter.getCount() < 0) {
            return;
        }
        if (item <= 0) {
            item = 0;
        } else if (item >= mAdapter.getCount()) {
            item = mAdapter.getCount() - 1;
        }
        populate(item, animation);
    }

    void setCurrentItemInternal(int item) {
        if (mAdapter == null && mAdapter.getCount() < 0) {
            return;
        }
        if (item < 0) {
            item = 0;
        } else if (item > mAdapter.getCount()) {
            item = mAdapter.getCount() - 1;
        }
        populate(item);
    }

    void populate(int newCurrentItem, boolean animation) {

        // populate(newCurrentItem);
        boolean isRemove = false;
        int preItem = mCurItem;
        boolean next = false;
        ItemInfo ii = null;
        ItemInfo cur = mItemsMap.get(newCurrentItem);
        ItemInfo pre = mItemsMap.get(preItem);
        mCurItem = newCurrentItem;
        final int N = mAdapter.getCount();
        count = N;
        {
            if (preItem < newCurrentItem) {
                next = true;
            } else if (preItem > newCurrentItem) {
                next = false;
            }
            
            final int prePos = Math.max(0, mCurItem - 1);
            final int endPos = Math.min(N - 1, mCurItem + 1);
            if (cur == null) {
                cur = addNewItem(newCurrentItem, next);
            }
            
            if (mCurItem != prePos) {
                ii = mItemsMap.get(prePos);
                if (ii == null) {
                    ii = addNewItem(prePos);
                }
                if (next) {
                    LayoutParams params = (LayoutParams) ((View) ii.object).getLayoutParams();
                    params.rightMargin = viewWidth;
                }
            }
            
            if (mCurItem != endPos) {
                ii = mItemsMap.get(endPos);
                if (ii == null) {
                    ii = addNewItem(endPos);
                }
            }
            
            /**回收不需要的view**/
            Iterator<Entry<Integer, ItemInfo>> iter = mItemsMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, ItemInfo> entry = (Map.Entry<Integer, ItemInfo>) iter.next();
                Object val = entry.getValue();
                ii = (ItemInfo) val;
                if (ii != null) {
                    if (Math.abs(cur.gravity - ii.gravity) >= 2) {

                        if ((ii.gravity != pre.gravity)) {
                            iter.remove();
                            mAdapter.destroyItem(this, ii.position, ii.object);
                        } else {
                            isRemove = true;
                        }

                    }

                }
            }
            
            if (animation) {
                if (mCurItem < preItem) {
                    animationBackItem(mCurItem, endPos, preItem, isRemove);
                } else if (mCurItem > preItem) {
                    animationNextItem(preItem, mCurItem, endPos, isRemove);
                } else {

                }

            }

        }

    }

    void animationBackItem(final int prePos, final int endPos, final int curPos, final boolean isRemove) {
        state = STATUS_SCROLLINT_ANIMATION;
        final View curView = ((View) mItemsMap.get(curPos).object);
        final View endView = ((View) mItemsMap.get(endPos).object);
        final View backView = ((View) mItemsMap.get(prePos).object);
        Animation curAnimation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // TODO Auto-generated method stub
                int x = curX - (int) ((viewWidth + curX) * interpolatedTime);
                backView.layout(-viewWidth - x, 0, Math.abs(x), viewHeight);
                int scaleX = backScaleX + (int) ((backScaleStartX - backScaleX) * interpolatedTime);
                int scaleY = backScaleY + (int) ((backScaleStartY - backScaleY) * interpolatedTime);
                curView.layout(scaleX, scaleY, viewWidth - scaleX, viewHeight - scaleY);
                endView.layout(scaleX, scaleY, viewWidth - scaleX, viewHeight - scaleY);
                if (interpolatedTime == 1) {
                    curX = 0;
                }
            }
        };
        curAnimation.setDuration(150);
        curView.startAnimation(curAnimation);
        curAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                backScaleX = 0;
                backScaleY = 0;
                state = STATUS_DONE;
                setCurrentItemInternal(prePos);

            }
        });
    }

    void animationNextItem(final int prePos, final int curPos, final int endPos, final boolean isRemove) {

        state = STATUS_SCROLLINT_ANIMATION;
        final View preView = ((View) mItemsMap.get(prePos).object);
        final View curView = ((View) mItemsMap.get(curPos).object);
        final View nextView = ((View) mItemsMap.get(endPos).object);
        Animation curAnimation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // TODO Auto-generated method stub
                int x = curX + (int) ((viewWidth - curX) * interpolatedTime);
                preView.layout(-x, 0, viewWidth - x, viewHeight);
                int scaleX = nextScaleX + (int) ((nextScaleStartX - nextScaleX) * interpolatedTime);
                int scaleY = nextScaleY + (int) ((nextScaleStartY - nextScaleY) * interpolatedTime);
                curView.layout(nextScaleStartX - scaleX, nextScaleStartY
                        - scaleY, nextScaleEndX
                        + scaleX, nextScaleEndY
                        + scaleY);
                nextView.layout(nextScaleStartX - scaleX, nextScaleStartY
                        - scaleY, nextScaleEndX
                        + scaleX, nextScaleEndY
                        + scaleY);
                if (interpolatedTime == 1) {
                    curX = 0;
                    // curView.layout(0, 0, viewWidth, viewHeight);
                    // nextView.layout(0, 0, viewWidth, viewHeight);

                }
            }
        };
        curAnimation.setDuration(150);
        curView.startAnimation(curAnimation);
        curAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                curView.layout(0, 0, viewWidth, viewHeight);
                nextView.layout(0, 0, viewWidth, viewHeight);
                state = STATUS_DONE;
                setCurrentItemInternal(curPos);
            }
        });

    }

    void populate(int newCurrentItem) {
        ItemInfo ii = null;
        ItemInfo cur = mItemsMap.get(newCurrentItem);
        mCurItem = newCurrentItem;
        final int N = mAdapter.getCount();
        count = N;
        {
            if (cur == null) {
                cur = addNewItem(newCurrentItem);
            }
            final int prePos = Math.max(0, mCurItem - 1);
            final int endPos = Math.min(N - 1, mCurItem + 1);
            if (mCurItem != prePos) {
                ii = mItemsMap.get(prePos);
                if (ii == null) {
                    ii = addNewItem(prePos);
                }
            }

            if (mCurItem != endPos) {
                ii = mItemsMap.get(endPos);
                if (ii == null) {
                    ii = addNewItem(endPos);
                }
            }
            Iterator<Entry<Integer, ItemInfo>> iter = mItemsMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, ItemInfo> entry = (Map.Entry<Integer, ItemInfo>) iter.next();
                Object val = entry.getValue();
                ii = (ItemInfo) val;
                if (ii != null) {
                    if (Math.abs(cur.gravity - ii.gravity) >= 2) {
                        iter.remove();
                        mAdapter.destroyItem(this, ii.position, ii.object);

                    }

                }
            }

        }
    }

    ItemInfo addNewItem(int position, boolean next) {
        ItemInfo ii = new ItemInfo();

        ii.position = position;
        ii.gravity = position;
        if (mCurItem < position) {
            ii.object = mAdapter.instantiateItem(this, position, true);
        } else if (mCurItem > position) {
            ii.object = mAdapter.instantiateItem(this, position, false);
        } else {
            ii.object = mAdapter.instantiateItem(this, position, next);

        }

        if (!mItemsMap.containsKey(position) || mItemsMap.get(position) == null) {
            mItemsMap.put(position, ii);
        }
        return ii;
    }

    ItemInfo addNewItem(int position) {
        ItemInfo ii = new ItemInfo();
        ii.position = position;
        ii.gravity = position;
        if (mCurItem < position) {
            ii.object = mAdapter.instantiateItem(this, position, true);
        } else if (mCurItem > position) {
            ii.object = mAdapter.instantiateItem(this, position, false);
            LayoutParams params = (LayoutParams) ((View) ii.object).getLayoutParams();
            params.rightMargin = viewWidth;
        } else {
            ii.object = mAdapter.instantiateItem(this, position, true);
        }

        if (!mItemsMap.containsKey(position) || mItemsMap.get(position) == null) {
            mItemsMap.put(position, ii);
        }
        return ii;
    }

    void addNewView(View child, int index, RelativeLayout.LayoutParams params) {
        addViewInLayout(child, index, params, true);
    }

    public FrameViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = tempX = (int) ev.getX();
                downY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                if (Math.abs(moveX - downX) > mTouchSlop
                        && Math.abs((int) ev.getRawY() - downY) < mTouchSlop) {
                    tempX = moveX;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (state == STATUS_SCROLLINT_ANIMATION) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getX();
                int deltaX = tempX - moveX;
                tempX = moveX;
                isSilding = true;
                if (isSilding) {
                    curX += deltaX;
                    final int prePos = Math.max(0, mCurItem - 1);
                    final int endPos = Math.min(count - 1, mCurItem + 1);
                    final View curView = ((View) mItemsMap.get(mCurItem).object);
                    if (curView == null) {
                        return super.onTouchEvent(event);
                    }
                    if (curX >= 0) { // 向左，下一页
                        if (endPos != mCurItem) { // 不在最后一个
                            final View nextView = ((View) mItemsMap.get(endPos).object);
                            if (nextView == null) {
                                return super.onTouchEvent(event);
                            }
                            if (state == STATUS_SCROLLING_BACK_PAGE) {
                                if (prePos != mCurItem) {
                                    final View backView = ((View) mItemsMap.get(prePos).object);
                                    if (backView == null) {
                                        return super.onTouchEvent(event);
                                    }
                                    backView.layout(-viewWidth - curX, 0, 0, viewHeight);
                                }
                            }
                            state = STATUS_SCROLLING_NEXT_PAGE;
                            curView.layout(-curX, 0, viewWidth - curX, viewHeight);
                            enlargeView(curX);
                            nextScaleX = (int) (mScrollPercent * nextScaleStartX);
                            nextScaleY = (int) (mScrollPercent * nextScaleStartY);
                            nextView.layout((nextScaleStartX - nextScaleX), (nextScaleStartY
                                    - nextScaleY), (nextScaleEndX
                                    + nextScaleX), (nextScaleEndY
                                    + nextScaleY));
                        } else if (endPos == mCurItem) {
                            if (state == STATUS_SCROLLING_BACK_PAGE) {
                                if (prePos != mCurItem) {
                                    final View backView = ((View) mItemsMap.get(prePos).object);
                                    if (backView == null) {
                                        return super.onTouchEvent(event);
                                    }
                                    backView.layout(-viewWidth - curX, 0, 0, viewHeight);
                                }
                            }
                            state = STATUS_SCROLLING_NEXT_PAGE_END;
                            // curX += (int)(curX * 0.35f);
                            if (Math.abs(curX) > viewWidth / 2) {
                                curX = viewWidth / 2;
                            }

                            curView.layout(-curX, 0, viewWidth - curX, viewHeight);
                        }

                    } else if (curX <= 0) { // 向右，下一页
                        if (prePos != mCurItem) { // 不在第一个
                            final View backView = ((View) mItemsMap.get(prePos).object);
                            final View nextView = ((View) mItemsMap.get(endPos).object);
                            if (backView == null || nextView == null) {
                                return super.onTouchEvent(event);
                            }
                            state = STATUS_SCROLLING_BACK_PAGE;
                            backView.layout(-viewWidth - curX, 0, Math.abs(curX), viewHeight);

                            narrowView(curX);
                            backScaleX = (int) (mScrollPercent * viewWidth);
                            backScaleY = (int) (mScrollPercent * viewHeight);
                            curView.layout(backScaleX, backScaleY, viewWidth
                                    - backScaleX, viewHeight
                                    - backScaleY);
                            nextView.layout(backScaleX, backScaleY, viewWidth
                                    - backScaleX, viewHeight
                                    - backScaleY);

                        } else if (prePos == mCurItem) {
                            final View nextView = ((View) mItemsMap.get(endPos).object);
                            if (nextView == null) {
                                return super.onTouchEvent(event);
                            }
                            state = STATUS_SCROLLING_BACK_PAGE_END;
                            narrowView(curX);
                            backScaleX = (int) (mScrollPercent * viewWidth);
                            backScaleY = (int) (mScrollPercent * viewHeight);
                            curView.layout(backScaleX, backScaleY, viewWidth
                                    - backScaleX, viewHeight
                                    - backScaleY);
                            nextView.layout(backScaleX, backScaleY, viewWidth
                                    - backScaleX, viewHeight
                                    - backScaleY);
                        }

                    }

                }

                break;
            case MotionEvent.ACTION_UP:
                isSilding = false;
                changeView();
                break;
        }

        return true;
    }

    private void setBackItem(int prePos, int endPos) {
        state = STATUS_SCROLLINT_ANIMATION;
        final View curView = ((View) mItemsMap.get(mCurItem).object);
        final View backView = ((View) mItemsMap.get(prePos).object);
        final View nextView = ((View) mItemsMap.get(endPos).object);
        Animation curAnimation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // TODO Auto-generated method stub
                int x = curX - (int) ((viewWidth + curX) * interpolatedTime);
                backView.layout(-viewWidth - x, 0, Math.abs(x), viewHeight);
                int scaleX = backScaleX + (int) ((backScaleStartX - backScaleX) * interpolatedTime);
                int scaleY = backScaleY + (int) ((backScaleStartY - backScaleY) * interpolatedTime);
                curView.layout(scaleX, scaleY, viewWidth - scaleX, viewHeight - scaleY);
                nextView.layout(scaleX, scaleY, viewWidth - scaleX, viewHeight - scaleY);
                if (interpolatedTime == 1) {
                    curX = 0;
                }
            }
        };
        curAnimation.setDuration(150);
        curView.startAnimation(curAnimation);
        curAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                backScaleY = 0;
                backScaleX = 0;
                curX = 0;
                LayoutParams params = (LayoutParams) backView.getLayoutParams();
                params.rightMargin = 0;
                mCurItem--;
                setCurrentItemInternal(mCurItem);
                state = STATUS_DONE;
            }
        });
    }

    private void setNextItem(int endPos) {
        state = STATUS_SCROLLINT_ANIMATION;
        final View curView = ((View) mItemsMap.get(mCurItem).object);
        final View nextView = ((View) mItemsMap.get(endPos).object);
        Animation curAnimation = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // TODO Auto-generated method stub
                int x = curX + (int) ((viewWidth - curX) * interpolatedTime);
                curView.layout(-x, 0, viewWidth - x, viewHeight);
                int scaleX = nextScaleX + (int) ((nextScaleStartX - nextScaleX) * interpolatedTime);
                int scaleY = nextScaleY + (int) ((nextScaleStartY - nextScaleY) * interpolatedTime);
                nextView.layout(nextScaleStartX - scaleX, nextScaleStartY
                        - scaleY, nextScaleEndX
                        + scaleX, nextScaleEndY
                        + scaleY);
                if (interpolatedTime == 1) {
                    curX = 0;

                }
            }
        };
        curAnimation.setDuration(150);
        curView.startAnimation(curAnimation);
        curAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                curX = 0;
                LayoutParams nextParams = (LayoutParams) nextView.getLayoutParams();
                nextParams.rightMargin = 0;
                nextView.layout(0, 0, viewWidth, viewHeight);
                LayoutParams curParams = (LayoutParams) curView.getLayoutParams();
                curParams.rightMargin = viewWidth;
                mCurItem++;
                setCurrentItemInternal(mCurItem);
                state = STATUS_DONE;
            }
        });
    }

    private void scrollNormal() {
        state = STATUS_SCROLLINT_ANIMATION;
        final View curView = ((View) mItemsMap.get(mCurItem).object);
        Animation curAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                // TODO Auto-generated method stub
                int x = curX - (int) (curX * interpolatedTime);
                curView.layout(-x, 0, viewWidth - x, viewHeight);
                if (interpolatedTime == 1) {
                    curX = 0;
                    state = STATUS_DONE;
                }
            }
        };
        curAnimation.setDuration(150);
        curView.startAnimation(curAnimation);
        curAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                curX = 0;
                state = STATUS_DONE;
            }
        });
    }

    private void setCurItem(int curPos) {
        state = STATUS_SCROLLINT_ANIMATION;
        final View curView = ((View) mItemsMap.get(mCurItem).object);
        final View nextView = ((View) mItemsMap.get(curPos).object);
        if (curX >= 0) {
            Animation curAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    // TODO Auto-generated method stub
                    int x = curX - (int) (curX * interpolatedTime);
                    curView.layout(-x, 0, viewWidth - x, viewHeight);
                    int scaleX = nextScaleX - (int) (nextScaleX * interpolatedTime);
                    int scaleY = nextScaleY - (int) (nextScaleY * interpolatedTime);
                    nextView.layout(nextScaleStartX - scaleX, nextScaleStartY
                            - scaleY, nextScaleEndX
                            + scaleX, nextScaleEndY
                            + scaleY);
                    if (interpolatedTime == 1) {
                        curX = 0;
                        state = STATUS_DONE;
                    }
                }
            };
            curAnimation.setDuration(150);
            curView.startAnimation(curAnimation);
            curAnimation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    curX = 0;
                    state = STATUS_DONE;
                }
            });
        } else {
            Animation curAnimation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    // TODO Auto-generated method stub
                    int x = curX - (int) (curX * interpolatedTime);
                    nextView.layout(-viewWidth - x, 0, Math.abs(x), viewHeight);
                    int scaleX = backScaleX - (int) (backScaleX * interpolatedTime);
                    int scaleY = backScaleY - (int) (backScaleY * interpolatedTime);
                    curView.layout(scaleX, scaleY, viewWidth - scaleX, viewHeight - scaleY);
                }
            };
            curAnimation.setDuration(150);
            curView.startAnimation(curAnimation);
            curAnimation.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // TODO Auto-generated method stub
                    curX = 0;
                    curView.layout(0, 0, viewWidth, viewHeight);
                    state = STATUS_DONE;
                }
            });
        }

    }

    @Override
    public void addView(View child, int index) {
        if (!(child.getLayoutParams() instanceof LayoutParams)) {
            super.addView(child, index, new FrameViewPager.LayoutParams(FrameViewPager.LayoutParams.MATCH_PARENT,
                    FrameViewPager.LayoutParams.MATCH_PARENT));
        } else {
            super.addView(child, index);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public int rightMargin;

        public LayoutParams(Context arg0, AttributeSet arg1) {
            super(arg0, arg1);
        }

        public LayoutParams(int arg0, int arg1) {
            super(arg0, arg1);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams arg0) {
            super(arg0);
        }

    }

    private void enlargeView(float deltaX) {

        mScrollPercent = Math.abs(deltaX / (viewWidth));
    }

    private void narrowView(float deltaX) {

        mScrollPercent = Math.abs(deltaX / ((1.0f / nextScalePercent) * viewWidth));
    }

    private int mHeight;
    private int mWidth;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.measure(MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            view.layout(params.rightMargin, 0, mWidth + params.rightMargin, mHeight);
        }
        nextScalePercent = 1.0f / 10.0f;
        viewHeight = getHeight();
        viewWidth = getWidth();
        nextScaleStartX = (int) (nextScalePercent * viewWidth);
        nextScaleStartY = (int) (nextScalePercent * viewHeight);
        nextScaleEndX = (int) (viewWidth - nextScaleStartX);
        nextScaleEndY = (int) (viewHeight - nextScaleStartY);
        backScaleStartX = nextScaleStartX;
        backScaleStartY = nextScaleStartY;
    }

}
