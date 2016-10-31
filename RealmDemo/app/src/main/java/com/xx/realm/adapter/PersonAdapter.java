package com.xx.realm.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.xx.realm.model.Person;

/**
 * Created by wxx on 2016/10/23.
 */

public class PersonAdapter extends RecyclerArrayAdapter<Person> {

    public PersonAdapter(Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new PersonViewHolder(parent);
    }

}