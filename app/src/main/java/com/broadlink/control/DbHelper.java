package com.broadlink.control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.broadlink.control.model.Code;
import com.broadlink.control.model.Function;
import com.broadlink.control.model.FunctionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by morfeusys on 01.02.16.
 */
public class DbHelper extends SQLiteOpenHelper {
    public DbHelper(Context context) {
        super(context, "BroadlinkControlDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table codes ("
                + "id integer primary key autoincrement,"
                + "mac text,"
                + "dtype text,"
                + "name text COLLATE NOCASE,"
                + "data text"
                + ");");

        db.execSQL("create table functions ("
                + "id integer primary key autoincrement,"
                + "name text COLLATE NOCASE"
                + ");");

        db.execSQL("create table buttons ("
                + "id integer primary key autoincrement,"
                + "function_id integer,"
                + "code_id integer"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long saveCode(Code code) {
        ContentValues cv = new ContentValues();
        if (code.getId() > 0) cv.put("id", code.getId());
        cv.put("mac", code.getMac());
        cv.put("dtype", code.getType());
        cv.put("name", code.getName());
        cv.put("data", code.getData());
        if (code.getId() > 0) {
            getWritableDatabase().update("codes", cv, "id = " + code.getId(), null);
            return code.getId();
        } else {
            return getWritableDatabase().insert("codes", null, cv);
        }
    }

    public List<Code> getCodes() {
        List<Code> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("codes", new String[] {"id", "name", "mac", "dtype", "data"}, null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                list.add(new Code(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void removeCode(long id) {
        getWritableDatabase().delete("codes", "id = " + id, null);
    }

    public long saveFunction(Function function) {
        ContentValues cv = new ContentValues();
        if (function.getId() > 0) cv.put("id", function.getId());
        cv.put("name", function.getName());
        if (function.getId() > 0) {
            getWritableDatabase().update("functions", cv, "id = " + function.getId(), null);
            return function.getId();
        } else {
            return getWritableDatabase().insert("functions", null, cv);
        }
    }

    public List<Function> getFunctions() {
        List<Function> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("functions", new String[] {"id", "name"}, null, null, null, null, null);
        try {
            while (c.moveToNext()) {
                list.add(new Function(c.getLong(0), c.getString(1)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public void removeFunction(long id) {
        getWritableDatabase().delete("functions", "id = " + id, null);
    }

    public long saveButton(FunctionButton button, long fid) {
        ContentValues cv = new ContentValues();
        cv.put("function_id", fid);
        cv.put("code_id", button.getCode().getId());
        return getWritableDatabase().insert("buttons", null, cv);
    }

    public void removeButton(long id) {
        getWritableDatabase().delete("buttons", "id = " + id, null);
    }

    public Code getCode(long id) {
        Cursor c = getReadableDatabase().query("codes", new String[] {"name", "mac", "dtype", "data"}, "id = " + id, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                return new Code(id, c.getString(0), c.getString(1), c.getString(2), c.getString(3));
            }
        } finally {
            c.close();
        }
        return null;
    }

    public Function getFunction(long id) {
        Cursor c = getReadableDatabase().query("functions", new String[] {"name"}, "id = " + id, null, null, null, null);
        try {
            if (c.moveToFirst()) {
                return new Function(id, c.getString(0));
            }
        } finally {
            c.close();
        }
        return null;
    }

    public List<FunctionButton> getButtons(long fid) {
        List<FunctionButton> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("buttons", new String[] {"id", "code_id"}, "function_id = " + fid, null, null, null, null);
        try {
            while (c.moveToNext()) {
                list.add(new FunctionButton(c.getLong(0), getCode(c.getLong(1))));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public List<Function> queryFunctions(String query) {
        List<Function> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("functions", new String[] {"id", "name"}, "name LIKE ?", new String[] {query}, null, null, null);
        try {
            while (c.moveToNext()) {
                list.add(new Function(c.getLong(0), c.getString(1)));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public List<Code> queryCodes(String query) {
        List<Code> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("codes", new String[] {"id", "name", "mac", "dtype", "data"}, "name LIKE ?", new String[] {query}, null, null, null);
        try {
            while (c.moveToNext()) {
                list.add(new Code(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
            }
        } finally {
            c.close();
        }
        return list;
    }
}
