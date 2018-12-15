package net.kehui.www.sparkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.robinhood.spark.SparkAdapter;
import com.robinhood.spark.SparkView;


public class MainActivity extends AppCompatActivity {

    private SparkView mSparkView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSparkView = this.findViewById(R.id.sparkview);
        mSparkView.setAdapter(new SparkAdapter() {
            @Override
            public int getCount() {
                return 400;
            }

            @Override
            public Object getItem(int index) {
                return null;
            }

            @Override
            public float getY(int index) {
                return 0;
            }
        });
    }
}
