/*
The MIT License (MIT)
Copyright (c) 2020 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/
package com.sentaroh.android.SMBSync2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;



public class CustomTabLayout extends TabLayout {
    private Context c=null;
    public CustomTabLayout(@NonNull Context context) {
        super(context);
        c=context;
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c=context;
    }

    public CustomTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        c=context;
    }

    public void addTab(String tab_name) {
        addTab(this.newTab().setText(tab_name).setTag(tab_name));
//        TextView tabOne = (TextView) LayoutInflater.from(c).inflate(R.layout.custom_tab_layout_tab_item, null);
//        tabOne.setText(tab_name);
////        tabOne.setTextSize(CommonDialog.toPixel(c.getResources(), 18));
//        this.getTabAt(this.getTabCount()-1).setCustomView(tabOne);
    }

    public void adjustTabWidth() {
        LinearLayout tab_layout=((LinearLayout)this.getChildAt(0));
        for(int i = 0; i<tab_layout.getChildCount(); i++) {
            LinearLayout layout = ((LinearLayout) tab_layout.getChildAt(i));
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
            layoutParams.weight = 1.0f; // e.g. 0.5f
            layoutParams.width= LinearLayout.LayoutParams.WRAP_CONTENT;
            layout.setLayoutParams(layoutParams);
        }
    }

    public void setCurrentTabByPosition(int position) {
        getTabAt(position).select();
    }

    public void setCurrentTabByName(String tab_name) {
        for(int i=0;i<getTabCount();i++) {
            String tag=(String)getTabAt(i).getTag();
            if (tag!=null && tag.equals(tab_name)) {
                getTabAt(i).select();
                break;
            }
        }
    }

    public String getSelectedTabName() {
        String tab_name=(String)getTabAt(getSelectedTabPosition()).getTag();
        return tab_name;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        LinearLayout tabStrip = ((LinearLayout)getChildAt(0));
        if (enabled) {
            for(int i = 0; i < tabStrip.getChildCount(); i++) {
                tabStrip.getChildAt(i).setOnTouchListener(null);
            }
        } else {
            for(int i = 0; i < tabStrip.getChildCount(); i++) {
                tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }
        }

    }

}
