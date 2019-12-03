package com.chad.baserecyclerviewadapterhelper.adapter;

import com.chad.baserecyclerviewadapterhelper.R;
import com.chad.baserecyclerviewadapterhelper.entity.DelegateMultiEntity;
import com.chad.baserecyclerviewadapterhelper.entity.QuickMultipleEntity;
import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DelegateMultiAdapter extends BaseDelegateMultiAdapter<DelegateMultiEntity, BaseViewHolder> {

    public DelegateMultiAdapter() {
        super();
        // 方式一，使用匿名内部类，进行如下两步：
        // 第一步，设置代理
        setMultiTypeDelegate(new BaseMultiTypeDelegate<DelegateMultiEntity>() {
            @Override
            public int getItemType(@NotNull List<? extends DelegateMultiEntity> data, int position) {
                switch (position % 3) {
                    case 0:
                        return DelegateMultiEntity.TEXT;
                    case 1:
                        return DelegateMultiEntity.IMG;
                    case 2:
                        return DelegateMultiEntity.IMG_TEXT;
                    default:
                        break;
                }
                return 0;
            }
        });
        // 第二部，绑定 item 类型
        getMultiTypeDelegate()
                .registerItemType(DelegateMultiEntity.TEXT, R.layout.item_text_view)
                .registerItemType(DelegateMultiEntity.IMG, R.layout.item_image_view)
                .registerItemType(DelegateMultiEntity.IMG_TEXT, R.layout.item_img_text_view);


        //******************************************************************************************
        // 方式二，实现自己的代理类：
        setMultiTypeDelegate(new MyMultiTypeDelegate());
    }

    @Override
    protected void convert(@NotNull BaseViewHolder helper, @Nullable DelegateMultiEntity item) {
        switch (helper.getItemViewType()) {
            case QuickMultipleEntity.TEXT:
                helper.setText(R.id.tv, "CymChad " + helper.getAdapterPosition());
                break;
            case QuickMultipleEntity.IMG_TEXT:
                switch (helper.getLayoutPosition() % 2) {
                    case 0:
                        helper.setImageResource(R.id.iv, R.mipmap.animation_img1);
                        break;
                    case 1:
                        helper.setImageResource(R.id.iv, R.mipmap.animation_img2);
                        break;
                    default:
                        break;
                }
                helper.setText(R.id.tv, "ChayChan " + helper.getAdapterPosition());
                break;
            default:
                break;
        }
    }

    // 方式二：实现自己的代理类
    class MyMultiTypeDelegate extends BaseMultiTypeDelegate<DelegateMultiEntity> {

        public MyMultiTypeDelegate() {
            registerItemType(DelegateMultiEntity.TEXT, R.layout.item_text_view);
            registerItemType(DelegateMultiEntity.IMG, R.layout.item_image_view);
            registerItemType(DelegateMultiEntity.IMG_TEXT, R.layout.item_img_text_view);
        }

        @Override
        public int getItemType(@NotNull List<? extends DelegateMultiEntity> data, int position) {
            switch (position % 3) {
                case 0:
                    return DelegateMultiEntity.TEXT;
                case 1:
                    return DelegateMultiEntity.IMG;
                case 2:
                    return DelegateMultiEntity.IMG_TEXT;
                default:
                    break;
            }
            return 0;
        }
    }
}
