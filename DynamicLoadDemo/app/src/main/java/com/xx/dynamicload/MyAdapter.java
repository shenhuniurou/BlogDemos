package com.xx.dynamicload;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;

/**
 * Created by wxx
 * on 2016/12/9.
 */

public class MyAdapter extends BaseAdapter {

    Context mContext;
    List<ItemModel> modelList;
    String pluginPath;

    public MyAdapter(Context context) {
        mContext = context;
        pluginPath = Environment.getExternalStorageDirectory().toString() + "/dynamicload/download/skin-plugin.apk";
    }

    public void setDatas(List<ItemModel> datas) {
        this.modelList = datas;
    }


    @Override public int getCount() {
        return modelList == null ?  0 : modelList.size();
    }


    @Override public ItemModel getItem(int position) {
        return modelList.get(position);
    }


    @Override public long getItemId(int position) {
        return position;
    }


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemModel item = getItem(position);

        if (item.getIconResId() != 0) {
            holder.imageView.setImageResource(item.getIconResId());
        }

        if (item.getIconResName() != null) {
            Drawable drawable = Util.getPluginResources(mContext, pluginPath).getDrawable(Util.getResId(mContext, pluginPath, Util.getPluginPackagename(mContext, pluginPath), item.getIconResName()));
            holder.imageView.setImageDrawable(drawable);
        }

        holder.textView.setText(item.getTitle());
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.imageView) ImageView imageView;

        @BindView(R.id.textView) TextView textView;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
