package com.yigotone.app.ui.home;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.library.utils.DensityUtil;
import com.android.library.utils.U;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ebupt.ebauth.biz.EbAuthDelegate;
import com.ebupt.ebauth.biz.auth.OnAuthLoginListener;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yigotone.app.R;
import com.yigotone.app.api.UrlUtil;
import com.yigotone.app.base.BaseFragment;
import com.yigotone.app.bean.CallBean;
import com.yigotone.app.bean.CodeBean;
import com.yigotone.app.ui.activity.DialActivity;
import com.yigotone.app.ui.activity.MainActivity;
import com.yigotone.app.ui.call.CallActivity;
import com.yigotone.app.ui.call.CallDetailActivity;
import com.yigotone.app.ui.disturb.NoDisturbActivity;
import com.yigotone.app.ui.packages.PackageListActivity;
import com.yigotone.app.user.UserManager;
import com.yigotone.app.util.AuthUtils;
import com.yigotone.app.util.DataUtils;
import com.yigotone.app.util.Utils;
import com.yigotone.app.view.TriangleDrawable;
import com.yigotone.app.view.statusLayoutView.StatusLayoutManager;
import com.zyyoona7.popup.EasyPopup;
import com.zyyoona7.popup.XGravity;
import com.zyyoona7.popup.YGravity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ZMM on 2018/10/23 15:46.
 */
