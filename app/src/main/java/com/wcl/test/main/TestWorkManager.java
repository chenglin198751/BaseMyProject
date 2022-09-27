package com.wcl.test.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class TestWorkManager extends Worker {
    private static int index = 0;

    public TestWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
//        参考文档：https://www.jianshu.com/p/284700112f37
//        执行成功return Result.success()
//        执行失败return Result.failure()
//        需要重新执行return Result.retry()


//        //设置触发任务的约束条件，分别是充电中+网络连接中+非低电量
//        Constraints constraints = new Constraints.Builder()
//                .setRequiresCharging(true)
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .setRequiresBatteryNotLow(true)
//                .build();
//
//        testWorkRequest = new OneTimeWorkRequest.Builder(TestWorkManager.class)
//                .setConstraints(constraints)
//                .addTag("tag_test_worker")
//                .build();
//
//        WorkManager.getInstance(getContext()).enqueue(testWorkRequest);
//
//        //监听任务
//        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(testWorkRequest.getId()).observe(getContext(), new Observer<WorkInfo>() {
//            @Override
//            public void onChanged(WorkInfo workInfo) {
//                Log.d("tag_999", "workInfo:" + workInfo);
//            }
//        });


//        //还可以设置周期任务，需要注意的是：周期性任务的间隔时间不能小于15分钟。
//        PeriodicWorkRequest testWorkRequest = new PeriodicWorkRequest.Builder(TestWorkManager.class, 16, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .addTag("tag_test_worker")
//                .build();

        if (index < 1){
            index++;
            Log.v("tag_999","retry=" + index);
            return Result.retry();
        }else{
            Log.v("tag_999","doWork() success");
            return Result.success();
        }


    }

}
