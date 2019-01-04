package net.kehui.www.t_907_origin.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.base.BaseActivity;
import net.kehui.www.t_907_origin.fragment.AdjustFragment;
import net.kehui.www.t_907_origin.fragment.FileFragment;
import net.kehui.www.t_907_origin.fragment.MethodFragment;
import net.kehui.www.t_907_origin.fragment.OptionFragment;
import net.kehui.www.t_907_origin.fragment.RangeFragment;
import net.kehui.www.t_907_origin.fragment.SettingFragment;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.ui.SparkView.SparkView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by IF on 2018/3/26.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.content)
    FrameLayout  content;
    @BindView(R.id.mainWave)
    SparkView    mainWave;
    @BindView(R.id.textView)
    TextView     textView;
    @BindView(R.id.tv_distance)
    TextView     tvDistance;
    @BindView(R.id.fullWave)
    SparkView    fullWave;
    @BindView(R.id.btn_mtd)
    Button       btnMtd;
    @BindView(R.id.btn_range)
    Button       btnRange;
    @BindView(R.id.btn_adj)
    Button       btnAdj;
    @BindView(R.id.btn_opt)
    Button       btnOpt;
    @BindView(R.id.btn_file)
    Button       btnFile;
    @BindView(R.id.btn_setting)
    Button       btnSetting;
    @BindView(R.id.btn_test)
    Button       btnTest;
    @BindView(R.id.btn_cursor)
    Button       btnCursor;
    @BindView(R.id.tv_method)
    TextView     tvMethod;
    @BindView(R.id.tv_gain)
    TextView     tvGain;
    @BindView(R.id.tv_vel)
    TextView     tvVel;
    @BindView(R.id.tv_range)
    TextView     tvRange;
    @BindView(R.id.vl_method)
    TextView     vlMethod;
    @BindView(R.id.vl_gain)
    TextView     vlGain;
    @BindView(R.id.vl_vel)
    TextView     vlVel;
    @BindView(R.id.vl_range)
    TextView     vlRange;
    @BindView(R.id.value_list)
    LinearLayout valueList;
    @BindView(R.id.stateList)
    LinearLayout stateList;
    @BindView(R.id.wave_display)
    LinearLayout waveDisplay;
    @BindView(R.id.wait_trigger)
    TextView     waitTrigger;
    //用于展示Fragment
    private             MethodFragment  methodFragment;
    private             RangeFragment   rangeFragment;
    private             AdjustFragment  adjustFragment;
    private             OptionFragment  optionFragment;
    private             FileFragment    fileFragment;
    private             SettingFragment settingFragment;
    private             FragmentManager fragmentManager;
    /*发送command的内容*/
    private             int             command_1;
    private             int             command_2;
    /*全局的handler对象用来执行UI更新*/
    public static final int             DEVICE_CONNECTING = 1;  //设备连接
    public static final int             DEVICE_CONNECTED  = 2;   //设备连接成功
    public static final int             SEND_SUCCESS      = 3;       //发送command成功
    public static final int             SEND_ERROR        = 4;         //发送command失败
    public static final int             GET_STREAM        = 5;         //GC20190103 接收WIFI数据流
    public static final int             RECEIVE_SUCCESS   = 6;   //设备接收command成功
    public static final int             RECEIVE_ERROR     = 7;     //设备接收command失败

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(listenerThread.getSocket(), handler);
                    connectThread.start();
                    break;
                case DEVICE_CONNECTED:
                    Toast.makeText(MainActivity.this, "T-907连接成功！", Toast.LENGTH_LONG).show();
                    command_1 = 0x02;
                    command_2 = 0x11;
                    sendCommand();  //GC20190102 发送初始化命令：连接成功后发送测试方式
                    break;
                case SEND_SUCCESS:
                    //hasSentCommand = true;
                    break;
                case SEND_ERROR:
                    Toast.makeText(MainActivity.this, "T-907已断开，请检查设备情况！", Toast.LENGTH_LONG)
                            .show();
                    break;
                case GET_STREAM:
                    WIFIStream = msg.getData().getIntArray("STM");
                    streamLen = WIFIStream.length;
                    Log.e("len", "" + streamLen);
                    doWIFIArray(WIFIStream, streamLen);
                    /*max = len;
                    for (int i = 0; i < len; i++) {
                        waveArray[i] = WIFIStream[i];
                    }
                    drawWIFIData();*/

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fragmentManager = getFragmentManager();
        //第一次启动时选中第0个tab
        setTabSelection(0);
        btnMtd.setEnabled(false);
        btnRange.setEnabled(true);
        btnAdj.setEnabled(true);
        btnOpt.setEnabled(true);
        btnFile.setEnabled(true);
        btnSetting.setEnabled(true);
        vlMethod.setText(getResources().getString(R.string.btn_tdr));
        vlRange.setText(getResources().getString(R.string.btn_500m));
        vlGain.setText(String.valueOf(getGainState()));
        vlGain.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        vlVel.setText(String.valueOf(getVelocityState()) + "m/μs");
        vlVel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        initWaveData();
        setChartListener(); //GC20181224 监听光标位置
        startThread();
    }

    //GC20190103 启动接收WIFI数据流的线程
    private void startThread() {
        //开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getWifiRouteIPAddress(MainActivity.this), PORT);
                    connectThread = new ConnectThread(socket, handler);
                    connectThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "通信连接失败", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        }).start();
        //开启监听线程
        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();
    }

    public void setTabSelection(int index) {
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragment(transaction);
        switch (index) {
            case 0:
                if (methodFragment == null) {
                    methodFragment = new MethodFragment();
                    transaction.add(R.id.content, methodFragment);
                } else {
                    transaction.show(methodFragment);
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

                if (optionFragment == null) {
                    optionFragment = new OptionFragment();
                    transaction.add(R.id.content, optionFragment);
                } else {
                    transaction.show(optionFragment);
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
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (methodFragment != null) {
            transaction.hide(methodFragment);
        }
        if (rangeFragment != null) {
            transaction.hide(rangeFragment);
        }
        if (adjustFragment != null) {
            transaction.hide(adjustFragment);
        }
        if (optionFragment != null) {
            transaction.hide(optionFragment);
        }
        if (fileFragment != null) {
            transaction.hide(fileFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }
    }

    //GC 初始化sparkView
    private void initWaveData() {
        for (int i = 0; i < max; i++) {
            waveArray[i] = 128;
        }
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);

    }

    //监听光标位置    //?1
    private void setChartListener() {
        mainWave.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if (clickCursor) {
                    positionReal = (int) value;
                    //Log.e("positionReal","" + positionReal);
                } else {
                    positionVirtual = (int) value;
                    //Log.e("positionVirtual","" + positionVirtual);
                }
                tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
                Log.e("VALUE", "" + value); //GN 数值从0开始计数
            }
        });
    }

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
        //GC
        initWaveData();
    }

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

    //测试按钮
    private void clickTest() {
        switch (range) {
            case 0x11:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[0];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[0];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x22:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[1];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[1];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x33:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[2];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[2];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x44:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[3];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[3];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x55:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[4];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[4];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x66:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[5];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[5];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x77:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[6];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[6];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case (byte) 0x88:
                if ((method == 17) || (method == 51)) {
                    max = readTdrSim[7];
                } else if ((method == 34) || (method == 68)) {
                    max = readIcmDecay[7];
                }
                waveArray = new int[max];  //GC20181227
                break;
            default:
                break;
        }
        //GC20190102 命令发送
        command_1 = 0x01;
        command_2 = 0x11;   //测试命令
        sendCommand();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                command_1 = 0x09;
                command_2 = 0x11;   //接收数据命令
                sendCommand();
            }
        }, 1000);
        clickTest = !clickTest;
        if (clickTest) {
            btnTest.setText(getResources().getString(R.string.btn_cancel));
            waitTrigger.setVisibility(View.VISIBLE);

        } else {
            btnTest.setText(getResources().getString(R.string.btn_test));
            waitTrigger.setVisibility(View.INVISIBLE);

        }

    }

    //GC20181223 光标按钮
    private void clickCursor() {
        clickCursor = myChartAdapterMainWave.getCursorState();
        clickCursor = !clickCursor;
        myChartAdapterMainWave.setCursorState(clickCursor);
    }

    /* 数据头   数据长度  指令  传输数据  校验和
    eb90aa55     03      01      11       15
    eb90aa55 03 01 11 15	    测试0x11
    eb90aa55 03 01 22 26	    取消测试0x22
    eb90aa55 03 02 11 16		TDR低压脉冲方式
    eb90aa55 03 02 22 27		ICM脉冲电流方式
    eb90aa55 03 02 33 38		SIM二次脉冲方式
    eb90aa55 03 03 11 17		范围500m
    eb90aa55 03 03 22 28
    eb90aa55 03 03 33 39
    eb90aa55 03 03 44 4a
    eb90aa55 03 03 55 5b
    eb90aa55 03 03 66 6c
    eb90aa55 03 03 77 7d
    eb90aa55 03 03 88 8e		范围64km
    eb90aa55 03 04 11 18		增益+
    eb90aa55 03 04 22 29		增益-
    eb90aa55 03 05 11 19		延时+
    eb90aa55 03 05 22 2a		延时-
    eb90aa55 03 07 11 1b  	    平衡+
    eb90aa55 03 07 22 2c		平衡-
    eb90aa55 03 08 11 1c		//G后续添加 接收到触发信号
    eb90aa55 03 09 11 1d		//G后续添加 接收数据命令
    eb90aa55 03 0a 11 1e		//G后续添加 关机重连*/
    public void sendCommand() {
        byte[] request = new byte[8];
        request[0] = (byte) 0xEB;
        request[1] = (byte) 0x90;
        request[2] = (byte) 0xAA;
        request[3] = (byte) 0x55;
        request[4] = (byte) 0x03;
        request[5] = (byte) command_1;
        request[6] = (byte) command_2;
        int sum = request[4] + request[5] + request[6];
        request[7] = (byte) sum;
        connectThread.sendCommand(request);

    }

    //GN 处理接收到的WIFI数据
    private void doWIFIArray(int[] WIFIArray, int length) {
        int l = 0;
        int doLength;   //待处理的数组数据个数
        int[] tempCommand = new int[8]; //command临时数组

        if (length == 8) {
            if (WIFIArray[0] == 235) {
                System.arraycopy(WIFIArray, 0, tempCommand, 0, 8);  //取command长度的数组
                boolean isCrc2 = doTempCrc2(tempCommand);
                if (isCrc2) {    //sum校验成功，判断为command
                    if (tempCommand[6] == 0x33) {
                        handler.sendEmptyMessage(RECEIVE_SUCCESS);
                        Log.e("hasReceivedCommand", "" + hasReceivedCommand);
                    } else if (tempCommand[1] == 0x44) {
                        handler.sendEmptyMessage(RECEIVE_ERROR);
                    }
                } else {
                    leftLen = length;
                    hasLeft = true;
                }
            }

        } else if (length > 8) {

            if (hasLeft) {
                //有剩余数据，拼接数组
                for (int i = 0, j = leftLen; i < length; i++, j++) {
                    leftArray[j] = WIFIArray[i];
                }
                doLength = leftLen + length;
                if (doLength == (max + 9)) {
                    for (int i = 8, j = 0; i < doLength - 1; i++, j++) {
                        waveArray[j] = leftArray[i];    //取wave长度的数组
                    }
                    drawWIFIData();
                    hasReceivedWave = false;
                } else if (doLength >= (max + 9)) {
                    for (int i = 8, j = 0; i < max + 9 - 1; i++, j++) {
                        waveArray[j] = leftArray[i];    //取wave长度的数组
                    }
                    drawWIFIData();
                    leftLen = doLength - max - 9;
                    hasLeft = true;

                } else {
                    leftLen = doLength;
                    hasLeft = true;
                }

            }
            //开始遍历
            for (; l < length - 8; l++) {
                if (WIFIArray[l] == 235) {
                    for (int j = l, k = 0; j < (l + 8); j++, k++) {
                        tempCommand[k] = WIFIArray[j];  //取command长度的数组
                    }
                    boolean isCrc2 = doTempCrc2(tempCommand);
                    if (isCrc2) {    //sum校验成功，判断为command
                        if (tempCommand[6] == 0x33) {
                            handler.sendEmptyMessage(RECEIVE_SUCCESS);
                            Log.e("hasReceivedCommand", "" + hasReceivedCommand);
                            if (tempCommand[6] == 0x09) {     //判断为接收数据命令，准备接收数据
                                if ((length - l - 8) == (max + 9)) {  //剩余数组为wave
                                    for (int i = l + 8 + 8, j = 0; i < length - 1; i++, j++) {
                                        waveArray[j] = WIFIArray[i];    //取wave长度的数组
                                    }
                                    drawWIFIData();

                                } else {
                                    hasReceivedWave = true;
                                    leftLen = length - 8 - l;
                                    for (int i = l + 8, j = 0; i < length; i++, j++) {
                                        leftArray[j] = WIFIArray[i];    //给剩余数组赋值
                                    }
                                    hasLeft = true;
                                }
                            }
                        } else if (tempCommand[1] == 0x44) {
                            handler.sendEmptyMessage(RECEIVE_ERROR);
                        }
                        l += 7;

                    } else {    //sum校验失败，判断为wave
                        if ((length - l) == (max + 9)) {  //剩余数组为wave
                            for (int i = l + 8, j = 0; i < length - 1; i++, j++) {
                                waveArray[j] = WIFIArray[i];    //取wave长度的数组
                            }
                            drawWIFIData();
                        } else {  //数组长度不够wave,准备拼接处理
                            leftLen = length - l;
                            for (int i = l, j = 0; i < length; i++, j++) {
                                leftArray[j] = WIFIArray[i];    //给剩余数组赋值
                            }
                            hasLeft = true;
                        }
                    }
                }
            }
        }


    }

    //波形数据sum校验
    private boolean doTempCrc(int[] tempWave) {
        int dataLen = tempWave.length;
        int a;
        int sum = 0;
        for (int i = 4; i < dataLen; i++) {
            a = tempWave[i];
            sum = sum + a;
        }
        return tempWave[max + 8] == sum;

    }

    //控制命令sum校验
    private boolean doTempCrc2(int[] tempCommand) {
        int sum = tempCommand[4] + tempCommand[5] + tempCommand[6];
        return tempCommand[7] == sum;

    }

    private void getTestWaveData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" +
                "wave.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c = 0;
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");

            for (int i = 0; i < max; i++) {
                waveArray[i] = Integer.valueOf(split[i], 16);
            }
            myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                    false, 0, false, max);  //GC20181227
            myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                    false, 0, false, max);  //GC20181227
            mainWave.setAdapter(myChartAdapterMainWave);
            fullWave.setAdapter(myChartAdapterFullWave);
            //GC
            positionReal = Integer.valueOf(split[6], 16);
            mainWave.setScrubLineReal(positionReal);
            positionVirtual = Integer.valueOf(split[7], 16);
            mainWave.setScrubLineVirtual(positionVirtual);
            tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void drawWIFIData() {
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
    }

    //GC
    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
        command_1 = 0x02;
        command_2 = method;
        switch (method) {
            case 17:
                vlMethod.setText(getResources().getString(R.string.btn_tdr));
                break;
            case 34:
                vlMethod.setText(getResources().getString(R.string.btn_icm));
                break;
            case 51:
                vlMethod.setText(getResources().getString(R.string.btn_sim));
                break;
            case 68:
                vlMethod.setText(getResources().getString(R.string.btn_decay));
                break;
            default:
                break;
        }
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
        command_1 = 0x03;
        command_2 = range;
        switch (range) {
            case 0x11:
                vlRange.setText(getResources().getString(R.string.btn_500m));
                gainState = 12;
                vlGain.setText("12");
                break;
            case 0x22:
                vlRange.setText(getResources().getString(R.string.btn_1km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x33:
                vlRange.setText(getResources().getString(R.string.btn_2km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x44:
                vlRange.setText(getResources().getString(R.string.btn_4km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x55:
                vlRange.setText(getResources().getString(R.string.btn_8km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x66:
                vlRange.setText(getResources().getString(R.string.btn_16km));
                gainState = 9;
                vlGain.setText("9");
                break;
            case 0x77:
                vlRange.setText(getResources().getString(R.string.btn_32km));
                gainState = 9;
                vlGain.setText("9");
                break;
            case 0x88:
                vlRange.setText(getResources().getString(R.string.btn_64km));
                gainState = 9;
                vlGain.setText("9");
                break;
            default:
                break;
        }
    }

    public int getGain() {
        return gain;
    }

    public void setGain(int gain) {
        this.gain = gain;
        command_1 = 0x04;
        command_2 = gain;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int range) {
        this.delay = delay;
    }

    //设置增益变化
    public int getGainState() {
        return gainState;
    }

    public void setGainState(int gainState) {
        this.gainState = gainState;
        vlGain.setText(String.valueOf(gainState));
    }

    //设置波速度变化
    public int getVelocityState() {
        return velocityState;
    }

    public void setVelocityState(int velocityState) {
        this.velocityState = velocityState;
        vlVel.setText(String.valueOf(velocityState) + "m/μs");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}


/*更改记录*/
//GC20181223 光标切换
//GC20181224 监听并绘制光标位置
//GC20181225 传递方式范围状态
//GC20181227 不同方式范围点数选择
//GC20190102 命令发送处理
//GC20190103 WIFI数据流接收处理