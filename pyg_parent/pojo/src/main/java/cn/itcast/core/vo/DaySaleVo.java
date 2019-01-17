package cn.itcast.core.vo;

import java.io.Serializable;

public class DaySaleVo implements Serializable {

    private String dayTime; //日期

    private Object daySale; //日销售额

    public DaySaleVo() {
    }

    public DaySaleVo(String dayTime, Object daySale) {
        this.dayTime = dayTime;
        this.daySale = daySale;
    }

    @Override
    public String toString() {
        return "DaySaleVo{" +
                "dayTime='" + dayTime + '\'' +
                ", daySale=" + daySale +
                '}';
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public Object getDaySale() {
        return daySale;
    }

    public void setDaySale(Object daySale) {
        this.daySale = daySale;
    }
}
