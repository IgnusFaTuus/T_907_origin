package net.kehui.www.t_907_origin.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.timmy.tdialog.TDialog;
import com.timmy.tdialog.base.BindViewHolder;
import com.timmy.tdialog.listener.OnBindViewListener;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.util.WifiUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * @author li.md
 * @date 19/06/27
 */
public class SplashActivity extends AppCompatActivity {

    private TDialog     tDialog;
    private ProgressBar progressBar;
    private TextView    tvProgress;
    private WifiManager WifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiUtil wifiUtil = new WifiUtil(this);
        wifiUtil.openWifi();
        wifiUtil.addNetwork(wifiUtil.createWifiInfo("T-9071", "123456789", 3));
        //隐藏状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        //进度条
        tDialog = new TDialog.Builder(getSupportFragmentManager())
                .setLayoutRes(R.layout.dialog_loading)
                .setScreenWidthAspect(this, 0.8f)
                .setCancelableOutside(false)
                .setGravity(Gravity.BOTTOM)
                .setOnBindViewListener(new OnBindViewListener() {
                    @Override
                    public void bindView(BindViewHolder viewHolder) {
                        progressBar = viewHolder.getView(R.id.progressBar);
                        tvProgress = viewHolder.getView(R.id.hardwareConnection);
                    }
                })
                .create()
                .show();

        //开启线程
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("splash-pool-%d").build();
        ExecutorService singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024), threadFactory,
                new ThreadPoolExecutor.AbortPolicy());

        singleThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //使程序休眠5秒
                    sleep(5000);
                    Intent it = new Intent(getApplicationContext(), MainActivity.class);
                    //启动MainActivity
                    startActivity(it);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        singleThreadPool.shutdown();

    }

}