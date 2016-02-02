package com.broadlink.control.model;

/**
 * Created by morfeusys on 01.02.16.
 */
public class Function {
    private long mId;
    private String mName;

    public Function(long id, String name) {
        mId = id;
        mName = name;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return mName;
    }
}
