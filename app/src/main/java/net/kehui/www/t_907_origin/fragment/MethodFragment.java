package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

/**
 * Created by IF on 2018/3/26.
 */

public class MethodFragment extends Fragment implements OnClickListener {

    private Button btn_tdr;
    private Button btn_icm;
    private Button btn_sim;
    private Button btn_decay;
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
    }

    private void initButton() {
        btn_tdr = getActivity().findViewById(R.id.btn_tdr);
        btn_icm = getActivity().findViewById(R.id.btn_icm);
        btn_sim = getActivity().findViewById(R.id.btn_sim);
        btn_decay = getActivity().findViewById(R.id.btn_decay);

        btn_tdr.setOnClickListener(this);
        btn_icm.setOnClickListener(this);
        btn_sim.setOnClickListener(this);
        btn_decay.setOnClickListener(this);
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
                ((MainActivity) getActivity()).sendInitData();

                break;
            case R.id.btn_icm:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(false);
                btn_sim.setEnabled(true);
                btn_decay.setEnabled(true);
                ((MainActivity)getActivity()).setMethod(0x22);
                ((MainActivity) getActivity()).sendInitData();
                break;
            case R.id.btn_sim:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(true);
                btn_sim.setEnabled(false);
                btn_decay.setEnabled(true);
                ((MainActivity)getActivity()).setMethod(0x33);
                ((MainActivity) getActivity()).sendInitData();
                break;
            case R.id.btn_decay:
                btn_tdr.setEnabled(true);
                btn_icm.setEnabled(true);
                btn_sim.setEnabled(true);
                btn_decay.setEnabled(false);
                ((MainActivity)getActivity()).setMethod(0x44);
                ((MainActivity) getActivity()).sendInitData();
                break;
            default:
                break;
        }

    }
}
