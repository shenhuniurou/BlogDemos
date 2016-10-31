package com.xx.realm.adapter;

import android.view.ViewGroup;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.xx.realm.R;
import com.xx.realm.model.Person;

/**
 * Created by wxx on 2016/10/23.
 */

public class PersonViewHolder extends BaseViewHolder<Person> {

    private TextView mTv_name;
    private TextView mTv_email;

    public PersonViewHolder(ViewGroup parent) {
        super(parent, R.layout.item_person);
        mTv_name = $(R.id.person_name);
        mTv_email = $(R.id.person_email);
    }

    @Override
    public void setData(final Person person){
        mTv_name.setText(person.getName());
        mTv_email.setText(person.getEmail());
    }

}
