package com.android.mslauncher;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class Draw_clock extends View
{

	public Draw_clock(Context context)
	{
		super(context);
	}

	public void onDraw(Canvas canvas)
	{

		canvas.drawColor(Color.TRANSPARENT);// 背景透明
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4.0f);

		drawClockPandle(canvas, paint);
		drawClockPointer(canvas, paint);
	}

	void drawClockPandle(Canvas canvas, Paint paint)
	{
		int px = getMeasuredWidth();
		int py = getMeasuredWidth();

		canvas.drawCircle(px / 2, py / 2, py / 2 - 18, paint);// 第三个参数为圆半径
		canvas.restore();
	}

	void drawClockPointer(Canvas canvas, Paint paint)
	{
		int px = (getMeasuredWidth()) / 2;
		int py = (getMeasuredWidth()) / 2;

		int mHour;
		int mMinutes;

		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mHour = mCalendar.get(Calendar.HOUR);
		mMinutes = mCalendar.get(Calendar.MINUTE);

		// float hDegree=((mHour+(float)mMinutes/60)/12)*360;
		float hDegree = ((mHour * 30 + (float) mMinutes / 2));
		float mDegree = mMinutes * 6;

		canvas.save();
		canvas.rotate(mDegree, px, py);
		Path path1 = new Path();
		path1.moveTo(px, py);
		path1.lineTo(px, py / 2.5f);
		canvas.drawPath(path1, paint);
		canvas.restore();

		canvas.save();
		canvas.rotate(hDegree, px, py);
		Path path2 = new Path();
		path2.moveTo(px, py);
		path2.lineTo(px, py / 1.55f);
		canvas.drawPath(path2, paint);
		canvas.restore();

	}

}
