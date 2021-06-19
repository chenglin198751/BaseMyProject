package com.wcl.test.test;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TestUserInfoViewModel extends ViewModel {
    private MutableLiveData<TestUserInfo> viewModel;

    public MutableLiveData<TestUserInfo> get() {
        if (viewModel == null) {
            viewModel = new MutableLiveData<>();
        }
        return viewModel;
    }


}

//  --使用方法
//  1、设置监听：
//    private void initObserver() {
//        final Observer<TestUserInfo> observer = new Observer<TestUserInfo>() {
//            @Override
//            public void onChanged(@Nullable final TestUserInfo userInfo) {
//                Log.d("tag_2", "onChanged: " + userInfo.uname);
//
//            }
//        };
//
//        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
//        viewModelProvider.get(TestUserInfoViewModel.class).get().observe(this, observer);
//    }

//  2、写入值：
//  TestUserInfo userInfo = new TestUserInfo();
//  userInfo.uname = "多想再见你";
//  ViewModelProvider viewModelProvider = new ViewModelProvider(getContext());
//  viewModelProvider.get(TestUserInfoViewModel.class).get().setValue(userInfo);