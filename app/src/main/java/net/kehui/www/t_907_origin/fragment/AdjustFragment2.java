package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 *
 * @author IF
 * @date 2019/3/19
 */
public class AdjustFragment2 extends Fragment {
    @BindView(R.id.btn_gain_plus2)
    Button btnGainPlus2;
    @BindView(R.id.btn_gain_minus2)
    Button btnGainMinus2;
    @BindView(R.id.btn_vel_plus2)
    Button btnVelPlus2;
    @BindView(R.id.btn_vel_minus2)
    Button btnVelMinus2;
    @BindView(R.id.btn_delay_plus)
    Button btnDelayPlus;
    @BindView(R.id.btn_delay_minus)
    Button btnDelayMinus;
    Unbinder unbinder;
    private int gain;
    private float velocity;
    private int delay;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View adjustLayout = inflater.inflate(R.layout.adj_layout2, container, false);

        unbinder = ButterKnife.bind(this, adjustLayout);
        return adjustLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();

    }

    @OnClick({R.id.btn_gain_plus2, R.id.btn_gain_minus2, R.id.btn_vel_plus2, R.id.btn_vel_minus2,
            R.id.btn_delay_plus, R.id.btn_delay_minus})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_gain_plus2:
                gain = ((MainActivity) getActivity()).getGainState();
                if (gain < 32) {
                    gain++;
                    ((MainActivity) getActivity()).setGainState(gain);
                }
                ((MainActivity) getActivity()).setGain(0x11);
                ((MainActivity) getActivity()).sendCommand();
                break;
            case R.id.btn_gain_minus2:
                gain = ((MainActivity) getActivity()).getGainState();
                if (gain > 0) {
                    gain--;
                    ((MainActivity) getActivity()).setGainState(gain);
                }
                ((MainActivity) getActivity()).setGain(0x22);
                ((MainActivity) getActivity()).sendCommand();
                break;
            case R.id.btn_delay_plus:
                break;
            case R.id.btn_delay_minus:
                break;
            case R.id.btn_vel_plus2:
                velocity = ((MainActivity) getActivity()).getVelocityState();
                if (velocity < 250) {
                    velocity++;
                    ((MainActivity) getActivity()).setVelocityState(velocity);
                }
                break;
            case R.id.btn_vel_minus2:
                velocity = ((MainActivity) getActivity()).getVelocityState();
                if (velocity > 0) {
                    velocity--;
                    ((MainActivity) getActivity()).setVelocityState(velocity);
                }
                break;
                default:
                    break;
        }
    }
}


