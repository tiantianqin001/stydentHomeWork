package com.telit.zhkt_three.Activity.HomeWork;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gyf.immersionbar.ImmersionBar;
import com.telit.zhkt_three.Activity.AfterHomeWork.NewJobReportActivity;
import com.telit.zhkt_three.Activity.BaseActivity;
import com.telit.zhkt_three.Adapter.VPHomeWorkDetailAdapter;
import com.telit.zhkt_three.Adapter.interactive.BankPracticeVPAdapter;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CusomPater;
import com.telit.zhkt_three.CustomView.LazyViewPager;
import com.telit.zhkt_three.CustomView.QuestionView.NewKnowledgeQuestionView;
import com.telit.zhkt_three.CustomView.QuestionView.SubjectiveToDoView;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.JavaBean.AppInfo;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.Gson.CollectQuestionByHandBean;
import com.telit.zhkt_three.JavaBean.Gson.HomeWorkByHandBean;
import com.telit.zhkt_three.JavaBean.Gson.HomeWorkByHandBeanTwo;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfoByhand;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.AnswerItem;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.JavaBean.HomeWorkCommit.HomeworkCommitBean;
import com.telit.zhkt_three.JavaBean.HomeWorkCommit.QuestionIdsBean;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ViewUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.telit.zhkt_three.greendao.LocalTextAnswersBeanDao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ?????????????????????
 * <p>
 * ?????????????????????(byhand==1)/????????????(byhand!=1)????????????
 * status 0
 */
public class HomeWorkDetailActivity extends BaseActivity implements View.OnClickListener {

    private Unbinder unbinder;
    @BindView(R.id.homework_detail_vp)
    CusomPater homework_vp;
    @BindView(R.id.homework_back)    ImageView homework_back;
    @BindView(R.id.homework_title)
    TextView homework_title;
    @BindView(R.id.homework_count)
    TextView homework_count;
    @BindView(R.id.homework_btn_commit)
    TextView homework_commit;
    @BindView(R.id.layout_left)
    LinearLayout layout_left;
    @BindView(R.id.layout_right)
    LinearLayout layout_right;
    @BindView(R.id.tv_comment_teacher)
    TextView tv_comment_teacher;
    //??????????????????  0?????????  1 ?????????  2 ?????????
    private String taskStatus;


    @BindView(R.id.homework_btn_save)
    TextView homework_btn_save;

    //???????????????id
    private String homeworkId;

    private String byHand;//1???????????????2???????????????

    //???????????????????????????
    private int commitFileCount;

