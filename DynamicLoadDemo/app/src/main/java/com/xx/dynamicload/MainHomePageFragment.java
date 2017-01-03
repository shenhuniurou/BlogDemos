package com.xx.dynamicload;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainHomePageFragment extends Fragment {

    @BindView(R.id.gridview)
    MyGridView gridview;
    MyAdapter mAdapter;
    List<ItemModel> mDatas;

    public MainHomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main_home_page, container, false);
        ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {
        mAdapter = new MyAdapter(getActivity());
        mDatas = new ArrayList<>();
        setDatas();
        mAdapter.setDatas(mDatas);
        gridview.setAdapter(mAdapter);
    }

    private void setDatas() {
        if (mDatas != null) {
            String pluginPath = Environment.getExternalStorageDirectory().toString() + "/dynamicload/download/skin-plugin.apk";
            if (Util.getPluginPackagename(getActivity(), pluginPath) == null) {
                //如果没有获取到插件的包名，则使用原有的资源，否则使用插件中的资源

                mDatas.add(new ItemModel("item1", R.drawable.icon_weiquan));
                mDatas.add(new ItemModel("item2", R.drawable.icon_bangfu));
                mDatas.add(new ItemModel("item3", R.drawable.icon_zhigong));
                mDatas.add(new ItemModel("item4", R.drawable.icon_jiceng));
                mDatas.add(new ItemModel("item5", R.drawable.icon_laodong));
                mDatas.add(new ItemModel("item6", R.drawable.icon_xuanchuan));
                mDatas.add(new ItemModel("item7", R.drawable.icon_jingsai));
                mDatas.add(new ItemModel("item8", R.drawable.icon_nvgong));
                mDatas.add(new ItemModel("item9", R.drawable.icon_huzhu));
                mDatas.add(new ItemModel("item10", R.drawable.icon_tuanpiao));
            } else {
                mDatas.add(new ItemModel("item1", "icon_weiquan"));
                mDatas.add(new ItemModel("item2", "icon_bangfu"));
                mDatas.add(new ItemModel("item3", "icon_zhigong"));
                mDatas.add(new ItemModel("item4", "icon_jiceng"));
                mDatas.add(new ItemModel("item5", "icon_laodong"));
                mDatas.add(new ItemModel("item6", "icon_xuanchuan"));
                mDatas.add(new ItemModel("item7", "icon_jingsai"));
                mDatas.add(new ItemModel("item8", "icon_nvgong"));
                mDatas.add(new ItemModel("item9", "icon_huzhu"));
                mDatas.add(new ItemModel("item10", "icon_tuanpiao"));
            }
        }
    }

}