public class HomeFragment extends BaseFragment<HomeContract.Presenter> implements HomeContract.View {
    @BindView(R.id.tv_phone) TextView tvPhone;
    @BindView(R.id.btn_take_over) Button btnTakeOver;
    @BindView(R.id.tv_balance) TextView tvBalance;
    @BindView(R.id.iv_add) ImageView ivAdd;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.iv_dial) ImageView ivDial;
    @BindView(R.id.iv_disturb) ImageView ivDisturb;
    @BindView(R.id.ll_take_over) LinearLayout llTakeOver;
    @BindView(R.id.refresh_layout) SwipeRefreshLayout refreshLayout;

    private EasyPopup popup;
    private String mobileStatus;   //1未托管，2托管中
    private StatusLayoutManager statusLayoutManager;
    private BaseQuickAdapter<CallBean.DataBean, BaseViewHolder> mAdapter;
    private boolean isEdit = false; // 编辑模式
    private boolean isSelectAll = false; // 全选模式
    private MainActivity mainActivity;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected HomeContract.Presenter initPresenter() {
        return new HomePresenter(this);
    }


    @Override
    public void initView(View view, Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        tvPhone.setText(Utils.hidePhoneNumber(UserManager.getInstance().userData.getMobile()));
        initRecyclerView();
        getCallRecord(true);
        mobileStatus = UserManager.getInstance().userData.getMobileStatus();
        tvBalance.setText(UserManager.getInstance().userData.getTalkTimeText());

        // 注册广播监听飞行模式改变
        //  mContext.registerReceiver(airplaneModeOn, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
    }

    private void getCallRecord(boolean isLoadingLayout) {
        if (isLoadingLayout) {
            statusLayoutManager.showLoadingLayout();
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", UserManager.getInstance().userData.getUid());
        map.put("token", UserManager.getInstance().userData.getToken());
        map.put("page", pageIndex);
        map.put("count", pageSize);
        map.put("type", 1);
        presenter.getCallRecords(map);
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        statusLayoutManager = new StatusLayoutManager.Builder(refreshLayout).setOnStatusClickListener(view -> {
            getCallRecord(true);
        }).build();
        statusLayoutManager.showLoadingLayout();

        recyclerView.setAdapter(mAdapter = new BaseQuickAdapter<CallBean.DataBean, BaseViewHolder>(R.layout.item_call_record) {
            @Override
            protected void convert(BaseViewHolder helper, CallBean.DataBean item) {
                String number = item.getCallNum() > 1 ? "(" + item.getCallNum() + ")" : "";
                helper.setText(R.id.tv_phone, item.getTargetName() + number);
                helper.setText(R.id.tv_time, Utils.getShortTime(Long.parseLong(item.getCreateAt())));
                helper.getView(R.id.iv_select).setSelected(item.isSelect);
                helper.setGone(R.id.iv_select, isEdit);
                helper.addOnClickListener(R.id.iv_select);
                helper.addOnClickListener(R.id.iv_detail);
            }
        });
        //   mAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mAdapter.setOnLoadMoreListener(() -> {
            pageIndex++;
            getCallRecord(false);
        }, recyclerView);
        refreshLayout.setOnRefreshListener(() -> {
            pageIndex = 1;
            getCallRecord(false);
        });
        mAdapter.setOnItemClickListener((adapter, v, p) -> {
            CallBean.DataBean item = (CallBean.DataBean) adapter.getItem(p);
            authenticate(item.getMobile());
        });
        mAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            CallBean.DataBean item = (CallBean.DataBean) adapter.getItem(position);
            isEdit = true;
            item.isSelect = true;
            mAdapter.notifyDataSetChanged();
            mainActivity.rlDelete.setVisibility(View.VISIBLE);
            return true;
        });
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            CallBean.DataBean item = (CallBean.DataBean) adapter.getItem(position);
            switch (view.getId()) {
                case R.id.iv_select:
                    item.isSelect = !item.isSelect;
                    mAdapter.notifyDataSetChanged();
                    break;
                case R.id.iv_detail:
                    startActivity(new Intent(mContext, CallDetailActivity.class).putExtra("data", item));
                    break;
            }
        });
    }

    @OnClick({R.id.btn_take_over, R.id.iv_add, R.id.iv_dial, R.id.ll_take_over})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_take_over:
            case R.id.ll_take_over:
                takeOver();
                break;
            case R.id.iv_add:
                showMenu();
                break;
            case R.id.iv_dial:
                new RxPermissions(this).request(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS).subscribe(granted -> {
                    if (granted) {
                        startActivity(new Intent(mContext, DialActivity.class));
                    } else {
                        U.showToast("没有获取到权限");
                    }
                });
                break;
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(mContext, R.layout.dialog_package_buy, null);
        TextView tv_tip = view.findViewById(R.id.tv_tip);
        TextView tv_cancel = view.findViewById(R.id.tv_cancel);
        TextView tv_sure = view.findViewById(R.id.tv_sure);
        tv_cancel.setText("取消");
        tv_sure.setText("确认");
        tv_tip.setText("确认删除所选通话记录？");
        tv_cancel.setOnClickListener(v -> dialog.dismiss());
        tv_sure.setOnClickListener(v -> {
            dialog.dismiss();
            deleteCallRecord();
        });

        dialog.setCancelable(true);
        dialog.setView(view);
        dialog.show();
        dialog.getWindow().setLayout(DensityUtil.dip2px(mContext, 340), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void deleteCallRecord() {
        StringBuilder sb = new StringBuilder();
        for (CallBean.DataBean bean : mAdapter.getData()) {
            if (bean.isSelect) {
                sb.append(bean.getMobile()).append(",");
            }
        }
        if (TextUtils.isEmpty(sb.toString())) {
            U.showToast("请选择至少一项删除");
            return;
        }
        showLoadingDialog("正在删除");
        Map<String, Object> map = new HashMap<>();
        map.put("uid", UserManager.getInstance().userData.getUid());
        map.put("token", UserManager.getInstance().userData.getToken());
        map.put("type", isSelectAll ? 1 : 2);
        map.put("targetMobile", sb.toString().substring(0, sb.toString().length() - 1));
        presenter.deleteCallRecord(UrlUtil.DELETE_CALL_RECORD, map);
    }

    private void takeOver() {
        showLoadingDialog("");
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", mobileStatus);
        map.put("mobile", UserManager.getInstance().userData.getMobile());
        presenter.updateMobileStatus(map);
    }

    private void showMenu() {
        popup = EasyPopup.create()
                .setContext(mContext)
                .setContentView(R.layout.layout_right_pop)
                //   .setAnimationStyle(R.style.RightTop2PopAnim)
                .setOnViewListener((view, basePopup) -> {
                    View arrowView = view.findViewById(R.id.v_arrow);
                    arrowView.setBackground(new TriangleDrawable(TriangleDrawable.TOP, Color.parseColor("#FFFFFF")));
                    view.findViewById(R.id.tv_no_disturb).setOnClickListener(v -> {
                        startActivity(new Intent(mContext, NoDisturbActivity.class));
                        popup.dismiss();
                    });
                    view.findViewById(R.id.tv_subscribe).setOnClickListener(v -> {
                        startActivity(new Intent(mContext, PackageListActivity.class));
                        popup.dismiss();
                    });
                })
                .setFocusAndOutsideEnable(true)
//                .setBackgroundDimEnable(true)
//                .setDimValue(0.5f)
//                .setDimColor(Color.RED)
//                .setDimView(mTitleBar)
                .apply();
        int offsetX = DensityUtil.dpToPx(mContext, 20) - ivAdd.getWidth() / 2;
        // int offsetY = (mTitleBar.getHeight() - ivAdd.getHeight()) / 2;
        popup.showAtAnchorView(ivAdd, YGravity.BELOW, XGravity.ALIGN_RIGHT, offsetX, 5);
    }

    @Override
    public void onFinish() {
        refreshLayout.setRefreshing(false);
        statusLayoutManager.showSuccessLayout();
    }

    @Override
    public void onError(Throwable throwable) {
        dismissLoadingDialog();
        refreshLayout.setRefreshing(false);
        U.showToast("网络错误");
    }

    @Override
    public void onMobileStatusResult(String status) {
        dismissLoadingDialog();
        if (!TextUtils.isEmpty(status)) {
            refreshMobileStatus();
            mobileStatus = mobileStatus.equals("1") ? "2" : "1";
            U.showToast(mobileStatus.equals("1") ? "取消托管成功" : "设置托管成功");
        }
    }

    @Override
    public void callRecordsResult(List<CallBean.DataBean> data) {
        if (data.size() > 0) {
            if (pageIndex == 1) {
                mAdapter.setNewData(data);
            } else {
                mAdapter.addData(data);
            }
            if (data.size() < pageSize) {
                mAdapter.loadMoreEnd(true);
            } else {
                mAdapter.loadMoreComplete();
            }
        } else {
            if (pageIndex == 1) {
                statusLayoutManager.showEmptyLayout();
            } else {
                mAdapter.loadMoreEnd();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String event) {
        Logger.d("eventBus: " + event);
        switch (event) {
            case "mobileNotShutDown":
                showDialogTip();
                break;
        }
    }

    private void showDialogTip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(mContext, R.layout.dialog_warning, null);
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_content = view.findViewById(R.id.tv_content);
        Button btn_ok = view.findViewById(R.id.btn_ok);
        tv_title.setText("提示");
        tv_content.setText("您的号码" + UserManager.getInstance().userData.getMobile() + "还处于开机状态，请设置飞行模式或者拔卡后再进行接管。");
        btn_ok.setText("知道了");
        btn_ok.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.setView(view);
        dialog.show();
        dialog.getWindow().setLayout(DensityUtil.dip2px(mContext, 330), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        // refreshMobileStatus();
        refreshDisturbStatus();
        getCallRecord(false);
    }

    private void refreshDisturbStatus() {
        int disturb = UserManager.getInstance().userData.getDisturb();
        ivDisturb.setVisibility(disturb == 2 ? View.VISIBLE : View.GONE);
    }

    public void refreshMobileStatus() {
        // 实时刷手机托管状态
        Map<String, Object> map = new HashMap<>();
        map.put("mobile", UserManager.getInstance().userData.getMobile());
        presenter.getMobileStatus(UrlUtil.GET_MOBILE_STATUS, map);
    }

    @Override
    public void refreshMobileStatus(String mobileStatus) {
        UserManager.getInstance().userData.setMobileStatus(this.mobileStatus = mobileStatus);
        btnTakeOver.setVisibility(mobileStatus.equals("1") ? View.VISIBLE : View.GONE);
        llTakeOver.setVisibility(mobileStatus.equals("2") ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecyclerViewError(Throwable throwable) {
        statusLayoutManager.showErrorLayout();
    }

    @Override
    public void onDeleteResult(CodeBean bean) {
        dismissLoadingDialog();
        if (bean.getStatus() == 0) {
            U.showToast("删除成功");
            pageIndex = 1;
            getCallRecord(false);
        } else {
            U.showToast("删除失败");
        }
    }

    private void authenticate(String phoneNumber) {
        if (Utils.CMAuthenticate(phoneNumber)) {
            startActivity(new Intent(mContext, CallActivity.class)
                    .putExtra("comefrom", "dial")
                    .putExtra("phonenum", phoneNumber));
        } else {
            EbAuthDelegate.AuthloginByVfc(UserManager.getInstance().userData.getMobile(), null, new OnAuthLoginListener() {
                @Override
                public void ebAuthOk(String authcode, String deadline) {
                    Logger.d("authcode " + authcode + deadline);
                    DataUtils.saveDeadline(phoneNumber, deadline, mContext);
                    if (AuthUtils.isDeadlineAvailable(deadline)) {
//                        EbLoginDelegate.login(phoneNumber, "ebupt");
//                        Logger.d("login");

                        startActivity(new Intent(mContext, CallActivity.class)
                                .putExtra("comefrom", "dial")
                                .putExtra("phonenum", phoneNumber));
                    }
                }

                @Override
                public void ebAuthFailed(int code, String reason) {
                    Logger.d("ebAuthFailed: " + code + reason);
                }
            });
        }
    }

    public void setActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        mainActivity.rlDelete.setOnClickListener(v -> {
        });
        mainActivity.tvCancel.setOnClickListener(v -> {
            mainActivity.rlDelete.setVisibility(View.GONE);
            isEdit = false;
            isSelectAll = false;
            for (CallBean.DataBean bean : mAdapter.getData()) {
                bean.isSelect = false;
            }
            mAdapter.notifyDataSetChanged();
        });
        mainActivity.tvDelete.setOnClickListener(v -> {
            deleteDialog();
        });
        mainActivity.tvSelectAll.setOnClickListener(v -> {
            isSelectAll = !isSelectAll;
            mainActivity.tvSelectAll.setText(isSelectAll ? "全不选" : "全选");
            for (CallBean.DataBean bean : mAdapter.getData()) {
                bean.isSelect = isSelectAll;
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    private BroadcastReceiver airplaneModeOn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {//飞行模式状态改变
                refreshMobileStatus();
            }
        }
    };
}
