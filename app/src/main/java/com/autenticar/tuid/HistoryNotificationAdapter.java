package com.autenticar.tuid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.autenticar.tuid.model.Notification;
import com.autenticar.tuid.utils.SQLHelper;

import java.util.List;

public class HistoryNotificationAdapter extends BaseAdapter {
    private Context context;
    private List<Notification> item;

    public HistoryNotificationAdapter(Context context, List<Notification> item){
        this.context = context;
        this.item=item;
    }
    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HistoryNotificationAdapter.ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new HistoryNotificationAdapter.ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.content_history_notification, parent, false);
            viewHolder.title_noti = (TextView) convertView.findViewById(R.id.title_notification);
            viewHolder.messager_noti = (TextView) convertView.findViewById(R.id.message_noti);
            viewHolder.date_noti = (TextView) convertView.findViewById(R.id.date_noti);
            viewHolder.cancel_noti= (ImageView) convertView.findViewById(R.id.btn_return);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (HistoryNotificationAdapter.ViewHolder) convertView.getTag();
        }
        if(!item.get(position).getId_noti().equals("")){
            viewHolder.title_noti.setText(item.get(position).getTitle_noti());
            viewHolder.messager_noti.setText(item.get(position).getMessager_noti());
            viewHolder.date_noti.setText(item.get(position).getDate_noti());
            viewHolder.cancel_noti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new SQLHelper(context).deleteNotification(item.get(position));
                    notifyDataSetChanged();
                }
            });

        }
        return convertView;

    }
    public void clear(){item.clear();}
    public static class ViewHolder{
        TextView title_noti;
        TextView messager_noti;
        TextView date_noti;
        ImageView cancel_noti;
    }
}
