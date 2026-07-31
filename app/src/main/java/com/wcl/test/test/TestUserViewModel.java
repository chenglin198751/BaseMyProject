package com.wcl.test.test;

import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TestUserViewModel extends ViewModel {
    // 使用 LiveData 让 UI 自动刷新
    public MutableLiveData<String> name = new MutableLiveData<>();
    private int index = 0;

    public TestUserViewModel() {
        name.setValue("张三");
    }

    // 事件处理
    public void onSubmitClick(View view) {
        name.setValue("张三" + (index++));
    }
}