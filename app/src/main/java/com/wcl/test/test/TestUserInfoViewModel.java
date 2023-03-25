package com.wcl.test.test;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import com.wcl.test.main.MainFirstFragment;

import java.util.ArrayList;
import java.util.List;

//public class TestUserInfoViewModel extends ViewModel {
//    private MutableLiveData<TestUserInfo> viewModel;
//
//
//    public MutableLiveData<TestUserInfo> get() {
//        if (viewModel == null) {
//            viewModel = new MutableLiveData<>();
//        }
//        return viewModel;
//    }
//
//
//}

public class TestUserInfoViewModel extends ViewModel {

    // Expose screen UI state
    private MutableLiveData<List<TestUserInfo>> users;



    public MutableLiveData<List<TestUserInfo>> getUsers() {
        if (users == null) {
            users = new MutableLiveData<List<TestUserInfo>>();
        }

        return users;
    }

    //ViewModel被销毁时被调用
    @Override
    protected void onCleared() {
        super.onCleared();
    }
}

//  --使用方法
//  注意：new ViewModelProvider(ViewModelStoreOwner)在传入的ViewModelStoreOwner参数必须是同一个，比如getActivity

//  1、设置监听和设置数据源：
//    private final List<TestUserInfo> mList = new ArrayList<>();
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mList.add(new TestUserInfo());
//
//        TestUserInfoViewModel model = new ViewModelProvider(this).get(TestUserInfoViewModel.class);
//        model.getUsers().observe(MainFirstFragment.this, users -> {
//            Log.d("tag_2", "observe=" + users.get(0).uname);
//        });
//        model.getUsers().setValue(mList);
//    }

//  2、写入值：
//    TestUserInfoViewModel model = new ViewModelProvider(MainFirstFragment.this).get(TestUserInfoViewModel.class);
//    mList.get(0).uname = "多想再见你，哪怕匆匆一眼就别离";
//    model.getUsers().postValue(mList);