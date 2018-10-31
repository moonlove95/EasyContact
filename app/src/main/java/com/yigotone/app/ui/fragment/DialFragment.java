package com.yigotone.app.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yigotone.app.R;
import com.yigotone.app.base.BaseFragment;
import com.yigotone.app.base.BasePresenter;
import com.yigotone.app.view.DigitsEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by ZMM on 2018/10/26 10:41.
 */
public class DialFragment extends BaseFragment {
    @BindView(R.id.iv_keyboard) ImageView ivKeyboard;
    @BindView(R.id.iv_add) ImageView ivAdd;
    @BindView(R.id.dial_keyword) LinearLayout dialKeyword;
    @BindView(R.id.et_phone) DigitsEditText etPhone;

    private StringBuffer phoneStr = new StringBuffer();

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

    private void refreshEditText() {
        etPhone.setText(phoneStr);
        etPhone.setSelection(phoneStr.length());
    }

    private void collapseDialKeyboard() {
        TranslateAnimation hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f);
        hideAnim.setDuration(360);
        dialKeyword.startAnimation(hideAnim);
        dialKeyword.setVisibility(View.GONE);
    }

    private void showDialKeyboard() {
        TranslateAnimation showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        showAnim.setDuration(360);
        dialKeyword.startAnimation(showAnim);
        dialKeyword.setVisibility(View.VISIBLE);
    }
}