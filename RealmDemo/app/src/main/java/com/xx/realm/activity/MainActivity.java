package com.xx.realm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xx.realm.R;
import com.xx.realm.model.Person;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmAsyncTask;

public class MainActivity extends AppCompatActivity {

    RealmAsyncTask addTask;
    List<Person> persons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tvAdd)
    void add() {
        Realm mRealm = Realm.getDefaultInstance();

        // Create a new object
        mRealm.beginTransaction();
        for (int i = 0; i < 100; i++) {
            Person person = mRealm.createObject(Person.class, i);
            person.setName("shenhuniurou---" + i);
            person.setEmail("shenhuniurou@gmail.com---" + i);
        }
        mRealm.commitTransaction();
        Toast.makeText(this, "成功添加数据", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tvAsyncAdd)
    void asyncAdd() {
        persons = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Person person = new Person();
            person.setId(i + 100);
            person.setName("神户牛肉---" + i);
            person.setEmail("shenhuniurou@gmail.com---" + i);
            persons.add(person);
        }
        addPerson(persons);
    }

    private void addPerson(final List<Person> persons) {
        Realm mRealm = Realm.getDefaultInstance();

        addTask =  mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (Person person: persons) {
                    realm.copyToRealm(person);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "增加数据成功", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                String message = error.getMessage();
                Toast.makeText(MainActivity.this, "增加数据失败---" + message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @OnClick(R.id.tvSelectUpdateDelete)
    void selectUpdateDelete() {
        startActivity(new Intent(this, PersonListActivity.class));
    }

    @OnClick(R.id.tvAsynctvSelectUpdateDelete)
    void asynctvSelectUpdateDelete() {
        startActivity(new Intent(this, PersonListAsyncActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addTask != null && !addTask.isCancelled()) {
            addTask.cancel();
        }
    }

}
