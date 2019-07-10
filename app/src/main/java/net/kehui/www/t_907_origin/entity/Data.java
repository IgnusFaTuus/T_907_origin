package net.kehui.www.t_907_origin.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import net.kehui.www.t_907_origin.util.DataConverters;


/**
 * @author li.md
 * @date 2019/7/4
 */
@TypeConverters(DataConverters.class)
@Entity(tableName = "data", indices = {@Index({"date", "mode", "range", "line", "phase", "tester",
        "location"})})
public class Data {
    @PrimaryKey(autoGenerate = true)
    public int dataId;

    /**
     * 波形参数
     */
    public int[] para = new int[4];

    /**
     * 波形信息
     */
    public String date;
    public String time;
    public String mode;
    public String range;
    public String line;
    public String phase;
    public String tester;
    public String location;

    /**
     * 波形数据
     */
    public int[] waveData;
    public int[] waveDataSim;
}
