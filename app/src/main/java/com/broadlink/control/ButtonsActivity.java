package com.broadlink.control;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.broadlink.control.model.Code;
import com.broadlink.control.model.Function;
import com.broadlink.control.model.FunctionButton;

import java.util.List;

/**
 * Created by morfeusys on 01.02.16.
 */
public class ButtonsActivity extends AppCompatActivity {
    public static final String FUNCTION_ID = "id";

    private DbHelper mDbHelper;
    private ArrayAdapter<FunctionButton> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buttons);
        long fid = getIntent().getLongExtra(FUNCTION_ID, 0);
        mDbHelper = new DbHelper(this);
        Function function = mDbHelper.getFunction(fid);
        setTitle(function.getName());
        ListView listView = (ListView) findViewById(R.id.list);
        List<FunctionButton> list = mDbHelper.getButtons(fid);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(mAdapter);
        registerForContextMenu(listView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addButton();
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        FunctionButton button = mAdapter.getItem(position);
        if (item.getItemId() == R.id.remove) {
            mAdapter.remove(button);
            mAdapter.notifyDataSetChanged();
            mDbHelper.removeButton(button.getId());
        }
        return true;
    }

    private void addButton() {
        final List<Code> list = mDbHelper.getCodes();
        CharSequence[] items = new CharSequence[list.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = list.get(i).getName();
        }
        new AlertDialog.Builder(this).setTitle("Выберите кнопку")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addButton(list.get(which));
                    }
                }).show();
    }

    private void addButton(Code code) {
        long fid = getIntent().getLongExtra(FUNCTION_ID, 0);
        long id = mDbHelper.saveButton(new FunctionButton(0, code), fid);
        mAdapter.add(new FunctionButton(id, code));
        mAdapter.notifyDataSetChanged();
    }
}
