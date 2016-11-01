package com.xx.realm.activity;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.xx.realm.R;
import com.xx.realm.adapter.PersonAdapter;
import com.xx.realm.model.Person;
import com.xx.realm.utils.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;


public class PersonListAsyncActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    EasyRecyclerView recyclerView;

    PersonAdapter mAdapter;

    RealmResults<Person> persons;

    RealmAsyncTask deleteTask, updateTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list_async);

        ButterKnife.bind(this);
        setTitle("数据列表");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new PersonAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, (int) Util.convertDpToPixel(0.5f, this), (int)Util.convertDpToPixel(10, this), (int)Util.convertDpToPixel(10, this));//color & height & paddingLeft & paddingRight
        itemDecoration.setDrawLastItem(true);//sometimes you don't want draw the divider for the last item,default is true.
        itemDecoration.setDrawHeaderFooter(false);//whether draw divider for header and footer,default is false.
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapterWithProgress(mAdapter);
        queryAll();
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final Person item = mAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonListAsyncActivity.this);
                builder.setMessage("确定修改数据？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Realm mRealm = Realm.getDefaultInstance();
                        updateTask = mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Person person = realm.where(Person.class).equalTo("id", item.getId()).findFirst();
                                person.setName("神户牛肉" + position);
                                item.setName(person.getName());
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                mAdapter.notifyItemChanged(position);
                                Toast.makeText(PersonListAsyncActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                Toast.makeText(PersonListAsyncActivity.this, "更新失败---" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.create().show();
            }
        });
        mAdapter.setOnItemLongClickListener(new RecyclerArrayAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final int position) {
                final Person item = mAdapter.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonListAsyncActivity.this);
                builder.setMessage("确定删除数据？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Realm mRealm = Realm.getDefaultInstance();
                        deleteTask = mRealm.executeTransactionAsync(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                Person person = realm.where(Person.class).equalTo("id", item.getId()).findFirst();
                                person.deleteFromRealm();
                            }
                        }, new Realm.Transaction.OnSuccess() {
                            @Override
                            public void onSuccess() {
                                mAdapter.remove(position);
                                Toast.makeText(PersonListAsyncActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            }
                        }, new Realm.Transaction.OnError() {
                            @Override
                            public void onError(Throwable error) {
                                Toast.makeText(PersonListAsyncActivity.this, "删除失败---" + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    //异步查询所有
    public void queryAll() {
        final Realm mRealm = Realm.getDefaultInstance();
        persons = mRealm.where(Person.class).findAllAsync();
        persons.addChangeListener(new RealmChangeListener<RealmResults<Person>>() {
            @Override
            public void onChange(RealmResults<Person> element) {
                element = element.sort("id");
                List<Person> personList = mRealm.copyFromRealm(element);
                mAdapter.addAll(personList);
                recyclerView.scrollToPosition(personList.size() - 1);
                persons.removeChangeListeners();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deleteTask != null && !deleteTask.isCancelled()) {
            deleteTask.cancel();
        }
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }
    }

}
