package net.kehui.www.t_907_origin.fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.kehui.www.t_907_origin.R;
import net.kehui.www.t_907_origin.view.MainActivity;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author IF
 * @date 2018/3/26
 */
public class SettingFragment extends Fragment {
    @BindView(R.id.btn_zero)
    Button  btnZero;
    @BindView(R.id.btn_lang)
    Button  btnLang;
    Unbinder unbinder;

    @Override
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

    @OnClick({ R.id.btn_zero, R.id.btn_lang})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_zero:
                //GC20190712    //G?
                int simZero = ((MainActivity) Objects.requireNonNull(getActivity())).zero;
                ((MainActivity) getActivity()).setSimZero(simZero);
                break;
            case R.id.btn_lang:
                break;
                default:
                    break;
        }
    }
}
