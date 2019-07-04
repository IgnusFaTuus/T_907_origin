package net.kehui.www.t_907_origin.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import net.kehui.www.t_907_origin.adpter.MyChartAdapterBase;
import net.kehui.www.t_907_origin.thread.ConnectThread;

import java.io.BufferedReader;

/**
 * @author IF
 * @date 2019/3/26
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * sparkView波形绘制部分
     */
    public MyChartAdapterBase myChartAdapterMainWave;
    public MyChartAdapterBase myChartAdapterFullWave;

    /**
     * 波形参数
     */
    public int  mode;
    public int  range;
    public int  rangeParameter;
    public int  gain;
    public float    velocity;
    public int  density;
    public int  balance;
    public int  delay;
    public int  selectSim;

    public int  dataMax;
    public int  positionReal;
    public int  positionVirtual;
    public int  parameterDensity;
    public int  densityMax;

    public int[]    waveArray;
    public int[]    simArray1;
    public int[]    simArray2;
    public int[]    simArray3;
    public int[]    simArray4;
    public int[]    simArray5;
    public int[]    simArray6;
    public int[]    simArray7;
    public int[]    simArray8;
    public int[]    waveDraw;
    public int[]    simDraw1;
    public int[]    simDraw2;
    public int[]    simDraw3;
    public int[]    simDraw4;
    public int[]    simDraw5;
    public int[]    simDraw6;
    public int[]    simDraw7;
    public int[]    simDraw8;
    public int[]    waveCompare;
    /**
     * 不同范围和方式下，波形数据的点数、需要去掉的冗余点数、比例值
     */
    public int[]    readTdrSim   = {  540, 1052, 2076,  4124,  8220, 16412, 32796, 65556 };
    public int[]    readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };
    public int[]    removeTdrSim    = {30,32,36,44, 60, 92,156,276};
    public int[]    removeIcmDecay  = {28,36,52,84,148,276,148,276};
    public int[]    densityMaxTdrSim    = {1,2, 4, 8,16, 32,64,128};
    public int[]    densityMaxIcmDecay  = {4,8,16,32,64,128,64,128};
    /**
     * 是否比较波形的标志
     */
    public boolean  isCom;
    public boolean  isDrawSim;
    public boolean  clickMemory;
    public boolean  clickCursor;


    /**
     * WiFi连接部分
     */
    public ConnectThread  connectThread;
    public BufferedReader br;
    public static final int PORT = 9000;
    public static final int MIN_DELAY_TIME = 400;
    public static long lastClickTime;
    public boolean  isSuccessful;
    public boolean  netBoolean;
    public boolean  isFirst = true;


    /**
     * WIFI数据处理部分
     *
     * 接收到的WIFI数据数组和处理过后的剩余数据数组
     */
    public int  streamLen;
    public int  leftLen;
    public int[]    wifiStream;
    public int[]    leftArray;
    public boolean  hasLeft;


    /**
     * 魔法值定义，改善代码易读性
     *
     * APP发送部分
     */
    public final static int COMMAND_DATA_LENGTH = 0x03;
    public final static int COMMAND_TEST            = 0x01;
    public final static int COMMAND_MODE            = 0x02;
    public final static int COMMAND_RANGE           = 0x03;
    public final static int COMMAND_GAIN            = 0x04;
    public final static int COMMAND_DELAY           = 0x05;
    public final static int COMMAND_BALANCE         = 0x07;
    public final static int COMMAND_TRIGGER         = 0x08;
    public final static int COMMAND_RECEIVE_DATA    = 0x09;
    public final static int TESTING     = 0x11;
    public final static int CANCEL_TEST = 0x22;
    public final static int TDR      = 0x11;
    public final static int ICM      = 0x22;
    public final static int SIM      = 0x33;
    public final static int DECAY    = 0x44;
    public final static int RANGE_500   = 0x11;
    public final static int RANGE_1_KM  = 0x22;
    public final static int RANGE_2_KM  = 0x33;
    public final static int RANGE_4_KM  = 0x44;
    public final static int RANGE_8_KM  = 0x55;
    public final static int RANGE_16_KM = 0x66;
    public final static int RANGE_32_KM = 0x77;
    public final static int RANGE_64_KM = 0x88;
    public final static int RECEIVING_DATA  = 0x11;
    /**
     * APP接收部分
     */
    /**
     * 数据头
     */
    public final static int COMMAND = 0x55;
    public final static int WAVE_TDR_ICM_DECAY = 0x66;
    public final static int WAVE_SIM0 = 0x77;
    public final static int WAVE_SIM1 = 0x88;
    public final static int WAVE_SIM2 = 0x99;
    public final static int WAVE_SIM3 = 0xaa;
    public final static int WAVE_SIM4 = 0xbb;
    public final static int WAVE_SIM5 = 0xcc;
    public final static int WAVE_SIM6 = 0xdd;
    public final static int WAVE_SIM7 = 0xee;
    public final static int WAVE_SIM8 = 0xff;

    public final static int TRIGGERED = 0x11;
    public final static int COMMAND_RECEIVE_RIGHT = 0x33;
    public final static int COMMAND_RECEIVE_WRONG = 0x44;
    /**
     * 接收波形
     * 数据头      数据长度    传输数据    校验和
     * eb90aaxx    aabbccdd       X         xx
     *
     * 发送命令(16进制显示)
     * 数据头   数据长度  指令  传输数据  校验和
     * eb90aa55     03      01      11       15
     * eb90aa55 03 01 11 15	    测试0x11
     * eb90aa55 03 01 22 26	    取消测试0x22
     * eb90aa55 03 02 11 16		TDR低压脉冲方式
     * eb90aa55 03 02 22 27		ICM脉冲电流方式
     * eb90aa55 03 02 33 38		SIM二次脉冲方式
     * eb90aa55 03 03 11 17		范围500m
     * eb90aa55 03 03 22 28
     * eb90aa55 03 03 33 39
     * eb90aa55 03 03 44 4a
     * eb90aa55 03 03 55 5b
     * eb90aa55 03 03 66 6c
     * eb90aa55 03 03 77 7d
     * eb90aa55 03 03 88 8e		范围64km
     * eb90aa55 03 04 11 18		增益+
     * eb90aa55 03 04 22 29		增益-
     * eb90aa55 03 05 11 19		延时+
     * eb90aa55 03 05 22 2a		延时-
     * eb90aa55 03 07 11 1b  	平衡+
     * eb90aa55 03 07 22 2c		平衡-
     * eb90aa55 03 08 11 1c		//G后续添加 接收到触发信号
     * eb90aa55 03 09 11 1d		//G后续添加 接收数据命令
     * eb90aa55 03 0a 11 1e		//G后续添加 关机重连
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

    }

    private void initData() {
        dataMax = 540;
        waveArray   = new int[540];
        //sparkView需要绘制的波形数组初始化
        waveDraw = new int[510];
        simDraw1 = new int[510];
        simDraw2 = new int[510];
        simDraw3 = new int[510];
        simDraw4 = new int[510];
        simDraw5 = new int[510];
        simDraw6 = new int[510];
        simDraw7 = new int[510];
        simDraw8 = new int[510];
        waveCompare = new int[510];

        leftArray   = new int[65565];

        mode = 0x11;
        range = 0x11;
        rangeParameter = 0;
        gain = 13;
        velocity = 172;
        density = 1;
        parameterDensity = 1;
        balance = 5;
        delay = 0;
        selectSim = 1;

        positionReal = 0;
        positionVirtual = 0;
        streamLen = 0;
        leftLen = 0;

    }

    public static boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

}

/*更改记录*/
//GT 工作信息测试
//GC20181223 光标切换
//GC20181224 监听并绘制光标位置
//GC20181227 不同方式范围sparkView点数选择（旧有方式去掉）
//GC20190103 WIFI数据流接收处理(旧有手动收全方式)

//GC20190628 光标虚化和限制范围
//GC20190629 光标使用优化
//GC20190702 波形绘制准备工作
//GC20190703 记忆比较功能
//GC20190704 增益、平衡、延时命令调节
//GC20190705 fragment切换显示优化