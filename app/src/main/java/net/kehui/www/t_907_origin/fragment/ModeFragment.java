package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author Gong
 * @date 2019/07/04
 */
public class ModeFragment extends Fragment {
    @BindView(R.id.btn_tdr)
    Button btnTdr;
    @BindView(R.id.btn_icm)
    Button btnIcm;
    @BindView(R.id.btn_sim)
    Button btnSim;
    @BindView(R.id.btn_decay)
    Button btnDecay;
    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View modeLayout = inflater.inflate(R.layout.mtd_layout, container, false);
        unbinder = ButterKnife.bind(this, modeLayout);
        return modeLayout;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnTdr.setEnabled(false);
        btnIcm.setEnabled(true);
        btnSim.setEnabled(true);
        btnDecay.setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_tdr, R.id.btn_icm, R.id.btn_sim, R.id.btn_decay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_tdr:
                btnTdr.setEnabled(false);
                btnIcm.setEnabled(true);
                btnSim.setEnabled(true);
                btnDecay.setEnabled(true);
                ((MainActivity)getActivity()).setMode(0x11);
                break;
            case R.id.btn_icm:
                btnTdr.setEnabled(true);
                btnIcm.setEnabled(false);
                btnSim.setEnabled(true);
                btnDecay.setEnabled(true);
                ((MainActivity)getActivity()).setMode(0x22);
                break;
            case R.id.btn_sim:
                btnTdr.setEnabled(true);
                btnIcm.setEnabled(true);
                btnSim.setEnabled(false);
                btnDecay.setEnabled(true);
                ((MainActivity)getActivity()).setMode(0x33);
                break;
            case R.id.btn_decay:
                btnTdr.setEnabled(true);
                btnIcm.setEnabled(true);
                btnSim.setEnabled(true);
                btnDecay.setEnabled(false);
                ((MainActivity)getActivity()).setMode(0x44);
                break;
            default:
                break;
        }
    }

}
