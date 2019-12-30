package net.kehui.www.t_907_origin.base;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.view.WindowManager;

import net.kehui.www.t_907_origin.adpter.DataAdapter;
import net.kehui.www.t_907_origin.adpter.MyChartAdapterBase;
import net.kehui.www.t_907_origin.dao.DataDao;
import net.kehui.www.t_907_origin.global.BaseAppData;
import net.kehui.www.t_907_origin.thread.ConnectThread;

import java.io.BufferedReader;

/**
 * @author Gong
 * @date 2019/07/15
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * sparkView图形绘制部分
     */
    public MyChartAdapterBase   myChartAdapterMainWave;
    public MyChartAdapterBase   myChartAdapterFullWave;

    /**
     * 波形参数
     */
    public int  mode;
    public int  modeBefore;
    public int  range;
    public int  rangeBefore;
    public int  rangeState;
    public int  gain;
    public int  velocity;
    public int  density;
    public int  densityMax;
    public int  balance;
    public int  delay;
    public int  inductor;
    public int  selectSim;
    public boolean isCom;
    public boolean isMemory;
    public boolean isDatabase;

    /**
     * 波形原始数据数组
     */
    public int  dataMax;
    public int[] waveArray;
    public int[] simArray1;
    public int[] simArray2;
    public int[] simArray3;
    public int[] simArray4;
    public int[] simArray5;
    public int[] simArray6;
    public int[] simArray7;
    public int[] simArray8;

    /**
     * 波形绘制数据数组（抽点510个）
     */
    public int[] waveDraw;
    public int[] waveCompare;
    public int[] waveDrawFull;
    public int[] waveCompareFull;
    public int[] simDraw1;
    public int[] simDraw2;
    public int[] simDraw3;
    public int[] simDraw4;
    public int[] simDraw5;
    public int[] simDraw6;
    public int[] simDraw7;
    public int[] simDraw8;
    public int[] simDraw1Full;
    public int[] simDraw2Full;
    public int[] simDraw3Full;
    public int[] simDraw4Full;
    public int[] simDraw5Full;
    public int[] simDraw6Full;
    public int[] simDraw7Full;
    public int[] simDraw8Full;

    /**
     * 不同范围和方式下，波形数据的点数、需要去掉的冗余点数、比例值
     */
    public final static int[] READ_TDR_SIM   = {540 , 1052, 2076, 4124 , 8220 , 16412, 32796, 65556};
    public final static int[] READ_ICM_DECAY = {2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556};
    public int[] removeTdrSim       = {30, 32, 36, 44, 60 , 92 , 156, 276};
    public int[] removeIcmDecay     = {28, 36, 52, 84, 148, 276, 148, 276};
    public int[] densityMaxTdrSim   = {1, 2, 4 , 8 , 16, 32 , 64, 128};
    public int[] densityMaxIcmDecay = {4, 8, 16, 32, 64, 128, 64, 128};

    /**
     * 光标参数
     */
    public int  zero;
    public int  pointDistance;
    public int  simZero;
    public int  positionReal;
    public int  positionVirtual;
    public boolean  cursorState;

    /**
     * ICM自动测距参数
     */
    public int     gainState;
    public int     breakdownPosition;
    public int     breakBk;
    public int     faultResult;
    public float[] waveArrayFilter   = new float[65560];
    public float[] waveArrayIntegral = new float[65560];
    public float[] s1                = new float[65560];
    public float[] s2                = new float[65560];
    public int[]   minPeak           = new int[255];


    /**
     * WiFi连接部分
     */
    public              ConnectThread  connectThread;
    public              BufferedReader br;
    public static final int            PORT    = 9000;
    public              boolean        isSuccessful;
    public              boolean        netBoolean;
    public              boolean        isFirst = true;
    public              int[]          wifiStream;

    /**
     * APP下发命令——指令内容command/传输数据data
     * 示例
     * 数据头   数据长度  指令  传输数据  校验和
     * eb90aa55     03     09      11       1d      //接收数据命令
     */
    public int  command;
    public final static int COMMAND_TEST         = 0x01;
    public final static int COMMAND_MODE         = 0x02;
    public final static int COMMAND_RANGE        = 0x03;
    public final static int COMMAND_GAIN         = 0x04;
    public final static int COMMAND_DELAY        = 0x05;
    public final static int COMMAND_BALANCE      = 0x07;
    public final static int COMMAND_RECEIVE_WAVE = 0x09;
    public int  dataTransfer;
    public final static int TESTING              = 0x11;
    public final static int CANCEL_TEST          = 0x22;
    public final static int TDR                  = 0x11;
    public final static int ICM                  = 0x22;
    public final static int SIM                  = 0x33;
    public final static int DECAY                = 0x44;
    public final static int RANGE_500            = 0x11;
    public final static int RANGE_1_KM           = 0x22;
    public final static int RANGE_2_KM           = 0x33;
    public final static int RANGE_4_KM           = 0x44;
    public final static int RANGE_8_KM           = 0x55;
    public final static int RANGE_16_KM          = 0x66;
    public final static int RANGE_32_KM          = 0x77;
    public final static int RANGE_64_KM          = 0x88;

    /**
     * APP接收命令
     */
    public final static int COMMAND_TRIGGER = 0x08;
    public final static int TRIGGERED       = 0x11;
    public final static int RECEIVE_RIGHT   = 0x33;
    public final static int RECEIVE_WRONG   = 0x44;

    /**
     * APP接收波形数据头
     * 数据头      数据长度    传输数据    校验和
     * eb90aaXX    aabbccdd     X……X        xx
     */
    public final static int WAVE_TDR_ICM_DECAY    = 0x66;
    public final static int WAVE_SIM              = 0x77;


    /**
     * 数据库存储波形部分
     */
    public DataAdapter adapter;
    public DataDao     dao;
    public int         selectedId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initParameter();
    }

    /**
     * 参数初始化
     */
    private void initParameter() {
        mode    = 0x11;
        range   = 0x11;
        rangeState  = 0;
        gain        = 13;
        velocity    = 172;
        density     = 1;
        densityMax  = 1;
        balance     = 5;
        delay       = 0;
        inductor    = 3;
        //二次脉冲多组数据选择
        selectSim = 1;

        dataMax = 540;
        waveArray   = new int[540];
        waveDraw        = new int[510];
        waveDrawFull    = new int[510];
        waveCompare     = new int[510];
        waveCompareFull = new int[510];
        simDraw1 = new int[510];
        simDraw2 = new int[510];
        simDraw3 = new int[510];
        simDraw4 = new int[510];
        simDraw5 = new int[510];
        simDraw6 = new int[510];
        simDraw7 = new int[510];
        simDraw8 = new int[510];
        simDraw1Full = new int[510];
        simDraw2Full = new int[510];
        simDraw3Full = new int[510];
        simDraw4Full = new int[510];
        simDraw5Full = new int[510];
        simDraw6Full = new int[510];
        simDraw7Full = new int[510];
        simDraw8Full = new int[510];

        zero = 0;
        pointDistance = 255;
        //光标显示的位置（变化范围0-509）
        positionReal = 0;
        positionVirtual = 255;
        //默认控制虚光标
        cursorState = false;

        //增益大小状态
        gainState = 0;
        //故障击穿时刻对应的那一点
        breakdownPosition = 0;
        //击穿点
        breakBk = 0;

        BaseAppData db = Room.databaseBuilder(getApplicationContext(), BaseAppData.class, "database-wave").build();
        dao = db.dataDao();
    }
}

/*更改记录*/
//GT 工作信息测试
//GC20181223 实、虚光标切换绘制
//GC20181227 不同方式范围sparkView点数选择（旧有方式弃用）

//GC20190628 光标虚化和限制范围
//GC20190629 光标使用优化
//GC20190702 波形绘制参数准备工作
//GC20190703 记忆比较功能
//GC20190704 增益、平衡、延时命令调节
//GC20190705 fragment切换显示优化，SIM波形选择
//GC20190706 数据处理优化
//GC20190708 ICM自动测距
//GC20190709 距离计算，比例选择
//GC20191210 增加电感
//GC20190711 放大缩小
//GC20190712 光标零点设置
//GC20190713 数据库波形显示
//GC20190716 存储显示、放大缩小操作之后的bug修正