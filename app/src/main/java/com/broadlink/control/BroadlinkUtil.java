package com.broadlink.control;

import android.content.Context;

import com.broadlink.control.api.BroadlinkAPI;
import com.broadlink.control.api.DeviceInfo;
import com.broadlink.control.model.Code;
import com.broadlink.control.model.Function;
import com.broadlink.control.model.FunctionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by morfeusys on 01.02.16.
 */
public class BroadlinkUtil {

    public static void processFunction(Context context, long id) {
        DbHelper helper = new DbHelper(context);
        List<FunctionButton> list = helper.getButtons(id);
        helper.close();
        List<Code> codes = new ArrayList<>();
        for (FunctionButton button : list) codes.add(button.getCode());
        process(context, codes);
    }

    public static void processFunction(Context context, String query) {
        if (query == null || query.isEmpty()) return;
        DbHelper helper = new DbHelper(context);
        List<Function> list = helper.queryFunctions(query);
        List<Code> codes = new ArrayList<>();
        for (Function function : list) {
            List<FunctionButton> buttons = helper.getButtons(function.getId());
            for (FunctionButton button : buttons) codes.add(button.getCode());
        }
        helper.close();
        process(context, codes);
    }

    public static void processButton(Context context, String query) {
        if (query == null || query.isEmpty()) return;
        DbHelper helper = new DbHelper(context);
        List<Code> list = helper.queryCodes(query);
        helper.close();
        process(context, list);
    }

    private static void process(final Context context, final List<Code> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BroadlinkAPI api = initAPI(context);
                for (int i = 0; i < list.size(); i++) {
                    try {
                        if (i > 0) Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Code code = list.get(i);
                        if (code.isRm1()) {
                            api.RM1Send(code.getMac(), code.getData());
                        } else {
                            api.RM2Send(code.getMac(), code.getData());
                        }
                    }
                }
            }
        }).start();
    }

    private static BroadlinkAPI initAPI(Context context) {
        BroadlinkAPI api = BroadlinkAPI.getInstance(context);
        ArrayList<DeviceInfo> devices = api.getProbeList();
        if (devices == null) {
            api.broadlinkInitNetwork();
            try {
                Thread.sleep(300);
                devices = api.getProbeList();
                if (devices != null) {
                    for (DeviceInfo info : devices) api.addDevice(info);
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        return api;
    }
}
