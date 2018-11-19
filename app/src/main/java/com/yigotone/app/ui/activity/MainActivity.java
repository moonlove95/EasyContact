package com.yigotone.app.ui.activity;

import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.yigotone.app.R;
import com.yigotone.app.base.BaseActivity;
import com.yigotone.app.base.BasePresenter;
import com.yigotone.app.bean.ContactBean;
import com.yigotone.app.ui.fragment.DataFragment;
import com.yigotone.app.ui.fragment.MineFragment;
import com.yigotone.app.ui.home.HomeFragment;
import com.yigotone.app.ui.message.MessageFragment;
import com.yigotone.app.user.UserManager;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.ll_content) LinearLayout llContent;
    @BindView(R.id.ll_home_btn) LinearLayout llHomeBtn;
    @BindView(R.id.ll_message_btn) LinearLayout llMessageBtn;
    @BindView(R.id.ll_data_btn) LinearLayout llDataBtn;
    @BindView(R.id.ll_mine_btn) LinearLayout llMineBtn;

    private Fragment mContent;
    private HomeFragment homeFragment;
    private MessageFragment messageFragment;
    private DataFragment dataFragment;
    private MineFragment mineFragment;

    @Override
    protected int getLayoutId() {
        // QMUIStatusBarHelper.translucent(this);
        return R.layout.activity_main;
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    @Override
    public void initView() {
        setBottomButton(llHomeBtn);
        switchContentFragment(homeFragment == null ? homeFragment = new HomeFragment() : homeFragment);
        queryContactInfo();
    }

    private void queryContactInfo() {  // 遍历通讯录并存储
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{"display_name", "sort_key", "contact_id",
                        "data1"}, null, null, null);
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int Id = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            ContactBean bean = new ContactBean(name, number.replaceAll("\\s*", ""), Id);
            UserManager.getInstance().contactList.add(bean);
        }
        cursor.close();
    }

    @OnClick({R.id.ll_home_btn, R.id.ll_message_btn, R.id.ll_data_btn, R.id.ll_mine_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_home_btn:
                setBottomButton(llHomeBtn);
                switchContentFragment(homeFragment == null ? homeFragment = new HomeFragment() : homeFragment);
                break;
            case R.id.ll_message_btn:
                setBottomButton(llMessageBtn);
                switchContentFragment(messageFragment == null ? messageFragment = new MessageFragment() : messageFragment);
                break;
            case R.id.ll_data_btn:
                setBottomButton(llDataBtn);
                switchContentFragment(dataFragment == null ? dataFragment = new DataFragment() : dataFragment);
                break;
            case R.id.ll_mine_btn:
                setBottomButton(llMineBtn);
                switchContentFragment(mineFragment == null ? mineFragment = new MineFragment() : mineFragment);
                break;
        }
    }

    private void setBottomButton(ViewGroup llSelect) {
        llHomeBtn.setSelected(false);
        llMessageBtn.setSelected(false);
        llDataBtn.setSelected(false);
        llMineBtn.setSelected(false);

        llSelect.setSelected(true);
    }

    public void switchContentFragment(Fragment to) {
        if (mContent == null) {
            mContent = new Fragment();
        }
        if (mContent != to) {
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            if (!to.isAdded()) { // 先判断是否被add过
                // Can not perform this action after onSaveInstanceState ,solve :commit -> commitAllowingStateLoss
                transaction.hide(mContent).add(R.id.ll_content, to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(mContent).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
            mContent = to;
        }
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void onError(Throwable throwable) {

    }
}
