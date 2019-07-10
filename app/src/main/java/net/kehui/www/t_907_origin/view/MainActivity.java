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
import android.os.Message;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

    @BindView(R.id.mainWave)
    SparkView mainWave;
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

    /**
     * APP下发命令 指令内容command/传输数据data
     */
    private int     command;
    private int     dataTransfer;
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
    public static final int WHAT_REFRESH        = 7;

    public Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case DEVICE_CONNECTED:
                sendInitCommand();
                break;
            case DEVICE_DISCONNECTED:
                break;
            case GET_COMMAND:
                if (!isSuccessful) {
                    Toast.makeText(MainActivity.this, "T-907连接成功！", Toast.LENGTH_LONG).show();
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
            case WHAT_REFRESH:
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
        //GC201907052  先初始化（否则fragment切换bug）
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
        Constant.RangeValue = RANGE_500;
        vlGain.setText(String.valueOf(gain));
        Constant.Gain = gain;
        vlVel.setText(velocity + "m/μs");
        Constant.Velocity = velocity;
        vlBalance.setText(String.valueOf(balance));
        vlDelay.setText(delay + "μs");
        vlDensity.setText("1 : " + density);
        //初始化信息栏不显示延时
        tvDelay.setVisibility(View.GONE);
        vlDelay.setVisibility(View.GONE);
        //初始化距离显示
        calculateDistance(Math.abs(positionVirtual - positionReal));
    }

    /**
     * 初始化sparkView //GC20181227
     */
    public void initSparkView() {
        for (int i = 0; i < 510; i++) {
            waveArray[i] = 128;
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
    }

    /**
     * 监听光标位置 BUG1
     */
    private void setChartListener() {
        mainWave.setScrubListener(value -> {
            if (cursorState) {
                //实光标   //GC20190629
                fullWave.setScrubLineReal((Integer) value);
                positionReal = (int) value;
            } else {
                //虚光标
                fullWave.setScrubLineVirtual((Integer) value);
                positionVirtual = (int) value;
            }
            Log.i("光标移动数值", "" + value); //GN 数值从0开始计数
            //GC20190709    响应移动光标显示的距离
            calculateDistance(Math.abs(positionVirtual - positionReal));
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
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
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
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "通信失败，请检查网络后重试",
                                    Toast.LENGTH_LONG).show());
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
            br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
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
                    command = COMMAND_RECEIVE_DATA;
                    dataTransfer = RECEIVING_DATA;
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
            distance = ((cursorDistance * velocity / 2) * k) / 2 * 0.01 * density;
        } else {
            distance = ((cursorDistance * velocity) * k) / 2 * 0.01 * density;
        }
        //距离界面显示
        tvDistance.setText(new DecimalFormat("0.00").format(distance) + "m");
    }


    /**
     * 脉冲电流方式  计算故障距离并显示
     *
     * @param sampling_points 方向脉冲法自动计算-显示故障距离
     */
    private void calculate_distance2(int sampling_points) {
        int Fx1, Fx2;
        int position_x, position_y;
        int k;
        int[] index = new int[16];

        //脉冲电流方式下range=6(32km)和range=7(64km)实时25M采样率，其余方式和范围实时100M采样率，此时相对其它方式采样周期扩大4倍
        k = 1;
        //方式选择
        if ((mode == ICM) && (rangeState >= 6)) {
            k = 4;
        }

        //算出距离的整数部分
        Fx1 = ((sampling_points * velocity) * k) / 200;
        //算出距离的小数部分
        Fx2 = (((sampling_points * velocity) * k) / 20) % 10;


        position_x = 280;
        position_y = 245;
        /* 显示距离值 */

        position_x = 450;
        position_y = 245;
        /* 显示距离值 */

    }

    public int getVelocity() {
        return velocity;
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
     * @param selectSim SIM显示波形的组数
     */
    public void setSelectSim(int selectSim) {
        this.selectSim = selectSim;
        switch (selectSim) {
            case 1:
                System.arraycopy(simDraw1, 0, waveCompare, 0, 510);
                if (simArray1 != null) {
                    System.arraycopy(simArray1, 0, Constant.SimData, 0, simArray1.length);
                }
                break;
            case 2:
                System.arraycopy(simDraw2, 0, waveCompare, 0, 510);
                if (simArray2 != null) {
                    System.arraycopy(simArray2, 0, Constant.SimData, 0, simArray2.length);
                }
                break;
            case 3:
                System.arraycopy(simDraw3, 0, waveCompare, 0, 510);
                if (simArray3 != null) {
                    System.arraycopy(simArray3, 0, Constant.SimData, 0, simArray3.length);
                }
                break;
            case 4:
                System.arraycopy(simDraw4, 0, waveCompare, 0, 510);
                if (simArray4 != null) {
                    System.arraycopy(simArray4, 0, Constant.SimData, 0, simArray4.length);
                }
                break;
            case 5:
                System.arraycopy(simDraw5, 0, waveCompare, 0, 510);
                if (simArray5 != null) {
                    System.arraycopy(simArray5, 0, Constant.SimData, 0, simArray5.length);
                }
                break;
            case 6:
                System.arraycopy(simDraw6, 0, waveCompare, 0, 510);
                if (simArray6 != null) {
                    System.arraycopy(simArray6, 0, Constant.SimData, 0, simArray6.length);
                }
                break;
            case 7:
                System.arraycopy(simDraw7, 0, waveCompare, 0, 510);
                if (simArray7 != null) {
                    System.arraycopy(simArray7, 0, Constant.SimData, 0, simArray7.length);
                }
                break;
            case 8:
                System.arraycopy(simDraw8, 0, waveCompare, 0, 510);
                if (simArray8 != null) {
                    System.arraycopy(simArray8, 0, Constant.SimData, 0, simArray8.length);
                }
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
     * 点击记忆按钮执行的方法  //GC20190703
     */
    public void clickMemory() {
        clickMemory = true;
        System.arraycopy(waveDraw, 0, waveCompare, 0, 510);
        //记录记忆数据的方式范围   //GC20190703再优化
        modeBefore = mode;
        rangeBefore = range;
    }

    /**
     * 点击比较按钮执行的方法  //GC20190703
     */
    public void clickCompare() {
        if (clickMemory) {
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
                clickMemory = false;
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
        request[0] = (byte) 0xeb;
        request[1] = (byte) 0x90;
        request[2] = (byte) 0xaa;
        request[3] = (byte) 0x55;
        request[4] = (byte) COMMAND_DATA_LENGTH;
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
            command = COMMAND_RECEIVE_DATA;
            dataTransfer = RECEIVING_DATA;
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
     * 设置波形绘制参数
     */
    private void setWaveParameter() {
        //擦除比较波形
        isCom = false;
        if (mode == TDR) {
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
            //GC20190702 SIM绘制波形数组准备
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
        zero = positionReal * density;
        pointDistance = positionVirtual * density;

    }

    /**
     * 处理APP接收的波形数据
     */
    private void doWifiWave(int[] wifiArray) {
        if (wifiArray[3] == WAVE_TDR_ICM_DECAY) {
            System.arraycopy(wifiArray, 8, waveArray, 0, dataMax);
            handler.sendEmptyMessage(WHAT_REFRESH);
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
            handler.sendEmptyMessage(WHAT_REFRESH);
        }
        if (waveArray != null) {
            System.arraycopy((waveArray), 0, Constant.WaveData, 0, waveArray.length);
        }
    }

    /**
     * 组织需要绘制的波形数组（抽点510个）——最终得到waveDraw和waveCompare    //GC20190702
     */
    private void organizeWaveData() {
        //起始位置
        int start = 0;
        //居中位置
        int k = 510 * density / 2;
        //寻找波形显示的起始地址在波形数据数组中的所处的位置(变量i即为此位置)
        if ((dataMax - pointDistance) < k) {
            if ((mode == TDR) || (mode == SIM)) {
                start = dataMax - 2 * k - removeTdrSim[rangeState];
            } else if ((mode == ICM) || (mode == DECAY)) {
                start = dataMax - 2 * k - removeIcmDecay[rangeState];
            }
        }
        //波形按比例抽出510个点
        for (int i = start, j = 0; j < 510; i = i + density, j++) {
            //组织TDR、ICM、DECAY和SIM的第一条波形的数据
            waveDraw[j] = waveArray[i];
            //组织SIM的第二条波形的数据
            if (mode == SIM) {
                waveCompare[j] = simArray1[i];
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

    /**
     * 在sparkView界面显示波形
     */
    private void displayWave() {
        myChartAdapterMainWave.setmTempArray(waveDraw);
        myChartAdapterFullWave.setmTempArray(waveDraw);
        myChartAdapterMainWave.setShowCompareLine(isCom);
        myChartAdapterFullWave.setShowCompareLine(isCom);
        if (mode == SIM) {
            if (isCom) {
                myChartAdapterMainWave.setmCompareArray(waveCompare);
                myChartAdapterFullWave.setmCompareArray(waveCompare);
                //GC201907052 优化SIM显示
                waveFragment.btnWavePrevious.setEnabled(true);
                waveFragment.btnWaveNext.setEnabled(true);
            }
        }
        myChartAdapterMainWave.notifyDataSetChanged();
        myChartAdapterFullWave.notifyDataSetChanged();
        if (tDialog != null) {
            tDialog.dismiss();
        }
        Log.e("DIA", "正在接受数据隐藏" + " 波形绘制完成");

        //刷新后显示控制虚光标    //GC20190629
        cursorState = false;
        myChartAdapterMainWave.setCursorState(false);
        btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_purple));
    }

    /**
     * 脉冲电流故障自动计算过程         //GN 如果在波形比较状态，擦除记忆波形，重画当前波形，重画光标
     */
    private void ICMTest() {
        //1.判断增益是否合适
        gainJudgment();
        switch (gainState) {
            case 0:
                break;
            case 1:
                gainState = 0;
                //GN组织数据画波形
                //GN显示增益过大
                return; //G?
            case 2:
                gainState = 0;
                //GN组织数据画波形
                //GN显示增益过小
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
            //GN组织数据画波形
            //GN显示不击穿
            return;
        }
        //计算方向脉冲
        calculatePulse();
        //计算故障距离
        correlationSimple();
        breakPointCalculate();
        //光标自动定位
        ICMAutoCursor();
        //GN组织数据画波形
    }

    /**
     * 添加脉冲电流方式自动测距 //GC20190708
     * <p>
     * 脉冲电流方式增益自动判断
     */
    int[] data_buffer_a = new int[65560];   //G? 来源

    private void gainJudgment() {
        int i;
        int max = 0;
        int sub;

        //求取前20个点内的极值
        for (i = (READ_ICM_DECAY[rangeState] - 20); i < READ_ICM_DECAY[rangeState]; i++) {
            data_buffer_a[i] = 128;
        }
        for (i = 0; i < READ_ICM_DECAY[rangeState]; i++) {
            sub = data_buffer_a[i] - 128;
            if (Math.abs(sub) > max) {
                max = Math.abs(sub);
            }
        }
        //如果最大值小于 15% 38
        if (max <= 42) {
            //判断增益过小
            gainState = 2;
            return;
        }
        for (i = 0; i < READ_ICM_DECAY[rangeState]; i++) {
            if ((data_buffer_a[i] > 242) || (data_buffer_a[i] < 13)) {
                //增益过大
                gainState = 1;
                return;
            }
        }
    }

    /**
     * 脉冲电流方式软件滤波   方向脉冲法自动计算-软件滤波，一阶滞后滤波，低通截止频率约750kHz（两个采样频率都是这个截止频率）
     */
    float[] dataBuf_filter = new float[65560];

    private void softwareFilter() {
        int i;
        for (i = 1; i < READ_ICM_DECAY[rangeState]; i++) {
            if (rangeState >= 6) {
                dataBuf_filter[i] =
                        (float) 0.8618 * dataBuf_filter[i - 1] + (float) 0.1382 * (float) (data_buffer_a[i] - 128);
            } else {
                dataBuf_filter[i] =
                        (float) 0.9524 * dataBuf_filter[i - 1] + (float) 0.0476 * (float) (data_buffer_a[i] - 128);
            }
        }
        //G??波形位置
        for (i = 1; i < 2000; i++) {
            dataBuf_filter[i] = dataBuf_filter[i] + 6;
        }
    }

    /**
     * 脉冲电流方式数字积分   方向脉冲法自动计算-数字积分,反演电流
     */
    float[] dataBuf_integral = new float[65560];

    private void integral() {
        for (int i = 1; i < READ_ICM_DECAY[rangeState]; i++) {
            //25M采样
            if (rangeState >= 6) {
                dataBuf_integral[i] = dataBuf_integral[i - 1] + dataBuf_filter[i] * 4;
            } else {
                dataBuf_integral[i] = dataBuf_integral[i - 1] + dataBuf_filter[i];
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
        //从触发开始计算初始值   //G?
        start = 120;
        min = 255;
        for (i = start; i < start + 64; i++) {
            if ((dataBuf_filter[i] < min) && (dataBuf_filter[i] < 0)) {
                min = dataBuf_filter[i];
            }
        }
        //积分电流  //G?
        a = 80 * min;
        for (i = start + 64; i < (READ_ICM_DECAY[rangeState] - 50); i++) {
            //1.8
            if ((dataBuf_integral[i] < 0) && (dataBuf_integral[i] < 1.3 * a)) {
                breakdownPosition = i;
                break;
            } else {
                breakdownPosition = 0;
            }
        }
    }

    float[] s1 = new float[65560];
    float[] s2 = new float[65560];
    float   L;
    float   z  = 25;

    /**
     * 脉冲电流方式  计算方向脉冲   方向脉冲法自动计算-使用滤波后电流的微分求VL=L * di/dt，滤波后电流*波阻抗
     */
    private void calculatePulse() {
        //使用滤波后电流进行微分
        float[] V = new float[65560];
        int i;

        //电感值   //G? 来源
        L = (float) icmInductor * (float) (1.0e-6);

        for (i = 0; i < READ_ICM_DECAY[rangeState]; i++) {
            //25M采样
            if (rangeState >= 6) {
                V[i] = (dataBuf_filter[i + 1] - dataBuf_filter[i]) * (float) 4.0e8;
            } else {
                V[i] = (dataBuf_filter[i + 1] - dataBuf_filter[i]) * (float) 1.0e8;
            }
        }
        //确定击穿点
        //计算VL
        for (i = 0; i < READ_ICM_DECAY[rangeState]; i++) {
            V[i] = V[i] * L * (-1.0f);
        }
        //计算方向行波
        for (i = 0; i < READ_ICM_DECAY[rangeState]; i++) {
            s1[i] = V[i] + dataBuf_filter[i] * z;
            s2[i] = V[i] - dataBuf_filter[i] * z;
        }
    }

    /**
     * 击穿点判断
     */
    private void breakPointCalculate() {
        int i;
        int bk_pos;
        //G?
        long remainder;

        i = breakdownPosition - min_peak[0];

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
            bk_pos = min_peak[0];
        } else {
            bk_pos = breakdownPosition;
        }
        for (i = bk_pos; i > 100; i--) {
            if (dataBuf_filter[i - 1] < dataBuf_filter[i]) {
                //时光标
                break_bk = i;
                break;
            }
        }
    }

    /**
     * 脉冲电流方式  计算故障距离(抽点做数据相关)  方向脉冲法自动计算-使用相关计算故障距离   //G?
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

        //采用负极大值进行计算
        findMinPeak();
        for (i = breakdownPosition; i > 100; i--) {
            if (dataBuf_filter[i - 1] <= dataBuf_filter[i]) {
                break_bk = i;
                break;
            }
        }
        //25M采样
        if (rangeState >= 6) {
            //需要修改，32km和64km采样频率变了，需要调整参数
            if (break_bk > (50 / 4)) {
                //相关窗左侧
                w1 = break_bk - (50 / 4);
            } else {
                w1 = break_bk;
            }
            //相关窗右侧
            w2 = break_bk + (350 / 4);
            //相关移动大小
            w3 = READ_ICM_DECAY[rangeState] - w2;
        } else {
            //需要修改，32km和64km采样频率变了，需要调整参数
            if (break_bk > 50) {
                //相关窗左侧
                w1 = break_bk - 50;
            } else {
                w1 = break_bk;
            }
            //相关窗右侧
            w2 = break_bk + 350;
            //相关移动大小
            w3 = READ_ICM_DECAY[rangeState] - w2;
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

        float[] S1 = new float[400];
        float[] S2 = new float[400];

        for (i = w1; i < w2; i++) {
            S1[i - w1] = s1_simple[i];
        }
        for (i = 0; i < w3; i++) {
            for (k = w1; k < w2; k++) {
                S2[k - w1] = s2_simple[k + i];
            }
            //G?清零
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
        int max_index = 0;

        for (i = 0; i < w3; i++) {
            if (P[i] > max) {
                max = P[i];
                max_index = i;
            }
        }

        //换算为整条波形数据中的点数
        max_index = (w1 + max_index) * densityMaxIcmDecay[rangeState];

        w1 = w1 * densityMaxIcmDecay[rangeState];
        w2 = w2 * densityMaxIcmDecay[rangeState];
        w3 = densityMaxIcmDecay[rangeState] - w2;

        for (i = w1; i < w2; i++) {
            S1[i - w1] = s1[i];
        }

        //将前后4个点改为zoom_icm_list[range]
        for (i = (max_index - densityMaxIcmDecay[rangeState]); i < (max_index + densityMaxIcmDecay[rangeState]); i++) {
            for (k = 0; k < w2 - w1; k++) {
                S2[k] = s2[k + i];
            }
            //清零
            p = (float) 0.0;
            //进行相关运算S
            for (j = 0; j < (w2 - w1); j++) {
                p += S1[j] * S2[j] * (-1.0f);
            }
            //将整条波形的相关运算值存入P数组中
            P[i - (max_index - densityMaxIcmDecay[rangeState])] = p;
        }
        max = P[0];
        int max_index1 = 0;
        //将前后4个点改为zoom_icm_list[range]
        for (i = 0; i < densityMaxIcmDecay[rangeState] * 2; i++) {
            if (P[i] > max) {
                max = P[i];
                max_index1 = i;
            }
        }
        max_index = max_index - densityMaxIcmDecay[range] + max_index1 - w1;

        calculate_distance2(max_index);
        faultResult = max_index;
    }

    /**
     * 脉冲电流方式  计算极小值点   方向脉冲法自动计算-使用极小值点找放电脉冲
     */
    float reference = 0;
    int[] min_peak  = new int[255];

    private void findMinPeak() {
        int i = 170;
        int first;
        int peak_max = 0;
        int[] min_data = new int[255];
        int j = 0;
        int k;
        int l = 0;
        float first_min;

        //25M采样
        if (rangeState >= 6) {
            i = 150;
        }

        while ((j < 255) && (i < (READ_ICM_DECAY[rangeState] - 55))) {
            if ((dataBuf_filter[i] < dataBuf_filter[i - 1]) && (dataBuf_filter[i] <= dataBuf_filter[i + 1])) {
                if ((i > 5) && (dataBuf_filter[i - 1] < dataBuf_filter[i - 2])) {
                    if (dataBuf_filter[i - 2] < dataBuf_filter[i - 3]) {
                        if (dataBuf_filter[i - 3] < dataBuf_filter[i - 4]) {
                            if (dataBuf_filter[i - 4] < dataBuf_filter[i - 5]) {
                                min_data[j] = i;
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
        first_min = dataBuf_filter[min_data[0]];
        first = min_data[0];
        for (k = 0; k <= j; k++) {
            if (dataBuf_filter[min_data[k]] < first_min) {
                first = min_data[k];
                first_min = dataBuf_filter[min_data[k]];
                peak_max = k;
            }
        }
        breakdownPosition = first;
        //求出负的最大极值
        reference = dataBuf_filter[breakdownPosition];

        for (k = 0; k <= peak_max; k++) {
            if (dataBuf_filter[min_data[k]] < (float) (reference * 0.7)) {
                min_peak[l] = min_data[k];
                l++;
            }
        }
    }

    /**
     * 脉冲电流方式光标自动定位
     */
    private void ICMAutoCursor() {
        zero = break_bk;
        pointDistance = zero + faultResult;
        //超出范围居中画光标
        if (pointDistance >= dataMax) {
            pointDistance = dataMax / 2;
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
                Constant.ModeValue = mode;

                //GC20190709
                switchDensity();
                initCursor();
                //GC20190705 信息栏显示
                tvBalance.setVisibility(View.VISIBLE);
                vlBalance.setVisibility(View.VISIBLE);
                tvDelay.setVisibility(View.GONE);
                vlDelay.setVisibility(View.GONE);
                //调节栏fragment显示
                adjustFragment.btnBalancePlus.setVisibility(View.VISIBLE);
                adjustFragment.btnBalanceMinus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayPlus.setVisibility(View.GONE);
                adjustFragment.btnDelayMinus.setVisibility(View.GONE);
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
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayMinus.setVisibility(View.VISIBLE);
                waveFragment.btnMemory.setVisibility(View.VISIBLE);
                waveFragment.btnCompare.setVisibility(View.VISIBLE);
                waveFragment.btnWavePrevious.setVisibility(View.INVISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.INVISIBLE);
                break;
            case SIM:
                vlMode.setText(getResources().getString(R.string.btn_sim));
                switchDensity();
                initCursor();
                tvBalance.setVisibility(View.GONE);
                vlBalance.setVisibility(View.GONE);
                tvDelay.setVisibility(View.GONE);
                vlDelay.setVisibility(View.GONE);
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.GONE);
                adjustFragment.btnDelayMinus.setVisibility(View.GONE);
                waveFragment.btnMemory.setVisibility(View.GONE);
                waveFragment.btnCompare.setVisibility(View.GONE);
                waveFragment.btnWavePrevious.setVisibility(View.VISIBLE);
                waveFragment.btnWaveNext.setVisibility(View.VISIBLE);
                break;
            case DECAY:
                vlMode.setText(getResources().getString(R.string.btn_decay));
                switchDensity();
                initCursor();
                tvBalance.setVisibility(View.GONE);
                vlBalance.setVisibility(View.GONE);
                tvDelay.setVisibility(View.VISIBLE);
                vlDelay.setVisibility(View.VISIBLE);
                adjustFragment.btnBalancePlus.setVisibility(View.GONE);
                adjustFragment.btnBalanceMinus.setVisibility(View.GONE);
                adjustFragment.btnDelayPlus.setVisibility(View.VISIBLE);
                adjustFragment.btnDelayMinus.setVisibility(View.VISIBLE);
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
        Constant.RangeValue = range;
        Log.e("RV", "CRV" + Constant.ModeValue);
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
     * 比例选择 //GC20190709
     */
    private void switchDensity() {
        if ((mode == TDR) || (mode == SIM)) {
            densityMax = densityMaxTdrSim[rangeState];
            density = densityMax;
            vlDensity.setText("1 : " + density);
        } else if ((mode == ICM) || (mode == DECAY)) {
            densityMax = densityMaxIcmDecay[rangeState];
            density = densityMax;
            vlDensity.setText("1 : " + density);
        }
    }

    /**
     * 光标位置和距离显示初始化 //GC20190709
     */
    private void initCursor() {
        //刷新后显示控制虚光标
        cursorState = false;
        myChartAdapterMainWave.setCursorState(false);
        btnCursor.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.T_purple));
        //画光标
        positionReal = 0;
        mainWave.setScrubLineReal(positionReal);
        fullWave.setScrubLineReal(positionReal);
        positionVirtual = 255;
        mainWave.setScrubLineVirtual(positionVirtual);
        fullWave.setScrubLineVirtual(positionVirtual);
        //显示距离
        calculateDistance(Math.abs(positionVirtual - positionReal));
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
        Constant.Gain = gain;
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
        Constant.Velocity = velocity;
        //GC20190709
        calculateDistance(Math.abs(positionVirtual - positionReal));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * ##############################以下为测试代码，留作参考##############################
     * <p>
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

}