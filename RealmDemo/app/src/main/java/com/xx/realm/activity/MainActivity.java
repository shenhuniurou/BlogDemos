package com.xx.realm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.xx.realm.R;
import com.xx.realm.model.Person;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.tvAdd)
    void add() {
        Realm realm = Realm.getDefaultInstance();

        // Create a new object
        realm.beginTransaction();
        for (int i = 0; i < 100; i++) {
            Person person = realm.createObject(Person.class, i);
            person.setName("shenhuniurou---" + i);
            person.setEmail("shenhuniurou@gmail.com---" + i);
        }
        realm.commitTransaction();
        Toast.makeText(this, "成功添加数据", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.tvQuery)
    void query() {
        startActivity(new Intent(this, PersonListActivity.class));
    }

    @OnClick(R.id.tvDelete)
    void delete() {
        startActivity(new Intent(this, PersonListActivity.class));
    }

    @OnClick(R.id.tvUpdate)
    void update() {
        startActivity(new Intent(this, PersonListActivity.class));
    }

}
