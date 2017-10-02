package com.arrow.a79361.arrowsmartcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by A79361 on 2016.07.28..
 */
public class CustomListOfBleAdapter extends BaseAdapter {

    private ArrayList<ControlItem> listData;
    private LayoutInflater layoutInflater;

    public CustomListOfBleAdapter(Context aContext, ArrayList<ControlItem> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(aContext);
    }

    @Override
    public int getCount() {
        if (listData != null) {
            return listData.size();
        } else {
            return 0;
        }
    }

    @Override
    public ControlItem getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.custom_list, null);
            holder = new ViewHolder();
            holder.itemName = (TextView) convertView.findViewById(R.id.Text1);
            holder.itemAddress = (TextView) convertView.findViewById(R.id.Text2);
            holder.itemType = (TextView) convertView.findViewById(R.id.Text3);
            holder.itemSs = (TextView) convertView.findViewById(R.id.tVss);
            holder.itemImage = (ImageView) convertView.findViewById(R.id.ItemImage);
            holder.rssImage = (ImageView) convertView.findViewById(R.id.RssImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemName.setText(listData.get(position).getName());
        holder.itemAddress.setText("@ " + listData.get(position).getAddress());
        holder.itemType.setText((listData.get(position).getState()));
        holder.itemSs.setText((listData.get(position).getRssi()) + " dB");

        int statusq = listData.get(position).getStateInt();

        switch (statusq) {
            case 0:
                holder.itemImage.setImageResource(R.drawable.conline);
                break;
            case 1:
                holder.itemImage.setImageResource(R.drawable.cconnect);
                break;
            case 2:
                holder.itemImage.setImageResource(R.drawable.cuconnected);
                break;
            default:
                holder.itemImage.setImageResource(R.drawable.cunavailable);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        TextView itemName;
        TextView itemAddress;
        TextView itemType;
        TextView itemSs;
        ImageView itemImage;
        ImageView rssImage;
    }
}
