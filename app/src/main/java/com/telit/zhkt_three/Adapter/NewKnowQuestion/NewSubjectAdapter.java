package com.telit.zhkt_three.Adapter.NewKnowQuestion;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.HomeWork.WhiteBoardActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.CustomView.QuestionView.NewKnowledgeQuestionView;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.JavaBean.WorkOwnResult;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.greendao.LocalTextAnswersBeanDao;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewSubjectAdapter extends RecyclerView.Adapter<NewSubjectAdapter.ViewHolder> {
    private Context mContext;
    private QuestionBank questionInfo;
    private String status;


    private FrameLayout subjective_answer_frame_one;
    private FrameLayout subjective_answer_frame_two;
    private FrameLayout subjective_answer_frame_three;

    private ImageView subjective_img_one;
    private ImageView subjective_img_two;
    private ImageView subjective_img_three;


    private RelativeLayout subjective_del_layout_one;
    private RelativeLayout subjective_del_layout_two;
    private RelativeLayout subjective_del_layout_three;
    private ImageView subjective_del_one;
    private ImageView subjective_del_two;
    private ImageView subjective_del_three;


    private RelativeLayout subjective_answer_tool_layout;
    private TextView subjective_camera;
    private TextView subjective_board;
    private TextView tv_teacher_answer_content;

    private EditText subjective_input;

    private static final String[] needPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //????????????
    private ArrayList<String> imgFilePathList;
    public static final int CODE_SYS_CAMERA = 1;//????????????RequestCode

    private File cameraFile;
    private boolean isClickCamera = false;
    private LocalTextAnswersBean localTextAnswersBean_sub;
    private  List<WorkOwnResult> ownList;
    private  List<String> teaDescFile;

    public NewSubjectAdapter(Context mContext, QuestionBank questionBank, String status) {
        this.mContext = mContext;
        this.questionInfo = questionBank;
        this.status = status;
        String optionJson = questionBank.getAnswerOptions();

        if (status.equals(Constant.Todo_Status) || status.equals(Constant.Retry_Status) || status.equals(Constant.Save_Status)) {
            //????????????   ?????????????????????
            localTextAnswersBean_sub = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                            LocalTextAnswersBeanDao.Properties.QuestionType.eq(questionBank.getQuestionChannelType()),
                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
            if (localTextAnswersBean_sub!=null){

                QZXTools.logD(localTextAnswersBean_sub.toString());
            }

        }
        if (status.equals(Constant.Commit_Status) || status.equals(Constant.Review_Status) || status.equals(Constant.Save_Status)) {
            ownList = questionBank.getOwnList();
            teaDescFile = questionBank.getTeaDescFile();


        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.subjective_option_complete_layout,viewGroup,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (status.equals(Constant.Todo_Status) || status.equals(Constant.Retry_Status)) {
            showImgsAndContent(localTextAnswersBean_sub);

            //??????????????????????????????
            subjective_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //-------------------------?????????????????????????????????id
                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                    localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
                    localTextAnswersBean.setQuestionId(questionInfo.getId() + "");
                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                    localTextAnswersBean.setQuestionType(questionInfo.getQuestionChannelType());
                    localTextAnswersBean.setAnswerContent(s.toString().trim());
                    localTextAnswersBean.setImageList(imgFilePathList);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                    //???????????????????????????
                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    //-------------------------?????????????????????????????????id
                }
            });
        }

        if (status.equals(Constant.Commit_Status)) {
            if (ownList!=null && ownList.size()>0){
                WorkOwnResult workOwnResult = ownList.get(0);
                String answerContent = workOwnResult.getAnswerContent();
                //??????
                String attachment = workOwnResult.getAttachment();
                if (TextUtils.isEmpty(attachment))return;
                String[] split = attachment.split("\\|");
                if (imgFilePathList==null)imgFilePathList=new ArrayList<>();
                for (String s : split) {
                    imgFilePathList.add(s);
                }

                if (imgFilePathList != null && imgFilePathList.size() > 0) {
                    for (int j = 0; j < imgFilePathList.size(); j++) {
                        switch (j) {
                            case 0:
                                subjective_answer_frame_one.setVisibility(View.VISIBLE);
                                subjective_answer_frame_two.setVisibility(View.GONE);
                                subjective_answer_frame_three.setVisibility(View.GONE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            case 1:
                                subjective_answer_frame_two.setVisibility(View.VISIBLE);
                                subjective_answer_frame_three.setVisibility(View.GONE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            case 2:
                                subjective_answer_frame_three.setVisibility(View.VISIBLE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            default:
                                QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                                break;
                        }
                    }
                }
                //????????????
                subjective_answer_tool_layout.setVisibility(View.GONE);
                subjective_del_one.setVisibility(View.GONE);
                subjective_del_two.setVisibility(View.GONE);
                subjective_del_three.setVisibility(View.GONE);

                //??????????????????
                subjective_input.setFocusable(false);
                subjective_input.setFocusableInTouchMode(false);
                subjective_input.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                if (!TextUtils.isEmpty(answerContent)){

                    subjective_input.setText("????????????: "+answerContent);
                }

            }
        }

        if (status.equals(Constant.Save_Status)){
            if (localTextAnswersBean_sub!=null){
                QZXTools.logE("??????????????????"+localTextAnswersBean_sub.toString(),null);

               showImgsAndContentTwo(localTextAnswersBean_sub);
            }else {
                if (ownList!=null && ownList.size()>0) {
                    WorkOwnResult workOwnResult = ownList.get(0);
                    String answerContent = workOwnResult.getAnswerContent();
                    //??????
                    String attachment = workOwnResult.getAttachment();
                    if (!TextUtils.isEmpty(attachment)){

                        String[] split = attachment.split("\\|");
                        if (imgFilePathList==null)imgFilePathList=new ArrayList<>();
                        for (String s : split) {
                            imgFilePathList.add(s);
                        }

                        if (imgFilePathList != null && imgFilePathList.size() > 0) {
                            for (int j = 0; j < imgFilePathList.size(); j++) {
                                switch (j) {
                                    case 0:
                                        subjective_answer_frame_one.setVisibility(View.VISIBLE);
                                        subjective_answer_frame_two.setVisibility(View.GONE);
                                        subjective_answer_frame_three.setVisibility(View.GONE);
                                        Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                        break;
                                    case 1:
                                        subjective_answer_frame_two.setVisibility(View.VISIBLE);
                                        subjective_answer_frame_three.setVisibility(View.GONE);
                                        Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                        break;
                                    case 2:
                                        subjective_answer_frame_three.setVisibility(View.VISIBLE);
                                        Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                        break;
                                    default:
                                        QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                                        break;
                                }
                            }
                        }


                        //????????????????????????
                        //-------------------------?????????????????????????????????id
                        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                        localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
                        localTextAnswersBean.setQuestionId(questionInfo.getId() + "");
                        localTextAnswersBean.setUserId(UserUtils.getUserId());
                        localTextAnswersBean.setQuestionType(questionInfo.getQuestionChannelType());
                        localTextAnswersBean.setAnswerContent(subjective_input.getText().toString().trim());
                        localTextAnswersBean.setImageList(imgFilePathList);
                        QZXTools.logE("subjective Save localTextAnswersBean=" + localTextAnswersBean, null);
                        //???????????????????????????
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    }

                    if (!TextUtils.isEmpty(answerContent)){
                        subjective_input.setText(answerContent);
                    }

                }
            }

            //??????????????????????????????
            subjective_input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //-------------------------?????????????????????????????????id
                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                    localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
                    localTextAnswersBean.setQuestionId(questionInfo.getId() + "");
                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                    localTextAnswersBean.setQuestionType(questionInfo.getQuestionChannelType());
                    localTextAnswersBean.setAnswerContent(s.toString().trim());
                    localTextAnswersBean.setImageList(imgFilePathList);
//                                QZXTools.logE("Save localTextAnswersBean=" + localTextAnswersBean, null);
                    //???????????????????????????
                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
                    //-------------------------?????????????????????????????????id
                }
            });
        }

        if (status.equals(Constant.Review_Status)){
            //????????????????????????
            if (teaDescFile!=null && teaDescFile.size()>0){
                if (imgFilePathList==null)imgFilePathList=new ArrayList<>();
                for (String s : teaDescFile) {
                    imgFilePathList.add(s);
                }
                if (imgFilePathList != null && imgFilePathList.size() > 0) {
                    for (int j = 0; j < imgFilePathList.size(); j++) {
                        switch (j) {
                            case 0:
                                subjective_answer_frame_one.setVisibility(View.VISIBLE);
                                subjective_answer_frame_two.setVisibility(View.GONE);
                                subjective_answer_frame_three.setVisibility(View.GONE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            case 1:
                                subjective_answer_frame_two.setVisibility(View.VISIBLE);
                                subjective_answer_frame_three.setVisibility(View.GONE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            case 2:
                                subjective_answer_frame_three.setVisibility(View.VISIBLE);
                                Glide.with(mContext).load(imgFilePathList.get(j)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                                break;
                            default:
                                QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                                break;
                        }
                    }
                }
                //????????????
                subjective_answer_tool_layout.setVisibility(View.GONE);
                subjective_del_one.setVisibility(View.GONE);
                subjective_del_two.setVisibility(View.GONE);
                subjective_del_three.setVisibility(View.GONE);

                //??????????????????
                subjective_input.setFocusable(false);
                subjective_input.setFocusableInTouchMode(false);
                subjective_input.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                //????????????
                if (ownList!=null && ownList.size()>0) {
                    WorkOwnResult workOwnResult = ownList.get(0);
                    String answerContent = workOwnResult.getAnswerContent();
                    String comment = workOwnResult.getComment();
                    if (!TextUtils.isEmpty(answerContent)){

                        subjective_input.setText("????????????: "+answerContent);
                    }
                    if (!TextUtils.isEmpty(comment)){
                        //????????????
                         tv_teacher_answer_content.setText("????????????: "+comment);
                    }
                }




            }
        }


    }



    @Override
    public int getItemCount() {
        return 1;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ZBVPermission.PermPassResult {

        public ViewHolder(@NonNull View view) {
            super(view);

            subjective_answer_frame_one = view.findViewById(R.id.subjective_answer_frame_one);
            subjective_answer_frame_two = view.findViewById(R.id.subjective_answer_frame_two);
            subjective_answer_frame_three = view.findViewById(R.id.subjective_answer_frame_three);

            subjective_img_one = view.findViewById(R.id.subjective_img_one);
            subjective_img_two = view.findViewById(R.id.subjective_img_two);
            subjective_img_three = view.findViewById(R.id.subjective_img_three);

            subjective_del_layout_one = view.findViewById(R.id.subjective_del_layout_one);
            subjective_del_layout_two = view.findViewById(R.id.subjective_del_layout_two);
            subjective_del_layout_three = view.findViewById(R.id.subjective_del_layout_three);

            subjective_del_one = view.findViewById(R.id.subjective_del_one);
            subjective_del_two = view.findViewById(R.id.subjective_del_two);
            subjective_del_three = view.findViewById(R.id.subjective_del_three);


            subjective_answer_tool_layout = view.findViewById(R.id.subjective_answer_tool_layout);
            subjective_camera = view.findViewById(R.id.subjective_camera);
            subjective_board = view.findViewById(R.id.subjective_board);

            subjective_input = view.findViewById(R.id.subjective_input);
            tv_teacher_answer_content = view.findViewById(R.id.tv_teacher_answer_content);

            subjective_answer_frame_one.setVisibility(View.GONE);
            subjective_answer_frame_two.setVisibility(View.GONE);
            subjective_answer_frame_three.setVisibility(View.GONE);

            subjective_camera.setOnClickListener(this);
            subjective_board.setOnClickListener(this);

            subjective_img_one.setOnClickListener(this);
            subjective_img_two.setOnClickListener(this);
            subjective_img_three.setOnClickListener(this);

            subjective_del_one.setOnClickListener(this);
            subjective_del_two.setOnClickListener(this);
            subjective_del_three.setOnClickListener(this);

            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "PingFang-SimpleBold.ttf");
            subjective_camera.setTypeface(typeface);
            subjective_board.setTypeface(typeface);
            subjective_input.setTypeface(typeface);
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.subjective_camera:
                    if (imgFilePathList==null)imgFilePathList=new ArrayList<>();
                    if (imgFilePathList.size() >= 3) {
                        QZXTools.popCommonToast(mContext, "??????????????????????????????", false);
                        return;
                    }

                    //???????????????ID
                    NewKnowledgeQuestionView.subjQuestionId = questionInfo.getId() + "";

                    isClickCamera = true;

                    ZBVPermission.getInstance().setPermPassResult(this);
                    if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                        ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                    } else {
                        //??????????????????
                        QZXTools.logD("?????????????????????????????????");
                        openCamera();
                    }

                    break;
                case R.id.subjective_board:
                    if (imgFilePathList==null)imgFilePathList=new ArrayList<>();
                    if (imgFilePathList.size() >= 3) {
                        QZXTools.popCommonToast(mContext, "??????????????????????????????", false);
                        return;
                    }

                    isClickCamera = false;

                    ZBVPermission.getInstance().setPermPassResult(this);
                    if (!ZBVPermission.getInstance().hadPermissions((Activity) mContext, needPermissions)) {
                        ZBVPermission.getInstance().requestPermissions((Activity) mContext, needPermissions);
                    } else {
                        Intent intent = new Intent(mContext, WhiteBoardActivity.class);
                        intent.putExtra("extra_info", questionInfo.getId() + "");
                       mContext.startActivity(intent);
                    }
                    break;
                case R.id.subjective_img_one:
                    showPhotoView(0);
                    break;
                case R.id.subjective_img_two:
                    showPhotoView(1);
                    break;
                case R.id.subjective_img_three:
                    showPhotoView(2);
                    break;
                case R.id.subjective_del_one:
                    imgFilePathList.remove(0);
                    //?????????????????????????????????
                    showImgsSaveAnswer();
                    break;
                case R.id.subjective_del_two:
                    imgFilePathList.remove(1);
                    showImgsSaveAnswer();
                    break;
                case R.id.subjective_del_three:
                    imgFilePathList.remove(2);
                    showImgsSaveAnswer();
                    break;
            }
        }

        @Override
        public void grantPermission() {

        }

        @Override
        public void denyPermission() {

        }
    }

    /**
     * ????????????
     */
    private void showImgsSaveAnswer() {

        if (imgFilePathList != null && imgFilePathList.size() <= 0) {
            subjective_answer_frame_one.setVisibility(View.GONE);
            subjective_answer_frame_two.setVisibility(View.GONE);
            subjective_answer_frame_three.setVisibility(View.GONE);
        }

        for (int i = 0; i < imgFilePathList.size(); i++) {
            switch (i) {
                case 0:
                    subjective_answer_frame_one.setVisibility(View.VISIBLE);
                    subjective_answer_frame_two.setVisibility(View.GONE);
                    subjective_answer_frame_three.setVisibility(View.GONE);
                 //   subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));

                    Glide.with(mContext)
                            .load(imgFilePathList.get(i))
                            .into(subjective_img_one);
                    break;
                case 1:
                    subjective_answer_frame_two.setVisibility(View.VISIBLE);
                    subjective_answer_frame_three.setVisibility(View.GONE);
                   // subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));

                    Glide.with(mContext)
                            .load(imgFilePathList.get(i))
                            .into(subjective_img_two);
                    break;
                case 2:
                    subjective_answer_frame_three.setVisibility(View.VISIBLE);
                   // subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));

                    Glide.with(mContext)
                            .load(imgFilePathList.get(i))
                            .into(subjective_img_three);
                    break;
                default:
                    QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                    break;
            }
        }

        //-------------------------?????????????????????????????????id
        LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
        localTextAnswersBean.setHomeworkId(questionInfo.getHomeworkId());
        localTextAnswersBean.setQuestionId(questionInfo.getId() + "");
        localTextAnswersBean.setUserId(UserUtils.getUserId());
        localTextAnswersBean.setQuestionType(questionInfo.getQuestionChannelType());
        localTextAnswersBean.setAnswerContent(subjective_input.getText().toString().trim());
        localTextAnswersBean.setImageList(imgFilePathList);
        QZXTools.logE("subjective Save localTextAnswersBean=" + localTextAnswersBean, null);
        //???????????????????????????
        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);
        //-------------------------?????????????????????????????????id
    }


    /**
     * ????????????
     */
    //???????????????
    private boolean isOpenCamera;
    private void openCamera() {
        isOpenCamera = true;

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
//        cameraIntent.putExtra("extra_info", questionInfo.getId());
        ((Activity) mContext).startActivityForResult(cameraIntent, CODE_SYS_CAMERA);
    }


    /**
     * ?????????????????????
     */
    private void showPhotoView(int curIndex) {
        Intent intent = new Intent(mContext, ImageLookActivity.class);
        intent.putStringArrayListExtra("imgResources", imgFilePathList);
        intent.putExtra("NeedComment", false);
        intent.putExtra("curImgIndex", curIndex);
        mContext.startActivity(intent);
        //??????????????????????????????????????????????????????
//        ActivityOptionsCompat options = ActivityOptionsCompat.
//                makeSceneTransitionAnimation((Activity) mContext, this, "");
//        mContext.startActivity(intent, options.toBundle());
    }


    private void showImgsAndContentTwo(LocalTextAnswersBean localTextAnswersBean) {
        //??????????????????
        if (localTextAnswersBean != null) {
            String textAnswer = localTextAnswersBean.getAnswerContent();
            subjective_input.setText(textAnswer);
            subjective_input.setSelection(textAnswer.length());

            imgFilePathList = (ArrayList<String>) localTextAnswersBean.getImageList();
            if (imgFilePathList != null && imgFilePathList.size() > 0) {
                for (int i = 0; i < imgFilePathList.size(); i++) {
                    switch (i) {
                        case 0:
                            subjective_answer_frame_one.setVisibility(View.VISIBLE);
                            subjective_answer_frame_two.setVisibility(View.GONE);
                            subjective_answer_frame_three.setVisibility(View.GONE);
                            Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                            break;
                        case 1:
                            subjective_answer_frame_two.setVisibility(View.VISIBLE);
                            subjective_answer_frame_three.setVisibility(View.GONE);
                            Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                            break;
                        case 2:
                            subjective_answer_frame_three.setVisibility(View.VISIBLE);
                            Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                            break;
                        default:
                            QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                            break;
                    }
                }
            }
        }
    }



    /**
     * ??????????????????????????????
     * <p>
     * notes:?????????????????????????????????????????????????????????????????????????????????????????????
     * <p>
     * ?????????????????????????????????OwnList???ImgFile?????????null;????????????????????????????????????;
     */
    public void showImgsAndContent(LocalTextAnswersBean localTextAnswersBean) {

        //????????????
        if (questionInfo.getOwnList() != null && questionInfo.getOwnList().size() > 0) {
            String textAnswer = questionInfo.getOwnList().get(0).getAnswerContent();
            subjective_input.setText(textAnswer);
            subjective_input.setSelection(textAnswer.length());
        } else {
            //??????????????????
            if (localTextAnswersBean != null) {
                String textAnswer = localTextAnswersBean.getAnswerContent();
                subjective_input.setText(textAnswer);
                subjective_input.setSelection(textAnswer.length());
            }
        }

        //????????????
        if (questionInfo.getImgFile() != null && questionInfo.getImgFile().size() > 0) {
            imgFilePathList = (ArrayList<String>) questionInfo.getImgFile();
        } else {
            if (localTextAnswersBean != null) {
                imgFilePathList = (ArrayList<String>) localTextAnswersBean.getImageList();
            }
        }

        //?????????????????????imgs??????
        if (imgFilePathList == null) {
            imgFilePathList = new ArrayList<>();
        }

        if (imgFilePathList != null && imgFilePathList.size() > 0) {
            for (int i = 0; i < imgFilePathList.size(); i++) {
                switch (i) {
                    case 0:
                        subjective_answer_frame_one.setVisibility(View.VISIBLE);
                        subjective_answer_frame_two.setVisibility(View.GONE);
                        subjective_answer_frame_three.setVisibility(View.GONE);
                        Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_one);
//                        subjective_img_one.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    case 1:
                        subjective_answer_frame_two.setVisibility(View.VISIBLE);
                        subjective_answer_frame_three.setVisibility(View.GONE);
                        Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_two);
//                        subjective_img_two.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    case 2:
                        subjective_answer_frame_three.setVisibility(View.VISIBLE);
                        Glide.with(mContext).load(imgFilePathList.get(i)).into(subjective_img_three);
//                        subjective_img_three.setImageBitmap(BitmapFactory.decodeFile(imgFilePathList.get(i)));
                        break;
                    default:
                        QZXTools.popCommonToast(mContext, "imgFileList????????????3??????", false);
                        break;
                }
            }
        }
    }


    /**
     * ???????????????   ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????(KB)
     */
    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        QZXTools.logE("Bank fromBoardCallback ExtraInfoBean=" + extraInfoBean + ";id=" + questionInfo.getId(), null);
        //???????????????????????????????????????SubjectiveToDoView??????????????????????????????
        if (extraInfoBean.getQuestionId().equals(questionInfo.getId() + "")) {
            imgFilePathList.add(extraInfoBean.getFilePath());
            showImgsSaveAnswer();
        }
    }

    /**
     * ???????????????   ???????????????????????????????????????????????????????????????????????????????????????????????????(MB)
     * <p>
     * ??????????????????????????????????????????????????????????????????????????????
     * <p>
     * ?????????????????????????????????????????????????????????list?????????
     */

    public void fromCameraCallback(String flag) {
        if (flag.equals("CAMERA_CALLBACK")) {
            QZXTools.logE("fromCameraCallback filePath=" + cameraFile.getAbsolutePath(), null);
            //???????????????????????????????????????SubjectiveToDoView??????????????????????????????
            if (NewKnowledgeQuestionView.subjQuestionId.equals(questionInfo.getId() + "")) {
                //??????????????????
//                File compressFile = compressImage(BitmapFactory.decodeFile(cameraFile.getAbsolutePath()));

                //?????????????????? notes:??????????????????????????????????????????????????????800???????????????325k,400???????????????85k
                File compressFile = compressBitmapToFile(cameraFile.getAbsolutePath(),
                        mContext.getResources().getDimensionPixelSize(R.dimen.x800));

                imgFilePathList.add(compressFile.getAbsolutePath());
                showImgsSaveAnswer();
            }
        }
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


}
