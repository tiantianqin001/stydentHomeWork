
package com.telit.zhkt_three.Activity.HomeScreen;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Activity.BaseActivity;
import com.telit.zhkt_three.Activity.OauthMy.ProviceActivity;
import com.telit.zhkt_three.Adapter.HomeViewPagerAdapter;
import com.telit.zhkt_three.Adapter.tree_adpter.Node;
import com.telit.zhkt_three.Adapter.vp_transformer.CustomPageTransformer;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.CustomView.CircleImageView;
import com.telit.zhkt_three.Fragment.CircleProgressDialogFragment;
import com.telit.zhkt_three.Fragment.Dialog.FileReceiveDialog;
import com.telit.zhkt_three.Fragment.Dialog.TipsDialog;
import com.telit.zhkt_three.Fragment.Dialog.UrlUpdateDialog;
import com.telit.zhkt_three.Fragment.HomeStopOneFragment;
import com.telit.zhkt_three.Fragment.HomeStopTwoFragment;
import com.telit.zhkt_three.Fragment.SysyemFragment;
import com.telit.zhkt_three.Fragment.SysyemFragment1;
import com.telit.zhkt_three.Fragment.SysyemFragment2;
import com.telit.zhkt_three.Fragment.SysyemFragment3;
import com.telit.zhkt_three.JavaBean.AppInfo;
import com.telit.zhkt_three.JavaBean.AppListBean;
import com.telit.zhkt_three.JavaBean.AppUpdate.UpdateBean;
import com.telit.zhkt_three.JavaBean.StudentInfo;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Service.AppInfoService;
import com.telit.zhkt_three.Service.UpDataApkServer;
import com.telit.zhkt_three.Utils.ApkListInfoUtils;
import com.telit.zhkt_three.Utils.CheckVersionUtil;
import com.telit.zhkt_three.Utils.Jpush.TagAliasOperatorHelper;
import com.telit.zhkt_three.Utils.OkHttp3_0Utils;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.Utils.ZBVPermission;
import com.telit.zhkt_three.Utils.eventbus.EventBus;
import com.telit.zhkt_three.Utils.eventbus.Subscriber;
import com.telit.zhkt_three.Utils.eventbus.ThreadMode;
import com.telit.zhkt_three.floatingview.PopWindows;
import com.telit.zhkt_three.greendao.AppInfoDao;
import com.telit.zhkt_three.greendao.StudentInfoDao;
import com.telit.zhkt_three.receiver.AppChangeReceiver;
import com.telit.zhkt_three.receiver.NetworkChangeBroadcastReceiver;
import com.telit.zhkt_three.websocket.JWebSocketClient;
import com.telit.zhkt_three.websocket.JWebSocketClientService;
import com.zbv.meeting.util.SharedPreferenceUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.jpush.android.api.JPushInterface;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ????????????????????????SingleInstance,???RVHomeAdapter??????mContext.startActivity(new Intent(mContext, Test2Activity.class));
 * ????????????onPause??????????????????????????????????????????????????????MainActivity
 * <p>
 * UserManager,sInstance????????????????????????
 * <p>
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, ZBVPermission.PermPassResult {
    private static final String TAG = "MainActivity";
    private Unbinder unbinder;
    @BindView(R.id.home_avatar)
    CircleImageView home_avatar;
    @BindView(R.id.home_nickname)
    TextView home_nickname;
    @BindView(R.id.home_clazz)
    TextView home_clazz;
    @BindView(R.id.home_time)
    TextView home_time;
    @BindView(R.id.home_date)
    TextView home_date;
    @BindView(R.id.home_weekend)
    TextView home_weekend;
    @BindView(R.id.home_wifi)
    ImageView home_wifi;
    @BindView(R.id.home_timetable)
    ImageView home_timetable;


    @BindView(R.id.home_viewpager)
    ViewPager home_viewpager;
    @BindView(R.id.home_dots_linear)
    LinearLayout home_dot_linear;
    @BindView(R.id.home_IndicatorDotView)
    ImageView home_dotview;

    protected static final String[] weekends = {"?????????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????"};
    private ScheduledExecutorService scheduledExecutorService;
    private HomeViewPagerAdapter homeViewPagerAdapter;
    private List<AppInfo> datas;
    private int vp_page_count;
    private int distance_dots;
    private ScheduledExecutorService timeExecutor;
    private static final int FIREST_UPDATE_VP = 0x7;
    private static final int UPDATE_TIME = 0x8;
    public static final int INSTALL_PACKAGES_REQUEST_CODE = 0x17;
    /**
     * ??????????????????Vp
     */
    private boolean isInitVp = true;
    //??????????????????
    private NetworkChangeBroadcastReceiver netConnectChangedReceiver;
    //????????????????????????????????????????????????Activity???????????????????????????????????????????????????????????????????????????
    private static final String HAT_APP_MARKET_CLASS = "com.example.edcationcloud.edcationcloud.activity.DownloadCenterActivity";

    //App???????????????
    private AppChangeReceiver appChangeReceiver;

    private CircleProgressDialogFragment circleProgressDialogFragment;

    private static final String[] NeedPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int Close_Ling_Chuang_brocast = 0x98;

    private JWebSocketClient client;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private JWebSocketClientService jWebSClientService;
    private static boolean isShow = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Close_Ling_Chuang_brocast:
                    if (isShow) {
                        if (executorLingChuangService != null) {
                            executorLingChuangService.shutdownNow();
                        }
                    }

                    break;
                case FIREST_UPDATE_VP:
                    if (isShow) {
                        if (circleProgressDialogFragment != null) {
                            circleProgressDialogFragment.dismissAllowingStateLoss();
                            circleProgressDialogFragment = null;
                        }
                        initVpAdapter();
                        ApkListInfoUtils.getInstance().onStop();
                    }

                    break;

                case TIMES_SEND:
                    if (isShow) {
                        //???????????????????????????????????????????????????????????????????????????
                        if (times % 2 == 0) {
                            iv_san_suo.setBackgroundColor(getResources().getColor(R.color.transGray));
                        } else {
                            iv_san_suo.setBackgroundColor(getResources().getColor(R.color.colorDarkBlue));
                        }
                    }

                    break;
                case UPDATE_TIME:
                    if (isShow) {
                        Bundle bundle = (Bundle) msg.obj;
                        home_weekend.setText(bundle.getString("weekend"));
                        home_time.setText(bundle.getString("time"));
                        int month = bundle.getInt("month");
                        int day = bundle.getInt("day");
                        String date = month + "???" + day + "???";
                        home_date.setText(date);

                    }
                    break;

            }
        }
    };
    private ExecutorService executorLingChuangService;
    private LingChuangAppsListReceiver lingChuangAppsListReceiver;
    private static final int REQUEST_OVERLAY = 4444;
    private ImageView iv_san_suo;
    private LingChuangSystemReceiver lingChuangSystemReceiver;

    private boolean isLingChuangLoginOut = true;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private PopWindows popWindows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QZXTools.logDeviceInfo(this);
        isShow = true;
        QZXTools.logE("MainActivity onCreate " + getTaskId(), null);
        //??????Url??????
        // updateUrl();
        //??????????????????????????????
        initLingChuangInfo();
        QZXTools.logE("????????????????????????" + "1111111111111....." + UserUtils.isLoginIn(), null);
        //????????????????????????
        if (!UserUtils.isLoginIn()) {
            startActivity(new Intent(this, ProviceActivity.class));
            finish();


            return;
        } else {
            //???????????????tat  ????????????
            refreshTgtLogin();


            //????????????
            startJWebSClientService();
            //????????????
            bindService();
            //??????????????????
            String getDeviceName = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                if (getDeviceName.equals("T10-SP-001") || getDeviceName.equals("AGS3-W00D")) {
                    //???????????????????????????????????????
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                            INSTALL_PACKAGES_REQUEST_CODE);
                    Intent intent = new Intent(MainActivity.this, UpDataApkServer.class);
                    startService(intent);

                    QZXTools.logE("LingChuangSystemReceiver........????????????.....", null);

                } else {
                    QZXTools.logE("LingChuangSystemReceiver........??????????????????.....", null);
                }
            }


        }

        //??????SD?????????
        ZBVPermission.getInstance().setPermPassResult(this);
        if (ZBVPermission.getInstance().hadPermissions(this, NeedPermission)) {
            isSDEnable = true;
        } else {
            ZBVPermission.getInstance().requestPermissions(this, NeedPermission);
        }


        unbinder = ButterKnife.bind(this);

        //-----------------------------------------------------??????????????????????????????
        IntentFilter filter = new IntentFilter();
        //??????wifi?????????????????????????????????????????????
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //????????????????????????????????????????????????????????????????????????????????????wifi???????????????
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //??????????????????????????????????????????wifi??????????????????
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netConnectChangedReceiver = new NetworkChangeBroadcastReceiver();
        registerReceiver(netConnectChangedReceiver, filter);
        //-----------------------------------------------------??????????????????????????????
        //-----------------------------------------------------App????????????????????????
        //Andoird8.0+????????????????????????
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction("android.intent.action.PACKAGE_ADDED");
        appFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        appFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        appFilter.addDataScheme("package");
        appChangeReceiver = new AppChangeReceiver();
        registerReceiver(appChangeReceiver, appFilter);

        //-----------------------------------------------------App???????????????
        //??????AppService
        //????????????o,api26???8.0?????????????????? AppInfoService.enqueueWork
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppInfoService.enqueueWork(this, new Intent(this, AppInfoService.class));
        } else {
            startService(new Intent(this, AppInfoService.class));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //?????????????????????
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            } else {

            }
        }
        //??????EventBus
        EventBus.getDefault().register(this);
        //????????????


        home_nickname.setText(UserUtils.getStudentName());
        home_clazz.setText(UserUtils.getClassName());
        Glide.with(this).load(UserUtils.getAvatarUrl()).
                placeholder(R.mipmap.icon_user).error(R.mipmap.icon_user).into(home_avatar);
        //??????GreenDao?????????????????????????????????????????????
        AppInfoDao appInfoDao = MyApplication.getInstance().getDaoSession().getAppInfoDao();
        //????????????  appInfoOtherList
        datas = appInfoDao.queryBuilder().list();
        QZXTools.logE("LingChuangSystemReceiver........555555555555555555555....." + datas, null);

        QZXTools.logE("??????????????????????????????" + datas, null);
        if (datas == null || datas.size() <= 0) {
            datas = new ArrayList<>();
        } else {
            if (appInfoOtherList != null) {
                appInfoOtherList.clear();
                for (AppInfo data : datas) {
                    if (!data.getIsSystemApp()) {
                        appInfoOtherList.add(data);
                    }
                }
            }


            mHandler.sendEmptyMessage(FIREST_UPDATE_VP);
        }

        if (circleProgressDialogFragment != null && circleProgressDialogFragment.isVisible()) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }
        circleProgressDialogFragment = new CircleProgressDialogFragment();
        circleProgressDialogFragment.show(getSupportFragmentManager(), CircleProgressDialogFragment.class.getSimpleName());


        //datas = ApkListInfoUtils.getInstance().getAppSystem("lingchang", appsLists);
        home_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) home_dotview.getLayoutParams();
                layoutParams.leftMargin = (int) (distance_dots * (positionOffset + position)
                        + QZXTools.dp2px(MainActivity.this, 5));
                home_dotview.setLayoutParams(layoutParams);
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        timeExecutor = Executors.newSingleThreadScheduledExecutor();
        timeExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                QZXTools.logE("fixedReate", null);
                //????????????????????? ??????????????????????????????????????????????????????????????????????????????????????????UI
                fillWeekAndTime();
            }
        }, 0, 30000, TimeUnit.MILLISECONDS);

        home_avatar.setOnClickListener(this);
        home_wifi.setOnClickListener(this);
        home_timetable.setOnClickListener(this);

        //??????????????????????????????Url??????
        home_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                long curTime = System.currentTimeMillis();
                if (count == 1) {
                    touchFirstTime = curTime;
                }
                if (count == 3 && curTime - touchFirstTime <= 1000) {
                    count = 0;
                    //???????????????????????????IP??????
                    UrlUpdateDialog urlUpdateDialog = new UrlUpdateDialog();
                    urlUpdateDialog.show(getSupportFragmentManager(), UrlUpdateDialog.class.getSimpleName());
                } else if (curTime - touchFirstTime > 1000) {
                    //??????
                    count = 0;
                } else if (count > 3) {
                    //??????
                    count = 0;
                }
            }
        });
    }

    int times = 0;
    private Timer timer;
    private static final int TIMES_SEND = 0X100;

    private void showReadTime() {
        if (timer == null) {

            timer = new Timer();
        }
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // System.out.println("????????????????????????");
                times++;
                //??????????????????
                mHandler.sendEmptyMessage(TIMES_SEND);

            }
        }, 100, 100);
        /*????????????????????????5s??????????????????2s???????????????????????????*/
    }

    private long touchFirstTime;
    private int count;

    @Override
    protected void onResume() {
        super.onResume();

        //????????????????????????
        /*if (!UserUtils.isLoginIn()) {
            startActivity(new Intent(this, ProviceActivity.class));
            finish();
            return;
        }*/
        if (JPushInterface.isPushStopped(MyApplication.getInstance())) {
            QZXTools.logE("JPush resume", null);
            JPushInterface.resumePush(MyApplication.getInstance());
        }
        if (popWindows == null) {
            popWindows = new PopWindows(getApplication());
            popWindows.setView(R.layout.popwindoeview)
                    .setGravity(Gravity.LEFT | Gravity.TOP)
                    .setYOffset(600)
                    .show();
            iv_san_suo = (ImageView) popWindows.findViewById(R.id.iv_san_suo);
        }
        showReadTime();

        //??????GreenDao?????????????????????????????????????????????
        AppInfoDao appInfoDao = MyApplication.getInstance().getDaoSession().getAppInfoDao();
        //????????????  appInfoOtherList
        datas = appInfoDao.queryBuilder().list();
        QZXTools.logE("LingChuangSystemReceiver...onResume..............555555555555555555555....." + datas, null);

        QZXTools.logE("??????????????????????????????" + datas, null);
        if (datas == null || datas.size() <= 0) {
            datas = new ArrayList<>();
        } else {
            if (appInfoOtherList != null) {
                appInfoOtherList.clear();
                for (AppInfo data : datas) {
                    if (!data.getIsSystemApp()) {
                        appInfoOtherList.add(data);
                    }
                }
            }
        }


    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        if (unbinder != null) {
            unbinder.unbind();
        }


        if (timeExecutor != null) {
            timeExecutor.shutdown();
            timeExecutor = null;
        }

        //??????????????????
        if (netConnectChangedReceiver != null) {
            unregisterReceiver(netConnectChangedReceiver);
        }
        if (lingChuangSystemReceiver != null) {
            unregisterReceiver(lingChuangSystemReceiver);
        }
        if (lingChuangAppsListReceiver != null) {
            unregisterReceiver(lingChuangAppsListReceiver);
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }

        //??????App?????????????????????
        if (appChangeReceiver != null) {
            unregisterReceiver(appChangeReceiver);
        }
        TagAliasOperatorHelper.getInstance().releaseHandler();
        if (circleProgressDialogFragment != null) {
            circleProgressDialogFragment.dismissAllowingStateLoss();
            circleProgressDialogFragment = null;
        }

        mHandler.removeCallbacksAndMessages(null);

        QZXTools.logE("???????????????", null);
        isShow = false;
        if (UserUtils.isLoginIn()) {
            unBindService();
            stopJWebSClientService();

            SharedPreferences sharedPreferences = getSharedPreferences("student_info", MODE_PRIVATE);
            UserUtils.setBooleanTypeSpInfo(sharedPreferences, "isLoginIn", false);

            QZXTools.logE("????????????????????????" + "22222222222....." + UserUtils.isLoginIn(), null);

            //?????????????????????
         /*   Intent intent = new Intent("com.drupe.swd.launcher.action.logoutworkspace");
            intent.setPackage("com.android.launcher3");
            sendBroadcast(intent);*/
            QZXTools.logE("????????????????????????" + "3333333333333....." + "???????????????????????????", null);
        }

        super.onDestroy();
    }

    /**
     * ??????Url
     */
    private void updateUrl() {
        String path = QZXTools.getExternalStorageForFiles(this, null) + "/config.txt";
        Properties properties = QZXTools.getConfigProperties(path);
        QZXTools.logE("rootIp=" + properties.getProperty("rootIp"), null);
        QZXTools.logE("path=" + path, null);
        String rootIp = properties.getProperty("rootIp");
   /*     String socketIp = properties.getProperty("socketIp");
        String socketPort = properties.getProperty("socketPort");
        String imgIp = properties.getProperty("imgIp");*/
        String uPAddressIp = properties.getProperty("uPAddressIp");
        if (TextUtils.isEmpty(uPAddressIp)) {
            properties.setProperty("uPAddressIp", UrlUtils.AppUpdate);

            try {
                FileOutputStream fos = new FileOutputStream(path);
                properties.store(new OutputStreamWriter(fos, "UTF-8"),
                        "Config");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(rootIp)) {
            UrlUtils.BaseUrl = rootIp;
        } else {
            //?????????????????????????????????
            properties.setProperty("rootIp", UrlUtils.BaseUrl);
        /*    properties.setProperty("socketIp", UrlUtils.SocketIp);
            properties.setProperty("socketPort", UrlUtils.SocketPort + "");
            properties.setProperty("imgIp", UrlUtils.ImgBaseUrl);*/
            //???????????????url
            properties.setProperty("uPAddressIp", UrlUtils.AppUpdate);

            try {
                FileOutputStream fos = new FileOutputStream(path);
                properties.store(new OutputStreamWriter(fos, "UTF-8"),
                        "Config");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

 /*       if (!TextUtils.isEmpty(socketIp)) {
            UrlUtils.SocketIp = socketIp;
        }

        if (!TextUtils.isEmpty(socketPort)) {
            UrlUtils.SocketPort = Integer.parseInt(socketPort);
        }

        if (!TextUtils.isEmpty(imgIp)) {
            UrlUtils.ImgBaseUrl = imgIp;
        }*/


    }


    private List<Fragment> fragments = new ArrayList<>();

    private void initVpAdapter() {
        QZXTools.logE("initVpAdapter:2222 " + distance_dots, null);
        int systemCom = 0;
        fragments.clear();
        //??????????????????
        home_viewpager.setPageTransformer(false, new CustomPageTransformer());


        HomeStopOneFragment homeStopOneFragment = new HomeStopOneFragment();
        HomeStopTwoFragment homeStopTwoFragment = new HomeStopTwoFragment();
        fragments.add(homeStopOneFragment);
        fragments.add(homeStopTwoFragment);
        //???????????????fragment
        if (datas.size() > 0 && datas.size() >= 12) {
            if (datas.size() % 12 == 0) {
                systemCom = datas.size() / 12;
            } else {
                systemCom = datas.size() / 12;
                systemCom++;
            }
        } else {
            systemCom = 1;
        }

        AppListBean appListBean = new AppListBean();
        appListBean.setDatas(datas);
        if (systemCom == 1) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            fragments.add(sysyemFragment);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);

        } else if (systemCom == 2) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);

        } else if (systemCom == 3) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            SysyemFragment2 sysyemFragment2 = new SysyemFragment2();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);
            fragments.add(sysyemFragment2);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);
            sysyemFragment2.setArguments(bundle);
        } else if (systemCom == 4) {
            SysyemFragment sysyemFragment = new SysyemFragment();
            SysyemFragment1 sysyemFragment1 = new SysyemFragment1();
            SysyemFragment2 sysyemFragment2 = new SysyemFragment2();
            SysyemFragment3 sysyemFragment3 = new SysyemFragment3();
            fragments.add(sysyemFragment);
            fragments.add(sysyemFragment1);
            fragments.add(sysyemFragment2);
            fragments.add(sysyemFragment3);

            Bundle bundle = new Bundle();
            bundle.putSerializable("applist", appListBean);
            sysyemFragment.setArguments(bundle);
            sysyemFragment1.setArguments(bundle);
            sysyemFragment2.setArguments(bundle);
            sysyemFragment3.setArguments(bundle);
        }
        homeViewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), fragments);
        vp_page_count = homeViewPagerAdapter.totalPage();

        Log.i("qin", "initVpAdapter: " + vp_page_count);

        home_viewpager.setAdapter(homeViewPagerAdapter);
        //????????????????????????????????????????????????
        if (vp_page_count > 0) {
            home_dotview.setVisibility(View.VISIBLE);
            home_dot_linear.removeAllViews();
            for (int i = 0; i < vp_page_count; i++) {
                addDot(false);
            }

            //????????????????????????????????????????????????
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) home_dotview.getLayoutParams();
            layoutParams.leftMargin = QZXTools.dp2px(MainActivity.this, 5);
            home_dotview.setLayoutParams(layoutParams);

            //???onMeasure onLayout???????????????????????????onDraw
            home_dotview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //????????????????????????
                    home_dotview.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    distance_dots = home_dot_linear.getChildAt(1).getLeft() - home_dot_linear.getChildAt(0).getLeft();

                }
            });
        } else {
            home_dotview.setVisibility(View.GONE);
        }
    }

    /**
     * ?????????????????????
     */
    private void fillWeekAndTime() {
        Date date = new Date();
        int week = QZXTools.judgeWeekFromDate(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String time = simpleDateFormat.format(date);

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Message message = mHandler.obtainMessage();
        message.what = UPDATE_TIME;
        Bundle bundle = new Bundle();
        bundle.putString("weekend", weekends[week - 1]);
        bundle.putInt("month", month);
        bundle.putInt("day", day);
        bundle.putString("time", time);
        message.obj = bundle;
        mHandler.sendMessage(message);
    }

    /**
     * ????????????dot
     */
    private void addDot(boolean isEnable) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.shape_enable_dot);
        imageView.setPadding(QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5),
                QZXTools.dp2px(this, 5));
        home_dot_linear.addView(imageView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_avatar:
                startActivity(new Intent(this, PersonInfoActivity.class));
                overridePendingTransition(R.anim.activity_enter_from_left_to_right, R.anim.out_fade);
                break;
            case R.id.home_wifi:
                QZXTools.enterWifiSetting(this);
                break;
            case R.id.home_timetable:
                startActivity(new Intent(this, TimeTableActivity.class));
                break;
        }
    }

    //----------------------------------------------AppUpdate-------------------------------------

    private String installFilePath;

    @Subscriber(tag = Constant.CAN_INSTALL, mode = ThreadMode.MAIN)
    public void getInstallPath(String path) {
        installFilePath = path;
    }

    /**
     * ??????????????????
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.INSTALL_PACKAGES_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    QZXTools.logE("onRequestPermissionsResult ?????????????????????", null);
                    SharedPreferences sharedPreferences = getSharedPreferences("access_mode", Context.MODE_PRIVATE);
                    String localFilePathApk = sharedPreferences.getString("localFilePathApk", "");

                    QZXTools.installApk(this, localFilePathApk);

                } else {
                    QZXTools.logE("onRequestPermissionsResult ????????????????????????????????????", null);
                    //  ????????????????????????????????????
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                    startActivityForResult(intent, Constant.GET_UNKNOWN_APP_SOURCES);
                }
                break;
            default:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.GET_UNKNOWN_APP_SOURCES) {
            QZXTools.logE("onActivityResult GET_UNKNOWN_APP_SOURCES", null);
            if (Build.VERSION.SDK_INT >= 26) {
                boolean b = getPackageManager().canRequestPackageInstalls();
                if (b) {
                    SharedPreferences sharedPreferences = getSharedPreferences("access_mode", Context.MODE_PRIVATE);
                    String localFilePathApk = sharedPreferences.getString("localFilePathApk", "");

                    QZXTools.installApk(this, localFilePathApk);
                } else {
                    QZXTools.popToast(this, "??????????????????????????????", false);
                }
            }
        }
    }

    /**
     * ??????????????????App
     */
    private void downloadNewApp(String downloadUrl) {
        FileReceiveDialog fileReceiveDialog = new FileReceiveDialog();
        fileReceiveDialog.setFileBodyString(true, downloadUrl, null);
        fileReceiveDialog.show(getSupportFragmentManager(), FileReceiveDialog.class.getSimpleName());
    }

    //------------------------------------------------????????????

    /**
     * todo ??????????????????apk????????????????????????????????????apk
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    @Subscriber(tag = Constant.Update_Avatar, mode = ThreadMode.MAIN)
    public void updateAvatar(String urlImg) {
        Glide.with(this).load(urlImg).
                placeholder(R.mipmap.icon_user).error(R.mipmap.icon_user).into(home_avatar);
    }


    private void initLingChuangInfo() {
        String getDeviceName = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
            if (getDeviceName.equals("D7")) {
                QZXTools.logE("LingChuangSystemReceiver.....???????????????????????????.....99999999999999.....D7" + getDeviceName, null);
                ApkListInfoUtils.getInstance().onStart();
                ApkListInfoUtils.getInstance().getAppSystem("lingchang");
            }
        }
        //??????home ???

        // LingChuangUtils.getInstance().startHome(MyApplication.getInstance());
        // ?????? recent ???(
        // LingChuangUtils.getInstance().startRecent(MyApplication.getInstance());
        //??????back ???
        // LingChuangUtils.getInstance().startBack(MyApplication.getInstance());
        // LingChuangUtils.getInstance().stopDisablenavigationbar(MyApplication.getInstance());
        //???????????????app ??????????????????????????????????????????
        lingChuangAppsListReceiver = new LingChuangAppsListReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.linspirer.edu.appstorelist");
        registerReceiver(lingChuangAppsListReceiver, filter);
        //????????????app ??????
        lingChuangSystemReceiver = new LingChuangSystemReceiver();
        IntentFilter intentFilterSystem = new IntentFilter();
        intentFilterSystem.addAction("com.android.launcher3.mdm.control_default_apps");
        registerReceiver(lingChuangSystemReceiver, intentFilterSystem);

    }

    List<AppInfo> appInfoOtherList = new ArrayList<>();
    List<AppInfo> appInfoSystemList = new ArrayList<>();
    List<AppInfo> appInfoAllList = new ArrayList<>();

    private class LingChuangAppsListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<String> applist = intent.getStringArrayListExtra("applist");
            QZXTools.logE("LingChuangSystemReceiver.....???????????????????????????.....1111111....." + applist, null);

            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    appInfoOtherList.clear();
                    PackageManager pm = MyApplication.getInstance().getPackageManager();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

                    if (applist != null && applist.size() > 0) {
                        QZXTools.logE("LingChuangSystemReceiver.....???????????????????????????...22222222222.....", null);

                        for (int j = 0; j < applist.size(); j++) {
                            String pakName = applist.get(j);

                            for (int i = 0; i < resolveInfos.size(); i++) {
                                ResolveInfo resolveInfo = resolveInfos.get(i);
                                String packageName = resolveInfo.activityInfo.packageName;


                                //????????????????????????1?????????
                                if (applist.get(j).equals(packageName)) {
                                    QZXTools.logE("LingChuangSystemReceiver....???????????????????????????....22222222222.....delApps.get(j)=" + applist.get(j), null);
                                    if (pakName.equals(MyApplication.getInstance().getPackageName())
                                            || pakName.equals("com.android.launcher3")
                                            // || pakName.equals("com.android.camera2")
                                            //  || pakName.equals("com.android.music")
                                            || pakName.equals("com.ndwill.swd.appstore")
                                            || pakName.equals("com.SSI.UnityAndroid")) {
                                        continue;
                                    }
                                    AppInfo appInfo = new AppInfo();

                                    if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                                        //?????????????????????
                                        appInfo.setIsSystemApp(true);
                                    } else {
                                        appInfo.setIsSystemApp(false);
                                    }

                                    appInfo.setName(resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
                                    appInfo.setPackageName(resolveInfo.activityInfo.packageName);
                                    // appInfo.setOrderNum(j);
                                    //??????resolveInfo.activityInfo.applicationInfo.name????????????null
                                    QZXTools.logE("LingChuangSystemReceiver....???????????????????????????....33333333333....." + appInfo, null);
                                    appInfoOtherList.add(appInfo);
                                }
                            }
                        }
                    }

                    MyApplication.getInstance().getDaoSession().getAppInfoDao().deleteAll();
                    QZXTools.logE("LingChuangSystemReceiver....???????????????????????????....4444444444444....." + appInfoOtherList, null);
                    //????????????
                    initlingchaungList();

                }
            });
        }


    }

    private ExecutorService executorService;
    private List<String> delApps = new ArrayList<>();

    private class LingChuangSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            QZXTools.logE("LingChuangSystemReceiver" + "??????????????????3.28 ?????????????????????/???????????????????????????/????????????", null);
            delApps.clear();

            List<Map<String, Integer>> systemData =
                    (List<Map<String, Integer>>) intent.getSerializableExtra("apps_status_list");

            Map<String, Integer> stringIntegerMap = systemData.get(0);
            Iterator<Map.Entry<String, Integer>> iterator =
                    stringIntegerMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> next = iterator.next();
                QZXTools.logE("LingChuangSystemReceiver  key="
                        + next.getKey() + "....value=" + next.getValue(), null);
                if (next.getValue() == 0) {
                    //??????????????????
                    delApps.add(next.getKey());
                }

            }

            QZXTools.logE("LingChuangSystemReceiver........111111....." + delApps, null);

            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    appInfoSystemList.clear();
                    PackageManager pm = MyApplication.getInstance().getPackageManager();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

                    if (delApps.size() > 0) {
                        QZXTools.logE("LingChuangSystemReceiver........22222222222.....", null);
                        for (int j = 0; j < delApps.size(); j++) {
                            for (int i = 0; i < resolveInfos.size(); i++) {
                                ResolveInfo resolveInfo = resolveInfos.get(i);
                                String packageName = resolveInfo.activityInfo.packageName;

                                //????????????????????????1?????????
                                if (delApps.get(j).equals(packageName)) {
                                    QZXTools.logE("LingChuangSystemReceiver........22222222222.....delApps.get(j)=" + delApps.get(j), null);

                                    AppInfo appInfo = new AppInfo();

                                    if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                                        //?????????????????????
                                        appInfo.setIsSystemApp(true);
                                        //??????resolveInfo.activityInfo.applicationInfo.name????????????null
//                QZXTools.logD("System AppInfo=" + appInfo + ";class name=" + resolveInfo.activityInfo.name);
                                    } else {
                                        appInfo.setIsSystemApp(false);
                                    }

                                    appInfo.setName(resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
                                    appInfo.setPackageName(resolveInfo.activityInfo.packageName);
                                    // appInfo.setOrderNum(j);
                                    //??????resolveInfo.activityInfo.applicationInfo.name????????????null
                                    QZXTools.logE("LingChuangSystemReceiver........33333333333....." + appInfo, null);
                                    appInfoSystemList.add(appInfo);
                                }
                            }
                        }
                    }
                    MyApplication.getInstance().getDaoSession().getAppInfoDao().deleteAll();
                    //????????????
                    initlingchaungList();
                }
            });
        }
    }

    private void initlingchaungList() {
        appInfoAllList.clear();
        appInfoAllList.addAll(appInfoOtherList);
        appInfoAllList.addAll(appInfoSystemList);
        QZXTools.logE("LingChuangSystemReceiver......initlingchaungList......1111111..." + appInfoSystemList, null);
        QZXTools.logE("LingChuangSystemReceiver......initlingchaungList......222222..." + appInfoOtherList, null);

        if (datas == null) return;
        datas.clear();
        datas.addAll(appInfoAllList);
        //???????????????
        MyApplication.getInstance().getDaoSession().getAppInfoDao().insertOrReplaceInTx(appInfoAllList);
        QZXTools.logE("LingChuangSystemReceiver......initlingchaungList......33333..." + appInfoAllList, null);
        mHandler.sendEmptyMessage(FIREST_UPDATE_VP);

    }

    /**
     * ???????????????  ???????????????????????????????????????????????????????????????,??????????????????onAppInt(int type)??????
     */
    @Subscriber(tag = Constant.EVENT_TAG_APP, mode = ThreadMode.MAIN)
    public void onAppInt(int type) {
        switch (type) {
            case Constant.APP_NEW_ADD:
            case Constant.APP_UPDATE:
            case Constant.APP_DELETE:
                AppInfoDao appInfoDao_add = MyApplication.getInstance().getDaoSession().getAppInfoDao();

                datas.clear();
                datas.addAll(appInfoDao_add.queryBuilder().list());
                mHandler.sendEmptyMessage(FIREST_UPDATE_VP);
                QZXTools.logE("LingChuangSystemReceiver...???????????????????????????apk.....666666666666....." + datas, null);

                //??????????????????apk  ?????????appInfoSystemList
                break;
        }
    }

    @Subscriber(tag = Constant.Add_UP_DATA_NEW_APP, mode = ThreadMode.MAIN)
    public void addApp(String packageName) {
        executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                AppInfo appInfo = new AppInfo();
                appInfo.setIsSystemApp(false);

                PackageManager pm = MyApplication.getInstance().getPackageManager();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                for (int i = 0; i < resolveInfos.size(); i++) {
                    ResolveInfo resolveInfo = resolveInfos.get(i);
                    String packageName_one = resolveInfo.activityInfo.packageName;
                    if (packageName_one.equals(packageName)) {
                        appInfo.setName(resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString());

                        appInfo.setPackageName(packageName);
                        appInfo.setName(resolveInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
                        if (appInfoOtherList != null && appInfoOtherList.size() > 0) {
                            appInfoOtherList.add(appInfo);
                        }
                    }

                }

            }
        });


    }

    @Subscriber(tag = Constant.Del_UP_DATA_NEW_APP, mode = ThreadMode.MAIN)
    public void deleteApp(String packageName) {
     /*   if (appInfoSystemList != null && appInfoSystemList.size() > 0) {
            Iterator<AppInfo> iterator = appInfoSystemList.iterator();
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                if (next.getPackageName().equals(packageName)) {
                    appInfoSystemList.remove(next);
                }
            }
        }*/
        if (appInfoOtherList!=null && appInfoOtherList.size()>0){
            Iterator<AppInfo> iterator = appInfoOtherList.iterator();
            while (iterator.hasNext()) {
                AppInfo next = iterator.next();
                if (next.getPackageName().equals(packageName)) {
                    appInfoOtherList.remove(next);
                }
            }
        }

    }


    //????????????
    private void refreshTgtLogin() {
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/refreshTgt";
        String getTgt = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt");
        String deviceId = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("deviceId");
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("tgt", getTgt);
        paramMap.put("client", "pc");//?????????????????????
        paramMap.put("deviceId", deviceId);

        QZXTools.logE("paramMap2222:" + "getTgt="+getTgt, null);
        QZXTools.logE("paramMap2222:" + "deviceId="+deviceId, null);
        QZXTools.logE("paramMap2222:" + new Gson().toJson(paramMap), null);

        OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, paramMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("??????", null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();//??????????????????response.body().string()
                    QZXTools.logE("response=" + resultJson, null);
                    if (!TextUtils.isEmpty(resultJson)) {
                        Gson gson = new Gson();
                        Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                        }.getType());
                        if (map.get("code").equals("1")) {
                            getCallback();
                        } else {
                            //????????????????????????
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(MainActivity.this, ProviceActivity.class));
                                    finish();
                                }
                            });
                        }
                    }
                } else {
                    //????????????????????????
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, ProviceActivity.class));
                            finish();
                        }
                    });
                }
            }
        });


    }

    public void getCallback() {
        String getTgt = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("getTgt");
        String deviceId = SharedPreferenceUtil.getInstance(MyApplication.getInstance()).getString("deviceId");
        String url = "http://open.ahjygl.gov.cn/sso-oauth/client/validateTgt";
        Map<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("appkey", Constant.EduAuthAppKey);
        paramMap.put("tgt", getTgt);
        paramMap.put("client", "pc");//?????????????????????
        paramMap.put("deviceId", deviceId);

        OkHttp3_0Utils.getInstance().asyncGetOkHttp(url, paramMap, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                QZXTools.logE("??????", null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    /**
                     *
                     * response={"code":"1","message":"success","data":"9c5e8fc2cc6e5a197b4ea823497f3da719ce42bcab3b04bf532573912beb97da4826d86417934d5f5d4a9af096137783f2e175af23ffe065","success":true}
                     * */
                    String resultJson = response.body().string();//??????????????????response.body().string()
                    QZXTools.logE("response=" + resultJson, null);
                    Gson gson = new Gson();
                    Map<String, Object> map = gson.fromJson(resultJson, new TypeToken<Map<String, Object>>() {
                    }.getType());
                    QZXTools.logE("data=" + map.get("data"), null);
                }
            }
        });

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            QZXTools.logE("???????????????????????????", null);
            binder = (JWebSocketClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            QZXTools.logE("???????????????????????????", null);
        }
    };

    /**
     * ????????????
     */
    private void bindService() {
        Intent bindIntent = new Intent(this, JWebSocketClientService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * ????????????
     */
    private void unBindService() {
        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    /**
     * ???????????????websocket??????????????????
     */
    private void startJWebSClientService() {
        Intent intent = new Intent(this, JWebSocketClientService.class);
        startService(intent);
    }

    /**
     * ???????????????websocket??????????????????
     */
    private void stopJWebSClientService() {
        Intent intent = new Intent(this, JWebSocketClientService.class);
        stopService(intent);
    }


    @Subscriber(tag = Constant.LINGCHUANG_APP_LIST, mode = ThreadMode.MAIN)
    public void apkUtils(List<AppInfo> appInfoList) {
        if (datas == null) datas = new ArrayList<>();
        datas.clear();
        datas.addAll(appInfoList);
        //???????????????

        QZXTools.logE("LingChuangSystemReceiver........88888888888888888...........apkUtils.." + datas, null);
        MyApplication.getInstance().getDaoSession().getAppInfoDao().insertOrReplaceInTx(datas);
        mHandler.sendEmptyMessage(FIREST_UPDATE_VP);
    }

    private boolean isSDEnable = false;

    @Override
    public void grantPermission() {
        isSDEnable = true;
    }

    @Override
    public void denyPermission() {
        isSDEnable = false;
    }
}
