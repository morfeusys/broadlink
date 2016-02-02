package com.broadlink.control;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.broadlink.control.api.BroadlinkAPI;
import com.broadlink.control.api.BroadlinkConstants;
import com.broadlink.control.api.DeviceInfo;
import com.broadlink.control.model.Code;

import java.util.List;

/**
 * Created by morfeusys on 31.01.16.
 */
public class ButtonsFragment extends ListFragment {
    private DbHelper mDbHelper;
    private BroadlinkAPI mAPI;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDbHelper = new DbHelper(getActivity());
        mAPI = BroadlinkAPI.getInstance(getActivity());
        List<Code> codes = mDbHelper.getCodes();
        ArrayAdapter<Code> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, codes);
        setListAdapter(adapter);
        registerForContextMenu(getListView());
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ArrayAdapter<Code> adapter = (ArrayAdapter<Code>) getListAdapter();
        Code code = adapter.getItem(position);
        if (code.isRm1()) {
            mAPI.RM1Send(code.getMac(), code.getData());
        } else {
            mAPI.RM2Send(code.getMac(), code.getData());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            addCode();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.menu_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position;
        ArrayAdapter<Code> adapter = (ArrayAdapter) getListAdapter();
        if (item.getItemId() == R.id.edit) {
            Code code = adapter.getItem(position);
            addCode(code);
            return true;
        } else if (item.getItemId() == R.id.remove) {
            Code code = adapter.getItem(position);
            adapter.remove(code);
            adapter.notifyDataSetChanged();
            mDbHelper.removeCode(code.getId());
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void addCode() {
        final List<DeviceInfo> devices = mAPI.getProbeList();
        if (devices.isEmpty()) {
            Toast.makeText(getActivity(), "Устройств не найдено", Toast.LENGTH_SHORT).show();
            return;
        }
        if (devices.size() > 1) {
            CharSequence[] items = new CharSequence[devices.size()];
            for (int i = 0; i < items.length; i++) {
                items[i] = devices.get(i).getName();
            }
            new AlertDialog.Builder(getActivity()).setTitle("Выберите устройство")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addCode(devices.get(which));
                        }
                    }).show();

        } else {
            addCode(devices.get(0));
        }
    }

    private void addCode(DeviceInfo device) {
        new StudyTask().execute(device);
    }

    private void addCode(final Code code) {
        if (code == null || isRemoving()) return;
        final EditText editText = new EditText(getActivity());
        if (code.getId() > 0) editText.setText(code.getName());
        new AlertDialog.Builder(getActivity()).setTitle("Название кнопки")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString().trim();
                        if (!name.isEmpty()) {
                            int pos = ((ArrayAdapter) getListAdapter()).getPosition(code);
                            addCode(new Code(code.getId(), name, code.getMac(), code.getType(), code.getData()), pos);
                        }
                    }
                }).show();
    }

    private void addCode(Code code, int pos) {
        long id = mDbHelper.saveCode(code);
        code = new Code(id, code.getName(), code.getMac(), code.getType(), code.getData());
        ArrayAdapter<Code> adapter = (ArrayAdapter<Code>) getListAdapter();
        if (pos > -1) {
            adapter.remove(adapter.getItem(pos));
            adapter.insert(code, pos);
        } else {
            adapter.add(code);
        }
        adapter.notifyDataSetChanged();
    }


    private class StudyTask extends AsyncTask<DeviceInfo, Void, Code> {
        private ProgressDialog mProgress;

        @Override
        protected void onPreExecute() {
            mProgress = new ProgressDialog(getActivity());
            mProgress.setIndeterminate(true);
            mProgress.setMessage("Нажмите кнопку на пульте");
            mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            mProgress.show();
        }

        @Override
        protected Code doInBackground(DeviceInfo... params) {
            DeviceInfo info = params[0];
            String type = info.getType();
            String mac = info.getMac();
            boolean rm1 = BroadlinkConstants.RM1.equals(type);
            boolean success = rm1 ? mAPI.RM1StudyMode(mac) : mAPI.RM2StudyMode(mac);
            if (!success) {
                return null;
            }
            String code;
            while ((code = (rm1 ? mAPI.RM1Code(mac) : mAPI.RM2Code(mac))) == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
            return new Code(0, null, mac, type, code);
        }

        @Override
        protected void onPostExecute(Code code) {
            mProgress.dismiss();
            if (code != null) {
                addCode(code);
            } else {
                Toast.makeText(getActivity(), "Невозможно получить данные", Toast.LENGTH_LONG).show();
            }
        }
    }
}