    private int curPageIndex = 0;
    private int totalPageCount;
    //??????????????????
    private CircleProgressDialogFragment circleProgressDialogFragment;
    /**
     * ????????????
     */
    private int totalQuestionCount;
    private static boolean isShow=false;
    private static final int Server_Error = 0;
    private static final int Error404 = 1;
    private static final int Operator_Success = 2;
    private static final int Commit_Result_Show = 3;
    private static final int Commit_Result_Show_Save = 4;
    private VPHomeWorkDetailAdapter vpHomeWorkDetailAdapter;
    private Handler mHandler = new Handler() {



        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Server_Error:
                    if (isShow){
                        QZXTools.popToast(HomeWorkDetailActivity.this, "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        //??????????????????
                        homework_commit.setEnabled(true);
                    }
                    break;
                case Error404:
                    if (isShow){
                        QZXTools.popToast(HomeWorkDetailActivity.this, "?????????????????????", false);
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        //??????????????????
                        homework_commit.setEnabled(true);
                    }
                    break;

                case Commit_Result_Show_Save:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        String resultSave = (String) msg.obj;
                        QZXTools.popCommonToast(HomeWorkDetailActivity.this, resultSave, false);

                        EventBus.getDefault().post("save_homework,"+homeworkId, Constant.Homework_Save);

                        finish();
                    }


                    break;
                case Operator_Success:
                    if (isShow){
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }

                        if ("1".equals(byHand)) {
                            List<QuestionInfoByhand> questionInfoByhandList = (List<QuestionInfoByhand>) msg.obj;

                            totalPageCount = questionInfoByhandList.size();

                            //????????????????????????
                            for (int i = 0; i < questionInfoByhandList.size(); i++) {
                                totalQuestionCount += questionInfoByhandList.get(i).getSheetlist().size();
                            }

                            if (totalPageCount > 1) {
                                layout_right.setVisibility(View.VISIBLE);
                                layout_left.setVisibility(View.INVISIBLE);
                            } else {
                                layout_left.setVisibility(View.INVISIBLE);
                                layout_right.setVisibility(View.INVISIBLE);
                            }

                            //??????Vp?????????  ?????????????????????   0?????????  1????????? ???1?????????????????????????????????????????????????????? taskStatus ?????????????????????
                            vpHomeWorkDetailAdapter = new VPHomeWorkDetailAdapter
                                    (HomeWorkDetailActivity.this, questionInfoByhandList, null, taskStatus,1,comType);
                            homework_vp.setAdapter(vpHomeWorkDetailAdapter);

                        } else {
                            HomeWorkByHandBeanTwo homeWorkByHandBeanTwo = (HomeWorkByHandBeanTwo) msg.obj;

                            List<QuestionBank> questionBankList = homeWorkByHandBeanTwo.getResult();

                            for (QuestionBank questionBank : questionBankList) {
                                questionBank.setHomeworkId(homeworkId);
                            }

                            totalPageCount = questionBankList.size();

                            totalQuestionCount = totalPageCount;

                            if (totalPageCount > 1) {
                                layout_right.setVisibility(View.VISIBLE);
                                layout_left.setVisibility(View.INVISIBLE);
                            } else {
                                layout_left.setVisibility(View.INVISIBLE);
                                layout_right.setVisibility(View.INVISIBLE);
                            }
                            bankPracticeVPAdapter = new BankPracticeVPAdapter(
                                    HomeWorkDetailActivity.this, questionBankList);
                            bankPracticeVPAdapter.setStatus(taskStatus);
                            homework_vp.setAdapter(bankPracticeVPAdapter);
                            bankPracticeVPAdapter.setOnCollectClickListener(new BankPracticeVPAdapter.OnCollectClickListener() {
                                @Override
                                public void OnCollectClickListener(NewKnowledgeQuestionView newKnowledgeQuestionView,QuestionBank questionBank, int curPosition) {
                                    if ("0".equals(questionBank.getIsCollect())){//??????
                                        collectYeOrNo(questionBank,"1",curPosition,newKnowledgeQuestionView);
                                    }else {//????????????
                                        collectYeOrNo(questionBank,"0",curPosition,newKnowledgeQuestionView);
                                    }
                                }
                            });
                        }
                    }

                    break;
                case Commit_Result_Show:
                    if (circleProgressDialogFragment != null) {
                        circleProgressDialogFragment.dismissAllowingStateLoss();
                        circleProgressDialogFragment = null;
                    }

                    String result = (String) msg.obj;
                    QZXTools.popCommonToast(HomeWorkDetailActivity.this, result, false);

                    //??????????????????
                    EventBus.getDefault().post("commit_homework,"+homeworkId, Constant.Homework_Commit);

