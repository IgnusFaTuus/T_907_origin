package net.kehui.www.t_907_origin.fragment;

        import android.app.Fragment;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import net.kehui.www.t_907_origin.R;

/**
 * Created by IF on 2018/3/26.
 */

public class AdjustFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View adjustLayout = inflater.inflate(R.layout.adj_layout, container, false);
        return adjustLayout;
    }

}
