package com.yigotone.app.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.library.utils.DensityUtil;
import com.ebupt.ebauth.biz.EbAuthDelegate;
import com.ebupt.ebauth.biz.auth.OnAuthLoginListener;
import com.orhanobut.logger.Logger;
import com.yigotone.app.R;
import com.yigotone.app.base.BaseFragment;
import com.yigotone.app.base.BasePresenter;
import com.yigotone.app.ui.call.CallActivity;
import com.yigotone.app.user.UserManager;
import com.yigotone.app.util.AuthUtils;
import com.yigotone.app.util.DataUtils;
import com.yigotone.app.util.Utils;
import com.yigotone.app.view.DigitsEditText;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ZMM on 2018/10/26 10:41.
 */
public class DialFragment extends BaseFragment  {
    @BindView(R.id.iv_keyboard) ImageView ivKeyboard;
    @BindView(R.id.iv_add) ImageView ivAdd;
    @BindView(R.id.dial_keyword) LinearLayout dialKeyword;
    @BindView(R.id.et_phone) DigitsEditText etPhone;

    private StringBuffer phoneStr = new StringBuffer();
    private TranslateAnimation hideAnim;
    private TranslateAnimation showAnim;
    private String phoneNumber;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_dial;
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        phoneNumber = UserManager.getInstance().userData.getMobile();
        initAnimation();
    }

    private void initAnimation() {
        hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
    }

    @Override
    public void onFinish() {


    }

    @Override
    public void onError(Throwable throwable) {

    }

    @OnClick({R.id.iv_keyboard, R.id.iv_add, R.id.iv_one, R.id.iv_two, R.id.iv_three, R.id.iv_four, R.id.iv_five, R.id.iv_six, R.id.iv_seven, R.id.iv_eight, R.id.iv_nine, R.id.iv_asterisk, R.id.iv_zero, R.id.iv_pound, R.id.iv_call, R.id.iv_collapse, R.id.iv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_add:
                showDialog();
                break;
            case R.id.iv_one:
                phoneStr.append(1);
                refreshEditText();
                break;
            case R.id.iv_two:
                phoneStr.append(2);
                refreshEditText();
                break;
            case R.id.iv_three:
                phoneStr.append(3);
                refreshEditText();
                break;
            case R.id.iv_four:
                phoneStr.append(4);
                refreshEditText();
                break;
            case R.id.iv_five:
                phoneStr.append(5);
                refreshEditText();
                break;
            case R.id.iv_six:
                phoneStr.append(6);
                refreshEditText();
                break;
            case R.id.iv_seven:
                phoneStr.append(7);
                refreshEditText();
                break;
            case R.id.iv_eight:
                phoneStr.append(8);
                refreshEditText();
                break;
            case R.id.iv_nine:
                phoneStr.append(9);
                refreshEditText();
                break;
            case R.id.iv_asterisk:
                phoneStr.append("*");
                refreshEditText();
                break;
            case R.id.iv_zero:
                phoneStr.append(0);
                refreshEditText();
                break;
            case R.id.iv_pound:
                phoneStr.append("#");
                refreshEditText();
                break;
            case R.id.iv_call:
                authenticate();
                break;
            case R.id.iv_collapse:
                collapseDialKeyboard();
                break;
            case R.id.iv_keyboard:
                showDialKeyboard();
                break;
            case R.id.iv_delete:
                if (phoneStr.length() > 0 && etPhone.getSelectionStart() > 0) {
                    phoneStr.deleteCharAt(etPhone.getSelectionStart() - 1);
                    refreshEditText();
                }
                break;
        }
    }

    private void authenticate() {
        if (Utils.CMAuthenticate(phoneNumber)) {
            startActivity(new Intent(mContext, CallActivity.class)
                    .putExtra("comefrom", "dial")
                    .putExtra("phonenum", Objects.requireNonNull(etPhone.getText()).toString().trim()));
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
                                .putExtra("phonenum", Objects.requireNonNull(etPhone.getText()).toString().trim()));
                    }
                }

                @Override
                public void ebAuthFailed(int code, String reason) {
                    Logger.d("ebAuthFailed: " + code + reason);
                }
            });

        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(mContext, R.layout.dialog_add_contact, null);
        TextView tv_create = view.findViewById(R.id.tv_create);
        TextView tv_exist = view.findViewById(R.id.tv_exist);

        dialog.setCancelable(true);
        dialog.setView(view);
        dialog.show();
        dialog.getWindow().setLayout(DensityUtil.dip2px(mContext, 266), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private void refreshEditText() {
        etPhone.setText(phoneStr);
        etPhone.setSelection(phoneStr.length());
    }

    private void collapseDialKeyboard() {
        hideAnim.setDuration(260);
        dialKeyword.startAnimation(hideAnim);
        dialKeyword.setVisibility(View.GONE);
    }

    private void showDialKeyboard() {
        showAnim.setDuration(260);
        dialKeyword.startAnimation(showAnim);
        dialKeyword.setVisibility(View.VISIBLE);
    }
}
