package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.kehui.www.t_907_origin.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by IF on 2018/3/26.
 */

public class SettingFragment extends Fragment {
    @BindView(R.id.btn_select)
    Button       btnSelect;
    @BindView(R.id.btn_time_plus)
    Button       btnTimePlus;
    @BindView(R.id.btn_time_minus)
    Button       btnTimeMinus;
    @BindView(R.id.btn_zero)
    Button       btnZero;
    @BindView(R.id.btn_lang)
    Button       btnLang;
    Unbinder unbinder;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View settingLayout = inflater.inflate(R.layout.setting_layout, container, false);
        unbinder = ButterKnife.bind(this, settingLayout);
        return settingLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_select, R.id.btn_time_plus, R.id.btn_time_minus, R.id.btn_zero, R.id
            .btn_lang})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_select:
                break;
            case R.id.btn_time_plus:
                break;
            case R.id.btn_time_minus:
                break;
            case R.id.btn_zero:
                break;
            case R.id.btn_lang:
                break;

        }
    }
}
