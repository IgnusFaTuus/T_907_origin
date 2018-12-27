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
            mTempWaveArray[i] = 128;
        }
        myChartAdapterMainWave = new MyChartAdapter(mTempWaveArray, null,
                false, 0, false, max, false);  //GC20181227
        myChartAdapterFullWave = new MyChartAdapter(mTempWaveArray, null,
                false, 0, false, max, false);  //GC20181227
        mainWave.setAdapter(myChartAdapterMainWave);
        fullWave.setAdapter(myChartAdapterFullWave);

    }
    //监听光标位置
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
        //Log.e("range","" + range);
        //Log.e("method","" + method);
        switch (range){
            case 1 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[0];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[0];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 2 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[1];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[1];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 3 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[2];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[2];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 4 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[3];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[3];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 5 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[4];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[4];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 6 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[5];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[5];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 7 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[6];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[6];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            case 8 :
                if ( (method == 1) || (method == 3) ){
                    max = readTdrSim[7];
                }else if((method == 2) || (method == 4)){
                    max = readIcmDecay[7];
                }
                mTempWaveArray = new int[max];  //GC20181227
                break;
            default:
                break;
        }
        getTestWaveData();
        Log.e("clickCursor","" + clickCursor);
    }
    //GC20181223 光标切换
    private void clickCursor(){
        myChartAdapterMainWave.setCursorState(clickCursor);
        clickCursor = !clickCursor;
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
                mTempWaveArray[i] = Integer.valueOf(split[i], 16);
            }
            myChartAdapterMainWave = new MyChartAdapter(mTempWaveArray, null,
                    false, 0, false, max, false);  //GC20181227
            myChartAdapterFullWave = new MyChartAdapter(mTempWaveArray, null,
                    false, 0, false, max, false);  //GC20181227
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
    }
    public int getRange() {
        return range;
    }
    public void setRange(int range) {
        this.range = range;
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
//GC20181227 不同范围点数选择