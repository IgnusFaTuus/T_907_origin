package net.kehui.www.t_907_origin.adpter;

import android.util.Log;

import net.kehui.www.t_907_origin.ui.SparkView.SparkAdapter;

public class MyChartAdapter extends SparkAdapter {

    private int[] mTempArray;
    private int[] mCompareArray;
    private boolean isShowCompareLine;
    private int splitNum;
    private boolean isShowSplitLine;
    private boolean isCursorState;  //GC20181223
    private int isMax;  //GC20181227

    public void setmCompareArray(int[] mCompareArray) {
        this.mCompareArray = mCompareArray;
    }

    public void setmTempArray(int[] mTempArray) {
        this.mTempArray = mTempArray;
    }

    public void setShowCompareLine(boolean showCompareLine) {
        isShowCompareLine = showCompareLine;
    }

    public MyChartAdapter(int[] mTempArray, int[] mCompareArray, boolean isShowCompareLine, int
            splitNum, boolean isShowSplitLine, int isMax) {
        this.mTempArray = mTempArray;
        this.mCompareArray = mCompareArray;
        this.isShowCompareLine = isShowCompareLine;
        this.splitNum = splitNum;
        this.isShowSplitLine = isShowSplitLine;
        this.isMax = isMax; //GC20181227
    }

    //GC20181223
    public void setCursorState(boolean cursorState) {
        isCursorState = cursorState;
    }

    @Override
    public int getCount() {
        return 540;
    }

    @Override
    public Object getItem(int index) {
        return index;
    }

    @Override
    public float getX(int index) {
        return super.getX(index);
    }

    @Override
    public float getY(int index) {
        return mTempArray[index];
    }

    @Override
    public float getY1(int index) {
        return mCompareArray[index];
    }

    @Override
    public boolean getCompare() {
        return isShowCompareLine;
    }

    @Override
    public boolean getCursorState(){
        return isCursorState;
    }

    @Override
    public int getMax() {
        return isMax;
    }

}
