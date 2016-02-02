package com.broadlink.control.model;

/**
 * Created by morfeusys on 01.02.16.
 */
public class FunctionButton {
    private long mId;
    private Code mCode;

    public FunctionButton(long id, Code code) {
        mId = id;
        mCode = code;
    }

    public long getId() {
        return mId;
    }

    public Code getCode() {
        return mCode;
    }

    @Override
    public String toString() {
        return mCode.getName();
    }
}
