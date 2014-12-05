package me.yugy.github.folderlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewGroupCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by yugy(me@yanghui.name) on 14/12/2.
 * A layout like a folder and you can show one of them like open a file.
 */
public class FolderLayout extends FrameLayout{

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 400;

    /**
     * If no fade color is given by default it will fade to 80% gray.
     */
    private static final int DEFAULT_FADE_COLOR = 0x99000000;

    private ViewDragHelper mDragHelper;

    private int mHandlerHeight;
    private int mMinHandlerHeight;
    private int mDuration;
    private int mClipBottom;

    /**
     * The fade color used for the panel covered by the slider. 0 = no fading.
     */
    private int mCoveredFadeColor = DEFAULT_FADE_COLOR;

    private float mInitialMotionX;
    private float mInitialMotionY;

    /**
     * Slide offset, from 0f to 1f.
     */
    private float mSlideOffset = 0f;

    public FolderLayout(Context context) {
        this(context, null);
    }

    public FolderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final float density = context.getResources().getDisplayMetrics().density;
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mDragHelper.setMinVelocity(MIN_FLING_VELOCITY * density);
        mHandlerHeight = (int) (48 * density + 0.5f);
        mMinHandlerHeight = (int) (24 * density + 0.5f);
        mDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FolderLayout);

            mHandlerHeight = a.getDimensionPixelSize(R.styleable.FolderLayout_handlerHeight, mHandlerHeight);
            mMinHandlerHeight = a.getDimensionPixelSize(R.styleable.FolderLayout_minHandlerHeight, mMinHandlerHeight);
            mCoveredFadeColor = a.getColor(R.styleable.FolderLayout_coverColor, mCoveredFadeColor);

            a.recycle();
        }

        setWillNotDraw(false);
        ViewGroupCompat.setMotionEventSplittingEnabled(this, false);
    }

    public void setHandlerHeight(int handlerHeight) {
        mHandlerHeight = handlerHeight;
        requestLayout();
    }

    public void setMinHandlerHeight(int minHandlerHeight) {
        mMinHandlerHeight = minHandlerHeight;
        requestLayout();
    }

    public void setCoveredFadeColor(int coveredFadeColor) {
        mCoveredFadeColor = coveredFadeColor;
        invalidate();
    }

    public void setCoveredFadeColorResource(@ColorRes int colorResource) {
        mCoveredFadeColor = getResources().getColor(colorResource);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
//        Log.d("FolderLayout", "onInterceptTouchEvent: " + action);

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mInitialMotionX = ev.getX();
                mInitialMotionY = ev.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float y = ev.getY();
                final float adx = Math.abs(x - mInitialMotionX);
                final float ady = Math.abs(y - mInitialMotionY);
                final int slop = mDragHelper.getTouchSlop();

                View horizontalScrollableView = findHorizontalScrollableViewAtPosition(this, (int) x, (int) y);

                if (adx < ady && ady > slop && horizontalScrollableView != null) {
                    if (canChildScrollUp(horizontalScrollableView)) {
                        mDragHelper.cancel();
                        return false;
                    }
                } else {
                    Log.d("FolderLayout", "adx: " + adx + ", ady: " + ady + ", slop: " + slop);
                }
            }
        }

        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(getLeft(), getTop(), getRight(), mClipBottom);
        if (mCoveredFadeColor != 0 && mSlideOffset > 0) {
            final int baseAlpha = (mCoveredFadeColor & 0xff000000) >>> 24;
            final int imag = (int) (baseAlpha * mSlideOffset);
            final int color = imag << 24 | (mCoveredFadeColor & 0xffffff);
            canvas.drawColor(color);
        }
        canvas.restore();
    }

    private View findHorizontalScrollableViewAtPosition(View parent, int x, int y) {
        if (parent instanceof AbsListView || parent instanceof ScrollView) {
            Rect rect = new Rect();
            parent.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                return parent;
            }
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            final int length = viewGroup.getChildCount();
            for (int i = 0; i < length; i++) {
                View child = viewGroup.getChildAt(i);
                View viewAtPosition = findHorizontalScrollableViewAtPosition(child, x, y);
                if (viewAtPosition != null) {
                    return viewAtPosition;
                }
            }
        }
        return null;
    }

    public boolean canChildScrollUp(View mTarget) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        final int parentLeft = getPaddingLeft();

        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int stackIndex = count - i - 1;

                int childLeft = parentLeft + lp.leftMargin;
                lp.shrinkTop = parentBottom - (stackIndex + 1) * mHandlerHeight;
                lp.expandTop = parentTop + lp.topMargin;
                lp.minimumTop = parentBottom - (stackIndex + 1) * mMinHandlerHeight;

                if (lp.isOpen) {
                    child.layout(childLeft, lp.expandTop, childLeft + width, lp.expandTop + height);
                } else {
                    if (hasItemExpanded()) {
                        child.layout(childLeft, lp.minimumTop, childLeft + width, lp.minimumTop + height);
                    } else {
                        child.layout(childLeft, lp.shrinkTop, childLeft + width, lp.shrinkTop + height);
                    }
                }
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Return true if there is a item expanded in this layout
     * @return true for has item expanded
     */
    public boolean hasItemExpanded() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).isOpen) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the index of expanded item, if all items are shrinked, return -1
     * @return index or -1 if not found
     */
    public int findExpandedItemIndex() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).isOpen) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Open a view based on index. Index 0 means item in the last
     * @param index index which < item count and > -1
     */
    public void expandedItem(int index) {
        if (index < 0 || index >= getChildCount() || hasItemExpanded()) {
            return;
        }
        final View releasedChild = getChildAt(index);
        final LayoutParams lp = (LayoutParams) releasedChild.getLayoutParams();
        lp.isOpen = true;
        mDragHelper.smoothSlideViewTo(releasedChild, releasedChild.getLeft(), lp.expandTop);
        int length = getChildCount();
        int expandChildIndex = indexOfChild(releasedChild);
        for (int i = 0; i < length; i++) {
            if (i != expandChildIndex) {
                View child = getChildAt(i);
                playShrinkItemAnimation(child);
            }
        }
        invalidate();
    }

    /**
     * Shrink a view based on index. Index 0 means item in the last
     * @param index index which < item count and > -1
     */
    public void shrinkItem(int index) {
        if (index < 0 || index >= getChildCount()) {
            return;
        }
        final View releasedChild = getChildAt(index);
        final LayoutParams lp = (LayoutParams) releasedChild.getLayoutParams();
        lp.isOpen = false;
        mDragHelper.smoothSlideViewTo(releasedChild, releasedChild.getLeft(), lp.shrinkTop);

        int length = getChildCount();
        int expandChildIndex = indexOfChild(releasedChild);
        for (int i = expandChildIndex + 1; i < length; i++) {
            if (i != expandChildIndex) {
                View child = getChildAt(i);
                playExpandItemAnimation(child);
            }
        }
        invalidate();
    }

    private void playShrinkItemAnimation(final View child) {
        final LayoutParams p = (LayoutParams) child.getLayoutParams();
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, p.minimumTop - child.getTop());
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(mDuration);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                child.offsetTopAndBottom(p.minimumTop - child.getTop());
                child.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        child.startAnimation(animation);
    }

    private void playExpandItemAnimation(final View child) {
        final LayoutParams p = (LayoutParams) child.getLayoutParams();
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, p.shrinkTop - child.getTop());
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setDuration(mDuration);
        animation.setFillAfter(true);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                child.offsetTopAndBottom(p.shrinkTop - child.getTop());
                child.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        child.startAnimation(animation);
    }

    /**
     * Shrink or expand a view based on its state. Index 0 means item in the last
     * @param index index which < item count and > -1
     */
    public void toggleItem(int index) {
        if (findExpandedItemIndex() == index) {
            shrinkItem(index);
        } else {
            expandedItem(index);
        }
    }

    private class DragHelperCallback extends ViewDragHelper.Callback{

        @Override
        public boolean tryCaptureView(View view, int i) {
            return !hasItemExpanded() || ((LayoutParams) view.getLayoutParams()).isOpen;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return MathUtils.clamp(top, lp.expandTop, lp.shrinkTop);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return child.getLeft();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            LayoutParams lp = (LayoutParams) changedView.getLayoutParams();
            mSlideOffset = 1f - (float) (lp.expandTop - top) / (lp.expandTop - lp.shrinkTop);
            mClipBottom = changedView.getBottom();
            invalidate();
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            LayoutParams lp = (LayoutParams) releasedChild.getLayoutParams();
            boolean oldOpenValue = lp.isOpen;
            if ( yvel < 0) {
                lp.isOpen = true;
            } else if (yvel > 0) {
                lp.isOpen = false;
            } else {
                lp.isOpen = releasedChild.getTop() < (lp.expandTop + lp.shrinkTop) / 2;
            }
            if (lp.isOpen) {
                mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), lp.expandTop);
            } else {
                mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), lp.shrinkTop);
            }

            if (oldOpenValue != lp.isOpen) { //value has been changed
                int length = getChildCount();
                int expandChildIndex = indexOfChild(releasedChild);
                for (int i = 0; i < length; i++) {
                    if (i != expandChildIndex) {
                        final View child = getChildAt(i);
                        if (lp.isOpen) {
                            playShrinkItemAnimation(child);
                        } else {
                            playExpandItemAnimation(child);
                        }
                    }
                }
            }
            invalidate();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            return lp.shrinkTop - lp.expandTop;
        }
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        boolean isOpen = false;
        int shrinkTop;
        int expandTop;
        int minimumTop;

        public LayoutParams() {
            super(MATCH_PARENT, MATCH_PARENT);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
    }

    @Override
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(@NonNull ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }


}
