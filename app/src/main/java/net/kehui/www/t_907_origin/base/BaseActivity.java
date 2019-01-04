package net.kehui.www.t_907_origin.base;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.WindowManager;

import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.util.WifiUtil;

public class BaseActivity extends AppCompatActivity {
    public int positionReal;
    public int positionVirtual; //GC20181224 光标位置
    public boolean clickCursor; //GC20181223 光标按钮点击状态
    public boolean clickTest;   //GC20190102 测试按钮点击状态
    public int method;
    public int range;
    public int gain;
    public int velocity;
    public int delay;     //GC20181225
    public int state;
    public int max;     //GC20181227
    public int[] readTdrSim = { 540, 1052, 2076, 4124, 8220, 16412, 32796, 65556 };
    public int[] readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };    //GC20181227 不同范围点数选择

    public int[] waveArray; //要画的波形数组
    public boolean hasSentCommand;          //command发送状态
    public boolean hasReceivedCommand;      //设备接收command状态
    public int streamLen = 0;         //接收到的WIFI数组长度
    public int[] WIFIStream;    //接收到的WIFI数组
    public int leftLen = 0;                  //剩余数据的数组长度
    public int[] leftArray = new int[102400];   //剩余数据的数组
    public boolean hasLeft;                     //处理数据后是否有剩余数据的标志
    public boolean hasReceivedWave;                     //处理数据后是否有剩余数据的标志

    public MyChartAdapter myChartAdapterMainWave;
    public MyChartAdapter myChartAdapterFullWave;

    public ConnectThread connectThread;     //连接线程
    public ListenerThread listenerThread;   //监听线程
    public static final int PORT = 9000;    //设置硬件端口 9000
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

        WifiUtil wifiUtil = new WifiUtil(this);
        wifiUtil.openWifi();
        wifiUtil.addNetwork(wifiUtil.CreateWifiInfo("T-9071", "123456789", 3));

    }

    private void initData() {
        positionReal = 0;
        positionVirtual = 0;
        method = 0x11;
        range = 0x11;
        max = 540;
        waveArray = new int[max];
    }

    //wifi获取 已连接网络路由  路由ip地址
    public static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);

        return routeIp;
    }

}
