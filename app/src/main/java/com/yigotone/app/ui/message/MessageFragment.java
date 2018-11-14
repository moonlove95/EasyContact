package com.yigotone.app.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.library.utils.U;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.yigotone.app.R;
import com.yigotone.app.api.UrlUtil;
import com.yigotone.app.base.BaseFragment;
import com.yigotone.app.user.UserManager;
import com.yigotone.app.view.statusLayoutView.StatusLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ZMM on 2018/10/23 15:46.
 */
public class MessageFragment extends BaseFragment<MessageContract.Presenter> implements MessageContract.View {
    @BindView(R.id.tv_edit) TextView tvEdit;
    @BindView(R.id.iv_add) ImageView ivAdd;
    @BindView(R.id.et_search) EditText etSearch;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;
    private StatusLayoutManager statusLayoutManager;
    private BaseQuickAdapter<String, BaseViewHolder> mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_message;
    }

    @Override
    protected MessagePresenter initPresenter() {
        return new MessagePresenter(this);
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {
        initRecyclerView();
        getMessageList(true);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 17; i++) {
            strings.add("消息 " + i);
        }
        recyclerView.setAdapter(mAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_message, strings) {
            @Override
            protected void convert(BaseViewHolder helper, String item) {
                helper.setText(R.id.tv_title, item);
                helper.setOnClickListener(R.id.right, view -> U.showToast(helper.getLayoutPosition() + ""));
                helper.setOnClickListener(R.id.content, view -> startActivity(new Intent(mContext, MessageDetailActivity.class)));
            }
        });
//        mAdapter.setOnItemClickListener((adapter, view1, position) -> {
//            startActivity(new Intent(mContext, MessageDetailActivity.class));
//            U.showToast("ddddd");
//        });
    }

    private void getMessageList(boolean isLoadingLayout) {
        if (isLoadingLayout) {
            statusLayoutManager.showLoadingLayout();
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", UserManager.getInstance().userData.getUid());
        map.put("token", UserManager.getInstance().userData.getToken());
        map.put("page", pageIndex);
        map.put("count", pageSize);
        presenter.getMessageList(UrlUtil.GET_MESSAGE_LIST, map, "getMessage");
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        statusLayoutManager = new StatusLayoutManager.Builder(recyclerView).build();
        //   statusLayoutManager.showLoadingLayout();
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @OnClick({R.id.tv_edit, R.id.iv_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_edit:
                break;
            case R.id.iv_add:
                startActivity(new Intent(mContext, NewMessageActivity.class));
                break;
        }
    }

    @Override
    public void onResult(Object result, String message) {

    }
}
