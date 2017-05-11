package com.example.ngfngf.sudokulock.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.ngfngf.sudokulock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngfngf on 2017/4/4.
 */

public class LockPatternView extends View {
    private static final int POINT_SELECTED_COUNT = 5;//选中点的数量
    private Matrix mMatrix = new Matrix();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Point[][] mPoints = new Point[3][3];
    private boolean isInit, isSelect, isFinish, movingNoPoint;//movingNoPoint选中重复的点的时候
    private float width, height, offsetX, offsetY, bitmapR, movingX, movingY;//偏移量,图片圆的半径,鼠标移动的坐标
    private Bitmap pointNormal, pointPressed, pointError, linePressed, lineError;
    private List<Point> selectedPoints = new ArrayList<Point>();//被选中的点
    private OnPatterChangeListener mOnPatterChangeListener;//图案监听器

    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isInit) {
            initPoints();
        }
        //画点
        point2Canvas(canvas);
        //画线
        if (selectedPoints.size() > 0) {
            Point a = selectedPoints.get(0);
            //绘制九宫格坐标点
            for (int i = 1; i < selectedPoints.size(); i++) {
                Point b = selectedPoints.get(i);
                line2Canvas(canvas, a, b);
                a = b;
            }
            //绘制鼠标坐标点
            if (movingNoPoint) {
                line2Canvas(canvas, a, new Point(movingX, movingY));
            }
        }
    }

    //绘制点
    private void point2Canvas(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point piont = mPoints[i][j];
                if (piont.state == Point.STATE_PRESSED) {
                    canvas.drawBitmap(pointPressed, piont.x - bitmapR, piont.y - bitmapR, mPaint);
                } else if (piont.state == Point.STATE_ERROR) {
                    canvas.drawBitmap(pointError, piont.x - bitmapR, piont.y - bitmapR, mPaint);
                } else {
                    canvas.drawBitmap(pointNormal, piont.x - bitmapR, piont.y - bitmapR, mPaint);
                }
            }
        }
    }

    //绘制线
    private void line2Canvas(Canvas canvas, Point a, Point b) {
        //线的长度
        float lineLength = (float) Point.distance(a, b);
        //两点间的角度
        float degree = Point.getDegrees(a, b);
        //旋转画布
        canvas.rotate(degree, a.x, a.y);
        if (a.state == Point.STATE_PRESSED) {
            //放长图片的宽
            mMatrix.setScale(lineLength / linePressed.getWidth(), 1);
            mMatrix.postTranslate(a.x - linePressed.getWidth() / 2, a.y - linePressed.getHeight() / 2);
            canvas.drawBitmap(linePressed, mMatrix, mPaint);
        } else {
            mMatrix.setScale(lineLength / lineError.getWidth(), 1);
            mMatrix.postTranslate(a.x - lineError.getWidth() / 2, a.y - lineError.getHeight() / 2);
            canvas.drawBitmap(lineError, mMatrix, mPaint);
        }
        //绘制完之后，回复画布
        canvas.rotate(-degree, a.x, a.y);
    }

    //初始化点
    private void initPoints() {
        //获取布局的宽高
        width = getWidth();
        height = getHeight();

        //九宫格是正方形，所以宽高一致
        //横屏
        if (width > height) {
            offsetX = (width - height) / 2;
            width = height;
        } else {//竖屏
            offsetY = (height - width) / 2;
            height = width;
        }

        //加载图片资源
        pointNormal = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_normal);
        pointPressed = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_pressed);
        pointError = BitmapFactory.decodeResource(getResources(), R.drawable.bitmap_error);
        lineError = BitmapFactory.decodeResource(getResources(), R.drawable.line_error);
        linePressed = BitmapFactory.decodeResource(getResources(), R.drawable.line_pressed);
        //点的坐标
        mPoints[0][0] = new Point(offsetX + width / 4, offsetY + width / 4);
        mPoints[0][1] = new Point(offsetX + width / 2, offsetY + width / 4);
        mPoints[0][2] = new Point(offsetX + width - width / 4, offsetY + width / 4);

        mPoints[1][0] = new Point(offsetX + width / 4, offsetY + width / 2);
        mPoints[1][1] = new Point(offsetX + width / 2, offsetY + width / 2);
        mPoints[1][2] = new Point(offsetX + width - width / 4, offsetY + width / 2);

        mPoints[2][0] = new Point(offsetX + width / 4, offsetY + width - width / 4);
        mPoints[2][1] = new Point(offsetX + width / 2, offsetY + width - width / 4);
        mPoints[2][2] = new Point(offsetX + width - width / 4, offsetY + width - width / 4);

        //处理图片的半径，让画笔以圆心绘制图片
        bitmapR = pointNormal.getHeight() / 2;
        //设置密码
        int index = 1;
        for (Point[] points : this.mPoints) {
            for (Point point : points) {
                point.index = index;
                index++;
            }
        }
        //初始化完成
        isInit = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        movingNoPoint = false;
        isFinish = false;
        movingY = event.getY();
        movingX = event.getX();
        Point p = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetPoint();
                //重新绘制
                if (mOnPatterChangeListener != null) {
                    mOnPatterChangeListener.onPatterStart(true);
                }
                p = checkSelectPoint();
                //开始选中第一点
                if (p != null) {
                    isSelect = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSelect) {
                    p = checkSelectPoint();
                    if (p == null) {
                        movingNoPoint = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isFinish = true;
                isSelect = false;
                break;
        }
        //检查被选中的点是否重复
        if (!isFinish && isSelect && p != null) {
            if (crossPoint(p)) {
                //该点为交叉的点时
                movingNoPoint = true;
            } else {
                //该点为新的点时
                p.state = Point.STATE_PRESSED;
                selectedPoints.add(p);
            }
        }
        if (isFinish) {
            //绘制不成立
            if (selectedPoints.size() == 1) {
                resetPoint();
            } else if (selectedPoints.size() >= 2 && selectedPoints.size() < POINT_SELECTED_COUNT) {//绘制错误
                errorPoint();
                if (mOnPatterChangeListener != null) {
                    mOnPatterChangeListener.onPatterChange(null);
                }
            } else {
                //绘制成功
                if (mOnPatterChangeListener != null) {
                    String passWord = "";
                    for (int i = 0; i < selectedPoints.size(); i++) {
                        passWord = passWord + selectedPoints.get(i).index;
                    }
                    if (!TextUtils.isEmpty(passWord)) {
                        mOnPatterChangeListener.onPatterChange(passWord);
                    }
                }
            }
        }
        //刷新View
        postInvalidate();
        return true;
    }

    //设置绘制错误
    public void errorPoint() {
        //图案设置到错误状态
        for (Point point : selectedPoints) {
            point.state = Point.STATE_ERROR;
        }
    }

    //设置绘制不成立
    public void resetPoint() {
        //图案恢复到正常的状态
        for (int i = 0; i < selectedPoints.size(); i++) {
            Point point = selectedPoints.get(i);
            point.state = Point.STATE_NORMAL;
        }
        selectedPoints.clear();
    }


    //检查鼠标的坐标是否在点的附近，若是则该点被选中，绘制连线
    private Point checkSelectPoint() {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point point = mPoints[i][j];
                if (Point.with(point.x, point.y, bitmapR, movingX, movingY)) {
                    return point;
                }
            }
        }
        return null;
    }

    //检查选中的点是否交叉
    public boolean crossPoint(Point point) {
        if (selectedPoints.contains(point)) {
            return true;
        } else {
            return false;
        }
    }

    public static class Point {
        public static int STATE_NORMAL = 0;//正常
        public static int STATE_PRESSED = 1;//选中
        public static int STATE_ERROR = 2;//错误
        public float x, y;
        public int index = 0, state = 0;

        public float getX() {
            return x;
        }


        public float getY() {
            return y;
        }


        public Point() {
        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        //返回距离
        public static double distance(Point a, Point b) {
            //X轴差的平方加上Y轴差的平方。对和开方,即勾股定理。
            return Math.sqrt(Math.abs(a.x - b.x) * Math.abs(a.x - b.x) + Math.abs(a.y - b.y) * Math.abs(a.y - b.y));
        }

        //判断是否重合,鼠标坐标到该点圆心的距离小于半径
        public static boolean with(float pointX, float pointY, float r, float movingX, float movingY) {
            return Math.sqrt((pointX - movingX) * (pointX - movingX) + (pointY - movingY) * (pointY - movingY)) < r;
        }

        //获取两点的角度
        public static float getDegrees(Point a, Point b) {
            float degrees = 0;
            float ax = a.getX();
            float ay = a.getY();
            float bx = b.getX();
            float by = b.getY();

            if (ax == bx) {
                if (by > ay) {
                    degrees = 90;
                } else {
                    degrees = 270;
                }
            } else if (by == ay) {
                if (ax > bx) {
                    degrees = 180;
                } else {
                    degrees = 0;
                }
            } else {
                if (ax > bx) {
                    if (ay > by) { // 第三象限
                        degrees = 180 + (float) (Math.atan2(ay - by, ax - bx) * 180 / Math.PI);
                    } else { // 第二象限
                        degrees = 180 - (float) (Math.atan2(by - ay, ax - bx) * 180 / Math.PI);
                    }
                } else {
                    if (ay > by) { // 第四象限
                        degrees = 360 - (float) (Math.atan2(ay - by, bx - ax) * 180 / Math.PI);
                    } else { // 第一象限
                        degrees = (float) (Math.atan2(by - ay, bx - ax) * 180 / Math.PI);
                    }
                }
            }
            return degrees;
        }

    }

    public static interface OnPatterChangeListener {
        //图案改变
        void onPatterChange(String passWord);

        //重新设图案
        void onPatterStart(Boolean isStart);
    }

    //设置图案监听器
    public void setPatterChangeListener(OnPatterChangeListener onPatterChangeListener) {
        if (onPatterChangeListener != null) {
            this.mOnPatterChangeListener = onPatterChangeListener;
        }
    }
}
