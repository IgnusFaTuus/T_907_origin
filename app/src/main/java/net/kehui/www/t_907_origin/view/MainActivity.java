package net.kehui.www.t_907_origin.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
    SparkView   mainWave;
    @BindView(R.id.tv_distance)
    TextView    tvDistance;
    @BindView(R.id.fullWave)
    SparkView   fullWave;
    @BindView(R.id.btn_mtd)
    Button  btnMtd;
    @BindView(R.id.btn_range)
    Button  btnRange;
    @BindView(R.id.btn_adj)
    Button  btnAdj;
    @BindView(R.id.btn_opt)
    Button  btnOpt;
    @BindView(R.id.btn_file)
    Button  btnFile;
    @BindView(R.id.btn_setting)
    Button  btnSetting;
    @BindView(R.id.btn_test)
    Button  btnTest;
    @BindView(R.id.btn_cursor)
    Button  btnCursor;
    @BindView(R.id.vl_method)
    TextView    vlMode;
    @BindView(R.id.vl_range)
    TextView    vlRange;
    @BindView(R.id.vl_gain)
    TextView    vlGain;
    @BindView(R.id.vl_vel)
    TextView    vlVel;
    @BindView(R.id.vl_density)
    TextView    vlDensity;
    @BindView(R.id.tv_balance)
    TextView    tvBalance;
    @BindView(R.id.vl_balance)
    TextView    vlBalance;
    @BindView(R.id.tv_delay)
    TextView    tvDelay;
    @BindView(R.id.vl_delay)
    TextView    vlDelay;

    /**
     * 用于展示Fragment
     */
    private ModeFragment modeFragment;
    private RangeFragment   rangeFragment;
    private AdjustFragment  adjustFragment;
    private WaveFragment waveFragment;
    private FileFragment    fileFragment;
    private SettingFragment settingFragment;
    private FragmentManager fragmentManager;

    /**
     * APP下发命令 指令内容command/传输数据data
     */
    private int command;
    private int dataTransfer;
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
    public static final int WAVE_COMPLETED      = 8;
    public static final int GET_STREAM          = 9;

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
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
                    wifiStream = msg.getData().getIntArray("DATA");
                    assert wifiStream != null;
                    streamLen = wifiStream.length;
                    Log.e("DATA", "max: " + dataMax);
                    setWaveParameter();
                    doWifiWave(wifiStream);
                    break;
                case WHAT_REFRESH:
                    organizeWaveData();
                    displayWave();
                    break;
                case GET_STREAM:
                    if (!isSuccessful) {
                        Toast.makeText(MainActivity.this, "T-907连接成功！", Toast.LENGTH_LONG).show();
                        isSuccessful = true;
                    }
                    //GC20190103
                    //接收WIFI数据流
                    wifiStream = msg.getData().getIntArray("STM");
                    assert wifiStream != null;
                    streamLen = wifiStream.length;
                    Log.e("STM", "streamLen: " + streamLen);
                    Log.e("STM", "hasLeft: " + hasLeft);
                    Log.e("STM", "dataMax: " + dataMax);
                    doWifiArray(wifiStream, streamLen);
                    break;
                default:
                    break;
            }
            return false;
        }
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
        fragmentManager = getFragmentManager();
        //GC201907052  先初始化（fragment切换bug修改）
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
        vlRange.setText(getResources().getString(R.string.btn_500m));
        vlGain.setText(String.valueOf(gain));
        vlVel.setText(velocity + "m/μs");
        vlBalance.setText(String.valueOf(balance));
        vlDelay.setText(delay + "μs");
        vlDensity.setText( "1 : " + density);

        //初始化状态栏不显示延时
        tvDelay.setVisibility(View.GONE);
        vlDelay.setVisibility(View.GONE);

    }

    /**
     * 初始化sparkView //GC20181227
     */
    public void initSparkView() {
        for (int i = 0; i < 510; i++) {
            waveArray[i] = 128;
            //simArrayCmp[i] = 128;
        }
        myChartAdapterMainWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        myChartAdapterFullWave = new MyChartAdapterBase(waveArray, null,
                false, 0, false, dataMax);
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
        Log.i("Draw", "初始化绘制结束");
        //初始化光标按钮颜色
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple));

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
        mainWave.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {  //GC20181224
                if (clickCursor) {
                    positionReal = (int) value;
                } else {
                    positionVirtual = (int) value;
                }
                tvDistance.setText(Math.abs(positionVirtual - positionReal) * velocity + "m");
                Log.e("光标移动数值", "" + value); //GN 数值从0开始计数
            }
        });
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
                            new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

                    singleThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "通信失败，请检查网络后重试",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                    if (tDialog != null) {
                        tDialog.dismiss();
                    }
                    Log.e("DIA", "WIFI连接：" +  "隐藏");
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
                    Log.e("DIA", "WIFI连接：" +  "显示");
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
     * @return  获取ip
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
     * 点击光标按钮事件
     */
    private void clickCursor() {
        clickCursor = myChartAdapterMainWave.getCursorState();
        clickCursor = !clickCursor;
        if (clickCursor) {
            btnCursor.setTextColor(getResources().getColor(R.color.T_red));
        } else {
            btnCursor.setTextColor(getResources().getColor(R.color.T_purple));
        }
        myChartAdapterMainWave.setCursorState(clickCursor);
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
                //GC20190705 信息显示
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
        command = COMMAND_RANGE;
        dataTransfer = range;
        sendCommand();
        switch (range) {
            case RANGE_500 :
                rangeParameter = 0;
                vlRange.setText(getResources().getString(R.string.btn_500m));
                gain = 13;
                vlGain.setText("13");
                break;
            case RANGE_1_KM :
                rangeParameter = 1;
                vlRange.setText(getResources().getString(R.string.btn_1km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_2_KM :
                rangeParameter = 2;
                vlRange.setText(getResources().getString(R.string.btn_2km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_4_KM :
                rangeParameter = 3;
                vlRange.setText(getResources().getString(R.string.btn_4km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_8_KM :
                rangeParameter = 4;
                vlRange.setText(getResources().getString(R.string.btn_8km));
                gain = 10;
                vlGain.setText("10");
                break;
            case RANGE_16_KM :
                rangeParameter = 5;
                vlRange.setText(getResources().getString(R.string.btn_16km));
                gain = 9;
                vlGain.setText("9");
                break;
            case RANGE_32_KM :
                rangeParameter = 6;
                vlRange.setText(getResources().getString(R.string.btn_32km));
                gain = 9;
                vlGain.setText("9");
                break;
            case RANGE_64_KM :
                rangeParameter = 7;
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
    public void setVelocity(float velocity) {
        this.velocity = velocity;
        vlVel.setText(velocity + "m/μs");
    }

    public float getVelocity() {
        return velocity;
    }

    /**
     * @param balance  需要发送的平衡控制命令值 / 响应信息栏平衡变化
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
            case 1 :
                System.arraycopy(simDraw1, 0, waveCompare, 0, 510);
                break;
            case 2 :
                System.arraycopy(simDraw2, 0, waveCompare, 0, 510);
                break;
            case 3 :
                System.arraycopy(simDraw3, 0, waveCompare, 0, 510);
                break;
            case 4 :
                System.arraycopy(simDraw4, 0, waveCompare, 0, 510);
                break;
            case 5 :
                System.arraycopy(simDraw5, 0, waveCompare, 0, 510);
                break;
            case 6 :
                System.arraycopy(simDraw6, 0, waveCompare, 0, 510);
                break;
            case 7 :
                System.arraycopy(simDraw7, 0, waveCompare, 0, 510);
                break;
            case 8 :
                System.arraycopy(simDraw8, 0, waveCompare, 0, 510);
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
    }

    /**
     * 点击比较按钮执行的方法  //GC20190703
     */
    public void clickCompare() {
        if (clickMemory) {
            isCom = !isCom;
        } else {
            Toast.makeText(this, getResources().getString(R.string
                    .You_have_no_memory_data_can_not_compare), Toast.LENGTH_SHORT).show();
        }
        myChartAdapterMainWave.setmTempArray(waveDraw);
        myChartAdapterMainWave.setShowCompareLine(isCom);
        myChartAdapterMainWave.setmCompareArray(waveCompare);
        myChartAdapterMainWave.notifyDataSetChanged();
    }

    /**
     * 启动时需要发送的初始化命令
     */
    private void sendInitCommand() {
        command = COMMAND_MODE;
        dataTransfer = TDR;
        sendCommand();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                command = COMMAND_RANGE;
                dataTransfer = RANGE_500;
                sendCommand();
            }
        }, 20);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clickTest();
            }
        }, 50);
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
        if ( (wifiArray[5] == COMMAND_TRIGGER) && (wifiArray[6] == TRIGGERED) ) {
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
     * 处理APP接收的波形数据
     */
    private void doWifiWave(int[] wifiArray) {
        if (wifiArray[3] == WAVE_TDR_ICM_DECAY) {
            System.arraycopy(wifiArray, 8, waveArray, 0, dataMax);
            handler.sendEmptyMessage(WHAT_REFRESH);
        } else if (wifiArray[3] == WAVE_SIM0) {
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
    }

    /**
     * 设置波形参数
     */
    private void setWaveParameter() {
        if (mode == TDR) {
            //GC20190702 绘制波形数组准备
            dataMax = readTdrSim[rangeParameter];
            waveArray = new int[dataMax];
            isCom = false;

            densityMax = densityMaxTdrSim[rangeParameter];
            parameterDensity = densityMax;
        } else if ((mode == ICM) || (mode == DECAY)) {
            dataMax = readIcmDecay[rangeParameter];
            waveArray = new int[dataMax];
            isCom = false;

            densityMax = densityMaxIcmDecay[rangeParameter];
            parameterDensity = densityMax;
        } else if (mode == SIM) {
            dataMax = readTdrSim[rangeParameter];
            waveArray = new int[dataMax];
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

            densityMax = densityMaxTdrSim[rangeParameter];
            parameterDensity = densityMax;

        }
        positionVirtual = 255 * parameterDensity;
    }

    /**
     * 组织需要绘制的波形数组  最终得到waveDraw和waveCompare
     */
    private void organizeWaveData() {
        int start = 0,k;
        //k=(MainPaintBox->Width/2)*Density;
        k = 510 * parameterDensity / 2;
        //寻找波形显示的起始地址在波形数据数组中的所处的位置(变量i即为此位置)
        if(positionVirtual < k) {
            start = 0;
        } else if( (dataMax - positionVirtual) < k) {
            //i=Data_Max - 2*k;
            if ((mode == TDR) || (mode == SIM)) {
                start = dataMax - 2 * k - removeTdrSim[rangeParameter];
            } else if ((mode == ICM) || (mode == DECAY)) {
                start = dataMax - 2 * k - removeIcmDecay[rangeParameter];
            }
        }
        //波形按比例抽出510个点
        //TDR、ICM、DECAY和SIM的第一条波形
        for (int i = start,j = 0; j < 510; i = i + parameterDensity, j++) {
            waveDraw[j] = waveArray[i];
            if (mode == SIM) {
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
        //SIM的第二条波形
        if (mode == SIM) {
            System.arraycopy(simDraw1, 0, waveCompare, 0, 510);
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
                isDrawSim = true;
                //GC201907052 优化SIM显示
                waveFragment.btnWavePrevious.setEnabled(true);
                waveFragment.btnWaveNext.setEnabled(true);
            }
        } else {
            isDrawSim = false;
        }
        myChartAdapterMainWave.notifyDataSetChanged();
        myChartAdapterFullWave.notifyDataSetChanged();
        if (tDialog != null) {
            tDialog.dismiss();
        }
        Log.e("DIA", "正在接受数据隐藏" + " 波形绘制完成");
        //画光标
        positionReal = 0;
        mainWave.setScrubLineReal(positionReal);
        positionVirtual = 255;
        mainWave.setScrubLineVirtual(positionVirtual);
        tvDistance.setText(Math.abs(positionVirtual - positionReal) * velocity + "m");
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * ##############################以下为测试代码，留作参考##############################
     *
     * 处理APP接收的数据（旧的方式）
     */
    private void doWifiArray(int[] wifiArray, int length) {
        //command临时数组
        int[] tempCommand = new int[8];

        if ( (length == 8) && (wifiArray[3] == COMMAND) ) {
            boolean isCommand = doCommandCrc(wifiArray);
            //校验成功：APP接收的是命令
            if (isCommand) {
                //仪器触发时：APP发送接收数据命令
                if ( (wifiArray[5] == COMMAND_TRIGGER) && (wifiArray[6] == COMMAND_RECEIVE_RIGHT) ) {
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

        } else if (wifiArray[3] == WAVE_TDR_ICM_DECAY) {
            //数组长度不够wave,拼接处理
            if (hasLeft) {
                for (int i = leftLen, j = 0; j < length; i++, j++) {
                    //与剩余数据进行拼接
                    leftArray[i] = wifiArray[j];
                }
                leftLen = leftLen + length;
                if (leftLen == (dataMax + 9)) {
                    for (int i = 8, j = 0; i < leftLen - 1; i++, j++) {
                        //取wave长度的数组
                        waveArray[j] = leftArray[i];
                    }
                    hasLeft = false;
                    leftLen = 0;
                    setWaveParameter();
                    organizeWaveData();
                    displayWave();
                }

            } else {
                //取command长度的数组(?ICM夹杂命令）
                System.arraycopy(wifiArray, 0, tempCommand, 0, 8);
                boolean isCommand = doCommandCrc(tempCommand);
                //sum校验成功，包含command
                if (isCommand) {
                    if (length == (dataMax + 9 + 8)) {
                        // 去掉command   GC20190126
                        for (int i = 16, j = 0; i < length - 1; i++, j++) {
                            waveArray[j] = wifiArray[i];
                        }
                        setWaveParameter();
                        organizeWaveData();
                        displayWave();

                    } else {  //数组长度不够wave,准备拼接处理  GC20190126
                        for (int i = leftLen, j = 8; j < length; i++, j++) {
                            // 去掉command
                            leftArray[i] = wifiArray[j];
                        }
                        hasLeft = true;
                        leftLen = leftLen + length - 8;
                    }
                } else {
                    if (length == (dataMax + 9)) {
                        for (int i = 8, j = 0; i < length - 1; i++, j++) {
                            waveArray[j] = wifiArray[i];
                        }
                        setWaveParameter();
                        organizeWaveData();
                        displayWave();

                    } else {
                        //数组长度不够wave,准备拼接处理
                        for (int i = leftLen, j = 0; j < length; i++, j++) {
                            leftArray[i] = wifiArray[j];
                        }
                        hasLeft = true;
                        leftLen = leftLen + length;
                    }
                }
            }
        }
    }

    /**
     * 控制命令sum校验
     */
    private boolean doCommandCrc(int[] tempCommand) {
        int sum = tempCommand[4] + tempCommand[5] + tempCommand[6];
        return tempCommand[7] == sum;
    }

    /**
     * 波形数据sum校验
     */
    private boolean doTempCrc(int[] tempWave) {
        int dataLen = tempWave.length;
        int a;
        int sum = 0;
        for (int i = 4; i < dataLen; i++) {
            a = tempWave[i];
            sum = sum + a;
        }
        return tempWave[dataMax + 8] == sum;
    }

    /**
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
        tvDistance.setText(Math.abs(positionVirtual - positionReal) * velocity + "m");
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple));
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
            tvDistance.setText(Math.abs(positionVirtual - positionReal) * velocity + "m");
            btnCursor.setTextColor(getResources().getColor(R.color.T_purple));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}