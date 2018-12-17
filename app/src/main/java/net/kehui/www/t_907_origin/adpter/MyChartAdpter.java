package net.kehui.www.t_907_origin.adpter;

import net.kehui.www.t_907_origin.ui.SparkView.SparkAdapter;

public class MyChartAdpter extends SparkAdapter {

    private int[] mTempArray;
    private int[] mCompareArray;
    private boolean isShowCompareLine;
    private int splitNum;
    private boolean isShowSplitLine;


    public void setmTempArray(int[] mTempArray) {
        this.mTempArray = mTempArray;
    }

    public void setmCompareArray(int[] mCompareArray) {
        this.mCompareArray = mCompareArray;
    }

    public void setShowCompareLine(boolean showCompareLine) {
        isShowCompareLine = showCompareLine;
    }

    public MyChartAdpter(int[] mTempArray, int[] mCompareArray, boolean isShowCompareLine, int
            splitNum, boolean isShowSplitLine) {
        this.mTempArray = mTempArray;
        this.mCompareArray = mCompareArray;
        this.isShowCompareLine = isShowCompareLine;
        this.splitNum = splitNum;
        this.isShowSplitLine = isShowCompareLine;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int index) {
        return null;
    }

    @Override
    public float getY(int index) {
        return 0;
    }

    @Override
    public float getY1(int index) {
        return 0;
    }

    @Override
    public boolean getCompare() {
        return isShowCompareLine;
    }

    public boolean isShowCompareLine() {
        return isShowCompareLine;
    }
}
