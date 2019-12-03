package com.chad.baserecyclerviewadapterhelper;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.baserecyclerviewadapterhelper.adapter.DelegateMultiAdapter;
import com.chad.baserecyclerviewadapterhelper.base.BaseActivity;
import com.chad.baserecyclerviewadapterhelper.data.DataServer;
import com.chad.baserecyclerviewadapterhelper.entity.DelegateMultiEntity;
import java.util.List;

public class MultiItemDelegateUseActivity extends BaseActivity {

    private DelegateMultiAdapter adapter = new DelegateMultiAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_item_use);

        setTitle("BaseMultiItemQuickAdapter");
        setBackBtn();

        initRv();
        setData();
    }

    private void initRv() {
        RecyclerView mRecyclerView = findViewById(R.id.rv_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);
    }

    private void setData() {
        final List<DelegateMultiEntity> data = DataServer.getDelegateMultiItemData();
        adapter.setNewData(data);
    }
}
