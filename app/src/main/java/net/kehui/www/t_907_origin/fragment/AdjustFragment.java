package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
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

public class AdjustFragment extends Fragment {
    @BindView(R.id.btn_gain_plus)
    Button btnGainPlus;
    @BindView(R.id.btn_gain_minus)
    Button btnGainMinus;
    @BindView(R.id.btn_banlance_plus)
    Button btnBanlancePlus;
    @BindView(R.id.btn_banlance_minus)
    Button btnBanlanceMinus;
    @BindView(R.id.btn_vel_plus)
    Button btnVelPlus;
    @BindView(R.id.btn_vel_minus)
    Button btnVelMinus;
    Unbinder unbinder;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View adjustLayout = inflater.inflate(R.layout.adj_layout, container, false);
        unbinder = ButterKnife.bind(this, adjustLayout);
        return adjustLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_gain_plus, R.id.btn_gain_minus, R.id.btn_banlance_plus, R.id.btn_banlance_minus, R.id.btn_vel_plus, R.id.btn_vel_minus})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_gain_plus:
                break;
            case R.id.btn_gain_minus:
                break;
            case R.id.btn_banlance_plus:
                break;
            case R.id.btn_banlance_minus:
                break;
            case R.id.btn_vel_plus:
                break;
            case R.id.btn_vel_minus:
                break;
        }
    }
}
