package net.kehui.www.t_907_origin;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    @BindView(R.id.btn_mtd)
    Button       btnMtd;
    @BindView(R.id.btn_range)
    Button       btnRange;
    @BindView(R.id.btn_adj)
    Button       btnAdj;
    @BindView(R.id.btn_opt)
    Button       btnOpt;
    @BindView(R.id.btn_file)
    Button       btnFile;
    @BindView(R.id.btn_setting)
    Button       btnSetting;
    @BindView(R.id.btn_test)
    Button btnTest;
    @BindView(R.id.fct_bottom)
    LinearLayout fctBottom;
    @BindView(R.id.content)
    FrameLayout  content;
    @BindView(R.id.mainWave)
    SparkView       mainWave;
    @BindView(R.id.fullWave)
    SparkView    fullWave;
    @BindView(R.id.hintText)
    TextView     hintText;
    //用于展示Fragement
    private MethodFragment  methodFragment;
    private RangeFragment   rangeFragment;
    private AdjustFragment  adjustFragment;
    private OptionFragment  optionFragment;
    private FileFragment    fileFragment;
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

        getWaveData();
    }

    private void getWaveData() {
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

            for (int i = 0; i < split.length; i++) {
                mTempWaveArray[i] = 0;
            }
            myChartAdapterWave = new MyChartAdapter(mTempWaveArray, null,
                    false,0,false);
            mainWave.setAdapter(myChartAdapterWave);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    @OnClick({R.id.btn_mtd, R.id.btn_range, R.id.btn_adj, R.id.btn_opt, R.id.btn_file, R.id
            .btn_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            // 点击方式tab，选中第1个tab
            case R.id.btn_mtd:
                setTabSelection(0);
                btnMtd.setEnabled(false);
                btnRange.setEnabled(true);
                btnAdj.setEnabled(true);
                btnOpt.setEnabled(true);
                btnFile.setEnabled(true);
                btnSetting.setEnabled(true);
                break;
            // 点击范围tab，选中第2个tab
            case R.id.btn_range:
                setTabSelection(1);
                btnMtd.setEnabled(true);
                btnRange.setEnabled(false);
                btnAdj.setEnabled(true);
                btnOpt.setEnabled(true);
                btnFile.setEnabled(true);
                btnSetting.setEnabled(true);
                break;
            // 点击调节tab，选中第3个tab
            case R.id.btn_adj:
                setTabSelection(2);
                btnMtd.setEnabled(true);
                btnRange.setEnabled(true);
                btnAdj.setEnabled(false);
                btnOpt.setEnabled(true);
                btnFile.setEnabled(true);
                btnSetting.setEnabled(true);
                break;
            //点击操作tab，选中第4个tab
            case R.id.btn_opt:
                setTabSelection(3);
                btnMtd.setEnabled(true);
                btnRange.setEnabled(true);
                btnAdj.setEnabled(true);
                btnOpt.setEnabled(false);
                btnFile.setEnabled(true);
                btnSetting.setEnabled(true);
                break;
            // 点击文档tab，选中第5个tab
            case R.id.btn_file:
                setTabSelection(4);
                btnMtd.setEnabled(true);
                btnRange.setEnabled(true);
                btnAdj.setEnabled(true);
                btnOpt.setEnabled(true);
                btnFile.setEnabled(false);
                btnSetting.setEnabled(true);
                break;
            // 点击设置tab，选中第6个tab
            case R.id.btn_setting:
                setTabSelection(5);
                btnMtd.setEnabled(true);
                btnRange.setEnabled(true);
                btnAdj.setEnabled(true);
                btnOpt.setEnabled(true);
                btnFile.setEnabled(true);
                btnSetting.setEnabled(false);
                break;
            default:
                break;
        }
    }
}
