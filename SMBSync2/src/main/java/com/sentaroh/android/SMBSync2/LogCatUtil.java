package com.sentaroh.android.SMBSync2;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.ZipUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogCatUtil {
    public static void prepareOptionMenu(GlobalParameters mGp, CommonUtilities mUtil, Menu menu) {
//        if (mGp.logCatActive) {
//            menu.findItem(R.id.menu_top_start_logcat).setEnabled(false);
//            menu.findItem(R.id.menu_top_stop_logcat).setEnabled(true);
//            menu.findItem(R.id.menu_top_send_logcat).setEnabled(false);
//        } else {
//            menu.findItem(R.id.menu_top_start_logcat).setEnabled(true);
//            menu.findItem(R.id.menu_top_stop_logcat).setEnabled(false);
//            menu.findItem(R.id.menu_top_send_logcat).setEnabled(true);
//        }
//        if (mUtil.isDebuggable()) {
//            menu.findItem(R.id.menu_top_start_logcat).setVisible(true);
//            menu.findItem(R.id.menu_top_stop_logcat).setVisible(true);
//            menu.findItem(R.id.menu_top_send_logcat).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_top_start_logcat).setVisible(false);
//            menu.findItem(R.id.menu_top_stop_logcat).setVisible(false);
//            menu.findItem(R.id.menu_top_send_logcat).setVisible(false);
//        }

    }

    public static void stopLogCat(GlobalParameters gp, CommonUtilities mUtil) {
        gp.logCatActive=false;
        mUtil.flushLog();
    }

    public static void startLogCat(GlobalParameters gp, final String log_cat_dir, final String log_cat_file) {
        Thread th=new Thread() {
            @Override
            public void run() {
                if (gp.logCatActive) return;
                gp.logCatActive=true;
                Process process = null;
                BufferedReader reader = null;
                BufferedOutputStream bos=null;
                try {
                    File log_dir=new File(log_cat_dir);
                    if (!log_dir.exists()) log_dir.mkdirs();
                    File log_out=new File(log_cat_dir+"/"+log_cat_file);
                    bos=new BufferedOutputStream(new FileOutputStream(log_out), 1024*1024*2);
//                    String[] command = { "logcat", "-v", "time", "*:V" };
                    String[] command = { "logcat", "-v", "time"};

                    // Logcat を出力する
                    process = Runtime.getRuntime().exec(command);
                    reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024*64);
                    String line;
                    while ((line = reader.readLine()) != null) {
//                        bos.write((StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+"\n").getBytes());
                        bos.write((line+"\n").getBytes());
                        if (!gp.logCatActive) break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            bos.flush();
                            bos.close();
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        th.setName("LogCatWriter");
        th.start();
    }

    public static void sendLogCat(final AppCompatActivity mActivity, final GlobalParameters mGp, final CommonUtilities mUtil,
                                  String log_cat_dir, String log_cat_name) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.send_logcat_dlg);

        final TextView tv_title=(TextView)dialog.findViewById(R.id.send_logcat_dlg_title);
        final Button btn_send=(Button)dialog.findViewById(R.id.send_logcat_dlg_ok_btn);
        final Button btn_close=(Button)dialog.findViewById(R.id.send_logcat_dlg_cancel_btn);
        final Button btn_preview_logcat=(Button)dialog.findViewById(R.id.send_logcat_dlg_preview_logcat);
        final Button btn_preview_applog=(Button)dialog.findViewById(R.id.send_logcat_dlg_preview_applog);

        CommonDialog.setDlgBoxSizeLimit(dialog,true);

        btn_preview_logcat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+mGp.getLogDirName()+"/logcat.txt"), "text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(Intent.createChooser(intent, mGp.getLogDirName()+"/logcat.txt"));
            }
        });

        btn_preview_applog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUtil.flushLog();
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+mGp.getLogDirName()+"/"+mGp.getLogFileName()+".txt"), "text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(Intent.createChooser(intent, mGp.getLogDirName()+"/"+mGp.getLogFileName()+".txt"));
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_SEND);
//                intent.setType("message/rfc822");
//                intent.setType("text/plain");
                intent.setType("application/zip");

                String zip_file_name=mGp.getLogDirName()+"/log.zip";
                File lf=new File(zip_file_name);
                lf.delete();
                String[] lmp= LocalMountPoint.convertFilePathToMountpointFormat(mGp.appContext, log_cat_dir+"/"+log_cat_name);
                ZipUtil.createZipFile(mGp.appContext, null, null, zip_file_name, lmp[0], log_cat_dir+"/"+log_cat_name, mGp.getLogDirName()+"/"+mGp.getLogFileName()+".txt");

                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"gm.developer.fhoshino@gmail.com"});
//                intent.putExtra(Intent.EXTRA_CC, new String[]{"cc@example.com"});
//                intent.putExtra(Intent.EXTRA_BCC, new String[]{"bcc@example.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "SMBSync2 LogCat");
//                intent.putExtra(Intent.EXTRA_TEXT, tv_msg_old.getText().toString());
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(lf));
                mGp.appContext.startActivity(intent);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_close.performClick();
            }
        });

        dialog.show();
    }

}
