package com.xx.eventbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xx.eventbus.model.NewsModel;

import org.greenrobot.eventbus.EventBus;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivLike;
    TextView tvComment;
    TextView tvCommentCount;
    TextView tvLikeCount;

    NewsModel newsModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        newsModel = (NewsModel) getIntent().getSerializableExtra("newsModel");

        tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
        tvLikeCount = (TextView) findViewById(R.id.tvLikeCount);

        ivLike = (ImageView) findViewById(R.id.ivLike);
        ivLike.setOnClickListener(this);
        tvComment = (TextView) findViewById(R.id.tvComment);
        tvComment.setOnClickListener(this);

        tvCommentCount.setText(String.valueOf(newsModel.getCommentCount()));
        tvLikeCount.setText(String.valueOf(newsModel.getLikeCount()));
        ivLike.setImageResource("0".equals(newsModel.getIsLiked()) ? R.drawable.ic_favorite_no : R.drawable.ic_favorite_yes);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLike:
                String likeCount = tvLikeCount.getText().toString();
                if ("0".equals(newsModel.getIsLiked())) {
                    newsModel.setIsLiked("1");
                    newsModel.setLikeCount(Integer.parseInt(likeCount) + 1);
                    ivLike.setImageResource(R.drawable.ic_favorite_yes);
                    tvLikeCount.setText(String.valueOf(Integer.parseInt(likeCount) + 1));
                    EventBus.getDefault().post(newsModel);
                } else {
                    newsModel.setIsLiked("0");
                    newsModel.setLikeCount(Integer.parseInt(likeCount) - 1);
                    ivLike.setImageResource(R.drawable.ic_favorite_no);
                    tvLikeCount.setText(String.valueOf(Integer.parseInt(likeCount) - 1));
                    EventBus.getDefault().post(newsModel);
                }
                break;
            case R.id.tvComment:
                String commentCount = tvCommentCount.getText().toString();
                tvCommentCount.setText(String.valueOf(Integer.parseInt(commentCount) + 1));
                newsModel.setCommentCount(Integer.parseInt(commentCount) + 1);
                EventBus.getDefault().post(newsModel);
                break;
        }
    }
}
