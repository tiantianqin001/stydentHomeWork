package com.telit.zhkt_three.Adapter.QuestionAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.telit.zhkt_three.Activity.AfterHomeWork.LearnResourceActivity;
import com.telit.zhkt_three.Activity.AfterHomeWork.TypicalAnswersActivity;
import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.HomeWork.WhiteBoardActivity;
import com.telit.zhkt_three.Activity.MistakesCollection.PerfectAnswerActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.CustomView.FullBlankView;
import com.telit.zhkt_three.CustomView.JudgeImageNewView;
import com.telit.zhkt_three.CustomView.LinkLineView;
import com.telit.zhkt_three.CustomView.MulipleChoiseView;
import com.telit.zhkt_three.CustomView.SubjectImagesView;
import com.telit.zhkt_three.CustomView.TowMulipleChoiseView;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfo;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * author: qzx
 * Date: 2019/5/23 16:44
 * <p>
 * ????????????????????????????????????Adapter?????????????????????????????????????????????????????????????????????/????????????
 */
public class RVQuestionTvAnswerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ZBVPermission.PermPassResult {
    private static final String TAG = "RVQuestionTvAnswerAdapter";
    private Context mContext;

    //0?????????  1 ?????????  2 ?????????
    private String taskStatus;

    //??????????????????????????????
    private boolean isMistakesShown;
    //0 ??????  1??????
    private int homeWorkType;

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


    private QuestionInfo subjectQuestionInfo;
    private LocalTextAnswersBean linkLocal;

    /**
     * ??????homeworkid???????????????????????????homeworkid??????QuestionInfo?????????homeworkid
     */
    public void setQuestionInfoList(List<QuestionInfo> questionInfoList, String homeworkId) {
        //???????????????????????????
        this.questionInfoList = questionInfoList;
        this.homeworkId = homeworkId;
    }

    private String imageAnsterType;


    //???????????????????????????
    // private List<SingleBean> singleBeans=new ArrayList<>();


    /**
     * ??????????????????????????????:
     * ???????????????????????????????????????
     */
    public void fetchNeedParam(String imageAnsterType) {
        this.imageAnsterType = imageAnsterType;
        QZXTools.logE("imageAnsterType=" + imageAnsterType, null);
    }

