package com.broadlink.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by morfeusys on 01.02.16.
 */
public class BroadlinkReceiver extends BroadcastReceiver {
    public static final String EXTRA_TEXT = "text";

    @Override
    public void onReceive(Context context, Intent intent) {
        String text = intent.getStringExtra(EXTRA_TEXT);
        if ("com.broadlink.control.action.QUERY".equals(intent.getAction())) {
            BroadlinkUtil.processFunction(context, text);
        } else {
            BroadlinkUtil.processButton(context, text);
        }
    }
}
