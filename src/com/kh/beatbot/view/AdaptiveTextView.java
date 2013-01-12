package com.kh.beatbot.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AdaptiveTextView extends TextView {
    private Paint mTestPaint;
    
    public AdaptiveTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }
 
    public AdaptiveTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
        
    /*
     * Text size adjustment from here on
     */
    private void initialize() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //max size defaults to the initially specified text size unless it is too small
    }

    /* Resize the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    public void refitText(String text, int textWidth) { 
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth) 
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }
}
