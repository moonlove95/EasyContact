package com.yigotone.app.ui.register;

import com.yigotone.app.base.BasePresenter;
import com.yigotone.app.base.BaseView;
import com.yigotone.app.bean.UserBean;

import java.util.Map;

/**
 * Created by ZMM on 2018/10/26 17:38.
 */
public class RegisterContract {
    public interface View extends BaseView {
        void codeObtained(UserBean bean);

        void onRegisterResult(UserBean bean);
    }

    public interface Presenter extends BasePresenter {
        void getRandomCode(String url);

        void register(String url, Map<String, String> map);
    }
}
