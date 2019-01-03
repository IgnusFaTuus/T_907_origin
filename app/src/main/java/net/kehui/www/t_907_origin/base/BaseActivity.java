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
import java.io.OutputStream;
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
    public int state;
    public int max;     //GC20181227
    public int[] readTdrSim = { 540, 1052, 2076, 4124, 8220, 16412, 32796, 65556 };
    public int[] readIcmDecay = { 2068, 4116, 8212, 16404, 32788, 65556, 32788, 65556 };    //GC20181227 不同范围点数选择

    public int[] WIFIStream;    //传过来的WIFI数组
    public int[] waveArray;
    public int[] commandArray;
    public int len = 0;         //传过来的WIFI数组长度
    public int waveLength = 0;
    public int commandLength = 0;
    public boolean sendCommand; //命令接收状态
    public boolean receiveCommand; //命令接收状态

    public InputStream mInputStream;
    public OutputStream mOutputStream;
    public int length = 0;                     //GN 进行处理的WIFI数据数组长度
    public int[] WIFIArray = new int[1024];   //GN 进行处理的WIFI数据数组
    public boolean getWIFIDataThread;    //GN 获取蓝牙数据线程是否启动的标志
    public boolean handleWIFIDataThread;        //处理WIFI数据线程的状态
    public boolean commandState; //命令接收状态

    public boolean hasLeft;     //处理数据后是否有剩余数据的标志
    public int hasLeftLen = 0;  //处理数据后是否有剩余数据的个数
    public int[] mTempLeft = new int[1024];    //GN 剩余数据时的临时数组

    public byte[] tempRequest = new byte[8];

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
                    /*mInputStream = socket.getInputStream();     //GC 获取WIFI输入流
                    mOutputStream = socket.getOutputStream();   //GC 获取WIFI输出流*/

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
        wifiUtil.addNetwork(wifiUtil.CreateWifiInfo("T-9071", "123456789", 3));

        /*listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();*/

        getWIFIData.start();   //GN 处理蓝牙数据的线程
        doWIFIData.start();   //GN 处理蓝牙数据的线程

    }

    private void initData() {
        positionReal = 0;
        positionVirtual = 0;
        method = 17;
        range = 0x11;
        max = 540;
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

    //获取WIFI数据的线程
    Thread getWIFIData = new Thread(new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] WIFIStream = new byte[1024];    //GN 存放每个输入流的字节数组
            if (mInputStream == null) {
                Log.e("打印-mInputStream", "null");
                return;
            }
            try {
                len = mInputStream.read(WIFIStream, 0, WIFIStream.length);
                Log.e("stream", "len:" + len + "时间" + System.currentTimeMillis());     //GT20180321 每个输入流的的长度
                byte[] data = new byte[len];
                System.arraycopy(WIFIStream, 0, data, 0, len);
                //GC 在没有处理WIFI数据时缓存输入流用做后续处理
                if ( !handleWIFIDataThread ) {
                    for (int i = 0; i < len; i++) {
                        WIFIArray[i] = data[i] & 0xff;   //将传过来的字节数组转变为int数组
                    }
                    length = len;
                    handleWIFIDataThread = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    });

    //GN 处理WIFI数据的线程
    Thread doWIFIData = new Thread(new Runnable() {
        @Override
        public void run() {
            if (handleWIFIDataThread) {
                doCommand(WIFIArray, length);
                handleWIFIDataThread = false;
            }
        }

    });

    //GN 处理Command
    private void doCommand(int[] WIFIArray, int length) {
        int[] tempCommand = new int[8];      //控制命令临时数组
        System.arraycopy(WIFIArray, 0, tempCommand, 0, length);

        if(commandLength == 8){
            for(int i = 0, j = 0; i < commandLength; i++, j++){
                commandArray[j] = tempCommand[i];
            }
            commandLength = 0;
            Log.e("GGG","" + commandLength);

        }else{
            for(int i = 0, j = commandLength; i < length; i++, j++){
                tempCommand[j] = WIFIArray[i];
            }
            Log.e("GGG","" + commandLength);
        }
        commandLength += length;


    }

    //控制命令发送
    public void sendCommand(byte[] request) {
        if (!commandState) {
            for (int i = 0; i < request.length; i++) {
                tempRequest[i] = request[i];
            }
            /*if (mOutputStream == null) {
                Toast.makeText(this, getResources().getString(R.string.Bluetooth_is_not_connected),
                        Toast.LENGTH_SHORT).show();
            }*/
            try {
                mOutputStream.write(request);
                //commandState = true;
            } catch (IOException e) {
                //Toast.makeText(this, "发送失败" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(MainActivity.this, "还没有收到来自设备端的回复", Toast.LENGTH_SHORT).show();
        }

    }

}
