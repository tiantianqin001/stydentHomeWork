package com.telit.zhkt_three.Adapter.AutoLearning;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.telit.zhkt_three.Activity.AutonomousLearning.ItemBankBookActivity;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.Download.DownloadProgressBar;
import com.telit.zhkt_three.CustomView.RoundCornerImageView;
import com.telit.zhkt_three.Fragment.Dialog.TipsDialog;
import com.telit.zhkt_three.JavaBean.Resource.FillResource;
import com.telit.zhkt_three.JavaBean.Resource.LocalResourceRecord;
import com.telit.zhkt_three.MediaTools.audio.AudioPlayActivity;
import com.telit.zhkt_three.MediaTools.ebook.FlipEBookResourceActivity;
import com.telit.zhkt_three.MediaTools.image.ImageLookActivity;
import com.telit.zhkt_three.MediaTools.video.VideoPlayerActivity;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.BuriedPointUtils;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;

import com.telit.zhkt_three.Utils.ViewUtils;

import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.greendao.LocalResourceRecordDao;
import com.zbv.meeting.util.SharedPreferenceUtil;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

/**
 * author: qzx
 * Date: 2019/6/10 8:47
 * <p>
 * ????????????????????????
 * todo ?????????????????????????????????
 */
public class RVAutoLearningAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<FillResource> mDatas;

    private static final int TeachingMaterial = 1;
    private static final int Other = 2;

    private String flag;

    public RVAutoLearningAdapter(Context context, List<FillResource> list,String flag) {
        mContext = context;
        mDatas = list;
        this.flag = flag;
        downloading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        /**
         * ???????????????int?????????getItemViewType??????????????????type??????
         * */
        if (viewType == TeachingMaterial) {
            return new RVAutoLearningTeachingMaterialViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.rv_item_teaching_material_layout, viewGroup, false));
        } else {
            return new RVAutoLearningNormalResourceViewHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.rv_item_homework_audio_pic_video_layout, viewGroup, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof RVAutoLearningNormalResourceViewHolder) {
            RVAutoLearningNormalResourceViewHolder rvAutoLearningNormalResourceViewHolder = (RVAutoLearningNormalResourceViewHolder) viewHolder;
            /**
             * ?????????3
             * ?????????2
             * ?????????1
             * ???????????????1010
             */
            if (mDatas.get(i).getType().equals("3")) {
                rvAutoLearningNormalResourceViewHolder.rv_item_main_content.setBackgroundResource(R.mipmap.image_video_default);

                rvAutoLearningNormalResourceViewHolder.rv_item_img_video.setVisibility(View.VISIBLE);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_type.setVisibility(View.GONE);

                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setBackgroundResource(R.drawable.shape_round_corner_video_bg);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setTextColor(mContext.getResources().getColor(R.color.video_text_color));
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setText("??????");
            } else if (mDatas.get(i).getType().equals("2")) {
                rvAutoLearningNormalResourceViewHolder.rv_item_main_content.setBackgroundResource(R.mipmap.audio_bg);

                rvAutoLearningNormalResourceViewHolder.rv_item_img_video.setVisibility(View.GONE);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_type.setVisibility(View.VISIBLE);

                rvAutoLearningNormalResourceViewHolder.rv_item_tv_type.setText("??????");

                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setBackgroundResource(R.drawable.shape_round_corner_audio_bg);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setTextColor(mContext.getResources().getColor(R.color.audio_text_color));
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setText("??????");
            } else if (mDatas.get(i).getType().equals("1")) {
                rvAutoLearningNormalResourceViewHolder.rv_item_main_content.setBackgroundResource(R.mipmap.pic_bg);

                rvAutoLearningNormalResourceViewHolder.rv_item_img_video.setVisibility(View.GONE);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_type.setVisibility(View.VISIBLE);

                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setBackgroundResource(R.drawable.shape_round_corner_pic_bg);
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setTextColor(mContext.getResources().getColor(R.color.picture_text_color));
                rvAutoLearningNormalResourceViewHolder.rv_item_tv_type.setText("??????");

                rvAutoLearningNormalResourceViewHolder.rv_item_tv_colorType.setText("??????");
            }

            //????????????????????????????????????????????????????????????????????????????????????
            rvAutoLearningNormalResourceViewHolder.rv_item_tv_teacher_name.setText(mDatas.get(i).getPressname());
            rvAutoLearningNormalResourceViewHolder.rv_item_tv_date.setText(mDatas.get(i).getGradename().concat(mDatas.get(i).getTermname()));

            String topicTitle = mDatas.get(i).getTitle();

            //????????????????????????\\|????????????????????????????????????
            int indexTwo = topicTitle.indexOf("-");
            int indexOne = topicTitle.indexOf("???");
            if (indexTwo != -1 && indexOne != -1) {
                topicTitle = topicTitle.substring(indexTwo + 1, indexOne - 1);
            }
            rvAutoLearningNormalResourceViewHolder.rv_item_tv_topic.setText(topicTitle);
            QZXTools.logE("flag: "+flag,null);
            rvAutoLearningNormalResourceViewHolder.rv_item_tv_subType.setText(mDatas.get(i).getSubjectName());

            //????????????
            LocalResourceRecordDao localResourceRecordDao = MyApplication.getInstance().getDaoSession().getLocalResourceRecordDao();

            QZXTools.logE("i=" + i, null);

            if (mDatas.get(i).getId() != null && mDatas.get(i).getType() != null) {
                LocalResourceRecord localResourceRecord = localResourceRecordDao.queryBuilder().where
                        (LocalResourceRecordDao.Properties.ResourceId.eq(mDatas.get(i).getId()),
                                LocalResourceRecordDao.Properties.ResourceType.eq(mDatas.get(i).getType())).unique();

                if (localResourceRecord != null) {
                    QZXTools.logE("localResourceRecord11=" + localResourceRecord + ";i=" + i, null);
                    rvAutoLearningNormalResourceViewHolder.rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_FINISH);
                    mDatas.get(i).setFilePath(localResourceRecord.getResourceFilePath());
                } else {
                    rvAutoLearningNormalResourceViewHolder.rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_READY);
                }
            }
        } else if (viewHolder instanceof RVAutoLearningTeachingMaterialViewHolder){
            RVAutoLearningTeachingMaterialViewHolder rvAutoLearningTeachingMaterialViewHolder =
                    (RVAutoLearningTeachingMaterialViewHolder) viewHolder;

            //?????????????????????????????????????????????
            if (mDatas.get(i).isItemBank()) {
                rvAutoLearningTeachingMaterialViewHolder.rv_item_book_download_tags.setVisibility(View.GONE);
            } else {
                rvAutoLearningTeachingMaterialViewHolder.rv_item_book_download_tags.setVisibility(View.VISIBLE);

                LocalResourceRecordDao localResourceRecordDao = MyApplication.getInstance().getDaoSession().getLocalResourceRecordDao();
                LocalResourceRecord localResourceRecord = localResourceRecordDao.queryBuilder().where
                        (LocalResourceRecordDao.Properties.ResourceId.eq(mDatas.get(i).getId()),
                                LocalResourceRecordDao.Properties.ResourceType.eq(mDatas.get(i).getType())).unique();

                if (localResourceRecord != null) {
                    QZXTools.logE("book localResourceRecord=" + localResourceRecord + ";i=" + i, null);
                    rvAutoLearningTeachingMaterialViewHolder.rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_FINISH);
                    mDatas.get(i).setFilePath(localResourceRecord.getResourceFilePath());
                } else {
                    rvAutoLearningTeachingMaterialViewHolder.rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_READY);
                }
            }

            Glide.with(mContext).load(mDatas.get(i).getCover()).placeholder(R.mipmap.no_cover).error(R.mipmap.no_cover)
                    .into(rvAutoLearningTeachingMaterialViewHolder.rv_item_book_face);

            rvAutoLearningTeachingMaterialViewHolder.rv_item_book_book.setText(mDatas.get(i).getGradename().concat(mDatas.get(i).getTermname()));
            rvAutoLearningTeachingMaterialViewHolder.rv_item_book_press.setText(mDatas.get(i).getPressname());

            String topicTitle = mDatas.get(i).getTitle();

            //????????????????????????\\|????????????????????????????????????
            int indexTwo = topicTitle.indexOf("-");
            int indexOne = topicTitle.indexOf("???");
            if (indexTwo != -1 && indexOne != -1) {
                topicTitle = topicTitle.substring(indexTwo + 1, indexOne - 1);
            }
            rvAutoLearningTeachingMaterialViewHolder.rv_item_book_topic.setText(topicTitle);
            if ("1".equals(flag)){
                rvAutoLearningTeachingMaterialViewHolder.rv_item_book_subType.setText(mDatas.get(i).getSubjectName());
            }else {
                rvAutoLearningTeachingMaterialViewHolder.rv_item_book_subType.setText(mDatas.get(i).getPressname());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * ??????????????????ViewHolder?????????????????????????????????????????????????????????
     */
    @Override
    public int getItemViewType(int position) {
        if (mDatas.get(position).isTeachingMaterial()) {
            return TeachingMaterial;
        }else {
            return Other;
        }
    }

    /**
     * ????????????????????????
     */
    public class RVAutoLearningNormalResourceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, TipsDialog.ClickInterface {

        //????????????
        private RelativeLayout rv_item_main_content;
        private TextView rv_item_tv_tags;
        private ImageView rv_item_img_video;
        private TextView rv_item_tv_type;
        private TextView rv_item_tv_teacher_name;
        private TextView rv_item_tv_date;
        private DownloadProgressBar rv_item_download_tags;
        private TextView rv_item_tv_topic;
        private TextView rv_item_tv_subType;
        private TextView rv_item_tv_colorType;

        private TipsDialog tipsDialog;

        public RVAutoLearningNormalResourceViewHolder(@NonNull View itemView) {
            super(itemView);
            rv_item_main_content = itemView.findViewById(R.id.rv_item_main_content);
            rv_item_tv_tags = itemView.findViewById(R.id.rv_item_tv_tags);
            rv_item_img_video = itemView.findViewById(R.id.rv_item_img_video);
            rv_item_tv_type = itemView.findViewById(R.id.rv_item_tv_type);
            rv_item_tv_teacher_name = itemView.findViewById(R.id.rv_item_tv_teacher_name);
            rv_item_tv_date = itemView.findViewById(R.id.rv_item_tv_date);
            rv_item_download_tags = itemView.findViewById(R.id.rv_item_download_tags);
            rv_item_tv_topic = itemView.findViewById(R.id.rv_item_tv_topic);
            rv_item_tv_subType = itemView.findViewById(R.id.rv_item_tv_subType);
            rv_item_tv_colorType = itemView.findViewById(R.id.rv_item_tv_colorType);

            rv_item_main_content.setOnClickListener(this);
            rv_item_download_tags.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rv_item_main_content:

                    if (!ViewUtils.isFastClick(1000)){
                        return;
                    }

                    //????????????????????????XRecyclerView??????HeadView
                    int position = getAdapterPosition() - 1;
                    if (mDatas.get(position).isItemBank()) {
                        //???????????????????????????
                        Intent intent = new Intent(mContext, ItemBankBookActivity.class);
                        //?????????????????????
                        String learning_section = mDatas.get(position).getXd();
                        String subject = mDatas.get(position).getChid();
                        String chapterId = mDatas.get(position).getKnowledgeId();
                        QZXTools.logE("learning_section=" + learning_section + ";subject="
                                + subject + ";chapterId=" + chapterId, null);
                        intent.putExtra("learning_section", learning_section);
                        intent.putExtra("subject", subject);
                        intent.putExtra("chapterId", chapterId);
                        mContext.startActivity(intent);

                        //????????????????????????????????????
                        int subjectId = -1;
                        if (!TextUtils.isEmpty(mDatas.get(getLayoutPosition() - 1).getSubjectId())) {
                            subjectId = Integer.parseInt(mDatas.get(getLayoutPosition() - 1).getSubjectId());
                        }
                      /*  MyApplication.getInstance().AutoLearningMaiDian(MyApplication.FLAG_AUTO_LEARNING_FOUR,
                                -1, subjectId, mDatas.get(getLayoutPosition() - 1).getSubjectName());*/
                    //??????????????????????????????????????????
                        String uuid = UUID.randomUUID().toString();
                        SharedPreferenceUtil.getInstance(MyApplication.getInstance()).setString("SelfLearning",uuid);
                        BuriedPointUtils.buriedPoint("2032","","","",uuid);


                    } else {
                        //??????????????????????????????
                        if (rv_item_download_tags.getStatus() == DownloadProgressBar.STATUS_READY) {
                            //??????????????????
                            tipsDialog = new TipsDialog();
                            tipsDialog.setTipsStyle("??????????????????????????????????????????????????????",
                                    "??????", "?????????", -1);
                            tipsDialog.setClickInterface(this);
                            tipsDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), TipsDialog.class.getSimpleName());
                        } else if (rv_item_download_tags.getStatus() == DownloadProgressBar.STATUS_FINISH) {

                            switch (mDatas.get(position).getType()) {
                                case "3":
                                    QZXTools.logE("filepath=" + mDatas.get(getLayoutPosition() - 1).getFilePath(), null);

                                    Intent intent_video = new Intent(mContext, VideoPlayerActivity.class);
                                    intent_video.putExtra("VideoFilePath", mDatas.get(getLayoutPosition() - 1).getFilePath());
                                    intent_video.putExtra("VideoTitle", mDatas.get(getLayoutPosition() - 1).getTitle());
                                    if (!TextUtils.isEmpty(mDatas.get(getLayoutPosition() - 1).getCover())) {
                                        intent_video.putExtra("VideoThumbnail", mDatas.get(getLayoutPosition() - 1).getCover());
                                    }
                                    mContext.startActivity(intent_video);

                                    //?????????????????????????????????????????????????????????????????????????????????
//                                    JZVideoPlayer.setMediaInterface(new IjkMediaEngine());
//                                    JZVideoPlayerStandard.NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
//                                    JZVideoPlayer.startFullscreen(mContext, CustomeJZVideoPlayerStandard.class,
//                                            mDatas.get(getLayoutPosition() - 1).getFilePath(), "...");
                                    break;
                                case "2":
                                    Intent intent = new Intent(mContext, AudioPlayActivity.class);
                                    intent.putExtra("AudioFilePath", mDatas.get(getLayoutPosition() - 1).getFilePath());
                                    intent.putExtra("AudioFileName", mDatas.get(getLayoutPosition() - 1).getTitle());
                                    mContext.startActivity(intent);
                                    break;
                                case "1":
                                    String filePath = mDatas.get(getLayoutPosition() - 1).getFilePath();
                                    if (filePath.substring(filePath.lastIndexOf(".") + 1).equals("zip")) {
                                        //??????zip??????
                                        String parentDir = QZXTools.getExternalStorageForFiles(mContext, null);
                                        String destinationDir = parentDir + File.separator +
                                                mDatas.get(getLayoutPosition() - 1).getId() + File.separator;
                                        File file = new File(destinationDir);
                                        QZXTools.logE("destination=" + destinationDir + ";file is exist=" + file.exists(), null);

                                        ArrayList<String> imgFilePathList = new ArrayList<>();

                                        if (file.exists() && file.isDirectory()) {
                                            File[] files = file.listFiles();
                                            for (File f : files) {
                                                imgFilePathList.add(f.getAbsolutePath());
                                            }
                                        } else {
                                            zipFileRead(filePath, destinationDir, imgFilePathList);
                                        }

                                        Intent intent_img = new Intent(mContext, ImageLookActivity.class);
                                        intent_img.putStringArrayListExtra("imgResources", imgFilePathList);
                                        intent_img.putExtra("curImgIndex", 0);
                                        mContext.startActivity(intent_img);
                                    } else {
                                        Intent intent_img = new Intent(mContext, ImageLookActivity.class);
                                        ArrayList<String> imgFilePathList = new ArrayList<>();
                                        imgFilePathList.add(filePath);
                                        intent_img.putStringArrayListExtra("imgResources", imgFilePathList);
                                        intent_img.putExtra("curImgIndex", 0);
                                        mContext.startActivity(intent_img);
                                    }
                                    break;
                                default:
                                    QZXTools.openFile(new File(mDatas.get(getLayoutPosition() - 1).getFilePath()), mContext);
                                    break;
                            }

                            //????????????????????????????????????????????????
                            int subjectId = -1;
                            if (!TextUtils.isEmpty(mDatas.get(getLayoutPosition() - 1).getSubjectId())) {
                                subjectId = Integer.parseInt(mDatas.get(getLayoutPosition() - 1).getSubjectId());
                            }
                            MyApplication.getInstance().AutoLearningMaiDian(MyApplication.FLAG_AUTO_LEARNING_FOUR,
                                    -1, subjectId, mDatas.get(getLayoutPosition() - 1).getSubjectName());
                        }
                    }
                    break;
                case R.id.rv_item_download_tags:
                    switch (rv_item_download_tags.getStatus()) {
//                        case DownloadProgressBar.STATUS_DOWNLOADING:
//                            OkHttp3_0Utils.getInstance().cancleDownloadMulti();
//                            //??????100??????,?????????????????????????????????????????????
//                            rv_item_download_tags.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_PAUSE);
//                                }
//                            }, 100);
//                            break;
//                        case DownloadProgressBar.STATUS_PAUSE:
//                            startDownload();
//                            break;
                        case DownloadProgressBar.STATUS_READY:
                            if (downloading){
                                QZXTools.popToast(mContext, "????????????????????????????????????", false);
                                return;
                            }

                            startDownload();
                            break;
                        case DownloadProgressBar.STATUS_FINISH:
                            //????????????
                            rv_item_main_content.performClick();
                            break;
                        case DownloadProgressBar.STATUS_ERROR:
                            //????????????---?????????????????????????????????????????????????????????????????????
                            QZXTools.deleteFileOrDirectory(mDatas.get(getLayoutPosition() - 1).getFilePath());
                            rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_READY);
                            break;
                    }
                    break;
            }
        }

        @Override
        public void cancle() {
            tipsDialog.dismissAllowingStateLoss();
            tipsDialog.setClickInterface(null);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void confirm() {
            tipsDialog.dismissAllowingStateLoss();
            tipsDialog.setClickInterface(null);

            if (downloading){
                QZXTools.popToast(mContext, "????????????????????????????????????", false);
                return;
            }

            startDownload();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void startDownload() {
            QZXTools.logE("rv_item_download_tags.getStatus()====" + rv_item_download_tags.getStatus(), null);

            //????????????
            rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_WATING);

            int position = getLayoutPosition() - 1;
            String baseUrl = "";
            String getParams = "?id=" + mDatas.get(position).getId() + "&userid=" + UserUtils.getUserId();
            //??????url??????????????????

            baseUrl = UrlUtils.CommonResourceDownload;

            QZXTools.logE("url====" + baseUrl + getParams, null);

            OkHttp3_0Utils.getInstance().downloadSingleFileForOnce(baseUrl + getParams, null, new OkHttp3_0Utils.DownloadCallback() {

                @Override
                public void downloadProcess(int value) {
                    rv_item_download_tags.setProgress(value);
                    rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_DOWNLOADING);
                    downloading = true;
                }

                @Override
                public void downloadComplete(String filePath) {
                    if (filePath.contains("userid")){
                        rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_ERROR);
                    }else {
                        rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_FINISH);

                        QZXTools.logE("filepath====" + filePath, null);

                        //???????????????
                        mDatas.get(getLayoutPosition() - 1).setFilePath(filePath);

                        //???????????????
                        LocalResourceRecord localResourceRecord = new LocalResourceRecord();
                        //QZXTools.logE("layoutPosition=" + getLayoutPosition(), null);//+1
                        localResourceRecord.setResourceType(mDatas.get(getLayoutPosition() - 1).getType());
                        localResourceRecord.setResourceId(mDatas.get(getLayoutPosition() - 1).getId());

                        localResourceRecord.setResourceName(mDatas.get(getLayoutPosition() - 1).getTitle());
                        localResourceRecord.setImageUrl(mDatas.get(getLayoutPosition() - 1).getCover());
                        localResourceRecord.setIsChoosed(false);

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String updateDate = simpleDateFormat.format(new Date());
                        localResourceRecord.setResourceUpdateDate(updateDate);

                        localResourceRecord.setCanChecked(false);

                        localResourceRecord.setResourceFilePath(filePath);
                        MyApplication.getInstance().getDaoSession().getLocalResourceRecordDao().insertOrReplace(localResourceRecord);

                        EventBus.getDefault().post(localResourceRecord.getResourceType(), Constant.Auto_Learning_Update);

                        downloading = false;
                    }
                }

                @Override
                public void downloadFailure() {
                    rv_item_download_tags.setStatus(DownloadProgressBar.STATUS_ERROR);

                    QZXTools.logE("downloadFailure", null);

                    downloading = false;
                }
            });
        }
    }

    private boolean downloading;

    /**
     * ??????
     */
    public class RVAutoLearningTeachingMaterialViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private RelativeLayout rv_item_book_main_content;
        private RoundCornerImageView rv_item_book_face;
        private TextView rv_item_book_book;
        private TextView rv_item_book_press;
        private DownloadProgressBar rv_item_book_download_tags;
        private TextView rv_item_book_topic;
        private TextView rv_item_book_subType;
        private TextView rv_item_book_colorType;

        public RVAutoLearningTeachingMaterialViewHolder(@NonNull View itemView) {
            super(itemView);
            rv_item_book_main_content = itemView.findViewById(R.id.rv_item_book_main_content);
            rv_item_book_face = itemView.findViewById(R.id.rv_item_book_face);
            rv_item_book_book = itemView.findViewById(R.id.rv_item_book_book);
            rv_item_book_press = itemView.findViewById(R.id.rv_item_book_press);
            rv_item_book_download_tags = itemView.findViewById(R.id.rv_item_book_download_tags);
            rv_item_book_topic = itemView.findViewById(R.id.rv_item_book_topic);
            rv_item_book_subType = itemView.findViewById(R.id.rv_item_book_subType);
            rv_item_book_colorType = itemView.findViewById(R.id.rv_item_book_colorType);

            rv_item_book_main_content.setOnClickListener(this);
            rv_item_book_download_tags.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rv_item_book_main_content:
                    //????????????????????????XRecyclerView??????HeadView
                    int position = getAdapterPosition() - 1;
                    if (mDatas.get(position).isItemBank()) {
                        //???????????????????????????
                        Intent intent = new Intent(mContext, ItemBankBookActivity.class);
                        //?????????????????????
                        String learning_section = mDatas.get(position).getXd();
                        String subject = mDatas.get(position).getChid();
                        String chapterId = mDatas.get(position).getKnowledgeId();
                        QZXTools.logE("learning_section=" + learning_section + ";subject="
                                + subject + ";chapterId=" + chapterId, null);
                        intent.putExtra("learning_section", learning_section);
                        intent.putExtra("subject", subject);
                        intent.putExtra("chapterId", chapterId);
                        mContext.startActivity(intent);

                        //????????????????????????????????????
                        int subjectId = -1;
                        if (!TextUtils.isEmpty(mDatas.get(getLayoutPosition() - 1).getSubjectId())) {
                            subjectId = Integer.parseInt(mDatas.get(getLayoutPosition() - 1).getSubjectId());
                        }
                        MyApplication.getInstance().AutoLearningMaiDian(MyApplication.FLAG_AUTO_LEARNING_FOUR,
                                -1, subjectId, mDatas.get(getLayoutPosition() - 1).getSubjectName());
                    } else {
                        //??????????????????????????????
                        if (rv_item_book_download_tags.getStatus() == DownloadProgressBar.STATUS_READY) {
                            //??????????????????
                            TipsDialog tipsDialog = new TipsDialog();
                            tipsDialog.setTipsStyle("??????????????????????????????????????????????????????",
                                    "??????", "?????????", -1);
                            tipsDialog.setClickInterface(new TipsDialog.ClickInterface() {
                                @Override
                                public void cancle() {
                                    tipsDialog.dismissAllowingStateLoss();
                                }

                                @Override
                                public void confirm() {
                                    tipsDialog.dismissAllowingStateLoss();

                                    if (downloading){
                                        QZXTools.popToast(mContext, "????????????????????????????????????", false);
                                        return;
                                    }

                                    startDownloadTeachingMaterial(rv_item_book_download_tags);
                                }
                            });
                            tipsDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), TipsDialog.class.getSimpleName());
                        } else if (rv_item_book_download_tags.getStatus() == DownloadProgressBar.STATUS_FINISH) {
                            //?????????????????????????????????
//                            Intent intent = new Intent(mContext, ElectronicResourceActivity.class);
//                            intent.putExtra("EBookFilePath", mDatas.get(getLayoutPosition() - 1).getFilePath());
//                            intent.putExtra("CoverUrl", mDatas.get(getLayoutPosition() - 1).getCover());
//                            mContext.startActivity(intent);

                            Intent intent = new Intent(mContext, FlipEBookResourceActivity.class);
                            intent.putExtra("EBookFilePath", mDatas.get(getLayoutPosition() - 1).getFilePath());
                            intent.putExtra("CoverUrl", mDatas.get(getLayoutPosition() - 1).getCover());
                            mContext.startActivity(intent);

                            //??????????????????????????????
                            int subjectId = -1;
                            if (!TextUtils.isEmpty(mDatas.get(getLayoutPosition() - 1).getSubjectId())) {
                                subjectId = Integer.parseInt(mDatas.get(getLayoutPosition() - 1).getSubjectId());
                            }
                            MyApplication.getInstance().AutoLearningMaiDian(MyApplication.FLAG_AUTO_LEARNING_FOUR,
                                    -1, subjectId, mDatas.get(getLayoutPosition() - 1).getSubjectName());
                        }
                    }
                    break;
                case R.id.rv_item_book_download_tags:
                    switch (rv_item_book_download_tags.getStatus()) {
//                        case DownloadProgressBar.STATUS_DOWNLOADING:
//                            OkHttp3_0Utils.getInstance().cancleDownloadMulti();
//                            //??????100??????,?????????????????????????????????????????????
//                            rv_item_book_download_tags.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_PAUSE);
//                                }
//                            }, 100);
//                            break;
//                        case DownloadProgressBar.STATUS_PAUSE:
//                            startDownload();
//                            break;
                        case DownloadProgressBar.STATUS_READY:
                            if (downloading){
                                QZXTools.popToast(mContext, "????????????????????????????????????", false);
                                return;
                            }

                            startDownloadTeachingMaterial(rv_item_book_download_tags);
                            break;
                        case DownloadProgressBar.STATUS_FINISH:
                            //??????????????????
                            TipsDialog tipsDialog = new TipsDialog();
                            tipsDialog.setTipsStyle("??????????????????????????????????????????????????????????????????????????????" +
                                            "????????????????????????????????????",
                                    "??????????????????", "???????????????", -1);
                            tipsDialog.setClickInterface(new TipsDialog.ClickInterface() {
                                @Override
                                public void cancle() {
                                    tipsDialog.dismissAllowingStateLoss();
                                    //????????????
                                    QZXTools.deleteFileOrDirectory(mDatas.get(getLayoutPosition() - 1).getFilePath());

                                    //?????????????????????
                                    LocalResourceRecord localResourceRecord = MyApplication.getInstance().getDaoSession().
                                            getLocalResourceRecordDao().queryBuilder().where
                                            (LocalResourceRecordDao.Properties.ResourceId.eq(mDatas.get(getLayoutPosition() - 1).getId())).unique();
                                    MyApplication.getInstance().getDaoSession().getLocalResourceRecordDao().delete(localResourceRecord);

                                    QZXTools.popToast(mContext, "???????????????", false);

                                    rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_READY);
                                }

                                @Override
                                public void confirm() {
                                    tipsDialog.dismissAllowingStateLoss();
                                    rv_item_book_main_content.performClick();
                                }
                            });
                            tipsDialog.show(((FragmentActivity) mContext).getSupportFragmentManager(), TipsDialog.class.getSimpleName());
                            break;
                        case DownloadProgressBar.STATUS_ERROR:
                            //????????????---?????????????????????????????????????????????????????????????????????
                            QZXTools.deleteFileOrDirectory(mDatas.get(getLayoutPosition() - 1).getFilePath());
                            rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_READY);
                            break;
                    }
                    break;
            }
        }

        //????????????
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void startDownloadTeachingMaterial(DownloadProgressBar rv_item_book_download_tags) {
            QZXTools.logE("Status====" + rv_item_book_download_tags.getStatus(), null);

            //????????????
            rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_WATING);

            int position = getLayoutPosition() - 1;
            String baseUrl = "";
            String getParams = "?id=" + mDatas.get(position).getId() + "&userid=" + UserUtils.getUserId();
            //??????url??????????????????

            baseUrl = UrlUtils.BaseUrl + UrlUtils.ElectronicBookDownload;

            QZXTools.logE("url====" + baseUrl + getParams, null);

            OkHttp3_0Utils.getInstance().downloadSingleFileForOnce(baseUrl + getParams, null, new OkHttp3_0Utils.DownloadCallback() {
                @Override
                public void downloadProcess(int value) {
                    rv_item_book_download_tags.setProgress(value);
                    rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_DOWNLOADING);

                    downloading = true;
                }

                @Override
                public void downloadComplete(String filePath) {
                    rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_FINISH);

                    //???????????????
                    mDatas.get(getLayoutPosition() - 1).setFilePath(filePath);

                    //???????????????
                    LocalResourceRecord localResourceRecord = new LocalResourceRecord();
                    //QZXTools.logE("layoutPosition=" + getLayoutPosition(), null);//+1
                    localResourceRecord.setResourceType(mDatas.get(getLayoutPosition() - 1).getType());
                    localResourceRecord.setResourceId(mDatas.get(getLayoutPosition() - 1).getId());

                    localResourceRecord.setResourceName(mDatas.get(getLayoutPosition() - 1).getTitle());
                    localResourceRecord.setImageUrl(mDatas.get(getLayoutPosition() - 1).getCover());
                    localResourceRecord.setIsChoosed(false);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String updateDate = simpleDateFormat.format(new Date());
                    localResourceRecord.setResourceUpdateDate(updateDate);

                    localResourceRecord.setCanChecked(false);

                    localResourceRecord.setResourceFilePath(filePath);
                    MyApplication.getInstance().getDaoSession().getLocalResourceRecordDao().insertOrReplace(localResourceRecord);

                    EventBus.getDefault().post(localResourceRecord.getResourceType(), Constant.Auto_Learning_Update);

                    QZXTools.logE("downloadComplete", null);

                    downloading = false;
                }

                @Override
                public void downloadFailure() {
                    rv_item_book_download_tags.setStatus(DownloadProgressBar.STATUS_ERROR);

                    QZXTools.logE("downloadFailure", null);

                    downloading = false;
                }
            });
        }
    }

    /**
     * @return void ????????????
     * @throws
     * @Description: TODO(??????Zip?????? ??? ??????zip??????????????????????????????)
     * @param????????????
     */
    public void zipFileRead(String file, String saveRootDirectory, ArrayList<String> imgList) {
        try {
            // ??????zip??????
            ZipFile zipFile = new ZipFile(file, "GBK");
//            @SuppressWarnings("unchecked")
//            Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.entries();
            Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.getEntries();
            while (enu.hasMoreElements()) {
                ZipEntry zipElement = (ZipEntry) enu.nextElement();
                InputStream read = zipFile.getInputStream(zipElement);
                String fileName = zipElement.getName();
                if (fileName != null && fileName.indexOf(".") != -1) {// ???????????????
                    unZipFile(zipElement, read, saveRootDirectory, imgList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @return void ????????????
     * @throws
     * @Description: TODO(??????????????????????????????????????????)
     */
    public void unZipFile(ZipEntry ze, InputStream read,
                          String saveRootDirectory, ArrayList<String> imgList) {
        // ???????????????????????????????????????OK.
        String fileName = ze.getName();
        imgList.add(saveRootDirectory.concat(fileName));

        // ??????????????????????????????????????????????????????????????????????????????String???????????????????????????????????????????????????
        File file = new File(saveRootDirectory + fileName);
        if (!file.exists()) {
            File rootDirectoryFile = new File(file.getParent());
            // ????????????
            if (!rootDirectoryFile.exists()) {
                boolean ifSuccess = rootDirectoryFile.mkdirs();
                if (ifSuccess) {
                    System.out.println("?????????????????????!");
                } else {
                    System.out.println("??????????????????!");
                }
            }
            // ????????????
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // ????????????
        BufferedOutputStream write = null;
        try {
            write = new BufferedOutputStream(new FileOutputStream(file));
            int cha = 0;
            while ((cha = read.read()) != -1) {
                write.write(cha);
            }
            // ?????????IO????????????????????????
            write.flush();
            write.close();
            read.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * ?????????
//     * ???zipFile???????????????folderPath?????????.
//     *
//     * @param zipFile    zip??????
//     * @param folderPath ??????????????????
//     * @throws IOException
//     */
//    private void upZipFile(File zipFile, String folderPath) throws IOException {
//
//
//        BufferedInputStream bi;
//        ZipFile zf = new ZipFile(zipFile, "GBK");
//        Enumeration e = zf.getEntries();
//        while (e.hasMoreElements())
//        {
//            ZipEntry ze2 = (ZipEntry) e.nextElement();
//            String entryName = ze2.getName();
//            String path = folderPath + "/" + entryName;
//            if (ze2.isDirectory())
//            {
//                System.out.println("???????????????????????? - " + entryName);
//                File decompressDirFile = new File(path);
//                if (!decompressDirFile.exists())
//                {
//                    decompressDirFile.mkdirs();
//                }
//            } else
//            {
//                System.out.println("???????????????????????? - " + entryName);
//                String fileDir = path.substring(0, path.lastIndexOf("/"));
//                File fileDirFile = new File(fileDir);
//                if (!fileDirFile.exists())
//                {
//                    fileDirFile.mkdirs();
//                }
//                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folderPath + "/" + entryName));
//                bi = new BufferedInputStream(zf.getInputStream(ze2));
//                byte[] readContent = new byte[1024];
//                int readCount = bi.read(readContent);
//                while (readCount != -1)
//                {
//                    bos.write(readContent, 0, readCount);
//                    readCount = bi.read(readContent);
//                }
//                bos.close();
//            }
//        }
//        zf.close();
//    }
}
