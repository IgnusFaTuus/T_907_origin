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
import net.kehui.www.t_907_origin.event.UINoticeEvent;
import net.kehui.www.t_907_origin.thread.ConnectThread;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.util.WifiUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    public int positionReal;
    public int positionVirtual; //GC20181224 光标位置
    public int method;
    public int range;
    public int gain;
    public int velocity;
    public int delay;     //GC20181225
    public int max;     //GC20181227
    public int[] readTdrSim = { 540, 1052, 2076, 4124, 8220, 16412, 32796, 65556 };
    public int[] readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };    //GC20181227 不同范围点数选择
    public int[] mTempWaveArray;
    public MyChartAdapter myChartAdapterMainWave;
    public MyChartAdapter myChartAdapterFullWave;
    public boolean clickCursor; //GC20181223 光标切换


    /*全局的handler对象用来执行UI更新*/
    public static final int SEND_COMMEND      = 1;//发送命令
    public static final int RECEIVE_COMMEND   = 2;//接受命令
    public static final int SEND_DATA         = 3;//发送数据
    public static final int RECEIVE_DATA      = 4;//接受数据
    public static final int DEVICE_CONNECTING = 5;//设备连接


    //连接线程
    private              ConnectThread            connectThread;
    //监听线程
    private              ListenerThread           listenerThread;
    private static final String WIFI_HOTSPOT_SSID = "T-907";
    //设置硬件端口 9000
    private static final int                      PORT = 9000;
    private              WifiManager              wifiManager;
    private              ArrayAdapter<ScanResult> wifiListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        /*//检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);*/

        //        开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(getWifiRouteIPAddress(BaseActivity.this),
                            PORT);
                    connectThread = new ConnectThread(socket, handler);
                    connectThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BaseActivity.this, "通信连接失败", Toast
                                    .LENGTH_LONG);
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
        range = 17;
        max = 540;
        mTempWaveArray = new int[max];
    }


    /**
     * 获取路由
     *
     * @return
     */

    private String getRouterIp() {
        //检查Wifi状态
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
        WifiInfo wi = wifiManager.getConnectionInfo();
        //获取32位整型IP地址
        int ipAdd = wi.getIpAddress();
        //把整型地址转换成“*.*.*.*”地址
        String ip = intToRouterIp(ipAdd);
        return ip;
    }

    private String intToRouterIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                1;
    }
    /**
     * wifi获取 已连接网络路由  路由ip地址
     *
     * @param context
     * @return
     */
    private static String getWifiRouteIPAddress(Context context) {
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        Log.i("route ip", "wifi route ip：" + routeIp);

        return routeIp;
    }

    private Handler           handler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EventBus.getDefault().post(new UINoticeEvent(msg.what));

        }
    };


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.w("BBB", "SCAN_RESULTS_AVAILABLE_ACTION");
                // wifi已成功扫描到可用wifi。
                List<ScanResult> scanResults = wifiManager.getScanResults();
                wifiListAdapter.clear();
                wifiListAdapter.addAll(scanResults);
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                Log.w("BBB", "WifiManager.WIFI_STATE_CHANGED_ACTION");
                int wifiState = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, 0);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        //获取到wifi开启的广播时，开始扫描
                        wifiManager.startScan();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        //wifi关闭发出的广播
                        break;
                }
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Log.w("BBB", "WifiManager.NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Toast.makeText(BaseActivity.this,
                            "连接已断开",Toast.LENGTH_LONG);
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Toast.makeText(BaseActivity.this,
                            "已连接到网络:" + wifiInfo.getSSID(),Toast.LENGTH_LONG);
                    Log.w("AAA","wifiInfo.getSSID():"+wifiInfo.getSSID()+"  WIFI_HOTSPOT_SSID:"+WIFI_HOTSPOT_SSID);
                    if (wifiInfo.getSSID().equals(WIFI_HOTSPOT_SSID)) {
                        //如果当前连接到的wifi是热点,则开启连接线程
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
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
                                }
                            }
                        }).start();
                    }
                } else {
                    NetworkInfo.DetailedState state = info.getDetailedState();
                    if (state == state.CONNECTING) {
                        Toast.makeText(BaseActivity.this,
                                "连接中...",Toast.LENGTH_LONG);
                    } else if (state == state.AUTHENTICATING) {
                        Toast.makeText(BaseActivity.this,
                                "正在验证身份信息...",Toast.LENGTH_LONG);
                    } else if (state == state.OBTAINING_IPADDR) {
                        Toast.makeText(BaseActivity.this,
                                "正在获取IP地址...",Toast.LENGTH_LONG);
                    } else if (state == state.FAILED) {
                        Toast.makeText(BaseActivity.this,
                                "连接失败",Toast.LENGTH_LONG);
                    }
                }

            }
        }
    };

    private ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
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
}
