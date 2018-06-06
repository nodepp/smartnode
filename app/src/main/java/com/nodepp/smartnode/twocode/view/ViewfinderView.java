
package com.nodepp.smartnode.twocode.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.twocode.camera.CameraManager;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Created by yuyue on 2016/9/8.
 */
public final class ViewfinderView extends View {
	/**
	 * 刷新界面的时间
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;
	private static final int MAX_RESULT_POINTS = 5;
	/**
	 * 四个蓝色边角对应的长度
	 */
	private int ScreenRate;

	/**
	 * 四个蓝色边角对应的宽度
	 */
	private static final int CORNER_WIDTH = 10;
	/**
	 * 扫描框中的中间线的宽度
	 */
	private static final int MIDDLE_LINE_WIDTH = 6;

	/**
	 * 扫描框中的中间线的与扫描框左右的间隙
	 */
	private static final int MIDDLE_LINE_PADDING = 5;

	/**
	 * 中间那条线每次刷新移动的距离
	 */
	private static final int SPEEN_DISTANCE = 5;

	/**
	 * 手机的屏幕密度
	 */
	private static float density;
	/**
	 * 字体大小
	 */
	private static final int TEXT_SIZE = 16;
	/**
	 * 字体距离扫描框下面的距离
	 */
	private static final int TEXT_PADDING_TOP = 30;

	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;

	/**
	 * 中间滑动线的最底端位置
	 */
	private int slideBottom;

	/**
	 * 将扫描的二维码拍下来，这里没有这个功能，暂时不考虑
	 */
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;

	private final int resultPointColor;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;
	private CameraManager cameraManager;
	boolean isFirst;

	public ViewfinderView(Context context) {
		this(context, null);
	}

	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 初始化一次，不是每次都调用onDraw（）
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskColor = 0x60000000;
		resultColor = 0xb0000000;
		resultPointColor = 0xc0ffff00;
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		lastPossibleResultPoints = null;

		density = context.getResources().getDisplayMetrics().density;
		// 将像素转换成dp
		ScreenRate = (int) (20 * density);
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		// 中间的扫描框，CameraManager里面可修改扫描框的大小
		Rect frame = cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}

		// 初始化中间线滑动的最上边和最下边
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}

		// 获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		// 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		// 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (resultBitmap != null) {
			// 在扫描框上面画出扫描出的二维码的图片
//			paint.setAlpha(OPAQUE);
//			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {

			// 绘制矩形框架内两个像素的实心黑色边
			paint.setColor(0xffffffff);
			canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
			canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
			canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
			canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

			// 这里画取景框四个角落的夹角
			paint.setColor(0xff0ec3ff);
			paint.setAntiAlias(true);
			canvas.drawRect(frame.left - CORNER_WIDTH + 2, frame.top - CORNER_WIDTH + 2, frame.left + ScreenRate
					- CORNER_WIDTH + 2, frame.top + 2, paint);
			canvas.drawRect(frame.left - CORNER_WIDTH + 2, frame.top - CORNER_WIDTH + 2, frame.left + 2, frame.top
					+ ScreenRate - CORNER_WIDTH + 2, paint);
			canvas.drawRect(frame.right - ScreenRate + CORNER_WIDTH - 2, frame.top - CORNER_WIDTH + 2, frame.right
					+ CORNER_WIDTH - 2, frame.top + 2, paint);
			canvas.drawRect(frame.right - 2, frame.top - CORNER_WIDTH + 2, frame.right + CORNER_WIDTH - 2, frame.top
					+ ScreenRate - CORNER_WIDTH + 2, paint);

			canvas.drawRect(frame.left - CORNER_WIDTH + 2, frame.bottom - 2,
					frame.left + ScreenRate - CORNER_WIDTH + 2, frame.bottom + CORNER_WIDTH - 2, paint);
			canvas.drawRect(frame.left - CORNER_WIDTH + 2, frame.bottom - ScreenRate + CORNER_WIDTH - 2,
					frame.left + 2, frame.bottom + CORNER_WIDTH - 2, paint);
			canvas.drawRect(frame.right - ScreenRate + CORNER_WIDTH - 2, frame.bottom - 2, frame.right + CORNER_WIDTH
					- 2, frame.bottom + CORNER_WIDTH - 2, paint);
			canvas.drawRect(frame.right - 2, frame.bottom - ScreenRate + CORNER_WIDTH - 2, frame.right + CORNER_WIDTH
					- 2, frame.bottom + CORNER_WIDTH - 2, paint);

			// 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
			slideTop += SPEEN_DISTANCE;
			if (slideTop >= frame.bottom) {
				slideTop = frame.top;
			}

			Rect lineRect = new Rect();
			lineRect.left = frame.left;
			lineRect.right = frame.right;
			lineRect.top = slideTop;
			lineRect.bottom = slideTop + 18;
			canvas.drawBitmap(((BitmapDrawable) (getResources().getDrawable(R.drawable.qr_scan_line))).getBitmap(),
					null, lineRect, paint);

			/** 不显示关键点
			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
				}
			}
			 */
			// 只刷新扫描框的内容，其他地方不刷新
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	/**
	 * 画出二维码扫描结果图
	 * 
	 * @param barcode
	 *            二维码图
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}
}
