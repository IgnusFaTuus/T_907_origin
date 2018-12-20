package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.kehui.www.t_907_origin.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by IF on 2018/3/26.
 */

public class RangeFragment extends Fragment {
    @BindView(R.id.btn_500m)
    Button       btn500m;
    @BindView(R.id.btn_1km)
    Button       btn1km;
    @BindView(R.id.btn_2km)
    Button       btn2km;
    @BindView(R.id.btn_4km)
    Button       btn4km;
    @BindView(R.id.btn_8km)
    Button       btn8km;
    @BindView(R.id.btn_16km)
    Button       btn16km;
    @BindView(R.id.btn_32km)
    Button       btn32km;
    @BindView(R.id.btn_64km)
    Button       btn64km;
    Unbinder unbinder;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rangeLayout = inflater.inflate(R.layout.range_layout, container, false);
        unbinder = ButterKnife.bind(this, rangeLayout);
        return rangeLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_500m, R.id.btn_1km, R.id.btn_2km, R.id.btn_4km, R.id.btn_8km, R.id
            .btn_16km, R.id.btn_32km, R.id.btn_64km})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_500m:
                btn500m.setEnabled(false);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView500 = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_1km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(false);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView1k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_2km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(false);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView2k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_4km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(false);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView4k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_8km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(false);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView8k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_16km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(false);
                btn32km.setEnabled(true);
                btn64km.setEnabled(true);
                TextView textView16k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_32km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(false);
                btn64km.setEnabled(true);
                TextView textView32k = getActivity().findViewById(R.id.btn_icm);
                break;
            case R.id.btn_64km:
                btn500m.setEnabled(true);
                btn1km.setEnabled(true);
                btn2km.setEnabled(true);
                btn4km.setEnabled(true);
                btn8km.setEnabled(true);
                btn16km.setEnabled(true);
                btn32km.setEnabled(true);
                btn64km.setEnabled(false);
                TextView textView64k = getActivity().findViewById(R.id.btn_icm);
                break;
                default:break;
        }
    }
}
