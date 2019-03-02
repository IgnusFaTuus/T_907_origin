package net.kehui.www.t_907_origin.base;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.WindowManager;

import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.util.WifiUtil;

import java.io.OutputStream;

public class BaseActivity extends AppCompatActivity {

    /*sparkView波形绘制*/
    public MyChartAdapter myChartAdapterMainWave;
    public MyChartAdapter myChartAdapterFullWave;
    public int            max;             //要画的波形数组个数
    public int[]          waveArray;     //要画的波形数组
    public int[]          simArray0;
    public int[]          simArray1;
    public int[]          simArray2;
    public int[]          simArray3;
    public int[]          simArray4;
    public int[] simArrayCmp;
    //public int[] readTdrSim = { 540, 1052, 2076, 4124, 8220, 16412, 32796, 65556 };
    public int[]          readTdrSim   =
            { 530, 1042, 2066, 4114, 8210, 16402, 32786, 65546 };         //GC20190104去掉末尾错误的数据
    //public int[] readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };    //不同范围点数选择
    public int[]          readIcmDecay = { 2058, 4106, 8202, 16394, 32778, 65546, 32778, 65546 };    //GC20190126去掉末尾错误的数据
    public int            positionReal;
    public int            positionVirtual; //光标位置
    public boolean        clickCursor; //光标按钮点击状态

    /*WIFI数据获取*/
    public ConnectThread connectThread;     //连接线程
    public ListenerThread listenerThread;   //监听线程
    public WifiManager wifiManager;

    public static final String WIFI_HOTSPOT_SSID = "T-907";
    public static final int PORT = 9000;    //设置硬件端口 9000
    public OutputStream wifiOutputStream;   //GC20190105 下发命令

    /*WIFI数据处理*/
    public int streamLen;                       //接收到的WIFI数组长度
    public int[] WIFIStream;                    //接收到的WIFI数组
    public int leftLen;                         //剩余数据的数组长度
    public int[] leftArray;                     //剩余数据的数组
    public boolean hasLeft;                     //处理数据后是否有剩余数据的标志
    public boolean hasSentCommand;              //发送command的状态
    public boolean hasReceivedCommand;          //接收command的状态
    public boolean isDraw;                       //是否画波形的标志
    public boolean hasReceivedData;             //是否接收到设备返回数据的标志

    /*fragment状态值传递给mainActivity*/
    public int method;
    public int range;
    public int rangeMethod;     //GC20190122
    public int gain;
    public int balance;
    public int velocity;
    public int delay;           //下发命令的数值
    public int gainState;
    public int balanceState;
    public int velocityState;   //信息栏数值变化

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WifiUtil wifiUtil = new WifiUtil(this);
        wifiUtil.openWifi();
        wifiUtil.addNetwork(wifiUtil.CreateWifiInfo("T-907", "123456789", 3));

        initData();

    }

    private void initData() {
        max = 530;
        waveArray = new int[max];
        positionReal = 0;
        positionVirtual = 0;
        streamLen = 0;
        leftLen = 0;
        leftArray = new int[65565];
        method = 0x11;
        range = 0x11;
        rangeMethod = 0;    //GC20190122
        gainState = 12;
        balanceState = 5;
        velocityState = 172;
    }

}

/*更改记录*/
//GX 助手调试屏蔽
//GT 工作信息测试
//GC20181223 光标切换
//GC20181224 监听并绘制光标位置
//GC20181225 传递方式范围状态
//GC20181227 不同方式范围点数选择
//GC20190102 命令发送处理
//GC20190103 WIFI数据流接收处理
//GC20190104 实测波形绘制修改
//GC20190105 下发命令方式修改
//GC20190110 命令响应时间调试
//GC20190122 添加脉冲电流
//GC20190126 ICM收数据校正