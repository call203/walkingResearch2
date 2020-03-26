package org.walkpackage.notification.models;

public class recordmodel {
    String mgetspeed;
    String mcalspeed;
    String mstepCount;
    String mtimediff;
    String mdistance;
    String uid;

    public recordmodel(){

    }
    public recordmodel(String mdistance, String mcalspeed, String mgetspeed, String mtimediff,String uid) {
        this.mdistance = mdistance;
        this.mcalspeed = mcalspeed;
        this.mgetspeed = mgetspeed;
        this.mtimediff = mtimediff;
        this.uid = uid;
    }

    public String getMgetSpeed() { return mgetspeed;}

    public void setMgetSpeed(String mgetSpeed) {
        this.mgetspeed = mgetSpeed;
    }

    public String getMcalSpeed() {
        return mcalspeed;
    }

    public void setMcalSpeed(String mcalSpeed) {
        this.mcalspeed = mcalSpeed;
    }

    public String getMstepCount() {
        return mstepCount;
    }

    public void setMstepCount(String mstepCount) {
        this.mstepCount = mstepCount;
    }

    public String getMtimediff() { return mtimediff; }

    public void setMtimediff(String mtimediff) {
        this.mtimediff = mtimediff;
    }

    public String getMdistance() {return mdistance; }

    public void setMdistance(String mdistance) {
        this.mdistance = mdistance;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }




}

