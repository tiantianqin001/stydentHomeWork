package com.telit.zhkt_three.CustomView.QuestionView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.telit.zhkt_three.Activity.AfterHomeWork.LearnResourceActivity;
import com.telit.zhkt_three.Activity.AfterHomeWork.TypicalAnswersActivity;
import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.MistakesCollection.MistakesImproveActivity;
import com.telit.zhkt_three.Activity.MistakesCollection.PerfectAnswerActivity;
import com.telit.zhkt_three.Adapter.NewKnowledgeAdapter;
import com.telit.zhkt_three.Adapter.NewKnowledgeTwoAdapter;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.TempSaveItemInfo;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTagHandler;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * author: qzx
 * Date: 2019/5/19 10:38
 * <p>
 * QuestionBank??????---????????????
 * ???????????????????????????????????????
 * <p>
 * ???????????????????????????  getQuestionChannelType()--->???????????????
 * <p>
 * ????????????????????????
 * <p>
 * ???????????????????????????
 * <p>
 * todo ???????????????????????????????????????bug
 */
public class NewKnowledgeQuestionView extends RelativeLayout {

    //???????????????
    private QuestionBank questionBank;

    /**
     * ?????????????????????????????????????????????
     */
    private List<String> saveMultiList;

    /**
     * ???????????????????????????
     */
    private int curPosition;

    /**
     * ????????????
     */
    private TextView Item_Bank_head_title;
    private HtmlTextView Item_Bank_head_content;
    //???????????????????????????
//    private TextView Item_Bank_head_score;

    private TextView Item_Bank_head_promote;

    private TextView Item_Bank_head_good_answer;

    /**
     * ???List?????????
     */
    private RecyclerView rv_item_bank_options_layout;

    /**
     * List?????????
     */


    /**
     * ??????
     */
    private ScrollView Item_Bank_Answer_Scroll;

    private LinearLayout Item_Bank_Answer_Layout;
    private TextView Item_Bank_my_Answer;
    private TextView Item_Bank_right_Answer;
    private LinearLayout Item_Bank_Point;
    private ImageView Item_Bank_Img_Point;
    private LinearLayout Item_Bank_Analysis;
    private ImageView Item_Bank_Img_Analysis;
    private LinearLayout Item_Bank_Answer;
    private ImageView Item_Bank_Img_Answer;
    private TextView img_total_typical_answers;
    private ImageView iv_collect;

    private TextView Item_Bank_Show_Remark;
    private TextView img_total_learn_resource;
    /**
     * 0?????????  1 ?????????  2 ?????????
     * ?????????????????????????????? todoView
     * ????????????????????????????????? showView
     * ???????????????????????????????????????????????? showView plus AnswerView
     */
    private String status;

    /**
     * ?????????????????????
     */
    private boolean isMistaken;

    /**
     * ??????????????????
     */
    private String showAnswerDate;

    private Context context;

    private NewKnowledgeAdapter newKnowledgeAdapter;

    /**
     * ??????????????????
     *  @param questionBank ????????????????????????
     * @param status       ????????????????????????????????????
     */
    public void setQuestionInfo(QuestionBank questionBank, int curPosition, String status, boolean isMistaken) {

//        QZXTools.logE("setQuestionInfo status=" + status, null);
        if (status.equals("2")) {
            //???????????????????????????
            questionBank.setShownAnswer(true);
        }
        this.isMistaken = isMistaken;
        this.questionBank = questionBank;
        this.curPosition = curPosition;
        this.status = status;
        showAnswerDate = questionBank.getAnswerPublishDate();
        initData();
    }

    public NewKnowledgeQuestionView(Context context) {
        this(context, null);
    }

    public NewKnowledgeQuestionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NewKnowledgeQuestionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.item_new_bank_view_layout, this, true);
        //????????????????????????????????????????????????????????????questionInfo????????????????????????set????????????
        initView();
    }

    private void initView() {
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "PingFang-SimpleBold.ttf");

        saveMultiList = new ArrayList<>();

        Item_Bank_head_title = findViewById(R.id.Item_Bank_head_title);
        Item_Bank_head_content = findViewById(R.id.Item_Bank_head_content);
