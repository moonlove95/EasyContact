package com.yigotone.app.ui.home;

import android.annotation.SuppressLint;

import com.yigotone.app.api.Api;
import com.yigotone.app.api.UrlUtil;
import com.yigotone.app.base.BasePresenterImpl;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ZMM on 2018/10/26 17:40.
 */
public class HomePresenter extends BasePresenterImpl<HomeContract.View> implements HomeContract.Presenter {

    public HomePresenter(HomeContract.View view) {
        super(view);
    }

    @SuppressLint("CheckResult")
    @Override
    public void getPackageList() {
        Api.getInstance().getPackageList(UrlUtil.GET_PACKAGE_LIST)
                .subscribeOn(Schedulers.io())
                .map(bean -> bean)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bean -> {

                }, throwable -> view.onError(throwable));
    }
}