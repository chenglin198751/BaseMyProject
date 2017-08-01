package main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import utils.MyUtils;

public class FlexBoxTestActivity extends BaseActivity {
    //例子：https://mp.weixin.qq.com/s/D0sCoIt7Gsms5JK9ZIhuhw
    private FlexboxLayout mFlexboxLayout;
    private static final String[] strArrs = {"百度助手", "新年", "穿衣服", "新年快乐3",
            "故事没说你吗好", "大家", "吃饭", "标签4", "内衣", "天气真好吗", "中文输入法", "四个字", "三个", "人提是",
            "你说呢", "好", "字体", "谷歌运", "白说的", "可以", "不是", "今天我们", "已经离去在人海", "茫茫搭讪",
            "可以不是吗", "不可以说话", "大臂收", "心仪", "对手吧", "放开我的手", "怕你说"
            , "一起", "那些北风", "吹起", "手气", "可以不是", "咿呀我", "时光疯狂"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.flex_box_test_layout);
        getTitleHelper().setTitle("flexbox");

        mFlexboxLayout = (FlexboxLayout) findViewById(R.id.flex_box);
        mFlexboxLayout.setFlexDirection(FlexDirection.ROW);

        for (int i = 0; i < strArrs.length; i++) {
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(-2, -2);
            params.leftMargin = MyUtils.dip2px(1f);
            params.rightMargin = MyUtils.dip2px(1f);
            params.topMargin = MyUtils.dip2px(1f);
            params.bottomMargin = MyUtils.dip2px(1f);
            mFlexboxLayout.addView(buildTextView(strArrs[i]), params);
        }

    }

    private TextView buildTextView(String text) {
        TextView textView = new TextView(this);
        textView.setBackgroundColor(Color.parseColor("#ffcccc"));
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20, 15, 20, 15);
        textView.setTextSize(14);
        textView.setTextColor(Color.parseColor("#333333"));
        return textView;
    }
}
