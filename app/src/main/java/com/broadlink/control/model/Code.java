package com.broadlink.control.model;

import com.broadlink.control.api.BroadlinkConstants;

/**
 * Created by morfeusys on 01.02.16.
 */
public class Code {
    private long mId;
    private String mName;
    private String mMac;
    private String mType;
    private String mData;

    public Code(long id, String name, String mac, String type, String data) {
        mId = id;
        mName = name;
        mMac = mac;
        mType = type;
        mData = data;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getMac() {
        return mMac;
    }

    public String getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }

    public boolean isRm1() {
        return BroadlinkConstants.RM1.equals(mType);
    }

    @Override
    public String toString() {
        return mName;
    }
}
