package com.sentaroh.android.SMBSync2;

/*
The MIT License (MIT)
Copyright (c) 2011-2018 Sentaroh

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

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {

    private static final int MAX_PROGRESS = 100;
    private static final int DEFAULT_PROGRESS = 50;
    private int currentProgress;
    private int oldProgress;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_seekbar);
    }

    /* 
     * Preference が 呼び出されるときにデフォルト値が読み込まれる必要がある
     * 異なる Preference 型は異なる 値型 は持つはずなので、サブクラスはそれにあわせた型を返す必要がある
     */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_PROGRESS);
    }

    /* 
     * Preference の初期値を設定する
     * restorePersistedValue が true の場合、Preference 値を、SharedPreference からレストアすべき
     * false の場合 Preference 値にデフォルト値をセット
     * (SharedPreference の　shouldPersist() が true の場合、可能ならSharedPreferenceに値を格納) 
     */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            currentProgress = getPersistedInt(currentProgress);
        } else {
            currentProgress = (Integer) defaultValue;
            persistInt(currentProgress);
        }
        oldProgress = currentProgress;
    }

    /*
     * Preference のために、ビューとデータをバインドする
     * レイアウトからカスタムビューを参照しプロパティを設定するのに適する
     * スーパークラスの実装の呼び出しを確実に行うこと
     */
    @Override
    protected void onBindView(View view) {
        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.pref_seekbar);
        if (seekbar != null) {
            seekbar.setProgress(currentProgress);
            seekbar.setMax(MAX_PROGRESS);
            seekbar.setOnSeekBarChangeListener(this);
        }
        super.onBindView(view);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        int progress = seekbar.getProgress();
        /* ユーザーが設定変更を行った後(内部的な値を設定する前)に呼び出す。 */
        currentProgress = (callChangeListener(progress)) ? progress : oldProgress;

        persistInt(currentProgress);
        oldProgress = currentProgress;
    }
}