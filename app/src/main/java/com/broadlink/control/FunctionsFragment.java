package com.broadlink.control;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.broadlink.control.model.Function;
import com.broadlink.control.model.FunctionButton;

import java.util.List;

/**
 * Created by morfeusys on 31.01.16.
 */
public class FunctionsFragment extends ListFragment {
    private DbHelper mDbHelper;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDbHelper = new DbHelper(getActivity());
        List<Function> list = mDbHelper.getFunctions();
        ArrayAdapter<Function> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
        setHasOptionsMenu(true);
        registerForContextMenu(getListView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDbHelper.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_function, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            saveFunction(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        ArrayAdapter<Function> adapter = (ArrayAdapter) getListAdapter();
        if (item.getItemId() == R.id.edit_function) {
            Function function = adapter.getItem(position);
            saveFunction(function);
            return true;
        } else if (item.getItemId() == R.id.remove_function) {
            Function function = adapter.getItem(position);
            adapter.remove(function);
            adapter.notifyDataSetChanged();
            mDbHelper.removeFunction(function.getId());
            return true;
        } else if (item.getItemId() == R.id.pref) {
            Function function = adapter.getItem(position);
            startActivity(new Intent(getActivity(), ButtonsActivity.class).putExtra(ButtonsActivity.FUNCTION_ID, function.getId()));
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Function function = (Function) getListAdapter().getItem(position);
        BroadlinkUtil.processFunction(getActivity(), function.getId());
    }

    private void saveFunction(final Function function) {
        final EditText editText = new EditText(getActivity());
        if (function != null) editText.setText(function.getName());
        new AlertDialog.Builder(getActivity()).setTitle("Название функции")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString().trim();
                        if (!name.isEmpty()) {
                            int pos = function != null ? ((ArrayAdapter) getListAdapter()).getPosition(function) : -1;
                            addFunction(new Function(function != null ? function.getId() : 0, name), pos);
                        }
                    }
                }).show();
    }

    private void addFunction(Function function, int pos) {
        long id = mDbHelper.saveFunction(function);
        function = new Function(id, function.getName());
        ArrayAdapter<Function> adapter = (ArrayAdapter) getListAdapter();
        if (pos > -1) {
            adapter.remove(adapter.getItem(pos));
            adapter.insert(function, pos);
        } else {
            adapter.add(function);
            startActivity(new Intent(getActivity(), ButtonsActivity.class).putExtra(ButtonsActivity.FUNCTION_ID, id));
        }
        adapter.notifyDataSetChanged();
    }
}