                    finish();
                    //????????????????????????
                    BuriedPointUtils.buriedPoint("2017","","","","");
                    break;
            }
        }
    };

    //????????????Json
    private String resultBackJson;
    private String comType;
    private int types;
    private BankPracticeVPAdapter bankPracticeVPAdapter;
    private List<LocalTextAnswersBean> localTextAnswersBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_work_detail);
        //????????????????????????
        ImmersionBar.with(this).navigationBarColor(R.color.colorPrimary).init();
        unbinder = ButterKnife.bind(this);
        isShow=true;

        //??????????????????????????????????????????????????????android:keepScreenOn="true"
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        EventBus.getDefault().register(this);

        layout_left.setOnClickListener(this);
        layout_right.setOnClickListener(this);
        homework_back.setOnClickListener(this);
        homework_commit.setOnClickListener(this);
        homework_btn_save.setOnClickListener(this);

        Intent intent = getIntent();
        //??????????????????????????????????????????
        resultBackJson = getIntent().getStringExtra("Report_Json");
        //????????????HomeWorkId????????????????????????taskStatus
        homeworkId = intent.getStringExtra("homeworkId");
        taskStatus = intent.getStringExtra("status");
        //1???????????????   2?????????
        byHand = intent.getStringExtra("byHand");
        if (byHand.equals("1")){
            homework_btn_save.setVisibility(View.GONE);
        }
        String title = intent.getStringExtra("title");
        //?????????????????????????????????
        comType = intent.getStringExtra("comType");
        //????????????????????????
        types = intent.getIntExtra("types", 0);
        if (!TextUtils.isEmpty(title)) {
            homework_title.setText(title);
        }

        QZXTools.logE("homeworkId=" + homeworkId + ";taskStatus=" + taskStatus + ";byHand=" + byHand, null);

        if (homeworkId == null || taskStatus == null) {
            QZXTools.popCommonToast(this, "????????????ID?????????", false);
            finish();
            return;
        }
        //????????????????????????????????????????????????????????????
        if (taskStatus.equals(Constant.Review_Status) || taskStatus.equals(Constant.Commit_Status)) {
            if (TextUtils.isEmpty(resultBackJson)) {
                homework_commit.setVisibility(View.GONE);

                //?????????????????????????????????
                tv_comment_teacher.setVisibility(View.VISIBLE);
                homework_btn_save.setVisibility(View.GONE);
                homework_commit.setVisibility(View.GONE);

            } else {
                homework_commit.setText("??????????????????");
            }
        }
        fetchNetHomeWorkDatas();
        homework_vp.setOnPageChangeListener(new LazyViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                curPageIndex = i;
                if (i >= (totalPageCount - 1)) {
                    layout_right.setVisibility(View.INVISIBLE);
                    layout_left.setVisibility(View.VISIBLE);
                } else if (i <= 0) {
                    layout_left.setVisibility(View.INVISIBLE);
                    layout_right.setVisibility(View.VISIBLE);
                } else {
                    layout_left.setVisibility(View.VISIBLE);
                    layout_right.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ZBVPermission.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ZBVPermission.ACTIVITY_REQUEST_CODE:
                    //????????????????????????????????????????????????????????????????????????  public static final int ACTIVITY_REQUEST_CODE = 0x9;??????
                    ZBVPermission.getInstance().onActivityResult(requestCode, resultCode, data);
                    break;
                case SubjectiveToDoView.CODE_SYS_CAMERA:
                    //data???null,???????????????????????????????????????????????????
                    QZXTools.logE("data=" + data, null);

                    if (vpHomeWorkDetailAdapter!=null){
                        vpHomeWorkDetailAdapter.fromCameraCallback("CAMERA_CALLBACK");
                    }

                    EventBus.getDefault().post("CAMERA_CALLBACK", Constant.Subjective_Camera_Callback);
                    break;
            }
        }
    }
    @Subscriber(tag = "deleteItemImage", mode = ThreadMode.MAIN)
    public void deleteItemImage(String deleteImage) {
        if (vpHomeWorkDetailAdapter!=null){
            vpHomeWorkDetailAdapter.fromCameraCallback(deleteImage);
        }
    }
    /**
     * ???????????????   ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(KB)
     */
    @Subscriber(tag = Constant.Subjective_Board_Callback, mode = ThreadMode.MAIN)
    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        if (vpHomeWorkDetailAdapter!=null){
            vpHomeWorkDetailAdapter.fromBoardCallback(extraInfoBean);
        }
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }

        /**
         * ??????????????????
         * */
        mHandler.removeCallbacksAndMessages(null);
        QZXTools.setmToastNull();

        ZBVPermission.getInstance().recyclerAll();
        isShow=false;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void fetchNetHomeWorkDatas() {
        String url;
        //??????????????????????????????????????????URL
        if ("1".equals(byHand)) {
            url = UrlUtils.BaseUrl + UrlUtils.HomeWorkDetailsByHand;
        } else {
            url = UrlUtils.BaseUrl + UrlUtils.HomeWorkDetailsByHandTwo;
        }
        Map<String, String> mapParams = new LinkedHashMap<>();

        mapParams.put("homeworkid", homeworkId);
        mapParams.put("status", taskStatus);
        mapParams.put("studentid", UserUtils.getUserId());

        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        //??????????????????
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("resultJson=" + resultJson, null);
                    Gson gson = new Gson();
                    if ("1".equals(byHand)) {
                        HomeWorkByHandBean homeWorkByHandBean = gson.fromJson(resultJson, HomeWorkByHandBean.class);
//                    QZXTools.logE("homeWorkByHandBean=" + homeWorkByHandBean, null);

                        Message message = mHandler.obtainMessage();
                        message.what = Operator_Success;
                        message.obj = homeWorkByHandBean.getResult();
                        mHandler.sendMessage(message);

                    } else {

                        HomeWorkByHandBeanTwo homeWorkByHandBeanTwo = gson.fromJson(resultJson, HomeWorkByHandBeanTwo.class);
//                    QZXTools.logE("homeWorkByHandBean=" + homeWorkByHandBean, null);

                        Message message = mHandler.obtainMessage();
                        message.what = Operator_Success;
                        message.obj = homeWorkByHandBeanTwo;
                        mHandler.sendMessage(message);
                    }
                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homework_back:
                finish();
                break;
            case R.id.homework_btn_save:
                //?????????????????????
                String url = UrlUtils.BaseUrl + UrlUtils.HomeWorkCommit;
                commitInfo(url,-1,"-2");

                break;

            case R.id.homework_btn_commit:
                //?????????????????????????????????????????????????????????
                if (!TextUtils.isEmpty(resultBackJson)) {
                    //???????????????????????????????????????
                    Intent intent = new Intent(this, NewJobReportActivity.class);
                    intent.putExtra("Report_Json", resultBackJson);
                    startActivity(intent);
                    finish();
                    return;
                }

                /**
                 * ????????????????????????
                 * 1???HomeworkCommitBean??????
                 * 2???QuestionIdsBean??????
                 * ????????????????????????
                 * ????????????questionId   homeworkId
                 * */
                String url1 = UrlUtils.BaseUrl + UrlUtils.HomeWorkCommit;
                commitInfo(url1,0,"1");
                break;
            case R.id.layout_left:
                if (ViewUtils.isFastClick(  1000)) {
                    curPageIndex--;
                    if (curPageIndex >= 0) {
                        if (curPageIndex == 0) {
                            layout_left.setVisibility(View.INVISIBLE);
                        } else {
                            layout_left.setVisibility(View.VISIBLE);
                        }
                        layout_right.setVisibility(View.VISIBLE);
                        homework_vp.setCurrentItem(curPageIndex, true);
                    }
                }

                break;
            case R.id.layout_right:
                if (ViewUtils.isFastClick(  1000)) {
                    curPageIndex++;
                    if (curPageIndex <= totalPageCount - 1) {
                        if (curPageIndex == totalPageCount - 1) {
                            layout_right.setVisibility(View.INVISIBLE);
                        } else {
                            layout_right.setVisibility(View.VISIBLE);
                        }
                        layout_left.setVisibility(View.VISIBLE);
                        homework_vp.setCurrentItem(curPageIndex, true);
                    }
                }
                break;
        }
    }
    private boolean matches;
    //????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private void commitInfo(String url, int types, String commitStatus) {

        List<HomeworkCommitBean> homeworkCommitBeanList = new ArrayList<>();
        List<QuestionIdsBean> questionIdsBeanList = new ArrayList<>();
        // question_files ??????s
        Map<String, File> fileHashMap = new LinkedHashMap<>();

        //?????????????????????ID????????????ID
        localTextAnswersBeanList = MyApplication.getInstance().getDaoSession()
                .getLocalTextAnswersBeanDao().queryBuilder()
                .where(LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).list();
        //?????????????????????
        if (types == 0) {
            if (localTextAnswersBeanList == null || localTextAnswersBeanList.size() <= 0) {
                QZXTools.popCommonToast(this, "???????????????????????????????????????", false);
                return;
            }else if (localTextAnswersBeanList == null || localTextAnswersBeanList.size() >= 0){
                //????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????
                for (LocalTextAnswersBean localTextAnswersBean : localTextAnswersBeanList) {
                    int questionType = localTextAnswersBean.getQuestionType();
                    if (questionType == Constant.Multi_Choose){
                        List<AnswerItem> answersBeanList = localTextAnswersBean.getList();
                        if (answersBeanList!=null && answersBeanList.size() == 0){
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(localTextAnswersBean.getQuestionId() + "");
                            localTextAnswersBeanList = MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().queryBuilder()
                                    .where(LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).list();
                        }
                    }
                }
                if (localTextAnswersBeanList.size() < totalQuestionCount){
                    //??????????????????????????????????????????????????????????????????
                    QZXTools.popCommonToast(this, "???????????????????????????", false);
                    return;
                }


            } else if (localTextAnswersBeanList.size() < totalQuestionCount) {
                //??????????????????????????????????????????????????????????????????
                QZXTools.popCommonToast(this, "???????????????????????????", false);
                return;
            }
        } else {
            if (localTextAnswersBeanList == null || localTextAnswersBeanList.size() <= 0) {
                QZXTools.popCommonToast(this, "???????????????????????????????????????", false);
                return;
            }
        }

        QZXTools.logE("homeworkDetail????????????????????????"+ localTextAnswersBeanList,null);
        if (types == 0) {

            homework_commit.setEnabled(false);
        }

        //??????????????????  ?????????  blanknum
        Map<String, String> mapParams = new LinkedHashMap<>();

        for (LocalTextAnswersBean localTextAnswersBean : localTextAnswersBeanList) {

            switch (localTextAnswersBean.getQuestionType()) {
                case Constant.Single_Choose:
                case Constant.Multi_Choose:
                    //?????????
                case Constant.Fill_Blank:
                    List<AnswerItem> answerItemList = localTextAnswersBean.getList();
                    QuestionIdsBean questionIdsBean1 = new QuestionIdsBean();
                    questionIdsBean1.setCount("0");
                    questionIdsBean1.setQuestionId(localTextAnswersBean.getQuestionId());
                    questionIdsBeanList.add(questionIdsBean1);


                    QZXTools.logE("answerItem=" + answerItemList, null);
                    for (AnswerItem answerItem : answerItemList) {
                        HomeworkCommitBean homeworkCommitBean = new HomeworkCommitBean();
                        homeworkCommitBean.setHomeworkId(homeworkId);
                        homeworkCommitBean.setClassId(UserUtils.getClassId());
                        //????????????StudentId????????????UserId
                        homeworkCommitBean.setStudentId(UserUtils.getUserId());
                        homeworkCommitBean.setQuestionId(localTextAnswersBean.getQuestionId());
                        homeworkCommitBean.setBlanknum(answerItem.getBlanknum());
                        QZXTools.logE("id=" + homeworkId + ";type=" + localTextAnswersBean.getQuestionType(), null);
                        homeworkCommitBean.setAnswerId(answerItem.getItemId());
                        //????????????
                        if ("1".equals(byHand)) {
                            homeworkCommitBean.setAnswerContent(answerItem.getContent());


                        } else {
                            if (localTextAnswersBean.getQuestionType() == Constant.Fill_Blank) {

                                //????????????????????????
                                homeworkCommitBean.setAnswerContent(answerItem.getContent());
                                homeworkCommitBean.setBlanknum(answerItem.getBlanknum());

//                                        //?????????Json
//                                        String answer = answerItem.getContent();
//                                        String[] splits = answer.split(":");
//                                        JSONObject jsonObject = new JSONObject();
//                                        try {
//                                            jsonObject.put(splits[0], splits[1]);
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                        QZXTools.logE("answer json=" + jsonObject.toString(), null);
//                                        homeworkCommitBean.setAnswerContent(jsonObject.toString());
                            } else {
                                homeworkCommitBean.setAnswerContent(answerItem.getContent());
                            }
                        }
                        homeworkCommitBeanList.add(homeworkCommitBean);
                    }
                    break;
                case Constant.Subject_Item:
                    commitFileCount = 0;


                    //?????????????????????????????????
                    List<String> imgPathList = localTextAnswersBean.getImageList();
                    //????????????????????????

                        if (imgPathList == null || imgPathList.size() == 0) {
                            QZXTools.popToast(HomeWorkDetailActivity.this, "????????????????????????????????????????????????", false);
                            homework_commit.setEnabled(true);
                            return;
                        }



                    /**
                     * ?????????????????????????????????questionId count=0
                     * */
                    QZXTools.logE("???????????????????????????=" + imgPathList.size(), null);
                    commitFileCount += imgPathList.size();
                    //??????????????? ????????????
                    //????????????
                    for (String imgPath : imgPathList) {
                        //String fileName = imgPath.substring(imgPath.lastIndexOf("/")+1);===>file.getName();
                        //????????????????????????????????????
                        matches = imgPath.matches("^(http|https|ftp)://.*$");
                        if (!matches) {
                            File file = new File(imgPath);
                            fileHashMap.put(file.getName(), file);
                        } else {
                            String savePath = QZXTools.getExternalStorageForFiles(HomeWorkDetailActivity.this, Environment.DIRECTORY_PICTURES);
                            String[] split = imgPath.split("\\/");
                            String  imageName="telit_"+split[split.length-2]+"_"+split[split.length-1];
                            File file = new File(savePath, imageName);
                            try {
                                if ( !file.exists()) {
                                    boolean isSuccess = file.createNewFile();
                                    if (isSuccess) {
                                        QZXTools.logE("is successful", null);
                                    } else {
                                        QZXTools.logE("is failure", null);

                                    }
                                    fileHashMap.put(imgPath, file);
                                }else {
                                    fileHashMap.put(imgPath, file);

                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    QuestionIdsBean questionIdsBean = new QuestionIdsBean();
                    questionIdsBean.setCount(commitFileCount + "");
                    questionIdsBean.setQuestionId(localTextAnswersBean.getQuestionId());
                    questionIdsBeanList.add(questionIdsBean);


                    HomeworkCommitBean homeworkCommitBean = new HomeworkCommitBean();
                    homeworkCommitBean.setHomeworkId(homeworkId);
                    homeworkCommitBean.setClassId(UserUtils.getClassId());
                    //????????????StudentId????????????UserId
                    homeworkCommitBean.setStudentId(UserUtils.getUserId());
                    homeworkCommitBean.setQuestionId(localTextAnswersBean.getQuestionId());
                    QZXTools.logE("id=" + homeworkId + ";type=" + localTextAnswersBean.getQuestionType(), null);

                    homeworkCommitBean.setAnswerContent(localTextAnswersBean.getAnswerContent());
                    homeworkCommitBeanList.add(homeworkCommitBean);



                    break;
                case Constant.Judge_Item:
                    QuestionIdsBean questionIdsBean2 = new QuestionIdsBean();
                    questionIdsBean2.setCount("0");
                    questionIdsBean2.setQuestionId(localTextAnswersBean.getQuestionId());

                    questionIdsBeanList.add(questionIdsBean2);
                    HomeworkCommitBean homeworkCommitBean_judge = new HomeworkCommitBean();
                    homeworkCommitBean_judge.setHomeworkId(homeworkId);
                    homeworkCommitBean_judge.setClassId(UserUtils.getClassId());
                    //????????????StudentId????????????UserId
                    homeworkCommitBean_judge.setStudentId(UserUtils.getUserId());
                    homeworkCommitBean_judge.setQuestionId(localTextAnswersBean.getQuestionId());

                    if ("1".equals(byHand)) {
                        homeworkCommitBean_judge.setAnswerContent(localTextAnswersBean.getAnswerContent());

                        QZXTools.logE("id=" + homeworkId + ";type=" + localTextAnswersBean.getQuestionType()
                                + ";content=" + localTextAnswersBean.getAnswerContent(), null);
                    } else {
                        List<AnswerItem> bankJudge = localTextAnswersBean.getList();
                        for (AnswerItem answerItem : bankJudge) {
                            homeworkCommitBean_judge.setAnswerContent(answerItem.getContent());
                        }
                    }
                    homeworkCommitBeanList.add(homeworkCommitBean_judge);
                    break;
                case Constant.Linked_Line:
                    QuestionIdsBean questionIdsBean3 = new QuestionIdsBean();
                    questionIdsBean3.setCount("0");
                    questionIdsBean3.setQuestionId(localTextAnswersBean.getQuestionId());
                    questionIdsBeanList.add(questionIdsBean3);

                    HomeworkCommitBean homeworkCommitBean_linked = new HomeworkCommitBean();
                    homeworkCommitBean_linked.setHomeworkId(homeworkId);
                    homeworkCommitBean_linked.setClassId(UserUtils.getClassId());
                    //????????????StudentId????????????UserId
                    homeworkCommitBean_linked.setStudentId(UserUtils.getUserId());
                    homeworkCommitBean_linked.setQuestionId(localTextAnswersBean.getQuestionId());
                    QZXTools.logE("id=" + homeworkId + ";type=" + localTextAnswersBean.getQuestionType(), null);

                    homeworkCommitBean_linked.setAnswerContent(localTextAnswersBean.getAnswerContent());
                    homeworkCommitBeanList.add(homeworkCommitBean_linked);
                    break;
            }
        }
        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());
        Gson gson = new Gson();

        String answerlist = gson.toJson(homeworkCommitBeanList);
        String question_ids = gson.toJson(questionIdsBeanList);

        QZXTools.logE("answerlist=" + answerlist + ";question_ids=" + question_ids, null);

        //?????????json?????????

        mapParams.put("answerlist", answerlist);
        mapParams.put("studentid", UserUtils.getUserId());
        mapParams.put("classid", UserUtils.getClassId());
        mapParams.put("homeworkid", homeworkId);
        //??????????????????
        mapParams.put("question_ids", question_ids);
        mapParams.put("commitStatus ", commitStatus);
      QZXTools.logE("answerlist="+answerlist,null);
        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        OkHttp3_0Utils.getInstance().asyncPostMultiOkHttp(url, "question_files", mapParams, fileHashMap, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("IOException:" + e.getMessage(), e);
                //???????????????
                mHandler.sendEmptyMessage(Server_Error);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                QZXTools.logE("response:" + response.body().toString(), null);
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    // {"success":true,"errorCode":"1","msg":"???????????????","result":[],"total":0,"pageNo":0}
                    QZXTools.logE("commit questions resultJson=" + resultJson, null);

                    Gson gson = new Gson();
                    Map<String, Object> data = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                    }.getType());

                    if (types == 0) {
                        Message message = mHandler.obtainMessage();
                        message.what = Commit_Result_Show;
                        message.obj = data.get("msg");
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage();
                        message.what = Commit_Result_Show_Save;
                        message.obj = data.get("msg");
                        mHandler.sendMessage(message);
                    }


                } else {
                    mHandler.sendEmptyMessage(Error404);
                }
            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param questionBank
     * @param option 1????????? 0???????????????
     * @param curPosition
     */
    private void collectYeOrNo(QuestionBank questionBank, String option,int curPosition,NewKnowledgeQuestionView newKnowledgeQuestionView){
        QZXTools.logE("collectId==========" + questionBank.getCollectId(), null);
        QZXTools.logE("questionId==========" + questionBank.getId(), null);
        QZXTools.logE("HomeworkId==========" + questionBank.getHomeworkId(), null);
        QZXTools.logE("subjectId==========" + questionBank.getSubjectId(), null);
        QZXTools.logE("studentId==========" + UserUtils.getUserId(), null);
        QZXTools.logE("title==========" + questionBank.getHomeworkTitle()+"-"+getQuestionChannelTypeName(questionBank)+"-???"+(curPosition+1)+"???", null);
        QZXTools.logE("option==========" + option, null);

        String url = UrlUtils.BaseUrl + UrlUtils.CollectQuestionYesOrNo;

        Map<String, String> mapParams = new LinkedHashMap<>();
        mapParams.put("collectId", questionBank.getCollectId()+"");
        mapParams.put("questionId", questionBank.getId()+"");
        mapParams.put("homeworkId", questionBank.getHomeworkId());
        mapParams.put("subjectId", questionBank.getSubjectId());
        mapParams.put("studentId", UserUtils.getUserId());
        mapParams.put("title", questionBank.getHomeworkTitle()+"-"+getQuestionChannelTypeName(questionBank)+"-???"+(curPosition+1)+"???");
        mapParams.put("option", option);

        /**
         * post????????????????????????int????????????????????????????????????????????????????????????
         * */
        OkHttp3_0Utils.getInstance().asyncPostOkHttp(url, mapParams, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.popToast(HomeWorkDetailActivity.this, "?????????????????????", false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("commit questions resultJson=" + resultJson, null);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CollectQuestionByHandBean collectQuestionByHandBean = new Gson().fromJson(resultJson, CollectQuestionByHandBean.class);
                            if ("1".equals(collectQuestionByHandBean.getErrorCode())){
                                if ("1".equals(option)){//??????
                                    questionBank.setCollectId(collectQuestionByHandBean.getResult().get(0).getCollectId());
                                }else{//????????????
                                    questionBank.setCollectId(null);
                                }

                                questionBank.setIsCollect(option);
                                newKnowledgeQuestionView.setCollect(option);
                            }
                        }
                    });
                } else {
                    QZXTools.popToast(HomeWorkDetailActivity.this, "?????????????????????", false);
                }
            }
        });
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private String getQuestionChannelTypeName(QuestionBank questionBank){
        switch (questionBank.getQuestionChannelType()){
            case Constant.Single_Choose:
                return "?????????";
            case Constant.Multi_Choose:
                return "?????????";
            case Constant.Fill_Blank:
                return "?????????";
            case Constant.Subject_Item:
                return "?????????";
            case Constant.Linked_Line:
                return "?????????";
            case Constant.Judge_Item:
                return "?????????";
            default:
                return "";
        }
    }


}
