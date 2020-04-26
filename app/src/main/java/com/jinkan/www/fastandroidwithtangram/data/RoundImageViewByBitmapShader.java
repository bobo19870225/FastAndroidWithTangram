package com.jinkan.www.fastandroidwithtangram.data;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.CellRender;
import com.tmall.wireless.tangram.util.ImageUtils;

public class RoundImageViewByBitmapShader extends AppCompatImageView {

    private Shader mShader;
    private Paint mPaint;

    public RoundImageViewByBitmapShader(Context context) {
        super(context);
    }

    public RoundImageViewByBitmapShader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageViewByBitmapShader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        if (drawable != null) {
            if (mShader == null) {
                mShader = new BitmapShader(drawable.getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            }
            mPaint.setShader(mShader);
            canvas.drawRoundRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), 10, 10, mPaint);
        } else {
            super.onDraw(canvas);
        }

    }

    @CellRender
    public void cellInited(BaseCell cell) {
        setOnClickListener(cell);

    }

    @CellRender
    public void postBindView(BaseCell cell) {
        String imgUrl = cell.optStringParam("imgUrl");
//        float ratioFromUrl = Utils.getImageRatio(imgUrl);
//        setRatio(ratioFromUrl);
//        if (cell.style != null) {
//            if (!Float.isNaN(cell.style.aspectRatio)) {
//                setRatio(cell.style.aspectRatio, RatioImageView.PRIORITY_HIGH);
//            }
//        }
        ImageUtils.doLoadImageUrl(this, imgUrl);
        setOnClickListener(cell);
    }

    @CellRender
    public void postUnBindView(BaseCell cell) {
    }
}