    /**
     * @param status          ???????????????
     * @param isImageQuestion ????????????????????????
     * @param mistakesShown   ??????????????????????????????
     * @param homeWorkType    0?????????   1?????????
     */
    public RVQuestionTvAnswerAdapter(Context context, String status, boolean isImageQuestion, boolean mistakesShown, int homeWorkType) {
        mContext = context;
        taskStatus = status;
        isImageTask = isImageQuestion;
        isMistakesShown = mistakesShown;
        this.homeWorkType = homeWorkType;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == Constant.Single_Choose) {
            return new SingleChooseHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.single_choose_view, viewGroup, false));
        } else if (i == Constant.Fill_Blank) {
            return new FillBlankHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.fill_blank_new_layout, null, false));
        } else if (i == Constant.Subject_Item) {
            return new SubjectItemHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.subject_item_image_show_layout, viewGroup, false));
        } else if (i == Constant.Linked_Line) {
            return new LinkedLineHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.linked_line_new_layout, viewGroup, false));
        } else if (i == Constant.Judge_Item) {
            return new JudgeItemHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.judgeselect_two_new_layout, viewGroup, false));
        } else if (i == Constant.Multi_Choose) {
            return new MultiChooseHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.mulit_choose_layout, viewGroup, false));
        }
        return null;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof SingleChooseHolder) {
            //   viewHolder.setIsRecyclable(false);
            //?????????
            //????????????
            ((SingleChooseHolder) viewHolder).mcv_choise_view.setTaskStatus(taskStatus);
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();

            //????????????
            ((SingleChooseHolder) viewHolder).mcv_choise_view.setViewData(selectBeans, questionInfoList, i, homeworkId, homeWorkType);

            //??????????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((SingleChooseHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((SingleChooseHolder) viewHolder).iv_mistake_my_quint);
            }

            if (homeWorkType==1){//??????
                ((SingleChooseHolder) viewHolder).tv_learn_resource.setVisibility(VISIBLE);

                ((SingleChooseHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {
                ((SingleChooseHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        } else if (viewHolder instanceof MultiChooseHolder) {
            //?????????
            //????????????
            ((MultiChooseHolder) viewHolder).two_tmcw.setTaskStatus(taskStatus);
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();

            //????????????
            ((MultiChooseHolder) viewHolder).two_tmcw.setViewData(selectBeans, questionInfoList, i, homeworkId, homeWorkType);

            //?????????????????? ???????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((MultiChooseHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((MultiChooseHolder) viewHolder).iv_mistake_my_quint);
            }

            if (homeWorkType==1){//??????
                ((MultiChooseHolder) viewHolder).tv_learn_resource.setVisibility(VISIBLE);

                ((MultiChooseHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {
                ((MultiChooseHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        } else if (viewHolder instanceof FillBlankHolder) {
            //todo  ??????????????????????????????
            viewHolder.setIsRecyclable(false);
            //?????????

            //????????????
            ((FillBlankHolder) viewHolder).full_blank_new.setTaskStatus(taskStatus);
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();
            ((FillBlankHolder) viewHolder).full_blank_new.setViewData(selectBeans, questionInfoList, i, homeworkId, homeWorkType);

            //??????????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((FillBlankHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((FillBlankHolder) viewHolder).iv_mistake_my_quint);
            }

            if (homeWorkType==1){//??????
                ((FillBlankHolder) viewHolder).tv_learn_resource.setVisibility(VISIBLE);

                ((FillBlankHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {
                ((FillBlankHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        } else if (viewHolder instanceof JudgeItemHolder) {
            //todo ???????????????????????????
            viewHolder.setIsRecyclable(false);
            //?????????
            //????????????
            ((JudgeItemHolder) viewHolder).judge_image_view.setTaskStatus(taskStatus);
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();

            //????????????
            ((JudgeItemHolder) viewHolder).judge_image_view.setViewData(selectBeans, questionInfoList, i, homeworkId, homeWorkType);

            //??????????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((JudgeItemHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((JudgeItemHolder) viewHolder).iv_mistake_my_quint);
            }

            if (homeWorkType==1){//??????
                ((JudgeItemHolder) viewHolder).tv_learn_resource.setVisibility(VISIBLE);

                ((JudgeItemHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {
                ((JudgeItemHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        } else if (viewHolder instanceof SubjectItemHolder) {
            //?????????
            //todo ???????????????????????????
            viewHolder.setIsRecyclable(false);


            //??????????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((SubjectItemHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((SubjectItemHolder) viewHolder).iv_mistake_my_quint);
            }

            QZXTools.logD("???????????????" + viewHolder.getLayoutPosition());
            ((SubjectItemHolder) viewHolder).practice_head_index.setText("???" + (i + 1) + "??? ???" + questionInfoList.size() + "???");
            //0 ??????  1??????
            if (homeWorkType == 0) {
                siv_images.setHideDel();
            }

            //0?????????  1 ?????????  2 ?????????    //?????????????????????????????????
            if (taskStatus.equals(Constant.Todo_Status) || taskStatus.equals(Constant.Retry_Status)
                    || taskStatus.equals(Constant.Save_Status)) {
                siv_images.setTag(questionInfoList.get(i).getId());
                //???????????????
                if (taskStatus.equals(Constant.Todo_Status) || taskStatus.equals(Constant.Retry_Status)) {
                    //?????????????????????  ???????????????
                    if (homeWorkType == 1) {
                        linkLocal = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                                .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionInfoList.get(i).getId()),
                                        LocalTextAnswersBeanDao.Properties.HomeworkId.eq(homeworkId),
                                        LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
                        Log.i(TAG, "onBindViewHolder: " + linkLocal);
                    }
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
                        if (taskStatus.equals(Constant.Todo_Status)) {
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
                            }
                        } else if (taskStatus.equals(Constant.Retry_Status)) {
                            //???????????????????????????
                            imgFilePathList.clear();
                            siv_images.fromCameraCallback(imgFilePathList);
                            MyApplication.getInstance().getDaoSession()
                                    .getLocalTextAnswersBeanDao().deleteByKey(questionInfoList.get(i).getId());
                            MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao().deleteByKey(questionInfoList.get(i).getId());

                        }

                    }
                } else if (taskStatus.equals(Constant.Save_Status)) {
                    List<WorkOwnResult> ownList = questionInfoList.get(i).getOwnList();
                    if (ownList != null && ownList.size() > 0) {
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
                    if (ownList != null && ownList.size() > 0) {
                        subjective_input.setText(ownList.get(0).getAnswerContent());

                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        localTextAnswersBean.setHomeworkId(questionInfoList.get(i).getHomeworkId());
                        localTextAnswersBean.setQuestionId(questionInfoList.get(i).getId());
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfoList.get(i).getQuestionType());
                        localTextAnswersBean.setAnswerContent(ownList.get(0).getAnswerContent());
                        String attachment = ownList.get(0).getAttachment();
                        String[] strings = attachment.split("\\|");
                        if (imgFilePathList == null) imgFilePathList = new ArrayList<>();
                        imgFilePathList.clear();
                        for (String string : strings) {
                            imgFilePathList.add(string);
                        }
                        localTextAnswersBean.setImageList(imgFilePathList);
                        QZXTools.logE("subjective Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

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
                        ZBVPermission.getInstance().setPermPassResult(RVQuestionTvAnswerAdapter.this);
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

                        ZBVPermission.getInstance().setPermPassResult(RVQuestionTvAnswerAdapter.this);
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

                //???????????????????????????????????????
                if (taskStatus.equals(Constant.Commit_Status)){

                }else {
                    //?????????????????????
                }

                List<WorkOwnResult> ownList = questionInfoList.get(i).getOwnList();
                if (ownList != null && ownList.size() > 0) {
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
                if (ownList != null && ownList.size() > 0) {
                    subjective_input.setText("????????????: " + ownList.get(0).getAnswerContent());
                }
                //????????????
                subjective_input_teacher.setVisibility(VISIBLE);
                subjective_input_teacher.setText("????????????: " + questionInfoList.get(i).getAnswer());

            }

            if (homeWorkType==1){//??????
                if ("1".equals(taskStatus)||"2".equals(taskStatus)){
                    ((SubjectItemHolder) viewHolder).tv_typical_answers.setVisibility(VISIBLE);
                    ((SubjectItemHolder) viewHolder).tv_work_good_answer.setVisibility(VISIBLE);

                    ((SubjectItemHolder) viewHolder).tv_typical_answers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            forwardTypicalAnswers(questionInfoList.get(i).getId(),questionInfoList.get(i).getHomeworkId());
                        }
                    });
                    ((SubjectItemHolder) viewHolder).tv_work_good_answer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            forwardPerfectAnswer(questionInfoList.get(i).getId(),questionInfoList.get(i).getHomeworkId());
                        }
                    });

                }else {
                    ((SubjectItemHolder) viewHolder).tv_typical_answers.setVisibility(GONE);
                    ((SubjectItemHolder) viewHolder).tv_work_good_answer.setVisibility(GONE);
                }

                ((SubjectItemHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {//??????
                ((SubjectItemHolder) viewHolder).tv_typical_answers.setVisibility(GONE);
                ((SubjectItemHolder) viewHolder).tv_work_good_answer.setVisibility(GONE);
                ((SubjectItemHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        } else if (viewHolder instanceof LinkedLineHolder) {
            //?????????
             viewHolder.setIsRecyclable(false);
            QZXTools.logE("ToLine viewHolder instanceof LinkedLineHolder......" + questionInfoList.get(i), null);

            //????????????
            ((LinkedLineHolder) viewHolder).link_line_new.setTaskStatus(taskStatus);
            List<QuestionInfo.SelectBean> selectBeans = questionInfoList.get(i).getList();
            ((LinkedLineHolder) viewHolder).link_line_new.setViewData(selectBeans, questionInfoList, i, homeworkId);

            //??????????????????
            if (isMistakesShown && imageAnsterType.equals("1")) {
                ((LinkedLineHolder) viewHolder).iv_mistake_my_quint.setVisibility(VISIBLE);
                Glide.with(mContext)
                        .load(questionInfoList.get(i).getAttachment())
                        .into(((LinkedLineHolder) viewHolder).iv_mistake_my_quint);
            }

            if (homeWorkType==1){//??????
                ((LinkedLineHolder) viewHolder).tv_learn_resource.setVisibility(VISIBLE);

                ((LinkedLineHolder) viewHolder).tv_learn_resource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        forwardLearnResource(questionInfoList.get(i).getId());
                    }
                });
            }else {
                ((LinkedLineHolder) viewHolder).tv_learn_resource.setVisibility(GONE);
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param questionId
     */
    private void forwardLearnResource(String questionId){
        Intent intent = new Intent(mContext, LearnResourceActivity.class);
        intent.putExtra("questionId", questionId);
        intent.putExtra("homeworkId", homeworkId);
        mContext.startActivity(intent);
    }

    /**
     * ??????????????????
     *
     * @param questionId
     * @param homeworkId
     */
    private void forwardTypicalAnswers(String questionId,String homeworkId){
        Intent intent = new Intent(mContext, TypicalAnswersActivity.class);
        intent.putExtra("questionId", questionId);
        intent.putExtra("homeworkId", homeworkId);
        mContext.startActivity(intent);
    }

    /**
     * ??????????????????
     *
     * @param questionId
     * @param homeworkId
     */
    private void forwardPerfectAnswer(String questionId,String homeworkId){
        Intent intent = new Intent(mContext, PerfectAnswerActivity.class);
        intent.putExtra("questionId", questionId);
        intent.putExtra("homeworkId", homeworkId);
        mContext.startActivity(intent);
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
        private final MulipleChoiseView mcv_choise_view;
        private final ImageView iv_mistake_my_quint;
        private TextView tv_learn_resource;
        public SingleChooseHolder(@NonNull View itemView) {
            super(itemView);
            mcv_choise_view = itemView.findViewById(R.id.mcv_choise_view);
            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);
            tv_learn_resource = itemView.findViewById(R.id.tv_learn_resource);
        }
    }

    public class MultiChooseHolder extends RecyclerView.ViewHolder {
        private final TowMulipleChoiseView two_tmcw;
        private final ImageView iv_mistake_my_quint;
        private TextView tv_learn_resource;
        public MultiChooseHolder(@NonNull View itemView) {
            super(itemView);
            two_tmcw = itemView.findViewById(R.id.two_tmcw);

            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);

            tv_learn_resource = itemView.findViewById(R.id.tv_learn_resource);
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

        private final ImageView iv_mistake_my_quint;

        private  TextView tv_learn_resource;
        private  TextView tv_typical_answers;
        private  TextView tv_work_good_answer;

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

            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);

            tv_learn_resource = itemView.findViewById(R.id.tv_learn_resource);
            tv_typical_answers = itemView.findViewById(R.id.tv_typical_answers);
            tv_work_good_answer = itemView.findViewById(R.id.tv_work_good_answer);
        }
    }

    //?????????
    public class FillBlankHolder extends RecyclerView.ViewHolder {
        private final FullBlankView full_blank_new;
        private final ImageView iv_mistake_my_quint;
        private TextView tv_learn_resource;
        public FillBlankHolder(@NonNull View itemView) {
            super(itemView);
            full_blank_new = itemView.findViewById(R.id.full_blank_new);

            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);

            tv_learn_resource = itemView.findViewById(R.id.tv_learn_resource);
        }
    }

    public class LinkedLineHolder extends RecyclerView.ViewHolder {
        private final LinkLineView link_line_new;
        private final ImageView iv_mistake_my_quint;

        private TextView tv_learn_resource;

        public LinkedLineHolder(@NonNull View itemView) {
            super(itemView);

            link_line_new = itemView.findViewById(R.id.link_line_new);

            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);

            tv_learn_resource =  itemView.findViewById(R.id.tv_learn_resource);
        }
    }

    public class JudgeItemHolder extends RecyclerView.ViewHolder {
        private final JudgeImageNewView judge_image_view;
        private final ImageView iv_mistake_my_quint;
        private TextView tv_learn_resource;
        public JudgeItemHolder(@NonNull View itemView) {
            super(itemView);

            //????????????????????????
            iv_mistake_my_quint = itemView.findViewById(R.id.iv_mistake_my_quint);
            judge_image_view = itemView.findViewById(R.id.judge_image_view);
            tv_learn_resource = itemView.findViewById(R.id.tv_learn_resource);
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
            notifyItem = true;
            SubjeatSaveBean subjeatSaveBean = new SubjeatSaveBean();
            subjeatSaveBean.setId(subjectQuestionInfo.getId() + "");
            subjeatSaveBean.setLayoutPosition(layoutPosition);
            SubjeatSaveBean saveBean = MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao()
                    .queryBuilder().where(SubjeatSaveBeanDao.Properties.Id.eq(subjectQuestionInfo.getId() + "")).unique();
            if (saveBean != null && !TextUtils.isEmpty(saveBean.getImages())) {
                String beanImages = saveBean.getImages();
                subjeatSaveBean.setImages(beanImages + "|" + extraInfoBean.getFilePath());
            } else {
                subjeatSaveBean.setImages(extraInfoBean.getFilePath());
            }
            MyApplication.getInstance().getDaoSession().getSubjeatSaveBeanDao().insertOrReplace(subjeatSaveBean);
            //notifyItemChanged(layoutPosition);
            notifyDataSetChanged();

        }
    }
}
