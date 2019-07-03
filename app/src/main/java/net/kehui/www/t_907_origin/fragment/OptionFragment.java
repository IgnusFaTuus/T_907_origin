package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
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
 * @author IF
 * @date 2018/3/26
 */

public class OptionFragment extends Fragment {
    @BindView(R.id.btn_zoom_in)
    Button btnZoomIn;
    @BindView(R.id.btn_zoom_out)
    Button btnZoomOut;
    @BindView(R.id.btn_res)
    Button btnRes;
    @BindView(R.id.btn_memory)
    Button btnMemory;
    @BindView(R.id.btn_compare)
    Button btnCompare;
    @BindView(R.id.wavePrevious)
    Button btnWavePrevious;
    @BindView(R.id.waveNext)
    Button btnWaveNext;
    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View optionLayout = inflater.inflate(R.layout.opt_layout, container, false);
        unbinder = ButterKnife.bind(this, optionLayout);
        return optionLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (((MainActivity) getActivity()).isDrawSim) {
            btnWavePrevious.setVisibility(View.VISIBLE);
            btnWaveNext.setVisibility(View.VISIBLE);
        } else {
            btnWavePrevious.setVisibility(View.INVISIBLE);
            btnWaveNext.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_zoom_in, R.id.btn_zoom_out, R.id.btn_res, R.id.btn_memory, R.id.btn_compare, R.id.wavePrevious, R.id.waveNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_zoom_in:
                break;
            case R.id.btn_zoom_out:
                break;
            case R.id.btn_res:
                break;
            case R.id.btn_memory:
                ((MainActivity) getActivity()).clickMemory();
                break;
            case R.id.btn_compare:
                ((MainActivity) getActivity()).clickCompare();
                break;
            case R.id.wavePrevious:
                int waveSelect = ((MainActivity) getActivity()).getGainState();
                if (waveSelect < 32) {
                    waveSelect++;
                    ((MainActivity) getActivity()).setGainState(waveSelect);
                }
                ((MainActivity) getActivity()).setGain(0x11);
                break;
            case R.id.waveNext:
                waveSelect = ((MainActivity) getActivity()).getGainState();
                if (waveSelect > 0) {
                    waveSelect--;
                    ((MainActivity) getActivity()).setGainState(waveSelect);
                }
                ((MainActivity) getActivity()).setGain(0x22);
                ((MainActivity) getActivity()).sendCommand();
                break;
                default:
                    break;

        }
    }
}
