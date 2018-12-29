package net.kehui.www.t_907_origin.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.application.MyApplication;
import net.kehui.www.t_907_origin.event.UINoticeEvent;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.util.WifiUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    public int positionReal;
    public int positionVirtual; //GC20181224 光标位置
    public int method;
    public int range;
    public int gain;
    public int change;
    public int velocity;
    public int delay;     //GC20181225
    public int max;     //GC20181227
    public int[] readTdrSim = { 540, 1052, 2076, 4124, 8220, 16412, 32796, 65556 };
    public int[] readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };    //GC20181227 不同范围点数选择

    public int[] WIFIStream;    //传过来的WIFI数组
    public int[] waveArray;
    public int[] commandArray;
    public int len = 0;         //传过来的WIFI数组长度
    public int waveLength = 0;
    public int commandLength = 0;
    public boolean commandState; //命令接收状态

    public MyChartAdapter myChartAdapterMainWave;
    public MyChartAdapter myChartAdapterFullWave;
    public boolean clickCursor; //GC20181223 光标切换
    public Socket      mSocket;

    /*全局的handler对象用来执行UI更新*/
    public static final int SEND_COMMEND      = 1;//发送命令
    public static final int RECEIVE_COMMEND   = 2;//接受命令
    public static final int SEND_DATA         = 3;//发送数据
    public static final int RECEIVE_DATA      = 4;//接受数据
    public static final int DEVICE_CONNECTING = 5;//设备连接

    //连接线程
    public               ConnectThread            connectThread;
    //监听线程
    private              ListenerThread           listenerThread;
    //设置硬件端口 9000
    private static final int                      PORT = 9000;
    private              WifiManager              wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

        //开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getWifiRouteIPAddress(BaseActivity.this), PORT);
                    connectThread = new ConnectThread(socket, handler);
                    connectThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BaseActivity.this, "通信连接失败", Toast.LENGTH_LONG);
                        }
                    });

                }
            }
        }).start();

        WifiUtil wifiUtil = new WifiUtil(this);
        wifiUtil.openWifi();
        wifiUtil.addNetwork(wifiUtil.CreateWifiInfo("T-907", "123456789", 3));

        /*listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();*/

    }

    private void initData() {
        positionReal = 0;
        positionVirtual = 0;
        method = 17;
        range = 0x11;
        max = 540;
        //MyApplication.getInstances().set_socket(mSocket);
        //mSocket = MyApplication.getInstances().get_socket();
        waveArray = new int[max];
        clickCursor = false;
    }

    private Handler handler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EventBus.getDefault().post(new UINoticeEvent(msg.what));

        }
    };

    //wifi获取 已连接网络路由  路由ip地址
    private static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);

        return routeIp;
    }

}
