package com.xx.eventbus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xx.eventbus.R;
import com.xx.eventbus.model.NewsModel;

import java.util.List;

/**
 * Created by wxx on 2016/11/6.
 */

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NewsModel> newsModelList;

    public NewsAdapter(List<NewsModel> newsModelList) {
        this.newsModelList = newsModelList;
    }
    public void refreshData(List<NewsModel> newsList) {
        this.newsModelList = newsList;
        notifyDataSetChanged();
    }

    public static interface OnRecyclerViewListener {
        void onItemClick(int position);
    }

    private OnRecyclerViewListener onRecyclerViewListener;

    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news_list, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NewsViewHolder viewHolder = (NewsViewHolder) holder;
        viewHolder.position = position;
        NewsModel newsModel = newsModelList.get(position);
        viewHolder.tvCommentCount.setText(String.valueOf(newsModel.getCommentCount()));
        viewHolder.tvLikeCount.setText(String.valueOf(newsModel.getLikeCount()));
        viewHolder.ivLike.setImageResource("0".equals(newsModel.getIsLiked()) ? R.drawable.ic_favorite_no : R.drawable.ic_favorite_yes);
    }

    @Override
    public int getItemCount() {
        return newsModelList == null ? 0 : newsModelList.size();
    }

    class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout rootview;
        TextView tvCommentCount;
        TextView tvLikeCount;
        ImageView ivLike;
        int position;

        public NewsViewHolder(View itemView) {
            super(itemView);
            rootview = (LinearLayout) itemView.findViewById(R.id.rootview);
            tvCommentCount = (TextView) itemView.findViewById(R.id.tvCommentCount);
            tvLikeCount = (TextView) itemView.findViewById(R.id.tvLikeCount);
            ivLike = (ImageView) itemView.findViewById(R.id.ivLike);
            rootview.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onRecyclerViewListener != null) {
                onRecyclerViewListener.onItemClick(position);
            }
        }
    }

}
