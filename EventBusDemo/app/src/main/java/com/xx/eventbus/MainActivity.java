package com.xx.eventbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.xx.eventbus.adapter.DividerItemDecoration;
import com.xx.eventbus.adapter.NewsAdapter;
import com.xx.eventbus.model.NewsModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NewsAdapter.OnRecyclerViewListener {

    private RecyclerView mRecyclerView;
    private NewsAdapter mAdapter;
    List<NewsModel> newsList;
    int clickPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        newsList = new ArrayList<>();
        mAdapter = new NewsAdapter(newsList);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnRecyclerViewListener(this);
        initData();
        EventBus.getDefault().register(this);
    }

    private void initData() {
        List<NewsModel> newsModelList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            NewsModel newsModel = new NewsModel();
            newsModel.setId(String.valueOf(i + 1));
            newsModel.setCommentCount(100 + i);
            newsModel.setLikeCount(66 + i);
            newsModel.setIsLiked(i % 2 == 0 ? "1" : "0");
            newsModelList.add(newsModel);
        }
        newsList.addAll(newsModelList);
        mAdapter.refreshData(newsList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(NewsModel newsModel) {
        newsList.set(clickPosition, newsModel);
        mAdapter.notifyItemChanged(clickPosition);
    }

    @Override
    public void onItemClick(int position) {
        clickPosition = position;
        NewsModel newsModel = newsList.get(position);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("newsModel", newsModel);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
