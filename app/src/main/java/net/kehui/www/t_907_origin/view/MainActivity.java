package net.kehui.www.t_907_origin.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.base.BaseActivity;
import net.kehui.www.t_907_origin.ui.SparkView.SparkView;
import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.fragment.AdjustFragment;
import net.kehui.www.t_907_origin.fragment.FileFragment;
import net.kehui.www.t_907_origin.fragment.MethodFragment;
import net.kehui.www.t_907_origin.fragment.OptionFragment;
import net.kehui.www.t_907_origin.fragment.RangeFragment;
import net.kehui.www.t_907_origin.fragment.SettingFragment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    //用于展示Fragment
    private MethodFragment methodFragment;
    private RangeFragment rangeFragment;
    private AdjustFragment adjustFragment;
    private OptionFragment optionFragment;
    private FileFragment fileFragment;
    private SettingFragment settingFragment;
    private FragmentManager fragmentManager;

    private int command_1;
    private int command_2;

    public boolean hasSendMessage;      //GN 控制命令是否发送成功的标志
    public byte[]  tempRequest            = new byte[8];


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
        initWaveData();
        setChartListener(); //GC20181224 监听光标位置

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
                if(clickCursor){
                    positionReal = (int) value;
                    //Log.e("positionReal","" + positionReal);
                }else {
                    positionVirtual = (int) value;
                    //Log.e("positionVirtual","" + positionVirtual);
                }
                tvDistance.setText(Math.abs(positionVirtual - positionReal) + "m");
                Log.e("VALUE","" + value); //GN 数值从0开始计数
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

    private void clickTest() {
        /*Log.e("range","" + range);
        Log.e("method","" + method);
        Log.e("command_1", "" + command_1);
        Log.e("command_2", "" + command_2);*/
        switch (range){
            case 0x11 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[0];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[0];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x22 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[1];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[1];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x33 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[2];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[2];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x44 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[3];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[3];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x55 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[4];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[4];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x66 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[5];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[5];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case 0x77 :
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[6];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[6];
                }
                waveArray = new int[max];  //GC20181227
                break;
            case (byte) 0x88:
                if ( (method == 17) || (method == 51) ){
                    max = readTdrSim[7];
                }else if((method == 34) || (method == 68)){
                    max = readIcmDecay[7];
                }
                waveArray = new int[max];  //GC20181227
                break;
            default:
                break;
        }
        //getTestWaveData();
        Log.e("clickCursor","" + clickCursor);
        int a = connectThread.getWIFIData().length;
        max = a;
        byte[] wifi = connectThread.getWIFIData();
        for(int i = 0; i < a ;i++){
            waveArray[i] = wifi[i] & 0xff;
        }
        Log.e("a","" + a);
        drawWIFIData();
        /*command_1 = 0x01;
        command_2 = 0x11;
        sendCommand();
        receiveCommand();
        if(commandState){
            command_1 = 0x09;
            command_2 = 0x11;
            sendCommand();
            receiveCommand();
            if(commandState){
                receiveWave();
            }
        }*/
    }

    //GC20181223 光标切换
    private void clickCursor(){
        clickCursor = myChartAdapterMainWave.getCursorState();
        clickCursor = !clickCursor;
        myChartAdapterMainWave.setCursorState(clickCursor);
    }

/*
//G       数据头   数据长度  指令  传输数据  校验和
//G     eb90aa55     03      01      11       15

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
*/
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

    public void receiveCommand(){
        getCommandStream();
        System.arraycopy(WIFIStream, 0, commandArray, 0, len);
        if(commandLength < 8){
            getCommandStream();
            for(int i = 0, j = commandLength; i < len; i++, j++){
                commandArray[j] = WIFIStream[i];
            }
        }else{
            commandLength = 0;
            if(commandArray[6] == 0x33){
                commandState = true;
            }else if(commandArray[6] == 0x44){
                commandState = false;
            }
        }

    }

    public void getCommandStream(){
        len = connectThread.getWIFIData().length;
        byte[] wifi = connectThread.getWIFIData();
        for(int i = 0; i < len ;i++){
            WIFIStream[i] = wifi[i] & 0xff;
        }
        commandLength += len;
    }

    public void receiveWave(){
        getWaveStream();
        System.arraycopy(WIFIStream, 0, waveArray, 0, len);
        if(waveLength < max + 9){
            getWaveStream();
            for(int i = 0, j = commandLength; i < len; i++, j++){
                waveArray[j] = WIFIStream[i];
            }
        }else{
            drawWIFIData();
        }
    }

    public void getWaveStream(){
        len = connectThread.getWIFIData().length;
        byte[] wifi = connectThread.getWIFIData();
        for(int i = 0; i < len ;i++){
            WIFIStream[i] = wifi[i] & 0xff;
        }
        waveLength += len;
    }

    private void drawWIFIData() {
        myChartAdapterMainWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(waveArray, null,
                false, 0, false, max);  //GC20181227
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);
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

    //GC
    public int getMethod() {
        return method;
    }
    public void setMethod(int method) {
        this.method = method;
        command_1 = 0x02;
        command_2 = method;
    }
    public int getRange() {
        return range;
    }
    public void setRange(int range) {
        this.range = range;
        command_1 = 0x03;
        command_2 = range;
    }
    public int getGain() {
        return gain;
    }
    public void setGain(int range) {
        this.gain = gain;
    }
    public int getVelocity() {
        return velocity;
    }
    public void setVelocity(int range) {
        this.velocity = velocity;
    }
    public int getDelay() {
        return delay;
    }
    public void setDelay(int range) {
        this.delay = delay;
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