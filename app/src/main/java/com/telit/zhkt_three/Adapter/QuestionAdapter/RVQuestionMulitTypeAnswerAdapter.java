package com.telit.zhkt_three.Adapter.QuestionAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.HomeWork.WhiteBoardActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.CustomView.QuestionView.matching.ToLineView;
import com.telit.zhkt_three.CustomView.SubjectImagesView;
import com.telit.zhkt_three.JavaBean.FillBlankBean;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfo;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.AnswerItem;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.JavaBean.LineMatchBean;
import com.telit.zhkt_three.JavaBean.MulitBean;
import com.telit.zhkt_three.JavaBean.SingleBean;
import com.telit.zhkt_three.JavaBean.SubjeatSaveBean;
import com.telit.zhkt_three.JavaBean.SubjectBean;
import com.telit.zhkt_three.JavaBean.WorkOwnResult;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ViewUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.greendao.LocalTextAnswersBeanDao;
import com.telit.zhkt_three.greendao.SubjeatSaveBeanDao;
import com.telit.zhkt_three.listener.EdtextListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * author: qzx
 * Date: 2019/5/23 16:44
 * <p>
 * ????????????????????????????????????Adapter?????????????????????????????????????????????????????????????????????/????????????
 */
public class RVQuestionMulitTypeAnswerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ZBVPermission.PermPassResult {
    private static final String TAG = "RVQuestionTvAnswerAdapter";
    private Context mContext;

    //0?????????  1 ?????????  2 ?????????
    private String taskStatus;

    //??????????????????????????????
    private boolean isMistakesShown;
    private int types;
    private String comType;

    //??????????????????????????????
    private int cursorPos;
    //???????????????EditText????????????
    private String inputAfterText;
    //???????????????EditText?????????
    private boolean resetText;
    private int layoutPosition;

    private static final String[] needPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * ????????????????????????????????????????????????
     */
    private boolean isImageTask;

    private List<QuestionInfo> questionInfoList;

    private String homeworkId;

    private List<SubjectBean> subjectBeanList = new ArrayList<>();

    /**
     * ???????????????ID
     */
    public static String subjQuestionId;

    /**
     * ??????????????????item
     */
    private View firstChooseView = null;
    private QuestionInfo subjectQuestionInfo;

    /**
     * ??????homeworkid???????????????????????????homeworkid??????QuestionInfo?????????homeworkid
     */
    public void setQuestionInfoList(List<QuestionInfo> questionInfoList, String homeworkId, String showAnswerDate) {
        //???????????????????????????
        this.questionInfoList = questionInfoList;
        this.homeworkId = homeworkId;

    }

    private String xd;
    private String chid;
    private String difficulty;
    private String type;

    //??????????????? ?????????????????????????????????
    private List<LineMatchBean> lineMatchs = new ArrayList<>();
    //???????????????????????????
    // private List<SingleBean> singleBeans=new ArrayList<>();

    //?????????????????? ?????????
    private String saveTrack;

    /**
     * ??????????????????????????????:
     * ???????????????????????????????????????
     */
    public void fetchNeedParam(String xd, String subjective, String difficulty, String questionType) {
        this.xd = xd;
        chid = subjective;
        this.difficulty = difficulty;
        type = questionType;
        QZXTools.logE("xd=" + xd + ";chid=" + chid + ";difficulty=" + difficulty + ";type=" + type, null);
    }

