package main;

import android.os.Bundle;
import android.view.View;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import widget.MyDialog;
import widget.MyWebViewActivity;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    String aa = "原来刷卡上缆车，今年搞了个虹膜识别，大冬天的每次摘了雪镜摘眼镜，睫毛上霜还识别不出来，一队人在冬天的寒风里站着等识别，着实有点弱智。\n" +
            "\n" +
            "怕偷上缆车指纹不行吗？况且即便这样也拦不住没雪卡上缆车的，说句实在的，看缆车的工人都私自往里放人，不治本光外部围堵真的一点用也没有。\n" +
            "\n" +
            "总之阳光这几年的所作所为非常不明智，伤透了滑雪发烧友的心。再说说亚布力这个地方。小小的一片区域有着7家滑雪场，能想象吗。\n" +
            "\n" +
            "体委和阳光是两家高山雪场，剩下的是初级雪场。雅旺斯酒店也是为了大冬会招商引资来的一家企业，占据了亚布力黄金地段的另一半，同样赔钱，占了大片地却没钱投资开发，还是地都荒着 。\n" +
            "\n" +
            "这些都是当初的政府规划不好造成的，为了招商引资就大面积割地，完全不顾后续发展，自己的政绩是有了，下一任的麻烦跟我无关。\n" +
            "\n" +
            "这还只是雪场的数量，还不包括各大中小酒店宾馆，仔细查查，这片度假区可开发的空间真的不剩什么了。曾经一度我们都以为亚布力就会这么慢慢的黄了。\n" +
            "\n" +
            "可是张家口申冬奥成功了，一时之间，滑雪场又成了炙手可热的项目，大量的资金往雪圈涌，但有没有来亚布力的呢，没有，一片沉寂。\n" +
            "\n" +
            "恒大和万科这类的大企业都来张望过，可是亚布力空白投资区域没有，现有的收购全都一屁股烂债，更何况还狮子大开口漫天要价。\n" +
            "\n" +
            "你要全收购，要满足大大小小十几家的贪婪，你要收一家，还要等着剩下的那些家接着挤兑你，这样的地方，谁会来。\n" +
            "\n" +
            "可是黑龙江政府不能等着这个地方黄啊，要不他们没法跟上面交代，于是就出现了管委会。\n" +
            "\n" +
            "终于开扒管委会这个地方了。管委会目前是归黑龙江省森工总局直属领导的，因为亚布力属于林区，归他们管开发建设都方便。\n" +
            "\n" +
            "管委会本是政府部门，行使的应该是政府职能，但后来却自己上手经营了。为什么呢，因为刚说过了，这地方没有大企业肯投资，等着招商引资成功黄花菜都凉了，难道等个十来年再升职么，等不了。\n" +
            "\n" +
            "所以管委会就集结了一帮地方土财主，大家出钱搞建设。\n" +
            "\n" +
            "成立了没几年，建了什么呢，修路，搞夜间亮化，建夏季水上乐园，建山地自行车公园，建游乐园，建熊猫馆，建酒店，建温泉，建民俗度假村，建三山联网雪道……完完全全体现了中国基建狂魔的本质。\n" +
            "\n" +
            "四年时间，切身体会，整个景区大变样，比之前十几年加起来的变化都大。\n" +
            "\n" +
            "最神奇的一点是什么，是这些基建钱真的没有大企业肯投，政府也不会划拨的，但怎么来的呢 ？\n" +
            "\n" +
            "首先是跟银行借，以政府项目为背书，借我错不了；找小地主小包工头没钱先干着，没事，我是政府，肯定不能欠钱不还；找地方中小企业家投，你看我们这个地方快发展起来了吧，有水上乐园还有游乐园呢，原来冬季经营现在四季经营，以后肯定错不了。\n" +
            "\n" +
            "几公里长的一段木质栈道，背后的“股东”就有好几个包工头。可以这么说，建了这么多东西，真没有几样是先给钱再干的，都是先干完了钱过后再说。\n" +
            "\n" +
            "那建设成本是有了，地哪儿来的呢，可用的空白区域";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog myDialog = new MyDialog(getContext());
                myDialog.setTitle("我们");
                myDialog.setMessage(aa);
                myDialog.setLeftButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                myDialog.setRightButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                myDialog.show();
            }
        });
    }

    @Override
    public void onMyBroadcastReceiver(String action, Bundle bundle) {
        super.onMyBroadcastReceiver(action, bundle);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }
}
