package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

/**
 * Created by IF on 2018/3/26.
 */

public class MethodFragment extends Fragment implements OnClickListener {
    private Button       btn_tdr;
    private Button       btn_icm;
    private Button       btn_sim;
    private Button       btn_decay;
    private Button       btn_wave1;
    private Button       btn_wave2;
    private Button       btn_wave3;
    private Button       btn_wave4;
    private LinearLayout wave_select;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View methodLayout = inflater.inflate(R.layout.mtd_layout, container, false);
        return methodLayout;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initButton();

        btn_tdr.setEnabled(false);
        btn_icm.setEnabled(true);
        btn_sim.setEnabled(true);
        btn_decay.setEnabled(true);
        btn_wave1.setEnabled(false);
        btn_wave2.setEnabled(true);
        btn_wave3.setEnabled(true);
        btn_wave4.setEnabled(true);

    }

    private void initButton() {
        btn_tdr = getActivity().findViewById(R.id.btn_tdr);
        btn_icm = getActivity().findViewById(R.id.btn_icm);
        btn_sim = getActivity().findViewById(R.id.btn_sim);
        btn_decay = getActivity().findViewById(R.id.btn_decay);
        btn_wave1 = getActivity().findViewById(R.id.btn_wave1);
        btn_wave2 = getActivity().findViewById(R.id.btn_wave2);
        btn_wave3 = getActivity().findViewById(R.id.btn_wave3);
        btn_wave4 = getActivity().findViewById(R.id.btn_wave4);
        wave_select = getActivity().findViewById(R.id.wave_select);
        btn_tdr.setOnClickListener(this);
        btn_icm.setOnClickListener(this);
        btn_sim.setOnClickListener(this);
        btn_decay.setOnClickListener(this);
        btn_wave1.setOnClickListener(this);
        btn_wave2.setOnClickListener(this);
        btn_wave3.setOnClickListener(this);
        btn_wave4.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_tdr:
                btn_tdr.setEnabled(false);
                btn_icm.setEnabled(true);
                btn_sim.setEnabled(true);
                btn_decay.setEnabled(true);
                ((MainActivity)getActivity()).setMethod(0x11); //GC20181225
                ((MainActivity)getActivity()).sendCommand();
                wave_select.setVisibility(View.INVISIBLE);

                break;
            case R.id.btn_icm:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(false);
                btn_sim.setEnabled(true);
                btn_decay.setEnabled(true);
                ((MainActivity)getActivity()).setMethod(0x22);
                ((MainActivity)getActivity()).sendCommand();
                wave_select.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_sim:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(true);
                btn_sim.setEnabled(false);
                btn_decay.setEnabled(true);
                ((MainActivity)getActivity()).setMethod(0x33);
                ((MainActivity)getActivity()).sendCommand();
                wave_select.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_decay:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(true);
                btn_sim.setEnabled(true);
                btn_decay.setEnabled(false);
                ((MainActivity)getActivity()).setMethod(0x44);
                ((MainActivity)getActivity()).sendCommand();
                wave_select.setVisibility(View.INVISIBLE);

                break;
            case R.id.btn_wave1:
                btn_wave1.setEnabled(false);
                btn_wave2.setEnabled(true);
                btn_wave3.setEnabled(true);
                btn_wave4.setEnabled(true);
                break;
            case R.id.btn_wave2:
                btn_wave1.setEnabled(true);
                btn_wave2.setEnabled(false);
                btn_wave3.setEnabled(true);
                btn_wave4.setEnabled(true);
                break;
            case R.id.btn_wave3:
                btn_wave1.setEnabled(true);
                btn_wave2.setEnabled(true);
                btn_wave3.setEnabled(false);
                btn_wave4.setEnabled(true);
                break;
            case R.id.btn_wave4:
                btn_wave1.setEnabled(true);
                btn_wave2.setEnabled(true);
                btn_wave3.setEnabled(true);
                btn_wave4.setEnabled(false);
                break;
            default:
                break;
        }
    }
}
