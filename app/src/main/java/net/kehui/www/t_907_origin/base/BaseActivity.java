package net.kehui.www.t_907_origin.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import net.kehui.www.t_907_origin.adpter.MyChartAdapter;
import net.kehui.www.t_907_origin.thread.ListenerThread;
import net.kehui.www.t_907_origin.util.WifiUtil;

public class BaseActivity extends AppCompatActivity {

    public int[]          mTempWaveArray;
    public MyChartAdapter myChartAdapterMainWave;
    public MyChartAdapter myChartAdapterFullWave;
    public boolean        isDraw;

    /*全局的handler对象用来执行UI更新*/
    public static final int SEND_COMMEND      = 1;//发送命令
    public static final int RECEIVE_COMMEND   = 1;//接受命令
    public static final int SEND_DATA         = 1;//发送数据
    public static final int RECEIVE_DATA      = 1;//接受数据
    public static final int DEVICE_CONNECTING = 1;//设备连接

    //全局handler更新ui
    public Handler handler = new Handler();
    //监听线程
    private              ListenerThread listenerThread;
    //设置端口
    private static final int            PORT = 54321;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initData();

       /* WifiUtil wifiUtil = new WifiUtil(this);
        wifiUtil.openWifi();
        wifiUtil.addNetwork(wifiUtil.CreateWifiInfo("大黑", "12345678", 3));


        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();*/
    }


    private void initData() {
        mTempWaveArray = new int[540];
        isDraw = true;
    }


}
