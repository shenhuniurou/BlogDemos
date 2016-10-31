package com.xx.realm.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

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
import io.realm.RealmResults;

public class PersonListActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    EasyRecyclerView recyclerView;

    PersonAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list);
        ButterKnife.bind(this);
        setTitle("查詢列表");

        List<Person> persons = queryAll();
        mAdapter = new PersonAdapter(this);
        mAdapter.addAll(persons);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        DividerDecoration itemDecoration = new DividerDecoration(Color.GRAY, (int)Util.convertDpToPixel(0.5f, this), (int)Util.convertDpToPixel(10, this), (int)Util.convertDpToPixel(10, this));//color & height & paddingLeft & paddingRight
        itemDecoration.setDrawLastItem(true);//sometimes you don't want draw the divider for the last item,default is true.
        itemDecoration.setDrawHeaderFooter(false);//whether draw divider for header and footer,default is false.
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setAdapterWithProgress(mAdapter);
        recyclerView.scrollToPosition(persons.size() - 1);
        mAdapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                final Person item = mAdapter.getItem(position);
                final Realm  mRealm = Realm.getDefaultInstance();
                final Person person = mRealm.where(Person.class).equalTo("id", item.getId()).findFirst();
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonListActivity.this);
                builder.setMessage("确定修改数据？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                person.setName("神户牛肉" + position);
                                item.setName(person.getName());
                                mAdapter.notifyItemChanged(position);
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
                Person item = mAdapter.getItem(position);
                final Realm  mRealm = Realm.getDefaultInstance();
                final Person person = mRealm.where(Person.class).equalTo("id", item.getId()).findFirst();
                AlertDialog.Builder builder = new AlertDialog.Builder(PersonListActivity.this);
                builder.setMessage("确定删除数据？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.remove(position);
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                person.deleteFromRealm();
                            }
                        });
                    }
                });
                builder.create().show();
                return true;
            }
        });
    }

    //查询所有
    public List<Person> queryAll() {
        Realm mRealm = Realm.getDefaultInstance();

        RealmResults<Person> persons = mRealm.where(Person.class).findAll();

        // 排序
        persons = persons.sort("id");

        return mRealm.copyFromRealm(persons);
    }

}
