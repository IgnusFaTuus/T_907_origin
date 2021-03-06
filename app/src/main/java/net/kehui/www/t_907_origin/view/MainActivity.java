package net.kehui.www.t_907_origin.view;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.adpter.MyChartAdapterBase;
import net.kehui.www.t_907_origin.application.AppConfig;
import net.kehui.www.t_907_origin.application.Constant;
import net.kehui.www.t_907_origin.base.BaseActivity;
import net.kehui.www.t_907_origin.fragment.AdjustFragment;
import net.kehui.www.t_907_origin.fragment.FileFragment;
import net.kehui.www.t_907_origin.fragment.ModeFragment;
import net.kehui.www.t_907_origin.fragment.WaveFragment;
import net.kehui.www.t_907_origin.fragment.RangeFragment;
import net.kehui.www.t_907_origin.fragment.SettingFragment;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.ui.SparkView.SparkView;
import net.kehui.www.t_907_origin.util.StateUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author IF
 * @date 2018/3/26
 */
public class MainActivity extends BaseActivity {

    /**
     * View布局
     */
    @BindView(R.id.mainWave)
    SparkView mainWave;
    @BindView(R.id.tv_information)
    TextView  tvInformation;
    @BindView(R.id.tv_icm)
    TextView  tvIcm;
    @BindView(R.id.tv_auto_distance)
    TextView  tvAutoDistance;
    @BindView(R.id.tv_distance)
    TextView  tvDistance;
    @BindView(R.id.fullWave)
    SparkView fullWave;
    @BindView(R.id.btn_mtd)
    Button    btnMtd;
    @BindView(R.id.btn_range)
    Button    btnRange;
    @BindView(R.id.btn_adj)
    Button    btnAdj;
    @BindView(R.id.btn_opt)
    Button    btnOpt;
    @BindView(R.id.btn_file)
    Button    btnFile;
    @BindView(R.id.btn_setting)
    Button    btnSetting;
    @BindView(R.id.btn_test)
    Button    btnTest;
    @BindView(R.id.btn_cursor)
    Button    btnCursor;
    @BindView(R.id.vl_method)
    TextView  vlMode;
    @BindView(R.id.vl_range)
    TextView  vlRange;
    @BindView(R.id.vl_gain)
    TextView  vlGain;
    @BindView(R.id.vl_vel)
    TextView  vlVel;
    @BindView(R.id.vl_density)
    TextView  vlDensity;
    @BindView(R.id.tv_balance)
    TextView  tvBalance;
    @BindView(R.id.vl_balance)
    TextView  vlBalance;
    @BindView(R.id.tv_delay)
    TextView  tvDelay;
    @BindView(R.id.vl_delay)
    TextView  vlDelay;
    @BindView(R.id.tv_inductor)
    TextView  tvInductor;
    @BindView(R.id.vl_inductor)
    TextView  vlInductor;
    @BindView(R.id.tv_temp_n)
    TextView tvSim;

    /**
     * 用于展示Fragment
     */
    private FragmentManager fragmentManager;
    private ModeFragment    modeFragment;
    private RangeFragment   rangeFragment;
    private AdjustFragment  adjustFragment;
    private WaveFragment    waveFragment;
    private FileFragment    fileFragment;
    private SettingFragment settingFragment;

    private TDialog tDialog;

    /**
     * 全局的handler对象用来执行UI更新
     */
    public static final int DEVICE_CONNECTED    = 1;
    public static final int DEVICE_DISCONNECTED = 2;
    public static final int SEND_SUCCESS        = 3;
    public static final int SEND_ERROR          = 4;
    public static final int GET_COMMAND         = 5;
    public static final int GET_WAVE            = 6;
    public static final int VIEW_REFRESH        = 7;
    public static final int DISPLAY_DATABASE    = 8;

