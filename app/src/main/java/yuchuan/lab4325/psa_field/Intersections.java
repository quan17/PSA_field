package yuchuan.lab4325.psa_field;

/**
 * Created by root on 8/10/15.
 */
public class Intersections {
    private int Id;
    private double lat;
    private double lng;
    private boolean passed=false;
    private String name;
    private int GreenEnd;
    private int RedEnd;
    private int cycleLength=134;

    private int clockDifference=0;



    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public Intersections(int id, String name, double lat, double lng){
        this.Id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
    }

    public Intersections(int id, String name, double lat, double lng, int GE, int RE){
        this.Id = id;
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.setGreenEnd(GE);
        this.setRedEnd(RE);
       // this.setClockDifference(TD);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getGreenEnd() {
        return GreenEnd;
    }

    public void setGreenEnd(int greenBegin) {
        GreenEnd = greenBegin;
    }

    public int getClockDifference() {
        return clockDifference;
    }

    public void setClockDifference(int clockDifference) {
        this.clockDifference = clockDifference;
    }

    public int getRedEnd() {
        return RedEnd;
    }

    public void setRedEnd(int redEnd) {
        RedEnd = redEnd;
    }

    public void getPossibleEnd(){
        int[] timeInfo;
        timeInfo=DBPossibilityTable.ReadPosTable(getId());
//        setGreenEnd(timeInfo[0]);
//        setRedEnd(timeInfo[1]);
//        setCycleLength(timeInfo[2]);
        setGreenEnd(55);
        setRedEnd(4);
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }
}