    /**
     * @param status          ???????????????
     * @param isImageQuestion ????????????????????????
     * @param mistakesShown   ??????????????????????????????
     * @param types
     * @param comType
     */
    public RVQuestionMulitTypeAnswerAdapter(Context context, String status, boolean isImageQuestion, boolean mistakesShown, int types, String comType) {
        mContext = context;
        taskStatus = status;
        isImageTask = isImageQuestion;
        isMistakesShown = mistakesShown;
        this.types = types;
        this.comType = comType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == Constant.Single_Choose) {
            return new SingleChooseHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.single_choose_image_layout, viewGroup, false));
        } else if (i == Constant.Fill_Blank) {
            return new FillBlankHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.fill_blank_image_layout, null, false));
        } else if (i == Constant.Subject_Item) {
            return new SubjectItemHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.subject_item_image_show_layout, viewGroup, false));
        } else if (i == Constant.Linked_Line) {
            return new LinkedLineHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.linked_line_image_layout, viewGroup, false));
        } else if (i == Constant.Judge_Item) {
            return new JudgeItemHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.judgeselect_two_image_layout, viewGroup, false));
        } else if (i == Constant.Multi_Choose) {
            return new MultiChooseHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.mulit_choose_image_layout, viewGroup, false));
        }
        return null;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof SingleChooseHolder) {
            //   viewHolder.setIsRecyclable(false);
            //?????????
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();
            if (selectBeans.size() == 1) {
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 2) {
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 3) {
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 4) {
                ((SingleChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 5) {
                ((SingleChooseHolder) viewHolder).ll_single_image_five.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 6) {

                ((SingleChooseHolder) viewHolder).ll_single_image_sex.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_five.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((SingleChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
            }

            //0?????????  1 ?????????  2 ?????????
            //?????????????????????
            ((SingleChooseHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");

            if (taskStatus.equals(Constant.Todo_Status)) {
                //???????????????
                LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                if (linkLocal != null && linkLocal.questionId.equals(questionInfoList.get(i).getId())) {
                    Log.i(TAG, "onBindViewHolder: " + linkLocal);
                    String content = linkLocal.getList().get(0).content;
                    if (content.equals("A")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                    } else if (content.equals("B")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                    } else if (content.equals("C")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                    } else if (content.equals("D")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                    } else if (content.equals("E")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                    } else if (content.equals("F")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);

                    }
                } else {
                    if (selectBeans.size() == 1) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                    } else if (selectBeans.size() == 2) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                    } else if (selectBeans.size() == 3) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                    } else if (selectBeans.size() == 4) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                    } else if (selectBeans.size() == 5) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                    } else if (selectBeans.size() == 6) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                    }
                }


                ((SingleChooseHolder) viewHolder).tv_single_image_one.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_one.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(0).getId());
                            answerItem.setContent(selectBeans.get(0).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(0);
                            //singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);


                        }

                        if (selectBeans.size() == 2) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 3) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }


                    }
                });
                ((SingleChooseHolder) viewHolder).tv_single_image_two.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_two.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(1).getId());
                            answerItem.setContent(selectBeans.get(1).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(1);
                            //singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);
                        }

                        if (selectBeans.size() == 2) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        } else if (selectBeans.size() == 3) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                });
                ((SingleChooseHolder) viewHolder).tv_single_image_three.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_three.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(2).getId());
                            answerItem.setContent(selectBeans.get(2).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(2);
                            //singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);
                        }

                        if (selectBeans.size() == 3) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                });
                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_fore.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(3).getId());
                            answerItem.setContent(selectBeans.get(3).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(3);
                            // singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);
                        }

                        if (selectBeans.size() == 4) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                });
                ((SingleChooseHolder) viewHolder).tv_single_image_five.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_five.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(4).getId());
                            answerItem.setContent(selectBeans.get(4).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(4);
                            // singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);
                        }
                        if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                });
                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        SingleBean singleBean = new SingleBean();
                        if (((SingleChooseHolder) viewHolder).tv_single_image_sex.isSelected()) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            //singleBeans.remove(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);

                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            List<AnswerItem> answerItems = new ArrayList<>();
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(5).getId());
                            answerItem.setContent(selectBeans.get(5).getOptions());
                            answerItems.add(answerItem);
                            localTextAnswersBean.setList(answerItems);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                            singleBean.setId(questionInfoList.get(i).getId());
                            singleBean.setPosition(5);
                            // singleBeans.add(singleBean);
                            MyApplication.getInstance().getDaoSession().getSingleBeanDao().insertOrReplace(singleBean);
                        }

                        if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        }
                    }
                });

                //?????????????????????
                List<SingleBean> singleBeans = MyApplication.getInstance().getDaoSession().getSingleBeanDao().loadAll();
                for (int j = 0; j < singleBeans.size(); j++) {
                    String id = questionInfoList.get(i).getId();
                    if (singleBeans.get(j).getId().equals(questionInfoList.get(i).getId())) {
                        int position = singleBeans.get(j).getPosition();
                        if (position == 0) {
                            if (selectBeans.size() == 1) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                            } else if (selectBeans.size() == 2) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            } else if (selectBeans.size() == 3) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            } else if (selectBeans.size() == 4) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            } else if (selectBeans.size() == 5) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            } else if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            }

                        } else if (position == 1) {
                            if (selectBeans.size() == 2) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                            } else if (selectBeans.size() == 3) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            } else if (selectBeans.size() == 4) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            } else if (selectBeans.size() == 5) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            } else if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            }


                        } else if (position == 2) {
                            if (selectBeans.size() == 3) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                            } else if (selectBeans.size() == 4) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            } else if (selectBeans.size() == 5) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            } else if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            }


                        } else if (position == 3) {
                            if (selectBeans.size() == 4) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                            } else if (selectBeans.size() == 5) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            } else if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            }


                        } else if (position == 4) {
                            if (selectBeans.size() == 5) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                            } else if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            }


                        } else if (position == 5) {
                            if (selectBeans.size() == 6) {
                                ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                                ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);
                            }

                        }
                        break;
                    } else {
                        //????????????????????????????????????????????????
                        if (selectBeans.size() == 1) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        } else if (selectBeans.size() == 2) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 3) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                }


            } else {
                //????????????????????????????????????  ???????????????????????????????????????
                ((SingleChooseHolder) viewHolder).practice_select_judge_answer.setVisibility(VISIBLE);
                //?????????????????????
                if (taskStatus.equals("2")) {
                    String answerContent = questionInfoList.get(i).getOwnList().get(0).getAnswerContent();
                    if (answerContent.equals("A")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                    } else if (answerContent.equals("B")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                    } else if (answerContent.equals("C")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_three.setSelected(true);

                    } else if (answerContent.equals("D")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);

                    } else if (answerContent.equals("E")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_five.setSelected(true);

                    } else if (answerContent.equals("F")) {
                        ((SingleChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);

                    }

                    ((SingleChooseHolder) viewHolder).practice_select_judge_answer.setText("???????????????" + questionInfoList.get(i).getAnswer());
                }


            }


        } else if (viewHolder instanceof MultiChooseHolder) {
            //?????????
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();
            if (selectBeans.size() == 1) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 2) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 3) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 4) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 5) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_five.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 6) {
                ((MultiChooseHolder) viewHolder).ll_single_image_one.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_two.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_three.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_fore.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_five.setVisibility(VISIBLE);
                ((MultiChooseHolder) viewHolder).ll_single_image_sex.setVisibility(VISIBLE);
            }

            //0?????????  1 ?????????  2 ?????????
            //?????????????????????
            ((MultiChooseHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");
            if (taskStatus.equals(Constant.Todo_Status)) {
                List<String> checkViews = new ArrayList<>();
                //???????????????
                //?????????????????????,???????????????????????????????????????
                LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                if (linkLocal != null && linkLocal.questionId.equals(questionInfoList.get(i).getId()) && linkLocal.getList() != null) {
                    Log.i(TAG, "onBindViewHolder: " + linkLocal);
                    List<String> wordsType = new ArrayList<>();
                    for (int j = 0; j < linkLocal.getList().size(); j++) {
                        wordsType.add(linkLocal.getList().get(j).content);
                    }

                    if (wordsType.contains("A")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                        checkViews.add("tv_single_image_one");
                    }
                    if (wordsType.contains("B")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                        checkViews.add("tv_single_image_two");
                    }
                    if (wordsType.contains("C")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                        checkViews.add("tv_single_image_three");
                    }
                    if (wordsType.contains("D")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                        checkViews.add("tv_single_image_fore");
                    }
                    if (wordsType.contains("E")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                        checkViews.add("tv_single_image_five");
                    }
                    if (wordsType.contains("F")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);
                        checkViews.add("tv_single_image_sex");

                    }

                    if (wordsType.size() == 0) {
                        if (selectBeans.size() == 1) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        } else if (selectBeans.size() == 2) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 3) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    } else {
                        if (wordsType.contains("A")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                        }
                        if (wordsType.contains("B")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                        }
                        if (wordsType.contains("C")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                        }
                        if (wordsType.contains("D")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                        }
                        if (wordsType.contains("E")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                        }
                        if (wordsType.contains("F")) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);
                        }
                    }
                }

                //?????????????????????
                List<MulitBean> mulitBeans = MyApplication.getInstance().getDaoSession().getMulitBeanDao().loadAll();
                List<String> contents = new ArrayList<>();
                for (int j = 0; j < mulitBeans.size(); j++) {
                    String id = questionInfoList.get(i).getId();
                    if (mulitBeans.get(j).getId().equals(questionInfoList.get(i).getId())) {
                        String checkViewsCount = mulitBeans.get(j).getCheckViews();
                        if (checkViewsCount.contains("|")) {
                            checkViewsCount = checkViewsCount.substring(0, checkViewsCount.length() - 1);
                            String[] strings = checkViewsCount.split("|");
                            for (int k = 0; k < strings.length; k++) {
                                contents.add(strings[k]);
                            }
                        } else {
                            contents.add(checkViewsCount);
                        }

                        if (contents.contains("A")) {
                            if (selectBeans.size() == 1) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                            } else if (selectBeans.size() == 2) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            } else if (selectBeans.size() == 3) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            } else if (selectBeans.size() == 4) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            } else if (selectBeans.size() == 5) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            } else if (selectBeans.size() == 6) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);

                            }

                        }
                        if (contents.contains("B")) {
                            if (selectBeans.size() == 2) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                            } else if (selectBeans.size() == 3) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                            } else if (selectBeans.size() == 4) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                            } else if (selectBeans.size() == 5) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                            } else if (selectBeans.size() == 6) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                            }


                        }
                        if (contents.contains("C")) {
                            if (selectBeans.size() == 3) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                            } else if (selectBeans.size() == 4) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);

                            } else if (selectBeans.size() == 5) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);

                            } else if (selectBeans.size() == 6) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                            }


                        }
                        if (contents.contains("D")) {
                            if (selectBeans.size() == 4) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                            } else if (selectBeans.size() == 5) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);

                            } else if (selectBeans.size() == 6) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);

                            }


                        }
                        if (contents.contains("E")) {
                            if (selectBeans.size() == 5) {

                                ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                            } else if (selectBeans.size() == 6) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);

                            }


                        } else if (contents.contains("F")) {
                            if (selectBeans.size() == 6) {
                                ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);
                            }

                        }
                        break;
                    } else {
                        //????????????????????????????????????????????????
                        if (selectBeans.size() == 1) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                        } else if (selectBeans.size() == 2) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                        } else if (selectBeans.size() == 3) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                        } else if (selectBeans.size() == 4) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                        } else if (selectBeans.size() == 5) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                        } else if (selectBeans.size() == 6) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                        }
                    }
                }

                ((MultiChooseHolder) viewHolder).tv_single_image_one.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_one.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(false);
                            checkViews.remove("tv_single_image_one");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                            checkViews.add("tv_single_image_one");
                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }

                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                    }
                });
                ((MultiChooseHolder) viewHolder).tv_single_image_two.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_two.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(false);
                            checkViews.remove("tv_single_image_two");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);
                            checkViews.add("tv_single_image_two");
                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }
                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((MultiChooseHolder) viewHolder).tv_single_image_three.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_three.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(false);
                            checkViews.remove("tv_single_image_three");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);
                            checkViews.add("tv_single_image_three");

                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }
                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((MultiChooseHolder) viewHolder).tv_single_image_fore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_fore.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(false);
                            checkViews.remove("tv_single_image_fore");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);
                            checkViews.add("tv_single_image_fore");
                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }
                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((MultiChooseHolder) viewHolder).tv_single_image_five.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_five.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(false);
                            checkViews.remove("tv_single_image_five");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);
                            checkViews.add("tv_single_image_five");

                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }
                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((MultiChooseHolder) viewHolder).tv_single_image_sex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        MulitBean mulitBean = new MulitBean();
                        if (((MultiChooseHolder) viewHolder).tv_single_image_sex.isSelected()) {
                            ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(false);
                            checkViews.remove("tv_single_image_sex");

                        } else {
                            ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);
                            checkViews.add("tv_single_image_sex");
                        }
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        for (int j = 0; j < checkViews.size(); j++) {
                            String word = checkViews.get(j);
                            if (word.equals("tv_single_image_one")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(0).getId());
                                answerItem.setContent(selectBeans.get(0).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_two")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(1).getId());
                                answerItem.setContent(selectBeans.get(1).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_three")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(2).getId());
                                answerItem.setContent(selectBeans.get(2).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_fore")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(3).getId());
                                answerItem.setContent(selectBeans.get(3).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_five")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(4).getId());
                                answerItem.setContent(selectBeans.get(4).getOptions());
                                answerItems.add(answerItem);

                            } else if (word.equals("tv_single_image_sex")) {
                                AnswerItem answerItem = new AnswerItem();
                                answerItem.setItemId(selectBeans.get(5).getId());
                                answerItem.setContent(selectBeans.get(5).getOptions());
                                answerItems.add(answerItem);

                            }

                            localTextAnswersBean.setList(answerItems);
                        }
                        mulitBean.setId(questionInfoList.get(i).getId());
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int j = 0; j < answerItems.size(); j++) {
                            stringBuffer.append(answerItems.get(j).content);
                            stringBuffer.append("|");
                        }
                        mulitBean.setCheckViews(stringBuffer.toString());
                        MyApplication.getInstance().getDaoSession().getMulitBeanDao().insertOrReplace(mulitBean);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
            } else {

                //????????????????????????????????????  ???????????????????????????????????????
                ((MultiChooseHolder) viewHolder).practice_select_judge_answer.setVisibility(VISIBLE);
                List<String> wordLists = new ArrayList<>();
                //?????????????????????
                if (taskStatus.equals("2")) {
                    List<WorkOwnResult> ownList = questionInfoList.get(i).getOwnList();
                    for (WorkOwnResult workOwnResult : ownList) {
                        wordLists.add(workOwnResult.getAnswerContent());
                    }
                    // String answerContent = questionInfoList.get(i).getOwnList().get(0).getAnswerContent();
                    if (wordLists.contains("A")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_one.setSelected(true);
                    }
                    if (wordLists.contains("B")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_two.setSelected(true);

                    }
                    if (wordLists.contains("C")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_three.setSelected(true);

                    }
                    if (wordLists.contains("D")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_fore.setSelected(true);

                    }
                    if (wordLists.contains("E")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_five.setSelected(true);

                    }
                    if (wordLists.contains("F")) {
                        ((MultiChooseHolder) viewHolder).tv_single_image_sex.setSelected(true);

                    }

                    ((MultiChooseHolder) viewHolder).practice_select_judge_answer.setText("???????????????" + questionInfoList.get(i).getAnswer());
                }

            }


        } else if (viewHolder instanceof FillBlankHolder) {
            //todo  ??????????????????????????????
            viewHolder.setIsRecyclable(false);
            //?????????
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();
            if (selectBeans.size() == 1) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 2) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_two.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 3) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_two.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_three.setVisibility(VISIBLE);

            } else if (selectBeans.size() == 4) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_two.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_three.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_fore.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 5) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_two.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_three.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_fore.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_five.setVisibility(VISIBLE);
            } else if (selectBeans.size() == 6) {
                ((FillBlankHolder) viewHolder).ll_fill_balank_one.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_two.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_three.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_fore.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_five.setVisibility(VISIBLE);
                ((FillBlankHolder) viewHolder).ll_fill_balank_sex.setVisibility(VISIBLE);
            }

            //0?????????  1 ?????????  2 ?????????
            //?????????????????????
            ((FillBlankHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" +
                    questionInfoList.size() + "???");

            //???????????????
            LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();


            if (linkLocal != null && linkLocal.questionId.equals(questionInfoList.get(i).getId()) && linkLocal.getList() != null) {
                Log.i(TAG, "onBindViewHolder: " + linkLocal);

                if (linkLocal.getList().size() == 6) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(linkLocal.getList().get(1).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(linkLocal.getList().get(2).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(linkLocal.getList().get(3).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_five.setText(linkLocal.getList().get(4).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_sex.setText(linkLocal.getList().get(5).content);

                } else if (linkLocal.getList().size() == 5) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(linkLocal.getList().get(1).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(linkLocal.getList().get(2).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(linkLocal.getList().get(3).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_five.setText(linkLocal.getList().get(4).content);

                } else if (linkLocal.getList().size() == 4) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(linkLocal.getList().get(1).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(linkLocal.getList().get(2).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(linkLocal.getList().get(3).content);

                } else if (linkLocal.getList().size() == 3) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(linkLocal.getList().get(1).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(linkLocal.getList().get(2).content);

                } else if (linkLocal.getList().size() == 2) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                    ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(linkLocal.getList().get(1).content);


                } else if (linkLocal.getList().size() == 1) {
                    ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(linkLocal.getList().get(0).content);
                }

            }

            if (taskStatus.equals(Constant.Todo_Status)) {

                //?????????
           /*     List<FillBlankBean> fillBlankBeans = MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().loadAll();
                if (fillBlankBeans!=null){
                    for (int j = 0; j < fillBlankBeans.size(); j++) {
                        if (fillBlankBeans.get(j).getId().equals(questionInfoList.get(i).getId())){
                            List<AnswerItem> list = fillBlankBeans.get(j).getList();
                            if (selectBeans.size() == 6) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(list.get(1).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(list.get(2).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(list.get(3).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_five.setText(list.get(4).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_sex.setText(list.get(5).getContent());
                                break;
                            }
                            if (selectBeans.size() == 5) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(list.get(1).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(list.get(2).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(list.get(3).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_five.setText(list.get(4).getContent());

                                break;
                            }
                            if (selectBeans.size() == 4) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(list.get(1).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(list.get(2).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText(list.get(3).getContent());

                            }
                            if (selectBeans.size() == 3) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(list.get(1).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText(list.get(2).getContent());
                                break;
                            }
                            if (selectBeans.size() == 2) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText(list.get(1).getContent());
                                break;
                            }
                            if (selectBeans.size() == 1) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText(list.get(0).getContent());
                                break;
                            }



                        }else {
                            //????????????
                            if (selectBeans.size() == 6) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_five.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_sex.setText("");
                                break;

                            }
                            if (selectBeans.size() == 5) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_five.setText("");
                                break;

                            }
                            if (selectBeans.size() == 4) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_fore.setText("");

                                break;
                            }
                            if (selectBeans.size() == 3) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_three.setText("");
                                break;

                            }
                            if (selectBeans.size() == 2) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                ((FillBlankHolder) viewHolder).et_fill_balank_two.setText("");

                                break;

                            }
                            if (selectBeans.size() == 1) {
                                ((FillBlankHolder) viewHolder).et_fill_balank_one.setText("");
                                break;
                            }
                        }
                    }
                }*/


       /*         if (!ids.contains(questionInfoList.get(i).getId())){

                }*/


                List<Integer> indexs = new ArrayList<>();
                HashMap<Integer, String> contentMaps = new HashMap<>();

                ((FillBlankHolder) viewHolder).et_fill_balank_one.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(0);
                        contentMaps.put(0, ((FillBlankHolder) viewHolder).et_fill_balank_one.getText().toString().trim());


                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();


                        List<String> answers = new ArrayList<>();

                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }


                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                        //Log.i(TAG, "afterTextChanged: "+connects);
                    }
                });
                ((FillBlankHolder) viewHolder).et_fill_balank_two.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(1);
                        contentMaps.put(1, ((FillBlankHolder) viewHolder).et_fill_balank_two.getText().toString().trim());
                        // connects.add(((FillBlankHolder) viewHolder).et_fill_balank_two.getText().toString().trim());

                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }


                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((FillBlankHolder) viewHolder).et_fill_balank_three.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(2);
                        contentMaps.put(2, ((FillBlankHolder) viewHolder).et_fill_balank_three.getText().toString().trim());
                        //connects.add(((FillBlankHolder) viewHolder).et_fill_balank_three.getText().toString().trim());

                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }

                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((FillBlankHolder) viewHolder).et_fill_balank_fore.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(3);
                        contentMaps.put(3, ((FillBlankHolder) viewHolder).et_fill_balank_fore.getText().toString().trim());
                        // connects.add(((FillBlankHolder) viewHolder).et_fill_balank_fore.getText().toString().trim());

                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }

                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((FillBlankHolder) viewHolder).et_fill_balank_five.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(4);
                        contentMaps.put(4, ((FillBlankHolder) viewHolder).et_fill_balank_five.getText().toString().trim());
                        //  connects.add(((FillBlankHolder) viewHolder).et_fill_balank_five.getText().toString().trim());

                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }
                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });
                ((FillBlankHolder) viewHolder).et_fill_balank_sex.addTextChangedListener(new EdtextListener() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        indexs.add(5);
                        contentMaps.put(5, ((FillBlankHolder) viewHolder).et_fill_balank_sex.getText().toString().trim());
                        //  connects.add(((FillBlankHolder) viewHolder).et_fill_balank_sex.getText().toString().trim());

                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        FillBlankBean fillBlankBean = new FillBlankBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        List<AnswerItem> answerItems = new ArrayList<>();
                        List<String> answers = new ArrayList<>();
                        for (int j = 0; j < selectBeans.size(); j++) {
                            AnswerItem answerItem = new AnswerItem();
                            answerItem.setItemId(selectBeans.get(j).getId());
                            // blanknum???1???????????????????????????????????????????????????
                            answerItem.setBlanknum((j + 1) + "");
                            //?????????????????????

                            if (indexs.contains(j)) {

                                answerItem.setContent(contentMaps.get(j));
                                answerItems.add(answerItem);
                            } else {

                                //?????????????????????????????????????????????
                                if (linkLocal != null && linkLocal.getList() != null) {
                                    if (!TextUtils.isEmpty(linkLocal.getList().get(j).getContent())) {
                                        answerItem.setItemId(linkLocal.getList().get(j).getItemId());
                                        // blanknum???1???????????????????????????????????????????????????
                                        answerItem.setBlanknum(linkLocal.getList().get(j).getBlanknum());
                                        //?????????????????????
                                        answerItem.setContent(linkLocal.getList().get(j).getContent());
                                        answerItems.add(answerItem);
                                    } else {
                                        answerItem.setContent("");

                                        answerItems.add(answerItem);
                                    }
                                } else {
                                    answerItem.setContent("");
                                    answerItems.add(answerItem);
                                }

                            }


                            answers.add(s.toString());
                        }

                        localTextAnswersBean.setList(answerItems);
                        //????????????????????????????????????
                        localTextAnswersBean.setAnswers(answers);
//                                QZXTools.logE("fill blank Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        fillBlankBean.setId(questionInfoList.get(i).getId());
                        fillBlankBean.setList(answerItems);
                        MyApplication.getInstance().getDaoSession().getFillBlankBeanDao().insertOrReplace(fillBlankBean);
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }
                });


            } else {
                //??????????????????

                ((FillBlankHolder) viewHolder).ll_show_quint.setVisibility(VISIBLE);
                List<String> answers = new ArrayList<>();
                String answer = questionInfoList.get(i).getAnswer();
                if (answer.contains("|")) {
                    String[] strings = answer.split("\\|");

                    for (int j = 0; j < strings.length; j++) {
                        answers.add(strings[j]);
                    }
                } else {
                    answers.add(answer);
                }

                if (selectBeans.size() == 6) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_two.setText(answers.get(1));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_three.setText(answers.get(2));
                    ;
                    ((FillBlankHolder) viewHolder).tv_fill_quint_fore.setText(answers.get(3));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_five.setText(answers.get(4));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_six.setText(answers.get(5));
                }
                if (selectBeans.size() == 5) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_two.setText(answers.get(1));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_three.setText(answers.get(2));
                    ;
                    ((FillBlankHolder) viewHolder).tv_fill_quint_fore.setText(answers.get(3));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_five.setText(answers.get(4));
                    ((FillBlankHolder) viewHolder).ll_fill_quint_six.setVisibility(GONE);

                }
                if (selectBeans.size() == 4) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_two.setText(answers.get(1));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_three.setText(answers.get(2));
                    ;
                    ((FillBlankHolder) viewHolder).tv_fill_quint_fore.setText(answers.get(3));
                    ((FillBlankHolder) viewHolder).ll_fill_quint_five.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_six.setVisibility(GONE);

                }
                if (selectBeans.size() == 3) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_two.setText(answers.get(1));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_three.setText(answers.get(2));
                    ((FillBlankHolder) viewHolder).ll_fill_quint_fore.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_five.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_six.setVisibility(GONE);

                }
                if (selectBeans.size() == 2) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).tv_fill_quint_two.setText(answers.get(1));
                    ((FillBlankHolder) viewHolder).ll_fill_quint_three.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_fore.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_five.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_six.setVisibility(GONE);

                }
                if (selectBeans.size() == 1) {
                    ((FillBlankHolder) viewHolder).tv_fill_quint_one.setText(answers.get(0));
                    ((FillBlankHolder) viewHolder).ll_fill_quint_two.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_three.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_fore.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_five.setVisibility(GONE);
                    ((FillBlankHolder) viewHolder).ll_fill_quint_six.setVisibility(GONE);


                }

            }

        } else if (viewHolder instanceof JudgeItemHolder) {
            //todo ???????????????????????????
            viewHolder.setIsRecyclable(false);
            //?????????
            // ((JudgeItemHolder) viewHolder)
            ((JudgeItemHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");

            //0?????????  1 ?????????  2 ?????????
            if (taskStatus.equals(Constant.Todo_Status)) {
                //???????????????
                LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
                Log.i(TAG, "onBindViewHolder: " + linkLocal);
                if (linkLocal != null) {
                    if (linkLocal.getAnswerContent().equals("0")) {
                        ((JudgeItemHolder) viewHolder).option_do_tv_one.setSelected(true);
                    } else {
                        ((JudgeItemHolder) viewHolder).option_do_tv_two.setSelected(true);
                    }
                }

                ((JudgeItemHolder) viewHolder).option_do_tv_one.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((JudgeItemHolder) viewHolder).option_do_tv_two.setSelected(false);
                        if (((JudgeItemHolder) viewHolder).option_do_tv_one.isSelected()) {
                            ((JudgeItemHolder) viewHolder).option_do_tv_one.setSelected(false);
                            MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((JudgeItemHolder) viewHolder).option_do_tv_one.setSelected(true);

                            //????????????????????????????????? ????????????????????????
                            //-------------------------?????????????????????????????????id
                            LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            localTextAnswersBean.setAnswerContent(0 + "");
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                        }
                    }
                });

                ((JudgeItemHolder) viewHolder).option_do_tv_two.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((JudgeItemHolder) viewHolder).option_do_tv_one.setSelected(false);
                        if (((JudgeItemHolder) viewHolder).option_do_tv_two.isSelected()) {
                            ((JudgeItemHolder) viewHolder).option_do_tv_two.setSelected(false);
                            MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                        } else {
                            ((JudgeItemHolder) viewHolder).option_do_tv_two.setSelected(true);

                            //-------------------------?????????????????????????????????id
                            LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            localTextAnswersBean.setAnswerContent(1 + "");
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                        }
                    }
                });

            } else {
                ((JudgeItemHolder) viewHolder).ll_current_quint_show.setVisibility(VISIBLE);
                //????????????????????????????????????
                List<WorkOwnResult> ownList = questionInfoList.get(i).getOwnList();
                if (ownList != null && ownList.size() > 0) {
                    WorkOwnResult workOwnResult = questionInfoList.get(i).getOwnList().get(0);
                    String answerContent = workOwnResult.getAnswerContent();
                    if (answerContent.equals("0")) {
                        ((JudgeItemHolder) viewHolder).option_do_tv_one.setSelected(true);
                    } else {
                        ((JudgeItemHolder) viewHolder).option_do_tv_two.setSelected(true);
                    }
                }
                //??????????????????
                String answer = questionInfoList.get(i).getAnswer();
                if (answer.equals("0")) {

                    Glide.with(mContext).load(R.mipmap.check_current_two).into(((JudgeItemHolder) viewHolder).iv_current_quint_show);
                } else {
                    Glide.with(mContext).load(R.mipmap.check_err_ome).into(((JudgeItemHolder) viewHolder).iv_current_quint_show);

                }
                //

            }

        } else if (viewHolder instanceof SubjectItemHolder) {
            //?????????
            //todo ???????????????????????????
            viewHolder.setIsRecyclable(false);

            QZXTools.logD("???????????????" + viewHolder.getLayoutPosition());
            ((SubjectItemHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");
            //0?????????  1 ?????????  2 ?????????
            if (taskStatus.equals(Constant.Todo_Status)) {
                //???????????????
                LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
                Log.i(TAG, "onBindViewHolder: " + linkLocal);

                siv_images.setTag(questionInfoList.get(i).getId());

                //??????????????????
                if (linkLocal != null && !TextUtils.isEmpty(linkLocal.getAnswerContent())) {
                    subjective_input.setText(linkLocal.getAnswerContent());
                }


                //??????????????????????????????????????????
                if (notifyItem) {
                    if (i == layoutPosition) {
                        notifyItem = false;

                        SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                                .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId())).unique();
                        if (saveBean != null) {
                            if (!TextUtils.isEmpty(saveBean.getImages())) {
                                String images = saveBean.getImages();
                                String[] strings = images.split("\\|");
                                Log.i(TAG, "onClick: " + strings);
                                imgFilePathList.clear();
                                for (String string : strings) {
                                    imgFilePathList.add(string);
                                }

                                //???????????????????????????recycleview
                                if (imgFilePathList.size() > 0)
                                    siv_images.fromCameraCallback(imgFilePathList);


                                //????????????

                                //-------------------------?????????????????????????????????id
                                LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                                localTextAnswersBean.setHomeworkId(homeworkId);
                                localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                                localTextAnswersBean.setUserId(UserUtils.getUserId());
                                localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                                localTextAnswersBean.setAnswerContent("");
                                localTextAnswersBean.setUserId(UserUtils.getUserId());
                                localTextAnswersBean.setAnswer(questionInfoList.get(i).getAnswer());
                                localTextAnswersBean.setImageList(imgFilePathList);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                                //???????????????????????????
                                MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                            }
                        }
                    } else {
                        if (taskStatus.equals(Constant.Save_Status)){
                            siv_images.setTag(questionInfoList.get(i).getId());
                        }

                        SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                                .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId())).unique();
                        if (saveBean != null) {
                            if (!TextUtils.isEmpty(saveBean.getImages())) {
                                String images = saveBean.getImages();
                                String[] strings = images.split("\\|");
                                Log.i(TAG, "onClick: " + strings);
                                imgFilePathList.clear();
                                for (String string : strings) {
                                    imgFilePathList.add(string);
                                }

                                if (imgFilePathList.size() > 0) {

                                    siv_images.fromCameraCallback(imgFilePathList);
                                }

                            }
                        }
                    }

                } else {
                    //???????????????
                    SubjeatSaveBean saveBeanTo = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                            .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId()))
                            .where(SubjeatSaveBeanDao.Properties.LayoutPosition.eq(i))
                            .unique();

                    if (saveBeanTo != null && saveBeanTo.getLayoutPosition() == i) {
                        if (!TextUtils.isEmpty(saveBeanTo.getImages())) {
                            String images = saveBeanTo.getImages();
                            String[] strings = images.split("\\|");
                            Log.i(TAG, "onClick: " + strings);
                            imgFilePathList.clear();
                            for (String string : strings) {
                                imgFilePathList.add(string);
                            }

                            if (imgFilePathList.size() > 0) {

                                siv_images.fromCameraCallback(imgFilePathList);
                            }


                        } else {
                            imgFilePathList.clear();
                            siv_images.fromCameraCallback(imgFilePathList);
                        }
                    } else {
                 /*       imgFilePathList.clear();
                        SubjectImageAdapter subjectImageAdapter=new SubjectImageAdapter(mContext,imgFilePathList);
                        rv_subjective_imgs_layout.setAdapter(subjectImageAdapter);
                        rv_subjective_imgs_layout.setLayoutManager(new GridLayoutManager(mContext,3));*/
                    }
                }

                //??????
                ((SubjectItemHolder) viewHolder).subjective_camera.setOnClickListener(new View.OnClickListener() {


                    @Override
                    public void onClick(View v) {
                        layoutPosition = i;
                        Log.i(TAG, "onClick:layoutPosition= " + layoutPosition + ".............." + i);
                        subjQuestionId = questionInfoList.get(layoutPosition).getId();
                        subjectQuestionInfo = questionInfoList.get(layoutPosition);

                        SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                                .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId())).unique();
                        if (saveBean != null) {
                            if (!TextUtils.isEmpty(saveBean.getImages())) {
                                String images = saveBean.getImages();
                                String[] strings = images.split("\\|");
                                Log.i(TAG, "onClick: " + strings);
                                imgFilePathList.clear();
                                for (String string : strings) {
                                    imgFilePathList.add(string);
                                }
                            }
                        } else {
                            imgFilePathList.clear();
                        }

                        if (imgFilePathList.size() >= 3) {
                            QZXTools.popCommonToast(MyApplication.getInstance(), "??????????????????????????????", false);
                            return;
                        }
                        ZBVPermission.getInstance().setPermPassResult(RVQuestionMulitTypeAnswerAdapter.this);
                        if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                            ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                        } else {
                            //??????????????????
                            QZXTools.logD("?????????????????????????????????");
                            openCamera();
                        }
                    }
                });
                //?????????????????????
                ((SubjectItemHolder) viewHolder).subjective_board.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        layoutPosition = viewHolder.getLayoutPosition();
                        subjQuestionId = questionInfoList.get(layoutPosition).getId();
                        subjectQuestionInfo = questionInfoList.get(layoutPosition);


                        SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                                .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId())).unique();
                        if (saveBean != null) {
                            if (!TextUtils.isEmpty(saveBean.getImages())) {
                                String images = saveBean.getImages();
                                String[] strings = images.split("\\|");
                                Log.i(TAG, "onClick: " + strings);
                                imgFilePathList.clear();
                                for (String string : strings) {
                                    imgFilePathList.add(string);
                                }
                            }
                        } else {
                            imgFilePathList.clear();
                        }


                        if (imgFilePathList.size() >= 3) {
                            QZXTools.popCommonToast(MyApplication.getInstance(), "??????????????????????????????", false);
                            return;
                        }

                        ZBVPermission.getInstance().setPermPassResult(RVQuestionMulitTypeAnswerAdapter.this);
                        if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                            ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                        } else {
                            Intent intent = new Intent(mContext, WhiteBoardActivity.class);
                            intent.putExtra("extra_info", questionInfoList.get(i).getId());
                            mContext.startActivity(intent);
                        }
                    }
                });

                //??????????????????????????????

                subjective_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if (!resetText) {
                            cursorPos = subjective_input.getSelectionEnd();
                            // ?????????s.toString()???????????????s??????????????????s???
                            // ?????????inputAfterText???s??????????????????????????????????????????s????????????
                            // inputAfterText????????????????????????????????????????????????
                            inputAfterText = s.toString();
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!resetText) {
                            if (count >= 2) {//????????????????????????????????????2
                                if ((cursorPos + count) <= s.toString().trim().length()) {
                                    CharSequence input = s.subSequence(cursorPos, cursorPos + count);
                                    if (ViewUtils.containsEmoji(input.toString())) {
                                        resetText = true;
                                        Toast.makeText(mContext, "???????????????Emoji????????????", Toast.LENGTH_SHORT).show();
                                        //?????????????????????????????????????????????????????????????????????
                                        subjective_input.setText(inputAfterText);
                                        QZXTools.logE("inputAfterText:" + inputAfterText, null);
                                        CharSequence text = subjective_input.getText();
                                        if (text.length() > 0) {
                                            if (text instanceof Spannable) {
                                                Spannable spanText = (Spannable) text;
                                                Selection.setSelection(spanText, text.length());
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            resetText = false;
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        localTextAnswersBean.setHomeworkId(homeworkId);
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        localTextAnswersBean.setAnswerContent(s.toString().trim());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setAnswer(questionInfoList.get(i).getAnswer());


                        SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                                .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(questionInfoList.get(i).getId())).unique();
                        if (saveBean != null) {
                            if (!TextUtils.isEmpty(saveBean.getImages())) {
                                String images = saveBean.getImages();
                                String[] strings = images.split("\\|");
                                Log.i(TAG, "onClick: " + strings);
                                imgFilePathList.clear();
                                for (String string : strings) {
                                    imgFilePathList.add(string);
                                }
                            }
                        }


                        localTextAnswersBean.setImageList(imgFilePathList);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                        //-------------------------?????????????????????????????????id

                        QZXTools.logE("?????????????????????:" + new Gson().toJson(localTextAnswersBean), null);
                    }
                });

            } else {
                //????????????????????????
                subjective_answer_tool_layout.setVisibility(GONE);
                siv_images.setHideDel();
                //?????????????????????

                List<WorkOwnResult> ownList = questionInfoList.get(i).getOwnList();
                if (ownList!=null && ownList.size()>0){
                    String images = ownList.get(0).getAttachment();
                    if (!TextUtils.isEmpty(images)) {

                        String[] strings = images.split("\\|");
                        Log.i(TAG, "onClick: " + strings);
                        imgFilePathList.clear();
                        for (String string : strings) {
                            imgFilePathList.add(string);
                        }

                        if (imgFilePathList.size() > 0) {

                            siv_images.fromCameraCallback(imgFilePathList);
                        }


                    } else {
                        imgFilePathList.clear();
                        siv_images.fromCameraCallback(imgFilePathList);
                    }
                }



                //?????????????????? ????????????

                if (ownList != null && !TextUtils.isEmpty(  ownList.get(0).getAnswerContent())) {

                    subjective_input.setText("????????????: "+ownList.get(0).getAnswerContent());
                }
                //????????????
                subjective_input_teacher.setVisibility(VISIBLE);
                subjective_input_teacher.setText("????????????: "+questionInfoList.get(i).getAnswer());

            }

        } else if (viewHolder instanceof LinkedLineHolder) {
            //?????????
            // viewHolder.setIsRecyclable(false);
            QZXTools.logE("ToLine viewHolder instanceof LinkedLineHolder......" + questionInfoList.get(i), null);

            Log.i("qin008", "onBindViewHolder: " + questionInfoList.get(i));
            List<Integer> leftPositions = new ArrayList<>();
            List<Integer> rightPositions = new ArrayList<>();
            //???????????????????????? ??????view ??????????????????view ?????????
            List<View> leftViews = new ArrayList<>();
            //?????????
            List<QuestionInfo.LeftListBean> leftList = questionInfoList.get(i).getLeftList();
            List<QuestionInfo.RightListBean> rightList = questionInfoList.get(i).getRightList();
            LineLeftAdapter lineLeftAdapter = new LineLeftAdapter(mContext, leftList, rightList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mContext) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            };

            ((LinkedLineHolder) viewHolder).rv_matching_show.setLayoutManager(layoutManager);
            ((LinkedLineHolder) viewHolder).rv_matching_show.setAdapter(lineLeftAdapter);
            ((LinkedLineHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");


            //0?????????  1 ?????????  2 ?????????
            if (taskStatus.equals(Constant.Todo_Status)) {
                //?????????????????????
                ((LinkedLineHolder) viewHolder).matching_reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (firstChooseView != null) {
                            firstChooseView = null;
                        }
                        saveTrack = "";
                        ((LinkedLineHolder) viewHolder).matching_toLine.resetDrawLine();

                        //????????????????????????????????????
                        Iterator<LineMatchBean> iterator = lineMatchs.iterator();
                        while (iterator.hasNext()) {
                            LineMatchBean lineMatchBean = iterator.next();
                            if (lineMatchBean.getTypeId().equals(questionInfoList.get(i).getId())) {
                                iterator.remove();
                                MyApplication.getInstance().getDaoSession().getLineMatchBeanDao().delete(lineMatchBean);
                            }
                        }

                        MyApplication.getInstance().getDaoSession().getLineMatchBeanDao().deleteAll();
                        //TODO ????????????????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteAll();
                        //todo ?????????????????????????????????saveTrack  ???????????????????????????????????????

                    }


                });

                //??????????????????
                lineLeftAdapter.setOnLeftOnClickListener(new LineLeftAdapter.onLeftOnClickListener() {
                    private TextView rightTextView;
                    private TextView liftTextView;
                    float sx;
                    float sy;
                    float ex;
                    float ey;
                    Path path = null;
                    int leftPosition;
                    int rightPosition;
                    boolean isLiftContains = false;
                    boolean isRightContains = false;

                    @Override
                    public void onLeftItemCheck(int position) {
                        //  ((LinkedLineHolder) viewHolder).matching_toLine.resetDrawLine();
                        View view = layoutManager.findViewByPosition(position);
                        //???????????????????????????
                        StringBuffer stringBuffer = new StringBuffer();
                        //????????????
                        stringBuffer.setLength(0);
                        LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                        Log.i(TAG, "onBindViewHolder: " + linkLocal);
                        if (linkLocal != null) {
                            //???????????????????????????  ??????id ?????????????????????????????????????????????
                            //????????????????????????????????????
                            String ownAnswer = linkLocal.getAnswerContent();
                            stringBuffer.append(ownAnswer);
                        }
                        if (!TextUtils.isEmpty(saveTrack)) {
                            //stringBuffer.append(saveTrack);
                            stringBuffer.append("|");
                        }

                        liftTextView = view.findViewById(R.id.tv_item_line_word_left);
                        if (isRightContains) {
                            //????????????????????????????????????????????????????????????????????????
                            isRightContains = false;
                            return;
                        }
                        //???????????????????????????
                        if (firstChooseView != null && firstChooseView == liftTextView) {
                            isLiftContains = true;
                            return;
                        }
                        isLiftContains = false;
                        //????????????????????????????????????????????????,?????????????????????????????????
                        if (leftPositions.contains(position) || ((LinkedLineHolder) viewHolder).matching_toLine.isAnimRunning()) {
                            return;
                        }


                        if (firstChooseView == null) {
                            firstChooseView = liftTextView;
                            sx = liftTextView.getLeft() + liftTextView.getWidth();
                            sy = view.getTop() + liftTextView.getHeight() * 1.0f / 2.0f;
                            path = new Path();
                            leftPosition = position;
                            liftTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_line_bg_with_border));
                        }
                        if (firstChooseView == rightTextView) {

                            //???????????????????????????view
                            ex = liftTextView.getLeft() + liftTextView.getWidth();
                            ey = view.getTop() + liftTextView.getHeight() * 1.0f / 2.0f;
                            path.addCircle(sx, sy, 10, Path.Direction.CW);
                            path.moveTo(sx, sy);
                            path.lineTo(ex, ey);
                            path.addCircle(ex, ey, 10, Path.Direction.CW);

                            //??????Path   ???????????????
                            ((LinkedLineHolder) viewHolder).matching_toLine.getDrawPath(path);
                            //??????????????????view
                            firstChooseView = null;
                            //?????????????????????????????????????????????????????????????????????
                            leftPositions.add(position);
                            rightPositions.add(rightPosition);
                            rightTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_line_item_bg));

                            //????????????????????????????????? ???????????? ?????????????????????????????????????????????????????????
                            LineMatchBean lineMatchBean = new LineMatchBean();
                            lineMatchBean.setTypeId(questionInfoList.get(i).getId());
                            lineMatchBean.setPosition(i);
                            lineMatchBean.setStartX(sx);
                            lineMatchBean.setStartY(sy);
                            lineMatchBean.setEndX(ex);
                            lineMatchBean.setEndY(ey);

                            lineMatchBean.setLeftId(leftList.get(position).getId());
                            lineMatchBean.setRightId(rightList.get(rightPosition).getId());

                            lineMatchs.add(lineMatchBean);
                            MyApplication.getInstance().getDaoSession().getLineMatchBeanDao().insertOrReplace(lineMatchBean);
                            // saveTrack ???????????????????????? ?????????  ??????????????????????????????id?????????

                            stringBuffer.append(questionInfoList.get(i).getRightList().get(rightPosition).getId());
                            stringBuffer.append(",");
                            stringBuffer.append(questionInfoList.get(i).getLeftList().get(position).getId());
                            saveTrack = stringBuffer.toString();

                            //?????????????????????????????????????????????????????????
                            //-------------------------?????????????????????????????????id   ????????????????????????id ?????????
                            LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            localTextAnswersBean.setAnswerContent(saveTrack);
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);


                            //?????????????????????saveTrack
                            ((LinkedLineHolder) viewHolder).matching_toLine.setLocalSave(homeworkId, questionInfoList.get(i), saveTrack);

                        }


                    }

                    @Override
                    public void onRightItemClick(int position) {
                        RelativeLayout view = (RelativeLayout) layoutManager.findViewByPosition(position);
                        rightTextView = view.findViewById(R.id.tv_item_line_word_right);

                        StringBuffer stringBuffer = new StringBuffer();
                        //????????????
                        stringBuffer.setLength(0);
                        LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                        Log.i(TAG, "onBindViewHolder: " + linkLocal);
                        if (linkLocal != null) {
                            //???????????????????????????  ??????id ?????????????????????????????????????????????
                            //????????????????????????????????????
                            String ownAnswer = linkLocal.getAnswerContent();
                            stringBuffer.append(ownAnswer);
                        }
                        if (!TextUtils.isEmpty(saveTrack)) {
                            //  stringBuffer.append(saveTrack);
                            stringBuffer.append("|");
                        }
                        if (isLiftContains) {
                            //???????????????????????????????????????????????????????????????????????? ???????????????????????????
                            isLiftContains = false;
                            return;
                        }
                        //???????????????????????????
                        if (firstChooseView != null && firstChooseView == rightTextView) {
                            return;
                        }

                        //????????????????????????????????????????????????,?????????????????????????????????
                        if (rightPositions.contains(position) || ((LinkedLineHolder) viewHolder).matching_toLine.isAnimRunning()) {
                            isRightContains = true;
                            return;
                        }
                        isRightContains = false;
                        //???????????????????????????view
                        if (firstChooseView == null) {
                            firstChooseView = rightTextView;
                            sx = rightTextView.getLeft();
                            sy = view.getTop() + rightTextView.getHeight() * 1.0f / 2.0f;
                            path = new Path();
                            rightPosition = position;
                            rightTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_line_bg_with_border));
                        }
                        QZXTools.logE("sx=" + sx + ";sy=" + sy + ";ex=" + ex + ";ey=" + ey, null);
                        if (firstChooseView == liftTextView) {

                            //???????????????????????????view
                            ex = rightTextView.getLeft();
                            ey = view.getTop() + rightTextView.getHeight() * 1.0f / 2.0f;
                            path.addCircle(sx, sy, 10, Path.Direction.CW);
                            path.moveTo(sx, sy);
                            path.lineTo(ex, ey);
                            path.addCircle(ex, ey, 10, Path.Direction.CW);

                            //??????Path   ???????????????
                            ((LinkedLineHolder) viewHolder).matching_toLine.getDrawPath(path);
                            //??????????????????view
                            firstChooseView = null;
                            rightPositions.add(position);
                            leftPositions.add(leftPosition);
                            liftTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_line_item_bg));

                            //????????????????????????????????? ???????????? ?????????????????????????????????????????????????????????
                            LineMatchBean lineMatchBean = new LineMatchBean();
                            //?????????????????????id
                            lineMatchBean.setTypeId(questionInfoList.get(i).getId());
                            lineMatchBean.setLeftId(leftList.get(leftPosition).getId());
                            lineMatchBean.setRightId(rightList.get(position).getId());
                            lineMatchBean.setPosition(i);
                            lineMatchBean.setStartX(sx);
                            lineMatchBean.setStartY(sy);
                            lineMatchBean.setEndX(ex);
                            lineMatchBean.setEndY(ey);


                            lineMatchs.add(lineMatchBean);
                            //?????????????????????????????????
                            MyApplication.getInstance().getDaoSession().getLineMatchBeanDao().insertOrReplace(lineMatchBean);

                            stringBuffer.append(questionInfoList.get(i).getLeftList().get(leftPosition).getId());
                            stringBuffer.append(",");
                            stringBuffer.append(questionInfoList.get(i).getRightList().get(position).getId());
                            saveTrack = stringBuffer.toString();

                            //?????????????????????????????????????????????????????????
                            //-------------------------?????????????????????????????????id   ????????????????????????id ?????????
                            LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                            localTextAnswersBean.setHomeworkId(homeworkId);
                            localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                            localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                            localTextAnswersBean.setAnswerContent(saveTrack);
                            localTextAnswersBean.setUserId(UserUtils.getUserId());
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                            //???????????????????????????
                            MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                            //?????????????????????saveTrack
                            ((LinkedLineHolder) viewHolder).matching_toLine.setLocalSave(homeworkId, questionInfoList.get(i), saveTrack);

                        }
                    }
                });
                ((LinkedLineHolder) viewHolder).ll_match_bind_tag.setTag(questionInfoList.get(i).getId());
                ((LinkedLineHolder) viewHolder).rv_matching_show.setLayoutManager(layoutManager);
                ((LinkedLineHolder) viewHolder).rv_matching_show.setAdapter(lineLeftAdapter);

                //????????????????????????????????????????????????
                List<LineMatchBean> currentLineMatchBeans = new ArrayList<>();

                //??????????????????????????????????????????????????????
                //??????????????????????????????????????????????????????id ???????????????javabean  ???????????????????????????????????????????????? ???????????????????????????
                if (lineMatchs.size() > 0) {
                    for (int j = 0; j < lineMatchs.size(); j++) {
                        LineMatchBean lineMatchBean = lineMatchs.get(j);
                        if (lineMatchBean.getTypeId().equals(questionInfoList.get(i).getId())) {
                            //????????????????????????????????????????????????
                            Path pathC = new Path();
                            pathC.addCircle(lineMatchBean.getStartX(), lineMatchBean.getStartY(), 10, Path.Direction.CW);
                            pathC.addCircle(lineMatchBean.getEndX(), lineMatchBean.getEndY(), 10, Path.Direction.CW);
                            Path pathL = new Path();
                            pathL.moveTo(lineMatchBean.getStartX(), lineMatchBean.getStartY());
                            pathL.lineTo(lineMatchBean.getEndX(), lineMatchBean.getEndY());
                            //???????????????????????????
                            ((LinkedLineHolder) viewHolder).matching_toLine.addDotPath(pathC, false, questionInfoList.get(i).getId());
                            ((LinkedLineHolder) viewHolder).matching_toLine.addLinePath(pathL, false);

                            currentLineMatchBeans.add(lineMatchBean);

                        } else {
                            if (currentLineMatchBeans.size() > 0) {
                                ((LinkedLineHolder) viewHolder).matching_toLine.setDrawStatus(0);
                            } else {
                                ((LinkedLineHolder) viewHolder).matching_toLine.resetDrawLine(questionInfoList.get(i).getId());
                            }
                        }
                    }
                }

                //???????????????
                //?????????????????????,???????????????????????????????????????
                LocalTextAnswersBean linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                        .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();

                List<LineMatchBean> lineMatchBeans = MyApplication.getInstance().getDaoSession().getLineMatchBeanDao().loadAll();
                Log.i(TAG, "onBindViewHolder: " + linkLocal + ".............." + lineMatchBeans);
                for (int j = 0; j < lineMatchBeans.size(); j++) {
                    LineMatchBean lineMatchBean = lineMatchBeans.get(j);
                    if (lineMatchBean.getTypeId().equals(questionInfoList.get(i).getId())) {
                        Path pathC = new Path();
                        pathC.addCircle(lineMatchBean.getStartX(), lineMatchBean.getStartY(), 10, Path.Direction.CW);
                        pathC.addCircle(lineMatchBean.getEndX(), lineMatchBean.getEndY(), 10, Path.Direction.CW);
                        Path pathL = new Path();
                        pathL.moveTo(lineMatchBean.getStartX(), lineMatchBean.getStartY());
                        pathL.lineTo(lineMatchBean.getEndX(), lineMatchBean.getEndY());
                        //???????????????????????????
                        ((LinkedLineHolder) viewHolder).matching_toLine.addDotPath(pathC);
                        ((LinkedLineHolder) viewHolder).matching_toLine.addLinePath(pathL);
                    }
                }

                //????????????
                ((LinkedLineHolder) viewHolder).matching_toLine.setDrawStatus(0);
            } else {
                //1 ?????????  2 ?????????
                ((LinkedLineHolder) viewHolder).matching_reset.setBackground(mContext.getResources().getDrawable(R.drawable.shape_line_reset_disable));
                ((LinkedLineHolder) viewHolder).matching_reset.setTextColor(0xFFD5D5D5);
                ((LinkedLineHolder) viewHolder).matching_reset.setOnClickListener(null);
                //?????????id ????????????????????????????????????????????????
                //View?????????????????????
                ((LinkedLineHolder) viewHolder).rv_matching_show.getViewTreeObserver()
                        .addOnGlobalLayoutListener(new ViewTreeObserver
                                .OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                //???????????????recycleview ???item  ??????????????????view ???
                                for (int j = 0; j < leftList.size(); j++) {
                                    View view = layoutManager.findViewByPosition(j);
                                    leftViews.add(view);


                                }

                                //OnGlobalLayoutListener????????????????????????
                                //????????????????????????????????????OnGlobalLayoutListener
                                ((LinkedLineHolder) viewHolder).rv_matching_show.getViewTreeObserver()
                                        .removeOnGlobalLayoutListener(this);

                                //todo ???????????????????????????????????????????????????????????????????????????????????????
                                //??????id  ??????????????????????????????????????????
                                View leftView = null;
                                View rightView = null;
                                View leftTextView = null;
                                View rightTextView = null;

                                String answer = questionInfoList.get(i).getAnswer();
                                // answer=answer.substring(0,answer.length()-1);
                                if (answer.contains("|")) {
                                    String[] split = answer.split("\\|");
                                    for (String item : split) {
                                        String[] trackStr = item.split(",");
                                        //drawLigature(trackStr[0], trackStr[1], false);
                                        for (int j = 0; j < leftList.size(); j++) {
                                            if (trackStr[0].equals(leftList.get(j).getId())) {
                                                leftView = leftViews.get(j);
                                                //???????????????view
                                                leftTextView = leftView.findViewById(R.id.tv_item_line_word_left);
                                                break;

                                            }
                                        }

                                        for (int j = 0; j < rightList.size(); j++) {
                                            if (trackStr[1].equals(rightList.get(j).getId())) {
                                                rightView = leftViews.get(j);
                                                //???????????????view
                                                rightTextView = rightView.findViewById(R.id.tv_item_line_word_right);
                                                break;

                                            }
                                        }

                                        float sx = leftView.getLeft() + leftTextView.getLeft() + leftTextView.getWidth();
                                        float sy = leftView.getTop() + leftTextView.getTop() + (leftTextView.getHeight() * 1.0f) / 2.0f;
                                        float ex = rightView.getLeft() + rightTextView.getLeft();
                                        float ey = rightView.getTop() + rightTextView.getTop() + (rightTextView.getHeight() * 1.0f) / 2.0f;


                                        //?????????????????????
                                        if (sx == ex) {
                                            return;
                                        }
                                        //??????
                                        Path pathC = new Path();
                                        pathC.addCircle(sx, sy, 10, Path.Direction.CW);
                                        pathC.addCircle(ex, ey, 10, Path.Direction.CW);
                                        //??????
                                        Path pathL = new Path();
                                        pathL.moveTo(sx, sy);
                                        pathL.lineTo(ex, ey);
                                        //???????????????????????????
                                        ((LinkedLineHolder) viewHolder).matching_toLine.addDotPath(pathC);
                                        ((LinkedLineHolder) viewHolder).matching_toLine.addLinePath(pathL);

                                    }
                                } else {
                                    //???????????????????????????
                                    String[] trackStr = answer.split(",");
                                    for (int j = 0; j < leftList.size(); j++) {
                                        if (trackStr[0].equals(leftList.get(j).getId())) {
                                            leftView = leftViews.get(j);
                                            //???????????????view
                                            leftTextView = leftView.findViewById(R.id.tv_item_line_word_left);
                                            break;

                                        }
                                    }

                                    for (int j = 0; j < rightList.size(); j++) {
                                        if (trackStr[1].equals(rightList.get(j).getId())) {
                                            rightView = leftViews.get(j);
                                            //???????????????view
                                            rightTextView = rightView.findViewById(R.id.tv_item_line_word_right);
                                            break;

                                        }
                                    }

                                    float sx = leftView.getLeft() + leftTextView.getLeft() + leftTextView.getWidth();
                                    float sy = leftView.getTop() + leftTextView.getTop() + (leftTextView.getHeight() * 1.0f) / 2.0f;
                                    float ex = rightView.getLeft() + rightTextView.getLeft();
                                    float ey = rightView.getTop() + rightTextView.getTop() + (rightTextView.getHeight() * 1.0f) / 2.0f;


                                    //?????????????????????
                                    if (sx == ex) {
                                        return;
                                    }
                                    //??????
                                    Path pathC = new Path();
                                    pathC.addCircle(sx, sy, 10, Path.Direction.CW);
                                    pathC.addCircle(ex, ey, 10, Path.Direction.CW);
                                    //??????
                                    Path pathL = new Path();
                                    pathL.moveTo(sx, sy);
                                    pathL.lineTo(ex, ey);
                                    //???????????????????????????
                                    ((LinkedLineHolder) viewHolder).matching_toLine.addDotPath(pathC);
                                    ((LinkedLineHolder) viewHolder).matching_toLine.addLinePath(pathL);
                                }

                                //????????????  ?????????????????????????????????
                                ((LinkedLineHolder) viewHolder).matching_toLine.setDrawStatus(1);

                            }
                        });
            }


        }

    }


    @Override
    public int getItemCount() {
        return questionInfoList != null ? questionInfoList.size() : 0;
    }

    @Override
    public void grantPermission() {

    }

    @Override
    public void denyPermission() {

    }


    /**
     * ???????????????????????????
     */
    public File compressBitmapToFile(String srcPath, float desWidth) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;//?????????,????????????
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float desHeight = desWidth * h / w;
        int be = 1;
        if (w > h && w > desWidth) {
            be = (int) (newOpts.outWidth / desWidth);
        } else if (w < h && h > desHeight) {
            be = (int) (newOpts.outHeight / desHeight);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//???????????????

//        newOpts.inPreferredConfig = Config.ARGB_8888;//?????????????????????,?????????
        newOpts.inPurgeable = true;// ????????????????????????
        newOpts.inInputShareable = true;//???????????????????????????????????????????????????

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);//???????????????????????????100????????????????????????????????????????????????baos???
        String fileDir = QZXTools.getExternalStorageForFiles(mContext, Environment.DIRECTORY_PICTURES);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMG_");
        stringBuilder.append(simpleDateFormat.format(new Date()));
        stringBuilder.append(".jpg");
        File file = new File(fileDir, stringBuilder.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return file;
    }


    public class SingleChooseHolder extends RecyclerView.ViewHolder {

        private final LinearLayout ll_single_image_one;
        private final LinearLayout ll_single_image_two;
        private final LinearLayout ll_single_image_three;
        private final LinearLayout ll_single_image_fore;
        private final LinearLayout ll_single_image_five;
        private final LinearLayout ll_single_image_sex;
        private final TextView tv_single_image_one;
        private final TextView tv_single_image_two;
        private final TextView tv_single_image_three;
        private final TextView tv_single_image_fore;
        private final TextView tv_single_image_five;
        private final TextView tv_single_image_sex;
        private final TextView practice_select_judge_answer;
        private final TextView practice_head_index;

        public SingleChooseHolder(@NonNull View itemView) {


            super(itemView);
            ll_single_image_one = itemView.findViewById(R.id.ll_single_image_one);
            ll_single_image_two = itemView.findViewById(R.id.ll_single_image_two);
            ll_single_image_three = itemView.findViewById(R.id.ll_single_image_three);
            ll_single_image_fore = itemView.findViewById(R.id.ll_single_image_fore);
            ll_single_image_five = itemView.findViewById(R.id.ll_single_image_five);
            ll_single_image_sex = itemView.findViewById(R.id.ll_single_image_sex);


            tv_single_image_one = itemView.findViewById(R.id.tv_single_image_one);
            tv_single_image_two = itemView.findViewById(R.id.tv_single_image_two);
            tv_single_image_three = itemView.findViewById(R.id.tv_single_image_three);
            tv_single_image_fore = itemView.findViewById(R.id.tv_single_image_fore);
            tv_single_image_five = itemView.findViewById(R.id.tv_single_image_five);
            tv_single_image_sex = itemView.findViewById(R.id.tv_single_image_sex);
            practice_head_index = itemView.findViewById(R.id.practice_head_index);

            //????????????
            practice_select_judge_answer = itemView.findViewById(R.id.practice_select_judge_answer);


        }
    }

    public class MultiChooseHolder extends RecyclerView.ViewHolder {
        private final LinearLayout ll_single_image_one;
        private final LinearLayout ll_single_image_two;
        private final LinearLayout ll_single_image_three;
        private final LinearLayout ll_single_image_fore;
        private final LinearLayout ll_single_image_five;
        private final LinearLayout ll_single_image_sex;
        private final TextView tv_single_image_one;
        private final TextView tv_single_image_two;
        private final TextView tv_single_image_three;
        private final TextView tv_single_image_fore;
        private final TextView tv_single_image_five;
        private final TextView tv_single_image_sex;
        private final TextView practice_select_judge_answer;
        private final TextView practice_head_index;

        public MultiChooseHolder(@NonNull View itemView) {
            super(itemView);

            ll_single_image_one = itemView.findViewById(R.id.ll_single_image_one);
            ll_single_image_two = itemView.findViewById(R.id.ll_single_image_two);
            ll_single_image_three = itemView.findViewById(R.id.ll_single_image_three);
            ll_single_image_fore = itemView.findViewById(R.id.ll_single_image_fore);
            ll_single_image_five = itemView.findViewById(R.id.ll_single_image_five);
            ll_single_image_sex = itemView.findViewById(R.id.ll_single_image_sex);


            tv_single_image_one = itemView.findViewById(R.id.tv_single_image_one);
            tv_single_image_two = itemView.findViewById(R.id.tv_single_image_two);
            tv_single_image_three = itemView.findViewById(R.id.tv_single_image_three);
            tv_single_image_fore = itemView.findViewById(R.id.tv_single_image_fore);
            tv_single_image_five = itemView.findViewById(R.id.tv_single_image_five);
            tv_single_image_sex = itemView.findViewById(R.id.tv_single_image_sex);
            practice_select_judge_answer = itemView.findViewById(R.id.practice_select_judge_answer);
            practice_head_index = itemView.findViewById(R.id.practice_head_index);
        }
    }


    private EditText subjective_input;
    private TextView subjective_input_teacher;
    private SubjectImagesView siv_images;
    private RelativeLayout subjective_answer_tool_layout;


    public class SubjectItemHolder extends RecyclerView.ViewHolder {

        private final TextView practice_head_index;
        private final TextView subjective_camera;
        private final TextView subjective_board;


        public SubjectItemHolder(@NonNull View itemView) {
            super(itemView);
            practice_head_index = itemView.findViewById(R.id.practice_head_index);

            //??????
            subjective_camera = itemView.findViewById(R.id.subjective_camera);
            //??????
            subjective_board = itemView.findViewById(R.id.subjective_board);
            subjective_answer_tool_layout = itemView.findViewById(R.id.subjective_answer_tool_layout);


            //???????????????
            subjective_input = itemView.findViewById(R.id.subjective_input);
            subjective_input_teacher = itemView.findViewById(R.id.subjective_input_teacher);
            siv_images = itemView.findViewById(R.id.siv_images);


        }
    }

    //?????????
    public class FillBlankHolder extends RecyclerView.ViewHolder {

        private final LinearLayout ll_fill_balank_one;
        private final LinearLayout ll_fill_balank_two;
        private final LinearLayout ll_fill_balank_three;
        private final LinearLayout ll_fill_balank_fore;
        private final LinearLayout ll_fill_balank_five;
        private final LinearLayout ll_fill_balank_sex;
        private final TextView practice_head_index;
        private final EditText et_fill_balank_one;
        private final EditText et_fill_balank_two;
        private final EditText et_fill_balank_three;
        private final EditText et_fill_balank_fore;
        private final EditText et_fill_balank_five;
        private final EditText et_fill_balank_sex;
        private final LinearLayout ll_show_quint;
        private final LinearLayout ll_fill_quint_one;
        private final LinearLayout ll_fill_quint_two;
        private final LinearLayout ll_fill_quint_three;
        private final LinearLayout ll_fill_quint_fore;
        private final LinearLayout ll_fill_quint_five;
        private final LinearLayout ll_fill_quint_six;
        private final TextView tv_fill_quint_one;
        private final TextView tv_fill_quint_two;
        private final TextView tv_fill_quint_three;
        private final TextView tv_fill_quint_fore;
        private final TextView tv_fill_quint_five;
        private final TextView tv_fill_quint_six;

        public FillBlankHolder(@NonNull View itemView) {
            super(itemView);
            ll_fill_balank_one = itemView.findViewById(R.id.ll_fill_balank_one);
            ll_fill_balank_two = itemView.findViewById(R.id.ll_fill_balank_two);
            ll_fill_balank_three = itemView.findViewById(R.id.ll_fill_balank_three);
            ll_fill_balank_fore = itemView.findViewById(R.id.ll_fill_balank_fore);
            ll_fill_balank_five = itemView.findViewById(R.id.ll_fill_balank_five);
            ll_fill_balank_sex = itemView.findViewById(R.id.ll_fill_balank_sex);
            //??????????????????
            practice_head_index = itemView.findViewById(R.id.practice_head_index);
            //???????????????
            et_fill_balank_one = itemView.findViewById(R.id.et_fill_balank_one);
            et_fill_balank_two = itemView.findViewById(R.id.et_fill_balank_two);
            et_fill_balank_three = itemView.findViewById(R.id.et_fill_balank_three);
            et_fill_balank_fore = itemView.findViewById(R.id.et_fill_balank_fore);
            et_fill_balank_five = itemView.findViewById(R.id.et_fill_balank_five);
            et_fill_balank_sex = itemView.findViewById(R.id.et_fill_balank_sex);
            //???????????????
            ll_show_quint = itemView.findViewById(R.id.ll_show_quint);
            ll_fill_quint_one = itemView.findViewById(R.id.ll_fill_quint_one);
            ll_fill_quint_two = itemView.findViewById(R.id.ll_fill_quint_two);
            ll_fill_quint_three = itemView.findViewById(R.id.ll_fill_quint_three);
            ll_fill_quint_fore = itemView.findViewById(R.id.ll_fill_quint_fore);
            ll_fill_quint_five = itemView.findViewById(R.id.ll_fill_quint_five);
            ll_fill_quint_six = itemView.findViewById(R.id.ll_fill_quint_six);
            tv_fill_quint_one = itemView.findViewById(R.id.tv_fill_quint_one);
            tv_fill_quint_two = itemView.findViewById(R.id.tv_fill_quint_two);
            tv_fill_quint_three = itemView.findViewById(R.id.tv_fill_quint_three);
            tv_fill_quint_fore = itemView.findViewById(R.id.tv_fill_quint_fore);
            tv_fill_quint_five = itemView.findViewById(R.id.tv_fill_quint_five);
            tv_fill_quint_six = itemView.findViewById(R.id.tv_fill_quint_six);


        }
    }

    public class LinkedLineHolder extends RecyclerView.ViewHolder {

        private final TextView matching_reset;
        private final ToLineView matching_toLine;
        private final RecyclerView rv_matching_show;
        private final LinearLayout ll_match_bind_tag;
        private final TextView practice_head_index;


        public LinkedLineHolder(@NonNull View itemView) {
            super(itemView);
            //??????
            matching_reset = itemView.findViewById(R.id.matching_reset);
            //?????????view
            matching_toLine = itemView.findViewById(R.id.matching_toLine);
            rv_matching_show = itemView.findViewById(R.id.rv_matching_show);
            ll_match_bind_tag = itemView.findViewById(R.id.ll_match_bind_tag);
            practice_head_index = itemView.findViewById(R.id.practice_head_index);


        }
    }

    public class JudgeItemHolder extends RecyclerView.ViewHolder {
        private final TextView practice_head_index;
        private final RelativeLayout option_do_tv_one;
        private final RelativeLayout option_do_tv_two;
        private final LinearLayout ll_current_quint_show;
        private final ImageView iv_current_quint_show;

        public JudgeItemHolder(@NonNull View itemView) {
            super(itemView);
            practice_head_index = itemView.findViewById(R.id.practice_head_index);
            option_do_tv_one = itemView.findViewById(R.id.option_do_tv_one);
            option_do_tv_two = itemView.findViewById(R.id.option_do_tv_two);
            ll_current_quint_show = itemView.findViewById(R.id.ll_current_quint_show);
            iv_current_quint_show = itemView.findViewById(R.id.iv_current_quint_show);
        }
    }

    /**
     * ??????????????????????????????,????????????LinearLayout??????
     */
    private void addAttachImgs(LinearLayout linearLayout, String src) {
        //??????????????????
        linearLayout.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ImageView photoView = new ImageView(mContext);
        Glide.with(mContext).load(src).into(photoView);
        linearLayout.addView(photoView, layoutParams);
    }



    /**
     * ??????????????????item??????????????????????????????
     * viewType ?????????position
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        switch (questionInfoList.get(position).getQuestionType()) {
            case Constant.Single_Choose:
                //?????????
                return Constant.Single_Choose;
            case Constant.Multi_Choose:

                return Constant.Multi_Choose;
            case Constant.Fill_Blank:

                return Constant.Fill_Blank;
            case Constant.Subject_Item:

                return Constant.Subject_Item;
            case Constant.Linked_Line:

                return Constant.Linked_Line;
            case Constant.Judge_Item:

                return Constant.Judge_Item;
        }
        return -1;
    }

    /**
     * ????????????
     */
    private File cameraFile;
    public static final int CODE_SYS_CAMERA = 1;//????????????RequestCode

    private void openCamera() {
        String fileDir = QZXTools.getExternalStorageForFiles(mContext, Environment.DIRECTORY_PICTURES);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("IMG_");
        stringBuilder.append(simpleDateFormat.format(new Date()));

        stringBuilder.append(".jpg");
        cameraFile = new File(fileDir, stringBuilder.toString());

        Uri cameraUri = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraUri = FileProvider.getUriForFile(mContext, mContext.getPackageName()
                    + ".fileprovider", cameraFile);
        } else {
            cameraUri = Uri.fromFile(cameraFile);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //???????????????????????????????????????????????????Uri??????????????????
        }
        //?????????????????????????????????????????????????????????onActivityResult????????????Intent??????
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        cameraIntent.putExtra("extra_info", subjectQuestionInfo.getId());
        ((Activity) mContext).startActivityForResult(cameraIntent, CODE_SYS_CAMERA);
    }

    /**
     * ??????????????????????????????
     *
     * @param flag
     */
    //????????????
    private ArrayList<String> imgFilePathList = new ArrayList<>();

    private boolean notifyItem = false;

    public void fromCameraCallback(String flag) {
        if (flag.equals("CAMERA_CALLBACK")) {
            notifyItem = true;
            QZXTools.logE("fromCameraCallback filePath=" + cameraFile.getAbsolutePath(), null);
            //???????????????????????????????????????SubjectiveToDoView??????????????????????????????
            Log.i("", "fromCameraCallback: " + subjectQuestionInfo);
            if (subjQuestionId.equals(subjectQuestionInfo.getId() + "")) {
                //??????????????????
//                File compressFile = compressImage(BitmapFactory.decodeFile(cameraFile.getAbsolutePath()));

                //?????????????????? notes:???????????????????????????????????????????????????
                File compressFile = compressBitmapToFile(cameraFile.getAbsolutePath(),
                        mContext.getResources().getDimensionPixelSize(R.dimen.x800));

                // imgFilePathList.add(compressFile.getAbsolutePath());
                SubjeatSaveBean subjeatSaveBean = new SubjeatSaveBean();
                subjeatSaveBean.setId(subjectQuestionInfo.getId() + "");
                subjeatSaveBean.setLayoutPosition(layoutPosition);
                SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                        .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(subjectQuestionInfo.getId() + "")).unique();
                if (saveBean != null && !TextUtils.isEmpty(saveBean.getImages())) {
                    String beanImages = saveBean.getImages();
                    subjeatSaveBean.setImages(beanImages + "|" + compressFile.getAbsolutePath());
                } else {
                    subjeatSaveBean.setImages(compressFile.getAbsolutePath());
                }

                MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao().insertOrReplace(subjeatSaveBean);


                //notifyItemChanged(layoutPosition);
                notifyDataSetChanged();
            }
        } else {
            notifyDataSetChanged();
        }
    }

    //?????????????????????
    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        if (extraInfoBean.getQuestionId().equals(subjectQuestionInfo.getId())) {
            //  imgFilePathList.add(extraInfoBean.getFilePath());

            SubjeatSaveBean subjeatSaveBean = new SubjeatSaveBean();
            subjeatSaveBean.setId(subjectQuestionInfo.getId() + "");
            subjeatSaveBean.setLayoutPosition(layoutPosition);
            SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                    .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(subjectQuestionInfo.getId() + "")).unique();
            if (saveBean != null) {
                String beanImages = saveBean.getImages();
                subjeatSaveBean.setImages(beanImages + "|" + extraInfoBean.getFilePath());
            } else {
                subjeatSaveBean.setImages(extraInfoBean.getFilePath());
            }

            MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao().insertOrReplace(subjeatSaveBean);


            //notifyItemChanged(layoutPosition);
            notifyDataSetChanged();
            notifyItem = true;


        }
    }


}
