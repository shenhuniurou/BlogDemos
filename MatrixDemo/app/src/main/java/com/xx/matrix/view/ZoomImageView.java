package com.xx.matrix.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by wxx on 2016/11/3.
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener,
                    ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener{

    /**
     * 是否已经初始化过
     */
    private boolean mInited = false;

    /**
     * 初始化时的缩放比例
     */
    private float mInitScale;

    /**
     * 双击时的缩放比例
     */
    private float mDoubleScale;

    /**
     * 最大能达到的缩放比例
     */
    private float mMaxScale;

    private Matrix mMatrix;

    /**
     * 监测缩放手势
     */
    private ScaleGestureDetector mScaleGestureDetector;

    /**
     * 记录上一次多点触控的数量
     */
    private int mLastPointerCount;

    /**
     * 上一次的中心点位置
     */
    private float mLastX;
    private float mLastY;

    private int mTouchSlop;
    /**
     * 是否可以移动图片
     */
    private boolean isCanDrag = false;

    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    //双击放大缩小的手势检测
    private GestureDetector mGestureDetector;

    /**
     * 是否在自动缩放
     */
    private boolean isAutoScale;


    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale) return true;
                float x = e.getX();
                float y = e.getY();
                if (getScale() < mDoubleScale) {
//                    mMatrix.postScale(mDoubleScale / getScale(), mDoubleScale / getScale(), x, y);
//                    setImageMatrix(mMatrix);
                    postDelayed(new AutoScaleRunnable(mDoubleScale, x, y), 16);
                    isAutoScale = false;
                } else {
//                    mMatrix.postScale(mInitScale / getScale(), mInitScale / getScale(), x, y);
//                    setImageMatrix(mMatrix);
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
                    isAutoScale = false;
                }
                return true;
            }
        });
    }

    /**
     * 自动放大缩小
     */
    private class AutoScaleRunnable implements Runnable {
        // 缩放的目标值
        private  float mTargetScale;
        private float x;
        private float y;

        private final float BIGGER = 1.05f;
        private final float SMALLER = 0.95f;

        private float tmpScale;

        public AutoScaleRunnable(float mTargetScale, float x, float y) {
            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;

            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            }
            if (getScale() > mTargetScale) {
                tmpScale = SMALLER;
            }
        }

        @Override
        public void run() {
            //进行缩放
            mMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(mMatrix);

            float currentScale = getScale();
            if (tmpScale > 1.0f && currentScale < mTargetScale
                    || tmpScale < 1.0f && currentScale > mTargetScale) {
                postDelayed(this, 16);
            } else {
                //设置为目标值
                float scale = mTargetScale / currentScale;
                mMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mMatrix);

                isAutoScale = false;
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        //在改方法中捕获图片完全加载的状态
        if (!mInited){
            //获取控件的宽高
            int width = getWidth();
            int height = getHeight();

            //获取图片和图片的宽高
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }

            int drawableW = drawable.getIntrinsicWidth();
            int drawableH = drawable.getIntrinsicHeight();

            //计算缩放比例
            float scale = 1.0f;

            /**
             * 图片宽度大于控件宽度且图片高度小于控件高度，缩小
             */
            if (drawableW > width && drawableH < height) {
                scale = width * 1.0f / drawableW;
            }

            /**
             * 图片高度大于控件宽度且图片宽度小于控件宽度，缩小
             */
            if (drawableH > height && drawableW < width) {
                scale = height * 1.0f / drawableH;
            }

            /**
             * 图片宽高大于控件宽高或者图片宽高小于控件宽高
             */
            if ((drawableW > width && drawableH > height) || (drawableW < width && drawableH < height)) {
                scale = Math.min(width * 1.0f / drawableW, height * 1.0f / drawableH);
            }

            mInitScale = scale;
            mDoubleScale = mInitScale * 2;
            mMaxScale = mInitScale * 4;

            //将图片移动至控件中心
            int dx = getWidth() / 2 - drawableW / 2;
            int dy = getHeight() / 2 - drawableH / 2;

            mMatrix = new Matrix();
            // 先平移
            mMatrix.postTranslate(dx,dy);
            //再缩放
            mMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mMatrix);

            mInited = true;

        }

    }

    /**
     * 获取当前图片的缩放值
     * @return
     */
    private float getScale() {
        float values[] = new float[9];
        mMatrix.getValues(values);
        return  values[Matrix.MSCALE_X];
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        //获取缩放比例
        float scaleFactor = detector.getScaleFactor();

        float scale = getScale();

        if (getDrawable() == null) return true;

        //缩放比例区间控制
        //条件表示缩放比例还未达到最大，且还在继续放大||缩放比例还未达到最小且还在继续缩小
        if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f)) {
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            }
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }
            //缩放的中心点在多指触控的中心点
            mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());

            checkBorderAndCenterWhenScale();

            setImageMatrix(mMatrix);
        }

        return true;
    }

    /**
     * 缩放时检查图片的边界和中心点
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if (rectF.width() >= width) {
            //如果图片与左边有空白
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }
            //如果图片与右边有空白
            if (rectF.right < width) {
                deltaX = width - rectF.right;
            }
        }

        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                deltaY = - rectF.top;
            }
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }

        //如果图片的宽或者高小于控件的宽或高，则让其居中
        if (rectF.width() < width) {
            deltaX = width / 2 - rectF.right + rectF.width() / 2;
        }

        if (rectF.height() < height) {
            deltaY = height / 2 - rectF.bottom + rectF.height() / 2;
        }
        mMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获得图片放大缩小后的宽高以及left、top、right和bottom
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mMatrix;
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        //将触摸事件交给mScaleGestureDetector去处理
        mScaleGestureDetector.onTouchEvent(event);

        //多点触控的中心点
        float x = 0;
        float y = 0;

        //多点触控的数量
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        //计算中心点位置
        x /= pointerCount;
        y /= pointerCount;

        if (mLastPointerCount != pointerCount) {
            mLastX = x;
            mLastY = y;
        }

        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.width() > getWidth() + 0.01 || rectF.height() > getHeight() + 0.01) {
                    if (rectF.right == getWidth() || rectF.left == 0) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() + 0.01 || rectF.height() > getHeight() + 0.01) {
                    if (rectF.right == getWidth() || rectF.left == 0) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!isCanDrag) {
                    //判断是否在移动
                    isCanDrag = isMoveAction(dx, dy);
                }

                if (isCanDrag) {
                    if (getDrawable() != null) {
                        isCheckLeftAndRight = isCheckTopAndBottom = true;
                        //图片宽度小于控件宽度时不能移动了
                        if (rectF.width() < getWidth()) {
                            isCheckLeftAndRight = false;
                            dx = 0;
                        }
                        //图片高度小于控件高度时不能移动了
                        if (rectF.height() < getHeight()) {
                            isCheckTopAndBottom = false;
                            dy = 0;
                        }
                        mMatrix.postTranslate(dx, dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;
        }

        return true;
    }

    /**
     * 当移动图片时进行边界控制
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();

        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if (rectF.top > 0 && isCheckTopAndBottom) {
            deltaY = -rectF.top;
        }
        if (rectF.bottom < height && isCheckTopAndBottom) {
            deltaY = height - rectF.bottom;
        }

        if (rectF.left > 0 && isCheckLeftAndRight) {
            deltaX = -rectF.left;
        }
        if (rectF.right < width && isCheckLeftAndRight) {
            deltaX = width - rectF.right;
        }
        mMatrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 判断是否足以触发 MOVE
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //view加到窗口时开始监听
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //view从窗口移除时停止监听
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

}
