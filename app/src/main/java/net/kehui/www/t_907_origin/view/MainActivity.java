package net.kehui.www.t_907_origin.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnViewClickListener;

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
import net.kehui.www.t_907_origin.util.FileUtils;

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
    FrameLayout content;
    @BindView(R.id.mainWave)
    SparkView mainWave;
    @BindView(R.id.textView)
    TextView textView;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.fullWave)
    SparkView fullWave;
    @BindView(R.id.btn_mtd)
    Button btnMtd;
    @BindView(R.id.btn_range)
    Button btnRange;
    @BindView(R.id.btn_adj)
    Button btnAdj;
    @BindView(R.id.btn_opt)
    Button btnOpt;
    @BindView(R.id.btn_file)
    Button btnFile;
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.btn_cursor)
    Button btnCursor;
    @BindView(R.id.tv_method)
    TextView tvMethod;
    @BindView(R.id.tv_gain)
    TextView tvGain;
    @BindView(R.id.tv_vel)
    TextView tvVel;
    @BindView(R.id.tv_range)
    TextView tvRange;
    @BindView(R.id.vl_method)
    TextView vlMethod;
    @BindView(R.id.vl_gain)
    TextView vlGain;
    @BindView(R.id.vl_vel)
    TextView vlVel;
    @BindView(R.id.vl_range)
    TextView vlRange;
    @BindView(R.id.value_list)
    LinearLayout valueList;
    @BindView(R.id.stateList)
    LinearLayout stateList;
    @BindView(R.id.wave_display)
    LinearLayout waveDisplay;

    @BindView(R.id.tv_balance)
    TextView tvBalance;
    @BindView(R.id.vl_balance)
    TextView vlBalance;
    //用于展示Fragment
    private MethodFragment methodFragment;
    private RangeFragment rangeFragment;
    private AdjustFragment adjustFragment;
    private OptionFragment optionFragment;
    private FileFragment fileFragment;
    private SettingFragment settingFragment;
    private FragmentManager fragmentManager;
    /*下发command*/
    private int command;
    private int data;
    private TDialog tDialog;
    /*全局的handler对象用来执行UI更新*/
    public static final int DEVICE_CONNECTING = 1;  //设备连接中
    public static final int DEVICE_CONNECTED = 2;   //设备连接成功
    public static final int SEND_SUCCESS = 3;       //发送command成功
    public static final int SEND_ERROR = 4;         //发送command失败
    public static final int GET_STREAM = 5;         //接收WIFI数据流
    public static final int RECEIVE_SUCCESS = 6;   //T-907接收command成功
    public static final int RECEIVE_ERROR = 7;     //T-907接收command失败
    public static final int RESPOND_TIME = 9;      //GC20190110 命令响应结束
    public static final int CLICK_TEST = 10;       //GC20190110 点击测试按钮事件

    public Handler handler = new Handler(new Handler.Callback()  {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == DEVICE_CONNECTING){
                connectThread = new ConnectThread(listenerThread.getSocket(), handler);
                connectThread.start();

            }else if(msg.what == DEVICE_CONNECTED){
                Toast.makeText(MainActivity.this, "正在连接T-907......", Toast.LENGTH_LONG).show();
                command = 0x02;
                data = 0x11;
                sendCommand();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        command = 0x03;
                        data = 0x11;
                        sendCommand();
                    }
                }, 50);    //发送初始化命令：方式、范围
                handler.postDelayed(new Runnable() {    //GC20190110
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(CLICK_TEST);
                    }
                }, 1200);

            }else if(msg.what == GET_STREAM){
                if(!hasReceivedData){
                    Toast.makeText(MainActivity.this, "T-907连接成功！", Toast.LENGTH_LONG).show();
                    hasReceivedData = true;
                }
                WIFIStream = msg.getData().getIntArray("STM");//GC20190103 接收WIFI数据流
                assert WIFIStream != null;
                streamLen = WIFIStream.length;
                Log.e("AAA", "streamLen：" + streamLen); //GT
                Log.e("hasLeft", "" + hasLeft);     //GT
                Log.e("max", "" + max); //GT
                doWIFIArray(WIFIStream, streamLen);
                /*max = streamLen;
                System.arraycopy(WIFIStream, 0, waveArray, 0, streamLen);
                drawWIFIData();*/

            }else if(msg.what == RESPOND_TIME){ //GC20190110
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnTest.setClickable(true);
                    }
                }, 1200);

            }else if(msg.what == CLICK_TEST){
                clickTest();

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
        setChartListener();
        startThread();
    }

    //初始化界面框架
    public void initFrame(){
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
        vlVel.setText(String.valueOf(getVelocityState()) + "m/μs");
        vlBalance.setText(String.valueOf(getBalanceState()));
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

    //初始化sparkView
    public void initSparkView() {
        for (int i = 0; i < max; i++) {
            waveArray[i] = 128;
        }
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
        Log.e("isDraw", "结束");  //GT
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple ));   //GT 初始化光标按钮颜色

    }

    //监听光标位置    //BUG1
    private void setChartListener() {
        mainWave.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {  //GC20181224
                if (clickCursor) {
                    positionReal = (int) value;
                } else {
                    positionVirtual = (int) value;
                }
                tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
                //Log.e("VALUE", "" + value); //GN 数值从0开始计数
            }
        });
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
                    wifiOutputStream = socket.getOutputStream();   //GC20190105 获取WIFI输出流

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "通信失败，请检查网络后重试", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
        //开启监听线程
        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();
    }

    //wifi获取 已连接网络路由  路由ip地址
    public static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);

        return routeIp;

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
        Log.e("isDraw", "开始");  //GT
        if (method == 0x11) {
            isDraw = false;
            command = 0x01;
            data = 0x11;
            sendCommand();  //测试
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    command = 0x09;
                    data = 0x11;
                    sendCommand(); //接收数据
                }
            }, 50);
            tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.receiving_data)
                    .setScreenWidthAspect(this,0.25f)
                    .setCancelableOutside(false)
                    .create()
                    .show();  //GX

        } else if ( (method == 0x22) || (method == 0x33) || (method == 0x44) ) {
            command = 0x01;
            data = 0x11;   //测试
            sendCommand();
            tDialog = new TDialog.Builder(getSupportFragmentManager())
                    .setLayoutRes(R.layout.wait_trigger)
                    .setScreenWidthAspect(this,0.3f)
                    .setCancelableOutside(false)
                    .addOnClickListener(R.id.tv_cancel)
                    .setOnViewClickListener(new OnViewClickListener() {
                        @Override
                        public void onViewClick(BindViewHolder viewHolder, View view, TDialog tDialog) {
                            tDialog.dismiss();
                            command = 0x01;
                            data = 0x22;   //取消测试
                            sendCommand();
                        }
                    })
                    .create()
                    .show();
        }
    }

    //点击光标按钮事件
    private void clickCursor() {
        clickCursor = myChartAdapterMainWave.getCursorState();
        clickCursor = !clickCursor;
        if(clickCursor){
            btnCursor.setTextColor(getResources().getColor(R.color.T_red));
        }else{
            btnCursor.setTextColor(getResources().getColor(R.color.T_purple));
        }
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
        request[0] = (byte) 0xeb;
        request[1] = (byte) 0x90;
        request[2] = (byte) 0xaa;
        request[3] = (byte) 0x55;
        request[4] = (byte) 0x03;
        request[5] = (byte) command;
        request[6] = (byte) data;
        int sum = request[4] + request[5] + request[6];
        request[7] = (byte) sum;
        //GC20190105 connectThread.sendCommand(request);
        sendCommand(request);
    }

    //GC20190105 下发命令
    public void sendCommand(byte[] request) {
        if (!hasSentCommand) {
            /*for (int i = 0; i < request.length; i++) {
                tempRequest[i] = request[i];
            }
            if (mOutputStream == null) {
                Toast.makeText(this, getResources().getString(R.string.Bluetooth_is_not_connected),
                        Toast.LENGTH_SHORT).show();
            }*/
            try {
                wifiOutputStream.write(request);
                //GX hasSentCommand = true;
                Log.e("AAA", "指令：" + command + "数据：" + data);    //GT
            } catch (IOException e) {
                Toast.makeText(this, "命令下发失败" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //GC20190103 处理接收到的WIFI数据
    private void doWIFIArray(int[] WIFIArray, int length) {
        int[] tempCommand = new int[8]; //command临时数组
        //GN收到设备返回命令
        if (length == 8) {
            if (WIFIArray[0] == 0xeb) {
                boolean isCrc2 = doTempCrc2(WIFIArray);
                if (isCrc2) {    //命令sum校验成功
                    hasSentCommand = false;
//                    handler.sendEmptyMessage(RESPOND_TIME); //GC20190110
                    if (WIFIArray[5] == 0x08){  //GC20190122 接收到触发信号
                        command = 0x09;
                        data = 0x11;
                        sendCommand(); //接收数据
                        if (tDialog != null){
                            tDialog.dismiss();
                        }
                        tDialog = new TDialog.Builder(getSupportFragmentManager())
                                .setLayoutRes(R.layout.receiving_data)
                                .setScreenWidthAspect(this,0.25f)
                                .setCancelableOutside(false)
                                .create()
                                .show();
                    }
                }
            }
        }else {     //GN收到设备返回的波形数据（脉冲电流会夹杂命令）
            if(hasLeft){    //数组长度不够wave,拼接处理
                for (int i = leftLen, j = 0; j < length; i++, j++) {
                    leftArray[i] = WIFIArray[j];    //与剩余数据进行拼接
                }
                leftLen = leftLen + length;
                if(leftLen == (max + 9 + 10)){
                    for (int i = 8, j = 0; i < leftLen - 1 - 10; i++, j++) {
                        waveArray[j] = leftArray[i];    //取wave长度的数组
                    }
                    hasLeft = false;
                    leftLen = 0;
                    drawWIFIData();
                }

            }else{
                //GC20190126
                System.arraycopy(WIFIArray, 0, tempCommand, 0, 8);  //取command长度的数组
                boolean isCrc2 = doTempCrc2(tempCommand);
                if (isCrc2) {    //sum校验成功，包含command
                    if (length == (max + 9 + 10 + 8)) {
                        for (int i = 16, j = 0; i < length - 1 - 10; i++, j++) {    //GC20190126 去掉command
                            waveArray[j] = WIFIArray[i];    //取wave长度的数组
                        }
                        drawWIFIData();

                    } else {  //数组长度不够wave,准备拼接处理
                        for (int i = leftLen, j = 8; j < length; i++, j++) {    //GC20190126 去掉command
                            leftArray[i] = WIFIArray[j];
                        }
                        hasLeft = true;
                        leftLen = leftLen + length - 8;
                    }
                }else{
                    if (length == (max + 9 + 10)) {  //GC20190104 波形数据去掉末尾10个点
                        for (int i = 8, j = 0; i < length - 1 - 10; i++, j++) {
                            waveArray[j] = WIFIArray[i];    //取wave长度的数组
                        }
                        drawWIFIData();

                    } else {  //数组长度不够wave,准备拼接处理
                        for (int i = leftLen, j = 0; j < length; i++, j++) {
                            leftArray[i] = WIFIArray[j];
                        }
                        hasLeft = true;
                        leftLen = leftLen + length;
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

    private void drawWIFIData() {
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
        Log.e("isDraw", "结束");  //GT
        if (tDialog != null){
            tDialog.dismiss();
        }
        //画光标
        positionReal = 0;
        mainWave.setScrubLineReal(positionReal);
        positionVirtual = max / 2;
        mainWave.setScrubLineVirtual(positionVirtual);
        tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple)); //20190104 初始化光标按钮颜色
    }

    public int getMethod() {
        return method;
    }

    public void setMethod(int method) {
        this.method = method;
        command = 0x02;
        data = method;
        switch (method) {
            case 17:
                vlMethod.setText(getResources().getString(R.string.btn_tdr));
                tvBalance.setVisibility(View.VISIBLE);
                vlBalance.setVisibility(View.VISIBLE);
                max = readTdrSim[rangeMethod];
                waveArray = new int[max];   //GC20190122
                break;
            case 0x22:
                vlMethod.setText(getResources().getString(R.string.btn_icm));
                tvBalance.setVisibility(View.INVISIBLE);
                vlBalance.setVisibility(View.INVISIBLE);
                max = readIcmDecay[rangeMethod];
                waveArray = new int[max];
                break;
            case 0x33:
                vlMethod.setText(getResources().getString(R.string.btn_sim));
                tvBalance.setVisibility(View.INVISIBLE);
                vlBalance.setVisibility(View.INVISIBLE);
                max = readTdrSim[rangeMethod];
                waveArray = new int[max];
                break;
            case 0x44:
                vlMethod.setText(getResources().getString(R.string.btn_decay));
                tvBalance.setVisibility(View.INVISIBLE);
                vlBalance.setVisibility(View.INVISIBLE);
                max = readIcmDecay[rangeMethod];
                waveArray = new int[max];
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
        command = 0x03;
        data = range;
//        btnTest.setClickable(false);    //GC20190110
        switch (range) {
            case 0x11:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[0];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[0];
                }
                rangeMethod = 0;
                waveArray = new int[max];  //GC20181227
                vlRange.setText(getResources().getString(R.string.btn_500m));
                gainState = 12;
                vlGain.setText("12");
                break;
            case 0x22:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[1];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[1];
                }
                rangeMethod = 1;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_1km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x33:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[2];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[2];
                }
                rangeMethod = 2;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_2km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x44:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[3];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[3];
                }
                rangeMethod = 3;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_4km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x55:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[4];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[4];
                }
                rangeMethod = 4;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_8km));
                gainState = 10;
                vlGain.setText("10");
                break;
            case 0x66:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[5];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[5];
                }
                rangeMethod = 5;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_16km));
                gainState = 9;
                vlGain.setText("9");
                break;
            case 0x77:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[6];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[6];
                }
                rangeMethod = 6;
                waveArray = new int[max];
                vlRange.setText(getResources().getString(R.string.btn_32km));
                gainState = 9;
                vlGain.setText("9");
                break;
            case 0x88:
                if ((method == 0x11) || (method == 0x33)) {
                    max = readTdrSim[7];
                } else if ((method == 0x22) || (method == 0x44)) {
                    max = readIcmDecay[7];
                }
                rangeMethod = 7;
                waveArray = new int[max];
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
        command = 0x04;
        data = gain;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
        command = 0x07;
        data = balance;
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

    public void setDelay(int delay) {
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

    //设置平衡变化
    public int getBalanceState() {
        return balanceState;
    }

    public void setBalanceState(int balanceState) {
        this.balanceState = balanceState;
        vlBalance.setText(String.valueOf(balanceState));
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

    //GT 测试绘制效果
    public void testWaveData() {
        for (int i = 0; i < max; i++) {
            waveArray[i] = 12;
        }
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
        Log.e("isDraw", "结束");  //GT
        positionReal = 0;
        //positionReal = Integer.valueOf(split[6], 16);
        mainWave.setScrubLineReal(positionReal);
        positionVirtual = max / 2;
        mainWave.setScrubLineVirtual(positionVirtual);
        tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
        btnCursor.setTextColor(getResources().getColor(R.color.T_purple)); //初始化光标按钮颜色
    }

    //GT 测试读文本绘制效果
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

            for (int i = 0; i < max; i++) {
                waveArray[i] = Integer.valueOf(split[i], 16);
            }
            myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                    false, 0, false, max);  //GC20181227
            myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                    false, 0, false, max);
            mainWave.setAdapter(myChartAdapterMainWave);
            fullWave.setAdapter(myChartAdapterFullWave);
            Log.e("isDraw", "结束");  //GT
            positionReal = 0;
            //positionReal = Integer.valueOf(split[6], 16);
            mainWave.setScrubLineReal(positionReal);
            positionVirtual = max / 2;
            mainWave.setScrubLineVirtual(positionVirtual);
            tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
            btnCursor.setTextColor(getResources().getColor(R.color.T_purple)); //初始化光标按钮颜色

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}