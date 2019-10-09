package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.sentaroh.android.Utilities.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonWordwrapTextView extends TextView {
    private static Logger log= LoggerFactory.getLogger(NonWordwrapTextView.class);
    private CharSequence mOrgText = "";
    private BufferType mOrgBufferType = BufferType.NORMAL;
    private int mSplitTextLineCount=0;
    private SpannableStringBuilder mSpannableSplitText=null;
    private boolean mWordWrapMode =false;

    public NonWordwrapTextView(Context context) {
        super(context);
    }

    public NonWordwrapTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonWordwrapTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setWordWrapEnabled(boolean word_wrap_enabled) {
        mWordWrapMode =word_wrap_enabled;
    }

    public boolean isWordWrapEnabled() {
        return mWordWrapMode;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        log.info("onLayout changed="+changed+", left="+left+", top="+top+", right="+right+", bottom="+bottom);
        if (!isWordWrapEnabled()) {
            super.setText(mSpannableSplitText, mOrgBufferType);
        }
    }

    @Override
    final protected void onMeasure(int w, int h) {
        TextPaint paint = getPaint();
        super.onMeasure(w, h);
//        log.info("onMeasure w="+MeasureSpec.getSize(w)+", h="+MeasureSpec.getSize(h));
        if (!isWordWrapEnabled()) {
            mSpannableSplitText=buildSplitText(MeasureSpec.getSize(w), MeasureSpec.getSize(h));
            float sep_line1=(int)toPixel(getResources(), 3);
            float sep_line2=(int)toPixel(getResources(), 7);
//            log.info("ts="+getTextSize()+", paint ts="+paint.getTextSize());
            int new_h=(int) Math.ceil((paint.getTextSize()+sep_line1)*(float)mSplitTextLineCount+sep_line2);
//            log.info("onMeasure w="+MeasureSpec.getSize(w)+", h="+MeasureSpec.getSize(h)+", new w="+MeasureSpec.getSize(w)+", new h="+new_h);
            setMeasuredDimension( MeasureSpec.getSize(w), new_h);
        }
    }

    @Override
    final protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        log.info("onSizeChanged w="+w+", h="+h+", oldw="+oldw+", oldh="+oldh);
    };

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOrgText = text;
        mOrgBufferType = type;
        super.setText(text, type);
//        log.info("setText length="+text.length()+", type="+type.toString());
    }

    @Override
    public CharSequence getText() {
        return mOrgText;
    }

    @Override
    public int length() {
        return mOrgText.length();
    }

    public SpannableStringBuilder buildSplitText(int w, int h) {
        boolean debug=false;
        TextPaint paint = getPaint();
        int wpl =getCompoundPaddingLeft();
        int wpr =getCompoundPaddingRight();
        int width = w - wpl - wpr;
        if (debug) log.info("buildSplitText width="+width+", w="+w+", wpl="+wpl+", wpr="+wpr+", h="+h+", length="+mOrgText.length());

        SpannableStringBuilder output = null;
        int add_cr_cnt=0;
        if (width<=0) {
            output=new SpannableStringBuilder(mOrgText);
            mSplitTextLineCount=mOrgText.toString().split("\n").length;
        } else {
            output=new SpannableStringBuilder(mOrgText);
            int start=0;
            if (debug) log.info("input="+output.toString());
            while(start<output.length()) {
                if (debug) log.info("start="+start);
                String in_text=output.subSequence(start, output.length()).toString();
                int cr_pos=in_text.indexOf("\n");
                if (cr_pos>0) {
                    in_text = output.subSequence(start, start + cr_pos).toString();
                    int nc = paint.breakText(in_text, true, width, null);
                    if (output.charAt(start + nc) != '\n') output.insert(start + nc, "\n");
                    start = start + nc + 1;
                } else if (cr_pos==0) {
                    start = start + 1;
                } else {
                    int nc=paint.breakText(in_text, true, width, null);
                    output.insert(start+nc, "\n");
                    start=start+nc+1;
                }
            }
            mSplitTextLineCount=output.toString().split("\n").length;
        }

        if (debug) {
            log.info("buildSplitText Number of Lines="+mSplitTextLineCount+", added_cr/lf_count="+add_cr_cnt);
            log.info("buildSplitText input  char="+mOrgText.toString());
            log.info("buildSplitText input  hex ="+ StringUtil.getDumpFormatHexString(mOrgText.toString().getBytes(), 0, mOrgText.toString().getBytes().length));
            log.info("buildSplitText output char="+output.toString());
            log.info("buildSplitText output hex ="+ StringUtil.getDumpFormatHexString(output.toString().getBytes(), 0, output.toString().getBytes().length));
        }
        return output;
    }

    final static private float toPixel(Resources res, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
        return px;
    };


}