package net.kehui.www.t_907_origin.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by IF on 2018/12/15.
 */
@Entity
public class UserData {
    @Id(autoincrement = true)
    private Long id;
    private String InfoId;
    private long testTime;
    private String testMethod;
    private String testRange;
    private String testPosition;
    private String cableFullLength;
    private String cablePhase;
    private String cableType;
    private String faultType;
    private String faultLength;
    private int Gain;
    private String language;

    @Generated(hash = 661905420)
    public UserData(Long id, String InfoId, long testTime, String testMethod,
            String testRange, String testPosition, String cableFullLength,
            String cablePhase, String cableType, String faultType,
            String faultLength, int Gain, String language) {
        this.id = id;
        this.InfoId = InfoId;
        this.testTime = testTime;
        this.testMethod = testMethod;
        this.testRange = testRange;
        this.testPosition = testPosition;
        this.cableFullLength = cableFullLength;
        this.cablePhase = cablePhase;
        this.cableType = cableType;
        this.faultType = faultType;
        this.faultLength = faultLength;
        this.Gain = Gain;
        this.language = language;
    }

    @Generated(hash = 1838565001)
    public UserData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInfoId() {
        return InfoId;
    }

    public void setInfoId(String infoId) {
        InfoId = infoId;
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getTestRange() {
        return testRange;
    }

    public void setTestRange(String testRange) {
        this.testRange = testRange;
    }

    public String getTestPosition() {
        return testPosition;
    }

    public void setTestPosition(String testPosition) {
        this.testPosition = testPosition;
    }

    public String getCableFullLength() {
        return cableFullLength;
    }

    public void setCableFullLength(String cableFullLength) {
        this.cableFullLength = cableFullLength;
    }

    public String getCablePhase() {
        return cablePhase;
    }

    public void setCablePhase(String cablePhase) {
        this.cablePhase = cablePhase;
    }

    public String getCableType() {
        return cableType;
    }

    public void setCableType(String cableType) {
        this.cableType = cableType;
    }

    public String getFaultType() {
        return faultType;
    }

    public void setFaultType(String faultType) {
        this.faultType = faultType;
    }

    public String getFaultLength() {
        return faultLength;
    }

    public void setFaultLength(String faultLength) {
        this.faultLength = faultLength;
    }

    public int getGain() {
        return Gain;
    }

    public void setGain(int gain) {
        Gain = gain;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}





