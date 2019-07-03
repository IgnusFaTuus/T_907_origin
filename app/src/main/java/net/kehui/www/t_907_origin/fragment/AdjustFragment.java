package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static net.kehui.www.t_907_origin.base.BaseActivity.isFastClick;

/**
 * @author Gong
 * @date 2019/07/03
 */

public class AdjustFragment extends Fragment {
    @BindView(R.id.btn_gain_plus)
    Button btnGainPlus;
    @BindView(R.id.btn_gain_minus)
    Button btnGainMinus;
    @BindView(R.id.btn_balance_plus)
    Button btnBalancePlus;
    @BindView(R.id.btn_balance_minus)
    Button btnBalanceMinus;
    @BindView(R.id.btn_vel_plus)
    Button btnVelPlus;
    @BindView(R.id.btn_vel_minus)
    Button btnVelMinus;
    Unbinder unbinder;
    @BindView(R.id.adj_sidebar)
    LinearLayout adjSidebar;

    private Handler handler;

    @Override
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

    @OnClick({R.id.btn_gain_plus, R.id.btn_gain_minus, R.id.btn_balance_plus,
            R.id.btn_balance_minus, R.id.btn_vel_plus, R.id.btn_vel_minus})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.btn_gain_plus:
                int gain = ((MainActivity) getActivity()).getGainState();
                if (gain == 31) {
                    btnGainPlus.setEnabled(false);
                }
                if (gain < 32) {
                    gain++;
                    ((MainActivity) getActivity()).setGainState(gain);
                    ((MainActivity) getActivity()).setGain(0x11);
                    btnGainMinus.setEnabled(true);
                }

                break;
            case R.id.btn_gain_minus:
                gain = ((MainActivity) getActivity()).getGainState();
                if (gain == 1) {
                    btnGainMinus.setEnabled(false);
                }
                if (gain > 0) {
                    gain--;
                    ((MainActivity) getActivity()).setGainState(gain);
                    ((MainActivity) getActivity()).setGain(0x22);
                    btnGainPlus.setEnabled(true);
                }
                break;
            case R.id.btn_balance_plus:
                int balance = ((MainActivity) getActivity()).getBalanceState();
                if (balance == 15) {
                    btnBalancePlus.setEnabled(false);
                }
                if (balance < 16) {
                    balance++;
                    ((MainActivity) getActivity()).setBalanceState(balance);
                    ((MainActivity) getActivity()).setBalance(0x11);
                    btnBalanceMinus.setEnabled(true);
                }
                break;
            case R.id.btn_balance_minus:
                balance = ((MainActivity) getActivity()).getBalanceState();
                if (balance == 1) {
                    btnBalanceMinus.setEnabled(false);
                }
                if (balance > 0) {
                    balance--;
                    ((MainActivity) getActivity()).setBalanceState(balance);
                    ((MainActivity) getActivity()).setBalance(0x22);
                    btnBalancePlus.setEnabled(true);
                }
                break;
            case R.id.btn_vel_plus:
                float velocity = ((MainActivity) getActivity()).getVelocityState();
                if (velocity < 250) {
                    velocity++;
                    ((MainActivity) getActivity()).setVelocityState(velocity);
                }
                break;
            case R.id.btn_vel_minus:
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
