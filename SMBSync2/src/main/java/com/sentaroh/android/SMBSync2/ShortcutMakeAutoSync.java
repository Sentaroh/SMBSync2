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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

public class ShortcutMakeAutoSync extends Activity {

    @TargetApi(25)
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);

        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setClassName(this, ShortcutAutoSync.class.getName());
        String shortcutName = getString(R.string.app_name_auto_sync);

        if (Build.VERSION.SDK_INT >= 26) {
            // Android 8 O API26 以降
//            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
//            Icon icon = Icon.createWithResource(getApplicationContext(), R.drawable.auto_sync);
//            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(getApplicationContext(), shortcutName)
//                    .setShortLabel(shortcutName)
//                    .setLongLabel(shortcutName)
//                    .setIcon(icon)
//                    .setIntent(shortcutIntent)
//                    .build();
//
//            Intent shortcutResultIntent = shortcutManager.createShortcutResultIntent(shortcutInfo);
//            PendingIntent shortcutCallBackIntent = PendingIntent.getBroadcast(this, 0,
//                    shortcutResultIntent, 0);
//            shortcutManager.requestPinShortcut(shortcutInfo, shortcutCallBackIntent.getIntentSender()); // フツーのショートカット
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
            Parcelable iconResource =
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.auto_sync);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            setResult(RESULT_OK, intent);
        } else {
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
            Parcelable iconResource =
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.auto_sync);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            setResult(RESULT_OK, intent);
            sendBroadcast(intent);
        }

        finish();
    }

}
