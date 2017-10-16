package ch.epfl.chili.cellulo.localizationbt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private int MAX_DEVICES = 100;
    private List<String> macs = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private boolean[] mItemChecked = new boolean[MAX_DEVICES];
    private Context mContext = null;
    private int sz = 0;

    public CustomAdapter() {
        for (int i = 0; i < MAX_DEVICES; i++)
            mItemChecked[i] = false;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void addElement(String mac, String name) {
        macs.add(mac);
        names.add(name);
        sz++;
    }

    public int getCount() {
        return(sz);
    }

    @Override
    public Object getItem(int position) {
        return macs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean itemIsChecked(int position) {
        return mItemChecked[position];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(position >= getCount())
            return(convertView);

        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview, parent, false);
            holder = new ViewHolder();
            holder.mac = (TextView) convertView.findViewById(R.id.textview1);
            holder.name = (TextView) convertView.findViewById(R.id.textview2);
            holder.cb = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mac.setText(macs.get(position));
        holder.name.setText(names.get(position));
        //Important to remove previous listener before calling setChecked
        holder.cb.setOnCheckedChangeListener(null);
        holder.cb.setChecked(mItemChecked[position]);
        holder.cb.setTag(position);
        holder.cb.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mItemChecked[position] = isChecked;
                    }
                });
        return convertView;
    }

    public ArrayList<String> getMacs()
    {
        ArrayList<String> res = new ArrayList<String>();
        for(int i = 0; i < sz; i++)
            if(mItemChecked[i]) res.add(macs.get(i));

        return(res);
    }

    private class ViewHolder {
        TextView mac;
        TextView name;
        CheckBox cb;
    }
}