package net.kehui.www.t_907_origin.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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

/**
 *
 * @author IF
 * @date 2018/3/26
 */

public class FileFragment extends Fragment {
    @BindView(R.id.btn_save)
    Button btnSave;
    @BindView(R.id.btn_browse)
    Button btnBrowse;
    @BindView(R.id.btn_display)
    Button btnDisplay;
    @BindView(R.id.btn_dele)
    Button btnDele;
    @BindView(R.id.file_sidebar)
    LinearLayout fileSidebar;
    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fileLayout = inflater.inflate(R.layout.file_layout, container, false);
        unbinder = ButterKnife.bind(this, fileLayout);
        return fileLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.btn_save, R.id.btn_browse, R.id.btn_display, R.id.btn_dele})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                break;
            case R.id.btn_browse:
                ((MainActivity)getActivity()).getTxtWaveData();
                break;
            case R.id.btn_display:
                ((MainActivity)getActivity()).testWaveData();
                break;
            case R.id.btn_dele:
                ((MainActivity)getActivity()).initSparkView();
                break;
                default:
                    break;
        }
    }
}