    public Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case DEVICE_CONNECTED:
                sendInitCommand();
                break;
            case GET_COMMAND:
                if (!isSuccessful) {
                    Toast.makeText(this, getResources().getString(R.string
                            .connect_success), Toast.LENGTH_SHORT).show();
                    isSuccessful = true;
                }
                wifiStream = msg.getData().getIntArray("CMD");
                assert wifiStream != null;
                doWifiCommand(wifiStream);
                break;
            case GET_WAVE:
                wifiStream = msg.getData().getIntArray("WAVE");
                assert wifiStream != null;
                setWaveParameter();
                doWifiWave(wifiStream);
                break;
            case VIEW_REFRESH:
                resetWhatNeed();
                organizeWaveData();
                displayWave();
                break;
            case DISPLAY_DATABASE:
                //数据库波形显示   //GC20190713
                setDateBaseParameter();
                organizeWaveData();
                displayWave();
                break;
            default:
                break;
        }
        return false;
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initFrame();
        initSparkView();
        initBroadcastReceiver();
        setChartListener();
    }

    /**
     * 初始化界面框架
     */
    public void initFrame() {
        fragmentManager = getSupportFragmentManager();
        //GC20190705  先初始化（否则fragment切换bug）
        setTabSelection(2);
        setTabSelection(3);
        //第一次启动时选中第0个tab
        setTabSelection(0);
        btnMtd.setEnabled(false);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);
        vlMode.setText(getResources().getString(R.string.btn_tdr));
        Constant.ModeValue = TDR;
        vlRange.setText(getResources().getString(R.string.btn_500m));
        Constant.RangeValue = 0x11;
        vlGain.setText(String.valueOf(gain));
        Constant.Gain = gain;
        vlVel.setText(velocity + "m/μs");
        Constant.Velocity = velocity;
        vlBalance.setText(String.valueOf(balance));
        vlDensity.setText("1 : " + density);
        vlDelay.setText(delay + "μs");
        vlInductor.setText(inductor + "μH");
        //初始化信息栏不显示延时和电感
        tvDelay.setVisibility(View.GONE);
        vlDelay.setVisibility(View.GONE);
        tvInductor.setVisibility(View.GONE);
        vlInductor.setVisibility(View.GONE);
        //初始化距离显示
        calculateDistance(Math.abs(pointDistance - zero));
        //自动测距显示    //GC20190708
        tvInformation.setVisibility(View.GONE);
        tvIcm.setVisibility(View.GONE);
        tvAutoDistance.setVisibility(View.GONE);
        //SIM光标位置初始化    //GC20190712
        simZero = StateUtils.getInt(MainActivity.this, AppConfig.CURRENT_CURSOR_POSITION, 12);
    }

    /**
     * 初始化sparkView //GC20181227
     */
    public void initSparkView() {
        for (int i = 0; i < 510; i++) {
            waveArray[i] = 128;
            //Constant.WaveData[i] = 128;
        }
        myChartAdapterMainWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        myChartAdapterFullWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
        Log.i("Draw", "初始化绘制结束");
        //初始化光标按钮颜色
        btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_purple));
    }

    /**
     * 初始化wifi监听广播
     */
    private void initBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
        //初始化画数据库监听广播   //GC20190713
        IntentFilter ifDisplay = new IntentFilter(ListActivity.DISPLAY_ACTION);
        registerReceiver(receiver, ifDisplay);
    }

    /**
     * 脉冲电流方式光标自动定位 //GC20190708
     */
    private void icmAutoCursor() {
        positionReal = breakBk / densityMax;
        positionVirtual = (breakBk + faultResult) / densityMax;
        //超出范围居中画光标
        if (positionVirtual > 510) {
            positionVirtual = 255;
        }
        //光标定位
        mainWave.setScrubLineReal(positionReal);
        mainWave.setScrubLineVirtual(positionVirtual);
        fullWave.setScrubLineReal(positionReal);
        fullWave.setScrubLineVirtual(positionVirtual);
    }

    /**
     * 监听光标位置 BUG1
     */
    private void setChartListener() {
        mainWave.setScrubListener(value -> {
            if (cursorState) {
                //实光标   //GC20190629
                fullWave.setScrubLineReal((Integer) value);
                //GT
                Log.e("zero", "" + zero);
                zero =  zero + ((int) value - positionReal) * density;
                Log.e("zero", "" + zero);
                positionReal = (int) value;
//                zero = positionReal * density;
            } else {
                //虚光标
                fullWave.setScrubLineVirtual((Integer) value);
                Log.e("pointDistance", "" + pointDistance);
                pointDistance =  pointDistance + ((int) value - positionVirtual) * density;
                Log.e("pointDistance", "" + pointDistance);
                positionVirtual = (int) value;
//                pointDistance = positionVirtual * density;
            }
            Log.e("光标所在位置", "" + value); //GN 数值从0开始计数
            //GC20190709    响应移动光标显示的距离 //G?
            calculateDistance(Math.abs(pointDistance - zero));
        });
    }

    /**
     * 点击光标按钮事件——选择实、虚光标的状态
     */
    private void clickCursor() {
        cursorState = !cursorState;
        if (cursorState) {
            btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_red));
        } else {
            btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_purple));
        }
        myChartAdapterMainWave.setCursorState(cursorState);
    }

    /**
     * @param index 侧边栏设置
     */
    public void setTabSelection(int index) {
        //开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragment(transaction);

        switch (index) {
            case 0:
                if (modeFragment == null) {
                    modeFragment = new ModeFragment();
                    transaction.add(R.id.content, modeFragment);
                } else {
                    transaction.show(modeFragment);
                }
                break;
            case 1:
                if (rangeFragment == null) {
                    rangeFragment = new RangeFragment();
                    transaction.add(R.id.content, rangeFragment);
                } else {
                    transaction.show(rangeFragment);
                }
                break;
            case 2:
                if (adjustFragment == null) {
                    adjustFragment = new AdjustFragment();
                    transaction.add(R.id.content, adjustFragment);
                } else {
                    transaction.show(adjustFragment);
                }
                break;
            case 3:
                if (waveFragment == null) {
                    waveFragment = new WaveFragment();
                    transaction.add(R.id.content, waveFragment);
                } else {
                    transaction.show(waveFragment);
                }
                break;
            case 4:
                if (fileFragment == null) {
                    fileFragment = new FileFragment();
                    transaction.add(R.id.content, fileFragment);
                } else {
                    transaction.show(fileFragment);
                }
                break;
            case 5:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.add(R.id.content, settingFragment);
                } else {
                    transaction.show(settingFragment);
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    /**
     * 隐藏不需要的Fragment
     */
    private void hideFragment(FragmentTransaction transaction) {
        if (modeFragment != null) {
            transaction.hide(modeFragment);
        }
        if (rangeFragment != null) {
            transaction.hide(rangeFragment);
        }
        if (adjustFragment != null) {
            transaction.hide(adjustFragment);
        }
        if (waveFragment != null) {
            transaction.hide(waveFragment);
        }
        if (fileFragment != null) {
            transaction.hide(fileFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
    }

    /**
     * 监听网络广播，匹配SSID后开启线程
     * IF190305
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //GC20190713
            handler.sendEmptyMessage(Objects.requireNonNull(intent.getExtras()).getInt("display_action"));
            String action = intent.getAction();
            assert action != null;
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                assert connectivityManager != null;
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                Intent netIntent = new Intent();
                netIntent.setAction("android.intent.action.netState");

                //当前开关状态
                boolean netAvailable;
                if (info != null) {
                    netAvailable = info.isAvailable();

                    ThreadFactory threadFactory = new ThreadFactoryBuilder()
                            .setNameFormat("connect-pool-%d").build();
                    ExecutorService singleThreadPool = new ThreadPoolExecutor(3, 3,
                            0L, TimeUnit.MILLISECONDS,
                            new LinkedBlockingQueue<>(1024), threadFactory,
                            new ThreadPoolExecutor.AbortPolicy());

                    singleThreadPool.execute(() -> {
                        try {
                            //Thread.sleep(1000);
                            ArrayList<String> connectedIP = getConnectedIP();
                            for (String ip : connectedIP) {
                                if (ip.contains(".")) {
                                    Log.w("AAA", "IP:" + ip);
                                    Socket socket = new Socket(ip, PORT);
                                    connectThread = new ConnectThread(socket, handler);
                                    connectThread.start();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, getResources().getString(R.string
                                    .communication_failed), Toast.LENGTH_LONG).show());
                        }
                    });
                    if (tDialog != null) {
                        tDialog.dismiss();
                    }
                    Log.e("DIA", "WIFI连接：" + "隐藏");
                    singleThreadPool.shutdown();

                } else {
                    netAvailable = false;

                    handler.sendEmptyMessage(DEVICE_DISCONNECTED);

                    if (tDialog != null) {
                        tDialog.dismiss();
                    }
                    tDialog = new TDialog.Builder(getSupportFragmentManager())
                            .setLayoutRes(R.layout.connecting_wifi)
                            .setScreenWidthAspect(MainActivity.this, 0.25f)
                            .setCancelableOutside(false)
                            .create()
                            .show();
                    Log.e("DIA", "WIFI连接：" + "显示");
                }

                if (isFirst) {
                    netIntent.putExtra("netAble", netAvailable);
                    sendBroadcast(netIntent);
                    edit.putBoolean("netAble", netAvailable);
                    netBoolean = netAvailable;
                    isFirst = false;
                } else {
                    if (netBoolean != netAvailable) {
                        netIntent.putExtra("netAble", netAvailable);
                        edit.putBoolean("netAble", netAvailable);
                        sendBroadcast(netIntent);
                        netBoolean = netAvailable;
                    }
                }
                edit.apply();
            }
        }
    };

    /**
     * @return 获取ip
     */
    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    /**
     * 菜单栏点击事件
     */
    @OnClick({R.id.btn_mtd, R.id.btn_range, R.id.btn_adj, R.id.btn_opt, R.id.btn_file, R.id
            .btn_setting, R.id.btn_test, R.id.btn_cursor})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            // 点击方式tab，选中第1个tab
            case R.id.btn_mtd:
                clickMethod();
                break;
            // 点击范围tab，选中第2个tab
            case R.id.btn_range:
                clickRange();
                break;
            // 点击调节tab，选中第3个tab
            case R.id.btn_adj:
                clickAdjust();
                break;
            //点击操作tab，选中第4个tab
            case R.id.btn_opt:
                clickOption();
                break;
            // 点击文档tab，选中第5个tab
            case R.id.btn_file:
                clickFile();
                break;
            // 点击设置tab，选中第6个tab
            case R.id.btn_setting:
                clickSetting();
                break;
            case R.id.btn_test:
                clickTest();
                break;
            case R.id.btn_cursor:
                clickCursor();
                break;
            default:
                break;
        }
    }

    private void clickMethod() {
        setTabSelection(0);
        btnMtd.setEnabled(false);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);
    }

    private void clickRange() {
        setTabSelection(1);
        btnMtd.setEnabled(true);
        btnRange.setEnabled(false);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);
    }

    private void clickAdjust() {
        setTabSelection(2);
        btnMtd.setEnabled(true);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(false);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);

    }

    /**
     * 操作（波形）fragment
     */
    private void clickOption() {
        setTabSelection(3);
        btnMtd.setEnabled(true);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(false);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);

    }

    private void clickFile() {
        setTabSelection(4);
        btnMtd.setEnabled(true);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(false);
        btnSetting.setEnabled(true);
    }

    private void clickSetting() {
        //GT
        setCursor();
        setTabSelection(5);
        btnMtd.setEnabled(true);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(false);
    }

    /**
     * 测试按钮
     */
    private void clickTest() {
        if (mode == TDR) {
            tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.receiving_data)
                    .setScreenWidthAspect(this, 0.25f)
                    .setCancelableOutside(false)
                    .create()
                    .show();
            Log.e("DIA", " 正在接受数据显示" + " TDR");
            command = COMMAND_TEST;
            dataTransfer = TESTING;
            sendCommand();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    command = COMMAND_RECEIVE_WAVE;
                    dataTransfer = 0x11;
                    sendCommand();
                }
            }, 20);

        } else if ((mode == ICM) || (mode == SIM) || (mode == DECAY)) {
            tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.wait_trigger)
                    .setScreenWidthAspect(this, 0.3f)
                    .setCancelableOutside(false)
                    .addOnClickListener(R.id.tv_cancel)
                    .setOnViewClickListener(new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view,
                                                TDialog tDialog) {
                            tDialog.dismiss();
                            command = COMMAND_TEST;
                            dataTransfer = CANCEL_TEST;
                            sendCommand();
                        }
                    })
                    .create()
                    .show();
            Log.e("DIA", " 等待触发显示");
            command = COMMAND_TEST;
            dataTransfer = TESTING;
            sendCommand();
        }
    }

    /**
     * 计算距离  //GC20190709
     */
    private void calculateDistance(int cursorDistance) {
        double distance;
        int k;
        //脉冲电流方式下range=6(32km)和range=7(64km)实时25M采样率，其余方式和范围实时100M采样率，此时相对其它方式采样周期扩大4倍
        if ((mode == ICM) && (rangeState >= 6)) {
            k = 4;
        } else {
            k = 1;
        }
        //DECAY方式距离/2
        if (mode == DECAY) {
            distance = (((double)cursorDistance * velocity / 2) * k) / 2 * 0.01;
        } else {
            distance = (((double)cursorDistance * velocity) * k) / 2 * 0.01;
        }
        //距离界面显示
        tvDistance.setText(new DecimalFormat("0.00").format(distance) + "m");
    }

    /**
     * @param samplingPoints 方向脉冲法自动计算-显示故障距离
     */
    private void calculateDistanceAuto(int samplingPoints) {
        double distance;
        int k;
        //脉冲电流方式下range=6(32km)和range=7(64km)实时25M采样率，其余方式和范围实时100M采样率，此时相对其它方式采样周期扩大4倍
        if ((mode == ICM) && (rangeState >= 6)) {
            k = 4;
        } else {
            k = 1;
        }
        //sc
        distance = (((double)samplingPoints * velocity) * k) / 2 * 0.01;
        //自动距离界面显示
//        tvAutoDistance.setText(new DecimalFormat("0.00").format(distance) + "m");
        //距离界面显示
        tvDistance.setText(new DecimalFormat("0.00").format(distance) + "m");

    }

    public int getVelocity() {
        return velocity;
    }

    /**
     * @param density 响应状态栏波速度变化
     */
    public void setDensity(int density) {
        this.density = density;
        vlDensity.setText("1 : " + density);
        organizeWaveData();
        displayWave();
    }

    public int getDensity() {
        return density;
    }

    /**
     * @param balance 需要发送的平衡控制命令值 / 响应信息栏平衡变化
     */
    public void setBalance(int balance) {
        this.balance = balance;
        command = COMMAND_BALANCE;
        dataTransfer = balance;
        sendCommand();
        vlBalance.setText(String.valueOf(balance));
    }

    public int getBalance() {
        return balance;
    }

    /**
     * @param delay 需要发送的延时控制命令值 / 响应信息栏延时变化
     */
    public void setDelay(int delay) {
        this.delay = delay;
        command = COMMAND_DELAY;
        dataTransfer = delay;
        sendCommand();
        vlDelay.setText(delay + "μs");
    }

    public int getDelay() {
        return delay;
    }

    /**
     * @param inductor 响应信息栏延时变化    //GC20190710
     */
    public void setInductor(int inductor) {
        this.inductor = inductor;
        vlInductor.setText(inductor + "μH");
    }

    public int getInductor() {
        return inductor;
    }

    /**
     * @param selectSim SIM显示波形的组数  //GC20190705
     */
    public void setSelectSim(int selectSim) {
        this.selectSim = selectSim;
        switch (selectSim) {
            case 1:
                System.arraycopy(simDraw1, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw1Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData1;
                tvSim.setText("波形1");
                break;
            case 2:
                System.arraycopy(simDraw2, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw2Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData2;
                tvSim.setText("波形2");
                break;
            case 3 :
                System.arraycopy(simDraw3, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw3Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData3;
                tvSim.setText("波形3");
                break;
            case 4:
                System.arraycopy(simDraw4, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw4Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData4;
                tvSim.setText("波形4");
                break;
            case 5:
                System.arraycopy(simDraw5, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw5Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData5;
                tvSim.setText("波形5");
                break;
            case 6:
                System.arraycopy(simDraw6, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw6Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData6;
                tvSim.setText("波形6");
                break;
            case 7:
                System.arraycopy(simDraw7, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw7Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData7;
                tvSim.setText("波形7");
                break;
            case 8:
                System.arraycopy(simDraw8, 0, waveCompare, 0, 510);
                System.arraycopy(simDraw8Full, 0, waveCompareFull, 0, 510);
                Constant.SimData = Constant.TempData8;
                tvSim.setText("波形8");
                break;
            default:
                break;
        }
        displayWave();
    }

    public int getSelectSim() {
        return selectSim;
    }

    /**
     * @param simZero 光标零点设置    //GC20190712
     */
    public void setSimZero(int simZero) {
        this.simZero = simZero;
        StateUtils.setInt(MainActivity.this, AppConfig.CURRENT_CURSOR_POSITION, simZero);
        Toast.makeText(this, getResources().getString(R.string
                .cursor_zero_set_success), Toast.LENGTH_SHORT).show();

    }

    /**
     * 点击记忆按钮执行的方法  //GC20190703
     */
    public void clickMemory() {
        isMemory = true;
        System.arraycopy(waveDraw, 0, waveCompare, 0, 510);
        //记录记忆数据的方式范围   //GC20190703再优化
        modeBefore = mode;
        rangeBefore = range;
    }

    /**
     * 点击比较按钮执行的方法  //GC20190703
     */
    public void clickCompare() {
        if (isMemory) {
            //GC20190703再优化
            if ((modeBefore == mode) && (rangeBefore == range)) {
                isCom = !isCom;
                myChartAdapterMainWave.setmTempArray(waveDraw);
                myChartAdapterMainWave.setShowCompareLine(isCom);
                myChartAdapterMainWave.setmCompareArray(waveCompare);
                myChartAdapterMainWave.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getResources().getString(R.string
                        .You_can_not_compare), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string
                    .You_have_no_memory_data_can_not_compare), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 启动时需要发送的初始化命令
     */
    private void sendInitCommand() {
        //方式
        command = COMMAND_MODE;
        dataTransfer = TDR;
        sendCommand();
        //范围
        handler.postDelayed(() -> {
            command = COMMAND_RANGE;
            dataTransfer = RANGE_500;
            sendCommand();
        }, 20);
        handler.postDelayed(this::clickTest, 50);
    }

    /**
     * APP下发命令
     */
    public void sendCommand() {
        byte[] request = new byte[8];
        //数据头部分
        request[0] = (byte) 0xeb;
        request[1] = (byte) 0x90;
        request[2] = (byte) 0xaa;
        request[3] = (byte) 0x55;
        //数据长度
        request[4] = (byte) 0x03;
        request[5] = (byte) command;
        request[6] = (byte) dataTransfer;
        int sum = request[4] + request[5] + request[6];
        request[7] = (byte) sum;
        connectThread.sendCommand(request);
        Log.e("appCMD", "指令：" + command + "传输数据：" + dataTransfer);
    }

    /**
     * 处理APP接收的命令
     */
    private void doWifiCommand(int[] wifiArray) {
        //仪器触发时：APP发送接收数据命令
        if ((wifiArray[5] == COMMAND_TRIGGER) && (wifiArray[6] == TRIGGERED)) {
            command = COMMAND_RECEIVE_WAVE;
            dataTransfer = 0x11;
            sendCommand();
            if (tDialog != null) {
                tDialog.dismiss();
            }
            tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.receiving_data)
                    .setScreenWidthAspect(this, 0.25f)
                    .setCancelableOutside(false)
                    .create()
                    .show();
            Log.e("DIA", " 正在接受数据显示" + " ICM/SIM/DECAY");
        }

    }

    /**
     * 比例选择 //GC20190709
     */
    private void switchDensity() {
        if ((mode == TDR) || (mode == SIM)) {
            densityMax = densityMaxTdrSim[rangeState];
        } else if ((mode == ICM) || (mode == DECAY)) {
            densityMax = densityMaxIcmDecay[rangeState];
        }
        density = densityMax;
        vlDensity.setText("1 : " + density);
    }

    /**
     * 设置波形绘制参数
     */
    private void setWaveParameter() {
        //记录当前显示波形的参数   //GC20190716 避免切换范围、方式之后存储的波形参数与波形数据不对应
        Constant.ModeValue = mode;
        Constant.RangeValue = range;
        Constant.Gain = gain;
        Constant.Velocity = velocity;
        //GC20190716 规避放大缩小操作的bug
        Constant.DensityMax = densityMax;
        if (density > densityMax) {
            density = densityMax;
            vlDensity.setText("1 : " + density);
        }
        //非显示数据库波形状态
        isDatabase = false;
        //擦除比较波形
        isCom = false;
        if (mode == TDR) {
            //需要绘制的波形原始数组初始化
            dataMax = READ_TDR_SIM[rangeState];
            waveArray = new int[dataMax];
            Constant.WaveData = new int[dataMax];
        } else if ((mode == ICM) || (mode == DECAY)) {
            dataMax = READ_ICM_DECAY[rangeState];
            waveArray = new int[dataMax];
            Constant.WaveData = new int[dataMax];
        } else if (mode == SIM) {
            dataMax = READ_TDR_SIM[rangeState];
            waveArray = new int[dataMax];
            Constant.WaveData = new int[dataMax];
            Constant.SimData = new int[dataMax];
            //GC20190702 SIM第二条波形初始化
            simArray1 = new int[dataMax];
            simArray2 = new int[dataMax];
            simArray3 = new int[dataMax];
            simArray4 = new int[dataMax];
            simArray5 = new int[dataMax];
            simArray6 = new int[dataMax];
            simArray7 = new int[dataMax];
            simArray8 = new int[dataMax];
            //利用比较功能绘制SIM的第二条波形数据
            isCom = true;
        }
        zero = positionReal * densityMax;
        pointDistance = positionVirtual * densityMax;

    }

    /**
     * 设置数据库波形绘制参数  //GC20190713
     */
    public void setDateBaseParameter() {
        //读取并设置数据库的参数
        setMode(Constant.Para[0]);
        setRange(Constant.Para[1]);
        setGain(Constant.Para[2]);
        setVelocity(Constant.Para[3]);
        //显示数据库波形状态
        isDatabase = true;
        //擦除比较波形
        isCom = false;
        //需要绘制的波形原始数组初始化
        if (mode == TDR) {
            dataMax = READ_TDR_SIM[rangeState];
        } else if ((mode == ICM) || (mode == DECAY)) {
            dataMax = READ_ICM_DECAY[rangeState];
        } else if (mode == SIM) {
            dataMax = READ_TDR_SIM[rangeState];
            //利用比较功能绘制SIM的第二条波形数据
            isCom = true;
        }

    }

    /**
     * 根据参数重置显示效果
     */
    public void resetWhatNeed() {
        //二次脉冲显示组数重置    //GC201907052   优化SIM显示
        if (mode == SIM) {
            selectSim = 1;
            tvSim.setText("波形1");
            waveFragment.btnWavePrevious.setEnabled(false);
            waveFragment.btnWaveNext.setEnabled(true);
        } else {
            tvSim.setText("");
        }

        if (isDatabase) {
            //GC201907052   优化SIM显示
            waveFragment.btnWavePrevious.setEnabled(false);
            waveFragment.btnWaveNext.setEnabled(false);
        }
        //放大缩小按钮显示重置    //GC20190711
        if (density == densityMax) {
            if (density == 1) {
                waveFragment.btnZoomIn.setEnabled(false);
            } else {
                waveFragment.btnZoomIn.setEnabled(true);
            }
            waveFragment.btnZoomOut.setEnabled(false);
            waveFragment.btnRes.setEnabled(false);
        }
    }

    /**
     * 处理APP接收的波形数据
     */
    private void doWifiWave(int[] wifiArray) {
        if (wifiArray[3] == WAVE_TDR_ICM_DECAY) {
            System.arraycopy(wifiArray, 8, waveArray, 0, dataMax);
            //GC20190708
            if (mode == ICM) {
                //ICM自动测距功能
                icmAutoTest();
            } else {
                handler.sendEmptyMessage(VIEW_REFRESH);
            }
        } else if (wifiArray[3] == WAVE_SIM) {
            System.arraycopy(wifiArray, 8, waveArray, 0, dataMax);
            System.arraycopy(wifiArray, dataMax + 9 + 8, simArray1, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 2 + 8, simArray2, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 3 + 8, simArray3, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 4 + 8, simArray4, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 5 + 8, simArray5, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 6 + 8, simArray6, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 7 + 8, simArray7, 0, dataMax);
            System.arraycopy(wifiArray, (dataMax + 9) * 8 + 8, simArray8, 0, dataMax);
            Constant.TempData1 = simArray1;
            Constant.TempData2 = simArray2;
            Constant.TempData3 = simArray3;
            Constant.TempData4 = simArray4;
            Constant.TempData5 = simArray5;
            Constant.TempData6 = simArray6;
            Constant.TempData7 = simArray7;
            Constant.TempData8 = simArray8;
            Constant.SimData = Constant.TempData1;
            handler.sendEmptyMessage(VIEW_REFRESH);
        }
        //记录当前显示波形的数据
        Constant.WaveData = waveArray;
    }

    /**
     * 组织需要绘制的波形数组（抽点510个）——最终得到waveDraw和waveCompare    //GC20190702
     */
    private void organizeWaveData() {
        //GT
        /*int k = 510 * density / 2;
        int i = pointDistance - k;
        if (pointDistance < k) {
            i = 0;
        } else if ((dataMax - pointDistance) < k) {
            i = dataMax - 2 * k;
        }
        //波形按比例抽出510个点
        for (int j = 0; j < 510; i = i + density, j++) {
            //组织TDR、ICM、DECAY和SIM的第一条波形的数据
            waveDraw[j] = Constant.WaveData[i];
            //组织SIM的第二条波形的数据
            if (mode == SIM) {
                waveCompare[j] = Constant.SimData[i];
                simDraw1[j] = simArray1[i];
                simDraw2[j] = simArray2[i];
                simDraw3[j] = simArray3[i];
                simDraw4[j] = simArray4[i];
                simDraw5[j] = simArray5[i];
                simDraw6[j] = simArray6[i];
                simDraw7[j] = simArray7[i];
                simDraw8[j] = simArray8[i];
            }
        }*/
        //起始位置
        int start = 0;
        //波形数据的居中位置
        int k = 510 * density / 2;
        //寻找波形显示的起始地址在波形数据数组中的所处的位置  (根据虚光标位置判断)
        if (positionVirtual > 255) {
            if ((mode == TDR) || (mode == SIM)) {
                start = dataMax - removeTdrSim[rangeState] - 2 * k;
            } else if ((mode == ICM) || (mode == DECAY)) {
                start = dataMax - removeIcmDecay[rangeState] - 2 * k;
            }
        }
        //mainWave按比例抽出510个点
        for (int i = start, j = 0; j < 510; i = i + density, j++) {
            //组织TDR、ICM、DECAY和SIM的第一条波形的数据
            waveDraw[j] = Constant.WaveData[i];
            //组织SIM的第二条波形的数据
            if (mode == SIM) {
                waveCompare[j] = Constant.SimData[i];
                if (!isDatabase) {
                    simDraw1[j] = simArray1[i];
                    simDraw2[j] = simArray2[i];
                    simDraw3[j] = simArray3[i];
                    simDraw4[j] = simArray4[i];
                    simDraw5[j] = simArray5[i];
                    simDraw6[j] = simArray6[i];
                    simDraw7[j] = simArray7[i];
                    simDraw8[j] = simArray8[i];
                }
            }
        }
        if (density == densityMax) {
            //fullWave按最大比例抽出510个点
            for (int i = 0, j = 0; j < 510; i = i + densityMax, j++) {
                //组织TDR、ICM、DECAY和SIM的第一条波形的数据
                waveDrawFull[j] = Constant.WaveData[i];
                //组织SIM的第二条波形的数据
                if (mode == SIM) {
                    waveCompareFull[j] = Constant.SimData[i];
                    if (!isDatabase) {
                        simDraw1Full[j] = simArray1[i];
                        simDraw2Full[j] = simArray2[i];
                        simDraw3Full[j] = simArray3[i];
                        simDraw4Full[j] = simArray4[i];
                        simDraw5Full[j] = simArray5[i];
                        simDraw6Full[j] = simArray6[i];
                        simDraw7Full[j] = simArray7[i];
                        simDraw8Full[j] = simArray8[i];
                    }
                }
            }
        }
    }

    /**
     * 在sparkView界面显示波形
     */
    private void displayWave() {
        //画波形
        myChartAdapterMainWave.setmTempArray(waveDraw);
        myChartAdapterFullWave.setmTempArray(waveDrawFull);
        myChartAdapterMainWave.setShowCompareLine(isCom);
        myChartAdapterFullWave.setShowCompareLine(isCom);
        if (mode == SIM) {
            if (isCom) {
                myChartAdapterMainWave.setmCompareArray(waveCompare);
                myChartAdapterFullWave.setmCompareArray(waveCompareFull);
            }
        }
        myChartAdapterMainWave.notifyDataSetChanged();
        myChartAdapterFullWave.notifyDataSetChanged();
        //有对话框消对话框
        if (tDialog != null) {
            tDialog.dismiss();
            Log.e("DIA", "正在接受数据隐藏" + " 波形绘制完成");
        }
        //刷新波形后后显示控制虚光标    //GC20190629
        cursorState = false;
        myChartAdapterMainWave.setCursorState(false);
        btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_purple));
    }

    /**
     * 脉冲电流故障自动计算过程  //GC20190708
     */
    private void icmAutoTest() {
        //1.判断增益是否合适
        gainJudgment();
        switch (gainState) {
            case 0:
                //擦除字符显示  sc
                tvInformation.setText("");
                break;
            case 1:
                gainState = 0;
                //组织数据画波形
                handler.sendEmptyMessage(VIEW_REFRESH);
                //显示增益过大    //GC20190710
                tvInformation.setVisibility(View.VISIBLE);
                tvInformation.setText(getResources().getString(R.string.gain_too_high));
                return;
            case 2:
                gainState = 0;
                //组织数据画波形   //GC20190710
                handler.sendEmptyMessage(VIEW_REFRESH);
                //显示增益过小
                tvInformation.setVisibility(View.VISIBLE);
                tvInformation.setText(getResources().getString(R.string.gain_too_low));
                return;
            default:
                break;
        }
        //软件滤波
        softwareFilter();
        //积分
        integral();
        //2.击穿放电判断
        breakdownJudgment();
        if (breakdownPosition == 0) {
            //组织数据画波形
            handler.sendEmptyMessage(VIEW_REFRESH);
            //显示不击穿    //GC20190710
            tvInformation.setVisibility(View.VISIBLE);
            tvInformation.setText(getResources().getString(R.string.not_break_down));
            return;
        }
        //计算方向脉冲
        calculatePulse();
        //计算故障距离并在界面显示
        correlationSimple();
        //放电脉冲位置确定——确定实光标
        breakPointCalculate();
        //光标自动定位
        icmAutoCursor();
        //组织数据画波形
        handler.sendEmptyMessage(VIEW_REFRESH);
    }

    /**
     * 脉冲电流方式增益自动判断
     */
    private void gainJudgment() {
        int i;
        int max = 0;
        int sub;

        //计算波形有效数据的极值
        for (i = 0; i < dataMax - removeIcmDecay[rangeState]; i++) {
            sub = waveArray[i] - 128;
            if (Math.abs(sub) > max) {
                max = Math.abs(sub);
            }
        }
        if (max <= 38) {
            //判断增益过小
            gainState = 2;
            return;
        }
        for (i = 0; i < dataMax - removeIcmDecay[rangeState]; i++) {
            if ((waveArray[i] > 242) || (waveArray[i] < 16)) {
                //判断增益过大
                gainState = 1;
                return;
            }
        }
    }

    /**
     * 脉冲电流方式软件滤波   方向脉冲法自动计算-软件滤波，一阶滞后滤波，低通截止频率约750kHz（两个采样频率都是这个截止频率）
     */
    private void softwareFilter() {
        int i;
        for (i = 1; i < dataMax - removeIcmDecay[rangeState]; i++) {
            if (rangeState >= 6) {
                waveArrayFilter[i] = (float) 0.8618 * waveArrayFilter[i - 1] + (float) 0.1382 * (float) (waveArray[i] - 128);
            } else {
                waveArrayFilter[i] = (float) 0.9524 * waveArrayFilter[i - 1] + (float) 0.0476 * (float) (waveArray[i] - 128);
                //sc
                waveArrayFilter[i] = (float)0.8744 * waveArrayFilter[i - 1] + (float)0.1256 * (float)(waveArray[i] - 128);
                //waveArrayFilter[i] = (float)0.9524 * waveArrayFilter[i - 1] + (float)0.0476 * (float)(waveArray[i] - 128);
            }
        }
    }

    /**
     * 脉冲电流方式数字积分   方向脉冲法自动计算-数字积分,反演电流
     */
    private void integral() {
        for (int i = 1; i < dataMax - removeIcmDecay[rangeState]; i++) {
            //25M采样
            if (rangeState >= 6) {
                waveArrayIntegral[i] = waveArrayIntegral[i - 1] + waveArrayFilter[i] * 4;
            } else {
                waveArrayIntegral[i] = waveArrayIntegral[i - 1] + waveArrayFilter[i];
            }
        }
    }

    /**
     * 脉冲电流方式判断是否击穿放电   方向脉冲法自动计算
     */
    private void breakdownJudgment() {
        int i;
        int start;
        float min;
        float a;
        //从触发开始计算初始值(去除ICM前面的直线部分)
        start = 120;
        min = 255;
        for (i = start; i < start + 64; i++) {
            if ((waveArrayFilter[i] < min) && (waveArrayFilter[i] < 0)) {
                min = waveArrayFilter[i];
            }
        }
        //积分电流
        a = 80 * min;
        for (i = start + 64; i < (dataMax - removeIcmDecay[rangeState]); i++) {
            //1.8
            if ((waveArrayIntegral[i] < 0) && (waveArrayIntegral[i] < 1.3 * a)) {
                breakdownPosition = i;
                break;
            } else {
                breakdownPosition = 0;
            }
        }
    }

    /**
     * 脉冲电流方式  计算方向脉冲   方向脉冲法自动计算-使用滤波后电流的微分求VL=L * di/dt，滤波后电流*波阻抗
     */
    private void calculatePulse() {
        //使用滤波后电流进行微分
        float[] V = new float[65560];
        int i;
        float L;
        float z = 25;

        //电感值
        L = (float) inductor * (float) (1.0e-6);

        for (i = 0; i < dataMax - removeIcmDecay[rangeState]; i++) {
            //25M采样
            if (rangeState >= 6) {
                V[i] = (waveArrayFilter[i + 1] - waveArrayFilter[i]) * (float) 4.0e8;
            } else {
                V[i] = (waveArrayFilter[i + 1] - waveArrayFilter[i]) * (float) 1.0e8;
            }
        }
        //确定击穿点
        //计算VL
        for (i = 0; i < dataMax - removeIcmDecay[rangeState]; i++) {
            V[i] = V[i] * L * (-1.0f);
        }
        //计算方向行波
        for (i = 0; i < dataMax - removeIcmDecay[rangeState]; i++) {
            s1[i] = V[i] + waveArrayFilter[i] * z;
            s2[i] = V[i] - waveArrayFilter[i] * z;
        }
    }

    /**
     * 脉冲电流方式  计算故障距离(抽点做数据相关)  方向脉冲法自动计算-使用相关计算故障距离
     */
    private void correlationSimple() {
        int i;
        int j = 0;
        int k;
        float p;
        float[] P = new float[512];
        int w1;
        long w2;
        long w3;
        float[] s1_simple = new float[512];
        float[] s2_simple = new float[512];

        //使用极小值点找放电脉冲点——breakdownPosition
        findMinPeak();
        for (i = breakdownPosition; i > 100; i--) {
            if (waveArrayFilter[i - 1] <= waveArrayFilter[i]) {
                breakBk = i;
                break;
            }
        }
        //25M采样
        if (rangeState >= 6) {
            //需要修改，32km和64km采样频率变了，需要调整参数
            if (breakBk > (50 / 4)) {
                //相关窗左侧
                w1 = breakBk - (50 / 4);
            } else {
                w1 = breakBk;
            }
            //相关窗右侧
            w2 = breakBk + (350 / 4);
        } else {
            //需要修改，32km和64km采样频率变了，需要调整参数
            if (breakBk > 50) {
                //相关窗左侧
                w1 = breakBk - 50;
            } else {
                w1 = breakBk;
            }
            //相关窗右侧
            w2 = breakBk + 350;
        }

        //抽点
        for (i = 0; i < 512; i++) {
            s1_simple[i] = s1[j];
            s2_simple[i] = s2[j];
            j = j + densityMaxIcmDecay[rangeState];
        }
        w1 = w1 / densityMaxIcmDecay[rangeState];
        w2 = w2 / densityMaxIcmDecay[rangeState];
        w3 = 512 - w2;

        float[] S1 = new float[65556];
        float[] S2 = new float[65556];

        for (i = w1; i < w2; i++) {
            S1[i - w1] = s1_simple[i];
        }
        for (i = 0; i < w3; i++) {
            for (k = w1; k < w2; k++) {
                S2[k - w1] = s2_simple[k + i];
            }
            p = (float) 0.0;
            //进行相关运算
            for (j = 0; j < (w2 - w1); j++) {
                p += S1[j] * S2[j] * (-1.0f);
            }
            //将整条波形的相关运算值存入P数组中
            P[i] = p;
        }

        //计算P数组中的最大值，并确定位置
        float max = P[0];
        int maxIndex = 0;
        for (i = 0; i < w3; i++) {
            if (P[i] > max) {
                max = P[i];
                maxIndex = i;
            }
        }

        //换算为整条波形数据中的点数
        maxIndex = (w1 + maxIndex) * densityMaxIcmDecay[rangeState];

        w1 = w1 * densityMaxIcmDecay[rangeState];
        w2 = w2 * densityMaxIcmDecay[rangeState];

        for (i = w1; i < w2; i++) {
            S1[i - w1] = s1[i];
        }

        for (i = (maxIndex - densityMaxIcmDecay[rangeState]); i < (maxIndex + densityMaxIcmDecay[rangeState]); i++) {
            for (k = 0; k < w2 - w1; k++) {
                S2[k] = s2[k + i];
            }
            //清零
            p = (float) 0.0;
            //进行相关运算S
            for (j = 0; j < (w2 - w1); j++) {
                p += S1[j] * S2[j] * (float) (-1.0);
            }
            //将整条波形的相关运算值存入P数组中
            P[i - (maxIndex - densityMaxIcmDecay[rangeState])] = p;
        }
        max = P[0];
        int maxIndex1 = 0;
        for (i = 0; i < densityMaxIcmDecay[rangeState] * 2; i++) {
            if (P[i] > max) {
                max = P[i];
                maxIndex1 = i;
            }
        }
        int maxIndex2 = maxIndex - densityMaxIcmDecay[rangeState] + maxIndex1 - w1;
        calculateDistanceAuto(maxIndex2);
        faultResult = maxIndex2;
    }

    /**
     * 脉冲电流方式  计算极小值点   方向脉冲法自动计算-使用极小值点找放电脉冲
     */
    private void findMinPeak() {
        int i = 170;
        int first;
        int peakMax = 0;
        int[] minData = new int[255];
        int j = 0;
        int k;
        int l = 0;
        float firstMin;
        float reference = 0;

        //25M采样
        if (rangeState >= 6) {
            i = 150;
        }
        while ((j < 255) && (i < dataMax - removeIcmDecay[rangeState])) {
            if ((waveArrayFilter[i] < waveArrayFilter[i - 1]) && (waveArrayFilter[i] <= waveArrayFilter[i + 1])) {
                if ((i > 5) && (waveArrayFilter[i - 1] < waveArrayFilter[i - 2])) {
                    if (waveArrayFilter[i - 2] < waveArrayFilter[i - 3]) {
                        if (waveArrayFilter[i - 3] < waveArrayFilter[i - 4]) {
                            if (waveArrayFilter[i - 4] < waveArrayFilter[i - 5]) {
                                minData[j] = i;
                                j++;
                            }
                        }
                    }
                }
            }
            i++;
        }
        if (j >= 1) {
            j = j - 1;
        }
        //找出第1个最小值在原始数组中的位置First
        firstMin = waveArrayFilter[minData[0]];
        first = minData[0];
        for (k = 0; k <= j; k++) {
            if (waveArrayFilter[minData[k]] < firstMin) {
                first = minData[k];
                firstMin = waveArrayFilter[minData[k]];
                peakMax = k;
            }
        }
        breakdownPosition = first;
        //求出负的最大极值
        reference = waveArrayFilter[breakdownPosition];

        for (k = 0; k <= peakMax; k++) {
            if (waveArrayFilter[minData[k]] < (float) (reference * 0.7)) {
                minPeak[l] = minData[k];
                l++;
            }
        }
    }

    /**
     * 击穿点位置判断
     */
    private void breakPointCalculate() {
        int i;
        int bk_pos;
        long remainder;

        i = breakdownPosition - minPeak[0];

        if ((faultResult - (i % faultResult)) < (i % faultResult)) {
            remainder = faultResult - (i % faultResult);
        } else {
            remainder = (i % faultResult);
        }
        int k = 1;
        //方式选择
        if ((mode == ICM) && (rangeState >= 6)) {
            k = 4;
        }

        if ((float) remainder <= (float) (0.05 * faultResult + (float) (((float) 3000 / velocity) / (float) k))) {
            bk_pos = minPeak[0];
        } else {
            bk_pos = breakdownPosition;
        }
        for (i = bk_pos; i > 100; i--) {
            if (waveArrayFilter[i - 1] < waveArrayFilter[i]) {
                //实光标位置
                breakBk = i;
                break;
            }
        }
    }

    /**
     * @param mode 需要发送的方式控制命令值 / 响应信息栏方式变化
     */
    public void setMode(int mode) {
        this.mode = mode;
        command = COMMAND_MODE;
        dataTransfer = mode;
        sendCommand();
        switch (mode) {
            case TDR:
                vlMode.setText(getResources().getString(R.string.btn_tdr));
                //GC20190709
                switchDensity();
                initCursor();
                //GC20190710
                tvInformation.setVisibility(View.GONE);
                tvIcm.setVisibility(View.GONE);
                tvAutoDistance.setVisibility(View.GONE);
                //GC20190705 信息栏显示
                tvBalance.setVisibility(View.VISIBLE);
                vlBalance.setVisibility(View.VISIBLE);
                tvDelay.setVisibility(View.GONE);
                vlDelay.setVisibility(View.GONE);
                tvInductor.setVisibility(View.GONE);
                vlInductor.setVisibility(View.GONE);
                //方式栏fragment显示
                modeFragment.btnTdr.setEnabled(false);
                modeFragment.btnIcm.setEnabled(true);
                modeFragment.btnSim.setEnabled(true);
                modeFragment.btnDecay.setEnabled(true);
                //调节栏fragment显示
                adjustFragment.btnBalancePlus.setVisibility(View.VISIBLE);
                adjustFragment.btnBalanceMinus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayPlus.setVisibility(View.GONE);
                adjustFragment.btnDelayMinus.setVisibility(View.GONE);
                adjustFragment.btnInductorPlus.setVisibility(View.GONE);
                adjustFragment.btnInductorMinus.setVisibility(View.GONE);
                //操作栏fragment显示
                waveFragment.btnMemory.setVisibility(View.VISIBLE);
                waveFragment.btnCompare.setVisibility(View.VISIBLE);
                waveFragment.btnWavePrevious.setVisibility(View.INVISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.INVISIBLE);
                break;
            case ICM:
                vlMode.setText(getResources().getString(R.string.btn_icm));
                switchDensity();
                initCursor();
                tvBalance.setVisibility(View.GONE);
                vlBalance.setVisibility(View.GONE);
                tvDelay.setVisibility(View.VISIBLE);
                vlDelay.setVisibility(View.VISIBLE);
                tvInductor.setVisibility(View.VISIBLE);
                vlInductor.setVisibility(View.VISIBLE);
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayMinus.setVisibility(View.VISIBLE);
                adjustFragment.btnInductorPlus.setVisibility(View.VISIBLE);
                adjustFragment.btnInductorMinus.setVisibility(View.VISIBLE);
                waveFragment.btnMemory.setVisibility(View.VISIBLE);
                waveFragment.btnCompare.setVisibility(View.VISIBLE);
                waveFragment.btnWavePrevious.setVisibility(View.INVISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.INVISIBLE);
                break;
            case SIM:
                vlMode.setText(getResources().getString(R.string.btn_sim));
                switchDensity();
                initCursor();
                tvInformation.setVisibility(View.GONE);
                tvIcm.setVisibility(View.GONE);
                tvAutoDistance.setVisibility(View.GONE);
                tvBalance.setVisibility(View.GONE);
                vlBalance.setVisibility(View.GONE);
                tvDelay.setVisibility(View.GONE);
                vlDelay.setVisibility(View.GONE);
                tvInductor.setVisibility(View.GONE);
                vlInductor.setVisibility(View.GONE);
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.GONE);
                adjustFragment.btnDelayMinus.setVisibility(View.GONE);
                adjustFragment.btnInductorPlus.setVisibility(View.GONE);
                adjustFragment.btnInductorMinus.setVisibility(View.GONE);
                waveFragment.btnMemory.setVisibility(View.GONE);
                waveFragment.btnCompare.setVisibility(View.GONE);
                waveFragment.btnWavePrevious.setVisibility(View.VISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.VISIBLE);
                break;
            case DECAY:
                vlMode.setText(getResources().getString(R.string.btn_decay));
                switchDensity();
                initCursor();
                tvInformation.setVisibility(View.GONE);
                tvIcm.setVisibility(View.GONE);
                tvAutoDistance.setVisibility(View.GONE);
                tvBalance.setVisibility(View.GONE);
                vlBalance.setVisibility(View.GONE);
                tvDelay.setVisibility(View.VISIBLE);
                vlDelay.setVisibility(View.VISIBLE);
                tvInductor.setVisibility(View.GONE);
                vlInductor.setVisibility(View.GONE);
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayMinus.setVisibility(View.VISIBLE);
                adjustFragment.btnInductorPlus.setVisibility(View.GONE);
                adjustFragment.btnInductorMinus.setVisibility(View.GONE);
                waveFragment.btnMemory.setVisibility(View.VISIBLE);
                waveFragment.btnCompare.setVisibility(View.VISIBLE);
                waveFragment.btnWavePrevious.setVisibility(View.INVISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
    }

    public int getMode() {
        return mode;
    }

    /**
     * @param range 需要发送的范围控制命令值 / 响应信息栏范围变化
     */
    public void setRange(int range) {
        this.range = range;
        command = COMMAND_RANGE;
        dataTransfer = range;
        sendCommand();
        switch (range) {
            case RANGE_500:
                rangeState = 0;
                //GC20190709
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_500m));
                gain = 13;
                vlGain.setText("13");
                break;
            case RANGE_1_KM:
                rangeState = 1;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_1km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_2_KM:
                rangeState = 2;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_2km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_4_KM:
                rangeState = 3;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_4km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_8_KM:
                rangeState = 4;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_8km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_16_KM:
                rangeState = 5;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_16km));
                gain = 9;
                vlGain.setText("9");
                break;
            case RANGE_32_KM:
                rangeState = 6;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_32km));
                gain = 9;
                vlGain.setText("9");
                break;
            case RANGE_64_KM:
                rangeState = 7;
                switchDensity();
                initCursor();
                vlRange.setText(getResources().getString(R.string.btn_64km));
                gain = 9;
                vlGain.setText("9");
                break;
            default:
                break;
        }
    }

    public int getRange() {
        return range;
    }

    /**
     * 光标位置和距离显示初始化 //GC20190709
     */
    private void initCursor() {
        //光标距离
        if (mode == SIM) {
            //GC20190712
            zero = simZero;
            Log.e("TEST","位置" + positionReal);
        } else {
            zero = 0;
        }
        pointDistance = 255 * densityMax;
        //计算并在界面显示距离
        calculateDistance(Math.abs(pointDistance - zero));
        //界面定位
        positionReal = zero / densityMax;
        positionVirtual = pointDistance / densityMax;
        mainWave.setScrubLineReal(positionReal);
        fullWave.setScrubLineReal(positionReal);
        mainWave.setScrubLineVirtual(positionVirtual);
        fullWave.setScrubLineVirtual(positionVirtual);
    }

    /**
     * @param gain 需要发送的增益控制命令值 / 响应信息栏增益变化
     */
    public void setGain(int gain) {
        this.gain = gain;
        command = COMMAND_GAIN;
        dataTransfer = gain;
        sendCommand();
        vlGain.setText(String.valueOf(gain));
    }

    public int getGain() {
        return gain;
    }

    /**
     * @param velocity 响应状态栏波速度变化
     */
    public void setVelocity(int velocity) {
        this.velocity = velocity;
        vlVel.setText(velocity + "m/μs");
        //GC20190709    //G?
        calculateDistance(Math.abs(pointDistance - zero));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        /*if (receiver != null) {
            unregisterReceiver(receiver);
        }*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * ##############################以下为测试代码，留作参考##############################
     * GT 测试绘制效果    //GC20181227
     */
    public void testWaveData() {
        for (int i = 0; i < 510; i++) {
            waveArray[i] = 12;
        }
        myChartAdapterMainWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        myChartAdapterFullWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        positionReal = 0;
        mainWave.setScrubLineReal(positionReal);
        positionVirtual = dataMax / 2;
        mainWave.setScrubLineVirtual(positionVirtual);
        //G? 距离显示待更改
        tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
    }

    /**
     * GT 测试读文本绘制效果
     */
    public void getTxtWaveData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" +
                "wave.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c;
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");

            for (int i = 0; i < split.length; i++) {
                waveArray[i] = Integer.valueOf(split[i], 16);
            }
            myChartAdapterMainWave = new MyChartAdapterBase(waveArray, null,
                    false, 0, false, dataMax);
            myChartAdapterFullWave = new MyChartAdapterBase(waveArray, null,
                    false, 0, false, dataMax);
            positionReal = 0;
            mainWave.setScrubLineReal(positionReal);
            positionVirtual = dataMax / 2;
            mainWave.setScrubLineVirtual(positionVirtual);
            //G? 距离显示待更改
            tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //GT
    public void setCursor() {
        mainWave.setScrubLineRealDisappear();
    }

}