//        Item_Bank_head_score = findViewById(R.id.Item_Bank_head_score);
        Item_Bank_head_promote = findViewById(R.id.Item_Bank_head_promote);

        Item_Bank_head_good_answer = findViewById(R.id.Item_Bank_head_good_answer);
        img_total_typical_answers = findViewById(R.id.img_total_typical_answers);
        img_total_learn_resource = findViewById(R.id.img_total_learn_resource);

        Item_Bank_head_title.setTypeface(typeface);
        Item_Bank_head_content.setTypeface(typeface);
//        Item_Bank_head_score.setTypeface(typeface);
        Item_Bank_head_promote.setTypeface(typeface);
        Item_Bank_head_good_answer.setTypeface(typeface);

        img_total_learn_resource.setTypeface(typeface);
        img_total_typical_answers.setTypeface(typeface);

        rv_item_bank_options_layout = findViewById(R.id.rv_item_bank_options_layout);
        Item_Bank_Answer_Scroll = findViewById(R.id.Item_Bank_Answer_Scroll);
        Item_Bank_Answer_Layout = findViewById(R.id.Item_Bank_Answer_Layout);
        Item_Bank_my_Answer = findViewById(R.id.Item_Bank_my_Answer);
        Item_Bank_my_Answer.setTypeface(typeface);

        Item_Bank_right_Answer = findViewById(R.id.Item_Bank_right_Answer);
        Item_Bank_right_Answer.setTypeface(typeface);

        Item_Bank_Point = findViewById(R.id.Item_Bank_Point);
        Item_Bank_Img_Point = findViewById(R.id.Item_Bank_Img_Point);
        Item_Bank_Analysis = findViewById(R.id.Item_Bank_Analysis);
        Item_Bank_Img_Analysis = findViewById(R.id.Item_Bank_Img_Analysis);
        Item_Bank_Answer = findViewById(R.id.Item_Bank_Answer);
        Item_Bank_Img_Answer = findViewById(R.id.Item_Bank_Img_Answer);

        Item_Bank_Show_Remark = findViewById(R.id.Item_Bank_Show_Remark);
        Item_Bank_Show_Remark.setTypeface(typeface);

        TextView Item_Bank_Tv_Point = findViewById(R.id.Item_Bank_Tv_Point);
        TextView Item_Bank_Tv_Analysis = findViewById(R.id.Item_Bank_Tv_Analysis);
        TextView Item_Bank_Tv_Answer = findViewById(R.id.Item_Bank_Tv_Answer);
        Item_Bank_Tv_Point.setTypeface(typeface);
        Item_Bank_Tv_Analysis.setTypeface(typeface);
        Item_Bank_Tv_Answer.setTypeface(typeface);

        iv_collect = findViewById(R.id.iv_collect);
        iv_collect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCollectClickListener!=null){
                    onCollectClickListener.OnCollectClickListener(questionBank,curPosition);
                }
            }
        });
        //??????????????????
        img_total_learn_resource.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_learn_resource = new Intent(getContext(), LearnResourceActivity.class);
                intent_learn_resource.putExtra("questionId", questionBank.getId() + "");
                intent_learn_resource.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent_learn_resource);
            }
        });

        Item_Bank_Show_Remark.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> imgFilePathList = (ArrayList<String>) questionBank.getTeaDescFile();
                Intent intent = new Intent(getContext(), ImageLookActivity.class);
                intent.putStringArrayListExtra("imgResources", imgFilePathList);
                intent.putExtra("NeedComment", false);
                intent.putExtra("curImgIndex", 0);
                getContext().startActivity(intent);
            }
        });
        //??????????????????
        img_total_typical_answers.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_typical_answers = new Intent(getContext(), TypicalAnswersActivity.class);
                intent_typical_answers.putExtra("questionId", questionBank.getId() + "");
                intent_typical_answers.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent_typical_answers);
            }
        });
        //todo  ?????????  ??????
        Item_Bank_head_promote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getBundle();
                String knowledge_json = bundle.getString("knowledge_json");
                if (TextUtils.isEmpty(knowledge_json)) {
                    QZXTools.popCommonToast(getContext(), "????????????????????????", false);
                    return;
                }

                /**
                 * ???????????????????????????
                 * */
                Intent intent = new Intent(getContext(), MistakesImproveActivity.class);
                intent.putExtra("improvement", getBundle());
                getContext().startActivity(intent);

                //?????????????????? ??????  TODO ???????????????
                BuriedPointUtils.buriedPoint("2021","","","","");
            }
        });
        //??????????????????
        Item_Bank_head_good_answer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PerfectAnswerActivity.class);
                intent.putExtra("questionId", questionBank.getId() + "");
                intent.putExtra("homeworkId", questionBank.getHomeworkId());
                getContext().startActivity(intent);
            }
        });
    }

    private void initData() {

        //  Item_Bank_list_question_layout.removeAllViews();

        if (isMistaken && bundle != null && !TextUtils.isEmpty(bundle.getString("knowledge_json"))) {
            Item_Bank_head_promote.setVisibility(VISIBLE);
        } else {
            Item_Bank_head_promote.setVisibility(GONE);
        }

        //???????????????????????????????????????
        Item_Bank_head_good_answer.setVisibility(GONE);
        img_total_typical_answers.setVisibility(GONE);

        //????????????????????????
        if (questionBank.getTeaDescFile() == null || questionBank.getTeaDescFile().size() <= 0) {
            Item_Bank_Show_Remark.setVisibility(GONE);
        } else {
            Item_Bank_Show_Remark.setVisibility(VISIBLE);
        }

        getHeadAndOptionsInfo();

        //?????????????????? ??????????????????  ???????????????
        if (isMistaken ){
            if (status.equals(Constant.Commit_Status) || status.equals(Constant.Review_Status)){
                Item_Bank_head_promote.setVisibility(VISIBLE);
            }
        }

        if ("2".equals(status) || !TextUtils.isEmpty(showAnswerDate)) {
            if (!status.equals(Constant.Retry_Status)){
                if (!status.equals(Constant.Save_Status) ){
                    showResumeAnswer();
                }
            }
        }
        if (status.equals(Constant.Retry_Status)){
            //??????????????????
            if (!questionBank.getQuestionChannelType().equals(Constant.Subject_Item)){
                showResumeAnswer();
                img_total_learn_resource.setVisibility(VISIBLE);
            }

        }
        //???????????????????????????

        if ("0".equals(status) ||"-1".equals(status) ||"-2".equals(status) || (bundle != null && "1".equals(bundle.getString("flag")))||isMistaken) {
            iv_collect.setVisibility(GONE);
        }

        setCollect(questionBank.getIsCollect());
    }

    /**
     * ????????????
     *
     * @param isCollect
     */
    public void setCollect(String isCollect){
        if ("0".equals(isCollect)){
            iv_collect.setImageResource(R.mipmap.collect_gray_icon);
        }else {
            iv_collect.setImageResource(R.mipmap.collect_red_icon);
        }
    }

    /**
     * ???????????????ID
     */
    public static String subjQuestionId;

    /**
     * ??????????????????????????????
     * ??????ownList
     * <p>
     * "ownList": [
     * {
     * "state": "2",
     * "score": "0",
     * "answerContent": "Fggb"
     * }
     * ],
     */
    private void getHeadAndOptionsInfo() {
        //??????
        switch (questionBank.getQuestionChannelType()) {
            case Constant.Single_Choose:
                Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                break;
            case Constant.Multi_Choose:
                Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                break;
            case Constant.Fill_Blank:
                Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                break;
            case Constant.Subject_Item:
                Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                if (status.equals(Constant.Commit_Status) || status.equals(Constant.Review_Status)){
                    img_total_typical_answers.setVisibility(VISIBLE);
                    Item_Bank_head_good_answer.setVisibility(VISIBLE);
                }
                break;
            case Constant.Judge_Item:
                Item_Bank_head_title.setText((curPosition + 1) + "???[?????????]");
                break;
        }
        //????????????List
//        QZXTools.logE("List=" + questionBank.getList(), null);
        if (TextUtils.isEmpty(questionBank.getList()) || questionBank.getList().equals("NULL")) {
            //????????????????????????    //????????????
            rv_item_bank_options_layout.setLayoutManager(new LinearLayoutManager(context));
            newKnowledgeAdapter = new NewKnowledgeAdapter(questionBank,context,status);
            rv_item_bank_options_layout.setAdapter(newKnowledgeAdapter);

            String optionJson = questionBank.getAnswerOptions();


        } else {

            //TODO   ?????????????????????????????????  ????????????

            rv_item_bank_options_layout.setLayoutManager(new LinearLayoutManager(context));
            NewKnowledgeTwoAdapter newKnowledgeTwoAdapter = new NewKnowledgeTwoAdapter(questionBank,context,status,questionBank.getQuestionChannelType());
            rv_item_bank_options_layout.setAdapter(newKnowledgeTwoAdapter);
        }
        //????????????
        String ItemBankTitle = questionBank.getQuestionText();
        if (TextUtils.isEmpty(ItemBankTitle))return;
//        Item_Bank_head_content.setHtml(ItemBankTitle, new HtmlHttpImageGetter(Item_Bank_head_content));

        //?????????????????? ---????????????????????????????????????????????????

        /**
         *     SpannableString spannableString = new SpannableString("??????????????????");
         spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), 2,
         spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         tv5.setText(spannableString);
         * */

        String score = questionBank.getScore();
        String scoreStr;
        if (status.equals("2")) {
            /**
             * notes:??????????????????ownscore????????????ownList??????score???
             * */
            String myScore = questionBank.getOwnscore();

//            double totalDScore = 0.0;

//            boolean isException = false;

//            List<WorkOwnResult> workOwnResults = questionBank.getOwnList();
//            if (workOwnResults != null && workOwnResults.size() > 0) {
//                for (WorkOwnResult workOwnResult : workOwnResults) {
//                    String scoreTag = workOwnResult.getScore();
//                    try {
//                        double dScore = Double.parseDouble(scoreTag);
//                        totalDScore += dScore;
//                    } catch (Exception e) {
//                        isException = true;
//                        //????????????????????????Double????????????????????????
//                        e.printStackTrace();
//                        CrashReport.postCatchedException(e);
//                    }
//                }
//            }
//
//            if (isException) {
//                scoreStr = "(????????????" + score + "???,???????????????" + totalDScore + "???;????????????????????????";
//            } else {
//                scoreStr = "(????????????" + score + "???,???????????????" + totalDScore + "??????";
//            }
//            Item_Bank_head_score.setText("(????????????" + score + "???,???????????????" + myScore + "??????");

            scoreStr = "(????????????" + score + "???,???????????????" + myScore + "??????";

        } else {
            scoreStr = "(" + score + "???)";
//            Item_Bank_head_score.setText("(" + score + "???)");
        }

        /**
         * ??????????????????TextView??????????????????????????????
         * */
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
//        spannableStringBuilder.append(ItemBankTitle);
//        spannableStringBuilder.append(scoreStr);
//        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#ff4444"));
//        spannableStringBuilder.setSpan(foregroundColorSpan, ItemBankTitle.length(), spannableStringBuilder.length(),
//                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        Item_Bank_head_content.setText(spannableStringBuilder);


        /**
         * ???????????????HtmlView???setHtml??????,???????????????????????????????????????????????????
         * */
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ItemBankTitle);
        stringBuilder.append(scoreStr);
        //???????????????????????????
        Item_Bank_head_content.setHtml(stringBuilder.toString(), new HtmlHttpImageGetter(Item_Bank_head_content),
                true, new HtmlTagHandler.FillBlankInterface() {
                    @Override
                    public void addSpans(Editable output) {
                        String content = output.toString();
                        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#ff4444"));
                        output.setSpan(foregroundColorSpan, ItemBankTitle.length(), content.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                });

    }

    /**
     * ????????????
     */
    private void getAnswerInfo() {

        //??????????????????
        if (questionBank.isShownAnswer() || !TextUtils.isEmpty(showAnswerDate)) {
            Item_Bank_Answer_Layout.setVisibility(VISIBLE);
        } else {
            Item_Bank_Answer_Layout.setVisibility(GONE);
            return;
        }

        //????????????
        Date date = new Date();
        if (!TextUtils.isEmpty(showAnswerDate) && QZXTools.getDateValue(showAnswerDate, "yyyy-MM-dd HH:mm:ss")
                > date.getTime()) {
            //????????????????????????????????????????????????????????????
            Item_Bank_Answer_Layout.setVisibility(GONE);
            return;
        } else if (!TextUtils.isEmpty(showAnswerDate) && QZXTools.getDateValue(showAnswerDate, "yyyy-MM-dd HH:mm:ss")
                <= date.getTime()) {
            //?????????????????????????????????
            Item_Bank_Answer_Layout.setVisibility(VISIBLE);
        }

        //????????????
        if (questionBank.getQuestionChannelType() == Constant.ItemBank_Judge
                || questionBank.getQuestionChannelType() == Constant.Single_Choose
                || questionBank.getQuestionChannelType() == Constant.Multi_Choose) {

            if (questionBank.getSaveInfos() != null) {
                StringBuffer stringBuffer = new StringBuffer();
                for (TempSaveItemInfo tempSaveItemInfo : questionBank.getSaveInfos()) {
                    stringBuffer.append(tempSaveItemInfo.getKey());
                    stringBuffer.append(" ");
                }
                Item_Bank_my_Answer.setVisibility(VISIBLE);
                Item_Bank_my_Answer.setText("???????????????" + stringBuffer.toString().trim());
            } else {
                Item_Bank_my_Answer.setVisibility(GONE);
            }
        }

        //????????????
        if (TextUtils.isEmpty(questionBank.getAnswerText())) {
            Item_Bank_right_Answer.setVisibility(GONE);
        } else {
            Item_Bank_right_Answer.setVisibility(VISIBLE);
            //??????????????????????????????
            if (questionBank.getQuestionChannelType() ==  Constant.ItemBank_Judge){
                if (questionBank.getAnswerText().equals("??????")){
                    Item_Bank_right_Answer.setText("???????????????" +"A");
                }else {
                    Item_Bank_right_Answer.setText("???????????????" +"B");
                }

            }else {

                Item_Bank_right_Answer.setText("???????????????" + questionBank.getAnswerText());
            }
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getKnowledge())) {
            Item_Bank_Point.setVisibility(GONE);
        } else {
            Item_Bank_Point.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getKnowledge();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Point);
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getExplanation())) {
            Item_Bank_Analysis.setVisibility(GONE);
        } else {
            Item_Bank_Analysis.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getExplanation();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Analysis);
        }

        //??????
        if (TextUtils.isEmpty(questionBank.getAnswer())) {
            Item_Bank_Answer.setVisibility(GONE);
        } else {
            Item_Bank_Answer.setVisibility(VISIBLE);
            String pointUrl = UrlUtils.ImgBaseUrl + questionBank.getAnswer();
            Glide.with(getContext()).load(pointUrl).into(Item_Bank_Img_Answer);
        }
    }

    /**
     * ???????????????????????????
     */
    private void showResumeAnswer() {
        //????????????list
   /*     if (Item_Bank_list_question_layout.getVisibility() == VISIBLE) {
            for (int i = 0; i < Item_Bank_list_question_layout.getChildCount(); i++) {
                NewKnowledgeToDoView newKnowledgeToDoView = (NewKnowledgeToDoView) Item_Bank_list_question_layout.getChildAt(i);

                if (newKnowledgeToDoView.getQuestionBank().isShownAnswer()) {
                    newKnowledgeToDoView.getQuestionBank().setShownAnswer(false);
                } else {
                    newKnowledgeToDoView.getQuestionBank().setShownAnswer(true);
                }

                newKnowledgeToDoView.getAnswerInfo();
            }
        }*/

        //????????????
//        if (questionBank.isShownAnswer()) {
//            questionBank.setShownAnswer(false);
//        } else {
//            questionBank.setShownAnswer(true);
//        }
        getAnswerInfo();
    }

    /**
     * ???????????????Bundle
     */
    private Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    private OnCollectClickListener onCollectClickListener;

    public interface OnCollectClickListener {
        void OnCollectClickListener(QuestionBank questionBank,int curPosition);
    }

    public void setOnCollectClickListener(OnCollectClickListener onCollectClickListener) {
        this.onCollectClickListener = onCollectClickListener;
    }

    @Override
    protected void onAttachedToWindow() {
//        QZXTools.logE("onAttachedToWindow", null);
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
//        QZXTools.logE("onDetachedFromWindow", null);
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }


    /**
     * ???????????????   ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(KB)
     */
    @Subscriber(tag = Constant.Subjective_Board_Callback, mode = ThreadMode.MAIN)
    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        QZXTools.logE("Bank fromBoardCallback ExtraInfoBean=" + extraInfoBean + ";id=" , null);

        if (newKnowledgeAdapter!=null){
            newKnowledgeAdapter.fromBoardCallback(extraInfoBean);
        }

    }

    /**
     * ???????????????   ???????????????????????????????????????????????????????????????????????????????????????????????????(MB)
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????
     * <p>
     * ?????????????????????????????????????????????????????????list?????????
     */
    @Subscriber(tag = Constant.Subjective_Camera_Callback, mode = ThreadMode.MAIN)
    public void fromCameraCallback(String flag) {
        if (flag.equals("CAMERA_CALLBACK")) {
            QZXTools.logE("fromCameraCallback filePath=" , null);
            if (newKnowledgeAdapter!=null){
                newKnowledgeAdapter.fromCameraCallback(flag);
            }
        }
    }
}
