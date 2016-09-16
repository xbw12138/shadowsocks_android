package com.xbw.mvp.scanner.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.xbw.mvp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.xbw.mvp.scanner.camera.CameraManager;
/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 * <br/>
 * <br/>
 * 璇ヨ鍥炬槸瑕嗙洊鍦ㄧ浉鏈虹殑棰勮瑙嗗浘涔嬩笂鐨勪竴灞傝鍥俱�傛壂鎻忓尯鏋勬垚鍘熺悊锛屽叾瀹炴槸鍦ㄩ瑙堣鍥句笂鐢诲洓鍧楅伄缃╁眰锛�
 * 涓棿鐣欎笅鐨勯儴鍒嗕繚鎸侀�忔槑锛屽苟鐢讳笂涓�鏉℃縺鍏夌嚎锛屽疄闄呬笂璇ョ嚎鏉″氨鏄睍绀鸿�屽凡锛屼笌鎵弿鍔熻兘娌℃湁浠讳綍鍏崇郴銆�
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	/**
	 * 鍒锋柊鐣岄潰鐨勬椂闂�
	 */
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	private int CORNER_PADDING;

	/**
	 * 鎵弿妗嗕腑鐨勪腑闂寸嚎鐨勫搴�
	 */
	private static int MIDDLE_LINE_WIDTH;

	/**
	 * 鎵弿妗嗕腑鐨勪腑闂寸嚎鐨勪笌鎵弿妗嗗乏鍙崇殑闂撮殭
	 */
	private static int MIDDLE_LINE_PADDING;

	/**
	 * 涓棿閭ｆ潯绾挎瘡娆″埛鏂扮Щ鍔ㄧ殑璺濈
	 */
	private static final int SPEEN_DISTANCE = 10;

	/**
	 * 鐢荤瑪瀵硅薄鐨勫紩鐢�
	 */
	private Paint paint;

	/**
	 * 涓棿婊戝姩绾跨殑鏈�椤剁浣嶇疆
	 */
	private int slideTop;

	/**
	 * 涓棿婊戝姩绾跨殑鏈�搴曠浣嶇疆
	 */
	private int slideBottom;

	private static final int MAX_RESULT_POINTS = 20;

	private Bitmap resultBitmap;

	/**
	 * 閬帺灞傜殑棰滆壊
	 */
	private final int maskColor;
	private final int resultColor;

	private final int resultPointColor;
	private List<ResultPoint> possibleResultPoints;

	private List<ResultPoint> lastPossibleResultPoints;

	/**
	 * 绗竴娆＄粯鍒舵帶浠�
	 */
	boolean isFirst = true;

	private CameraManager cameraManager;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		CORNER_PADDING = dip2px(context, 0.0F);
		MIDDLE_LINE_PADDING = dip2px(context, 20.0F);
		MIDDLE_LINE_WIDTH = dip2px(context, 3.0F);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG); // 寮�鍚弽閿娇

		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask); // 閬帺灞傞鑹�
		resultColor = resources.getColor(R.color.result_view);

		resultPointColor = resources.getColor(R.color.possible_result_points);
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		lastPossibleResultPoints = null;

	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}

		// 缁樺埗閬帺灞�
		drawCover(canvas, frame);

		if (resultBitmap != null) { // 缁樺埗鎵弿缁撴灉鐨勫浘
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(0xA0);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		}
		else {

			// 鐢绘壂鎻忔杈逛笂鐨勮
			drawRectEdges(canvas, frame);

			// 缁樺埗鎵弿绾�
			drawScanningLine(canvas, frame);

			List<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			}
			else {
				possibleResultPoints = new ArrayList<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			// 鍙埛鏂版壂鎻忔鐨勫唴瀹癸紝鍏朵粬鍦版柟涓嶅埛鏂�
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);

		}
	}

	/**
	 * 缁樺埗鎵弿绾�
	 * 
	 * @param canvas
	 * @param frame
	 *            鎵弿妗�
	 */
	private void drawScanningLine(Canvas canvas, Rect frame) {

		// 鍒濆鍖栦腑闂寸嚎婊戝姩鐨勬渶涓婅竟鍜屾渶涓嬭竟
		if (isFirst) {
			isFirst = false;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}

		// 缁樺埗涓棿鐨勭嚎,姣忔鍒锋柊鐣岄潰锛屼腑闂寸殑绾垮線涓嬬Щ鍔⊿PEEN_DISTANCE
		slideTop += SPEEN_DISTANCE;
		if (slideTop >= slideBottom) {
			slideTop = frame.top;
		}

		// 浠庡浘鐗囪祫婧愮敾鎵弿绾�
		Rect lineRect = new Rect();
		lineRect.left = frame.left + MIDDLE_LINE_PADDING;
		lineRect.right = frame.right - MIDDLE_LINE_PADDING;
		lineRect.top = slideTop;
		lineRect.bottom = (slideTop + MIDDLE_LINE_WIDTH);
		canvas.drawBitmap(((BitmapDrawable) (BitmapDrawable) getResources()
				.getDrawable(R.drawable.scan_laser)).getBitmap(), null,
				lineRect, paint);

	}

	/**
	 * 缁樺埗閬帺灞�
	 * 
	 * @param canvas
	 * @param frame
	 */
	private void drawCover(Canvas canvas, Rect frame) {

		// 鑾峰彇灞忓箷鐨勫鍜岄珮
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		// 鐢诲嚭鎵弿妗嗗闈㈢殑闃村奖閮ㄥ垎锛屽叡鍥涗釜閮ㄥ垎锛屾壂鎻忔鐨勪笂闈㈠埌灞忓箷涓婇潰锛屾壂鎻忔鐨勪笅闈㈠埌灞忓箷涓嬮潰
		// 鎵弿妗嗙殑宸﹁竟闈㈠埌灞忓箷宸﹁竟锛屾壂鎻忔鐨勫彸杈瑰埌灞忓箷鍙宠竟
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
	}

	/**
	 * 鎻忕粯鏂瑰舰鐨勫洓涓
	 * 
	 * @param canvas
	 * @param frame
	 */
	private void drawRectEdges(Canvas canvas, Rect frame) {

		paint.setColor(Color.WHITE);
		paint.setAlpha(OPAQUE);

		Resources resources = getResources();
		/**
		 * 杩欎簺璧勬簮鍙互鐢ㄧ紦瀛樿繘琛岀鐞嗭紝涓嶉渶瑕佹瘡娆″埛鏂伴兘鏂板缓
		 */
		Bitmap bitmapCornerTopleft = BitmapFactory.decodeResource(resources,
				R.drawable.scan_corner_top_left);
		Bitmap bitmapCornerTopright = BitmapFactory.decodeResource(resources,
				R.drawable.scan_corner_top_right);
		Bitmap bitmapCornerBottomLeft = BitmapFactory.decodeResource(resources,
				R.drawable.scan_corner_bottom_left);
		Bitmap bitmapCornerBottomRight = BitmapFactory.decodeResource(
				resources, R.drawable.scan_corner_bottom_right);

		canvas.drawBitmap(bitmapCornerTopleft, frame.left + CORNER_PADDING,
				frame.top + CORNER_PADDING, paint);
		canvas.drawBitmap(bitmapCornerTopright, frame.right - CORNER_PADDING
				- bitmapCornerTopright.getWidth(), frame.top + CORNER_PADDING,
				paint);
		canvas.drawBitmap(bitmapCornerBottomLeft, frame.left + CORNER_PADDING,
				2 + (frame.bottom - CORNER_PADDING - bitmapCornerBottomLeft
						.getHeight()), paint);
		canvas.drawBitmap(bitmapCornerBottomRight, frame.right - CORNER_PADDING
				- bitmapCornerBottomRight.getWidth(), 2 + (frame.bottom
				- CORNER_PADDING - bitmapCornerBottomRight.getHeight()), paint);

		bitmapCornerTopleft.recycle();
		bitmapCornerTopleft = null;
		bitmapCornerTopright.recycle();
		bitmapCornerTopright = null;
		bitmapCornerBottomLeft.recycle();
		bitmapCornerBottomLeft = null;
		bitmapCornerBottomRight.recycle();
		bitmapCornerBottomRight = null;

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
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

	/**
	 * dp杞琾x
	 * 
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
