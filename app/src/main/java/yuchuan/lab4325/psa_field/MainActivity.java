package yuchuan.lab4325.psa_field;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
//import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    /*
    * parameters
    *
    */
    private Timer timerGr;
    private Timer timerLoc;
    private SpeedometerGauge speedometer;
    float SpeedO=0;

    boolean appTestFlag=false;

    double distance=3400;  //feet -> m 0.3048
    double speed=55;       //mph -> m/s  *0.447
    double maxRange=100000*0.3048;
    double nextRange=300*0.3048;
    int signalClock=0; //s
    int GT=134;
    int AT=4;
    int RT=61;
    double safeSpeedLimit=18;
    int clockDifference=60;
    double acc=0;

    double advSpeed[]= new double[2];
    int signalResult[] = new int[3];
    int signalInfo[]=new int[4];

    boolean inRange=false;
    boolean interCheck=true;
    int checkRangeGapTime=1000;
    int checkNextGapTime=1000;
    Handler checkHandler;
    double tempDistance=10000000;

    boolean directionCheck=true;
    int intersectionsAmount=11;
    Intersections interPoints[] = new Intersections[intersectionsAmount];
    int currentInter=intersectionsAmount;

//    MockLocationProvider mock;

    TextView TestView=null;
    TextView ResultShowing=null;
    TextView SignalShowing=null;

    LinearLayout linear;

    int clockLength;
    int timeRemain;

    int siganlStatus;
    int greenEnd;
    int redEnd;

    double maxSpeedLimit=65*0.447;
    double minSpeedLimit=15*0.447;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        acquireWakeLock();
//
        interPoints[0]=new Intersections(1401,"Green St",40.563083, -74.300473,74,60);
        interPoints[1]=new Intersections(1402,"Gill Ln" ,40.557134,-74.308436,51,84);
        interPoints[2]=new Intersections(1403,"Ford Ave",40.550120,-74.321814);
        interPoints[3]=new Intersections(1404,"Parsonage",40.544934,-74.331105);
        interPoints[4]=new Intersections(1405,"Grandview Ave",40.539864,-74.338640);
        interPoints[5]=new Intersections(1406,"PrinceSt",40.523625,-74.360836);
        interPoints[6]=new Intersections(1407,"Forest Haven Blvd",40.519740,-74.365958);
        interPoints[7]=new Intersections(1408,"Old Post Rd North",40.515172,-74.374984);
        interPoints[8]=new Intersections(1409,"Old Post Rd South",40.510651,-74.385792);
        interPoints[9]=new Intersections(1410,"Wooding Ave",40.507970,-74.391544);
        interPoints[10]=new Intersections(1411,"Plainfield Av",40.504138,-74.398789);

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GetProvider();
        OpenGPS();
        location = locationManager.getLastKnownLocation(provider);
//		UpdateWithNewLocation(location);
        locationManager.requestLocationUpdates(provider, 200, (float) 0.05, locationListener);


        TestView=(TextView)findViewById(R.id.titletxt);
        ResultShowing =(TextView) findViewById(R.id.message);
        SignalShowing =(TextView) findViewById(R.id.SignalStatus);

        speedometer = (SpeedometerGauge) findViewById(R.id.speedometer);
        speedometer.setMaxSpeed(70);
        speedometer.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        speedometer.setMaxSpeed(70);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(4);
        speedometer.setSpeed(0);


    }
    @Override
    protected void onStart()
    {
        super.onStart();
        nextInter();
        checkRangeRunnable.run();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(timerGr!=null)
            timerGr.cancel();
        if(timerLoc!=null)
            timerLoc.cancel();
    }


    /*------------------ Range Check module-----------------------  */

    private Runnable checkRangeRunnable= new Runnable() {
        int i=0;

        public void run() {
            checkHandler= new Handler();
            inRange = checkRange(distance,maxRange);
            checkHandler.postDelayed(this, checkRangeGapTime);
            if(inRange) {
                checkRangeGapTime = Integer.MAX_VALUE;
                SignalShowing.setText("In range");
                checkNextGapTime=1000;
//                checkNextRunnable.run();
                timerGr = new Timer();
                timerGr.scheduleAtFixedRate(new MyCheckRangeTask(), 2000, 1000);
            }
            else {
                SignalShowing.setText("still"+distance+"m");
            }
        }
    };


    private class MyCheckRangeTask extends TimerTask {
        @Override
        public void run() {

            timeRemain=signalRemain(timeRemain);
            signalInfo=DBSignalSecond.ReadSignalTable(interPoints[currentInter].getId());
            signalClock=signalInfo[0];
            siganlStatus=signalInfo[1];

            advSpeed=SpeedRange();


            if(distance<=50&&checkPassing(distance, tempDistance))
            {
                tempDistance = 9000;


                interPoints[currentInter].setPassed(true);
                inRange = false;
                interCheck = true;

                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);

            }
            else
            {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }
    }



    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 1:
                    signalShow();
                    speedometer.addColoredRange(0, 100, Color.RED);
                    if(advSpeed[0]<=0||advSpeed[1]<=0)
                        ResultShowing.setText("Prepare to stop!");
                    else {
                        System.out.println("speed:"+advSpeed[0]);
                        speedometer.addColoredRange(advSpeed[0] * 2.2369, advSpeed[1] * 2.2369, Color.WHITE);
                        ResultShowing.setText("Distance remaining:" + (int) (distance * 3.28) + "  Ft");
                    }
                    speedometer.setSpeed(speed * 2.2369);
                    break;
                case 2:
                    ResultShowing.setText("Crossing");
                    speedometer.addColoredRange(0, 100, Color.RED);
                    speedometer.setSpeed(speed * 2.2369);
                    timerGr.cancel();
                    SignalShowing.setText("New Intersection" + currentInter + "  Come!");
                    TestView.setText("Next is " + interPoints[currentInter].getName() + "!");
                    checkRangeRunnable.run();
                    break;
            } super.handleMessage(msg);
        };
    };



    private boolean checkPassing(double curDis,double oldDis){
        if(curDis>oldDis+0.5&&speed>0.5)
        {
//            System.out.println("***"+curDis+";"+oldDis);
            return true;
        }

        else
        {
//            System.out.println("###"+curDis+";"+oldDis);
            return false;
        }
    }




    private boolean checkRange(double Distance,double Range){
        if(Distance<=Range)
            return true;
        else
            return false;
    }

    private int checkNext(double Distance,double Range, double speed) {
        int remainTime=0;
        if(Distance<=0)
            return Integer.MAX_VALUE;

        remainTime=(int)(Distance%Range*1000/speed);
        if(remainTime<200)
            remainTime=(int)(91.44/speed*1000);
        return remainTime;

    }


    /*------------------ Range Check module-----------------------  */

    /*------------------ Green Remain Time module-----------------------  */




    private class MySiganlTimeTask extends TimerTask {
        @Override
        public void run() {
            signalInfo=DBSignalSecond.ReadSignalTable(interPoints[currentInter].getId());
            signalClock=signalInfo[0];
            siganlStatus=signalInfo[1];
//            updateDB.updateDB(location.getLatitude(),location.getLongitude(),distance,location.getSpeed(),0.0,signalInfo[1],signalClock);
//            GT=signalInfo[2];
//            AT=signalInfo[2]+5;
//            RT=signalInfo[3];
            Message message = new Message();
            message.what = 1;
            mHandler.sendMessage(message);

        }
    }


    /*------------------ Green Remain Time module-----------------------  */



    /*------------------ Advisor Speed module-----------------------  */


    private double[] SpeedRange() {
        double minS, maxS;

        double advSpeed;
        if(acc>=-3.0&&acc<=3.0){
            advSpeed=speed+acc*timeRemain;

            if(siganlStatus==2)
            {
                minS=minSpeedLimit;
                maxS=advSpeed<maxSpeedLimit?advSpeed:maxSpeedLimit;
                if(maxS<=minS)
                    maxS=minS+5;
            }
            else
            {
                maxS=maxSpeedLimit;
                minS=advSpeed>minSpeedLimit?advSpeed:minSpeedLimit;
                if(maxS<=minS)
                    minS=maxS-5;
            }

        }
        else if (acc<=-3.0||acc>=3.0)
        {
            minS=-1;
            maxS=-1;
        }
        else
        {
            minS=minSpeedLimit;
            maxS=maxSpeedLimit;
        }
        double[] result ={minS,maxS};
        return result;
    }

    private double AccEstimated(){

        return 2*(distance-speed*timeRemain)/timeRemain/timeRemain;
    }
    /*------------------ Advisor Speed module-----------------------  */


    /*------------------ Signal module-----------------------  */

    private int signalRemain(int oldRT){
        int rT;
        if(siganlStatus==2)
            if(redEnd<greenEnd)
                rT=redEnd+clockLength-signalClock;
            else
                rT=redEnd-signalClock;
        else
            if(redEnd<greenEnd)
                rT=greenEnd-signalClock;
            else
                rT=greenEnd+clockLength-signalClock;
        if(rT<0)
        {
            System.out.println("remain time negative!! :"+rT);
            rT =oldRT>0? oldRT - 1:0;
        }
        return rT;

    }

    private void signalShow(){
        if(siganlStatus==2) {
            SignalShowing.setText("Red Remaining : " + "\n" + timeRemain + "s");
            SignalShowing.setBackgroundResource(R.drawable.textview_style_red);
            SignalShowing.setTextColor(Color.WHITE);
        }
        else {
            SignalShowing.setText("Green Remaining : " + "\n" + timeRemain + "s");
            SignalShowing.setBackgroundResource(R.drawable.textview_style_green);
            SignalShowing.setTextColor(Color.BLACK);
        }

    }
	/*------------------ Signal module-----------------------  */


    /*------------------ Intersection Check module-----------------------  */
    private void nextInter()
    {
        int tempInter=currentInter;
        if(interCheck)
            tempInter=findNearInter(0,intersectionsAmount,location);
//            else
//                return;

        if(currentInter!=tempInter)
        {
            interCheck = false;
            currentInter = tempInter;
            interPoints[currentInter].getPossibleEnd();
            redEnd=interPoints[currentInter].getRedEnd();
            greenEnd=interPoints[currentInter].getGreenEnd();
            clockLength=interPoints[currentInter].getCycleLength();
        }
    }


    private int findNearInter(int startI, int endI, Location location){
        double minDis=100000000;
        double curDis=0;
        int interIndex=-1;
        for(int i=startI; i<endI;i++){
            if(!interPoints[i].isPassed())
            {
                if(location!=null)
                    curDis=gps2m(location.getLatitude(), location.getLongitude(), interPoints[i].getLat(), interPoints[i].getLng());
                else
                    curDis=gps2m(40.566293, -74.297787, interPoints[i].getLat(), interPoints[i].getLng());
                if(minDis>curDis){
                    minDis=curDis;
                    interIndex=i;
                    directionCheck=true;
                }

            }
        }

        return interIndex;
    }
    /*------------------ Intersection Check module-----------------------  */


    /*------------------ GPS module-----------------------  */

    private LocationManager locationManager;
    private String provider;
    private Location location;


    private float UnitSpeed = 2.2369f;
    private String UnitSpeedString = "MPH";

    private void OpenGPS() {

        if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "GPS already Set", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "GPS not set yet", Toast.LENGTH_SHORT).show();
//       	Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//        	startActivityForResult(intent,0);
    }

    private void GetProvider(){

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
//        provider = locationManager.GPS_PROVIDER;
        provider = locationManager.getBestProvider(criteria,true);
    }
    boolean tempflag=false;
    private final LocationListener locationListener = new LocationListener(){
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
//			UpdateWithNewLocation(location);
            if(currentInter<intersectionsAmount)
                distance=gps2m(location.getLatitude(), location.getLongitude(), interPoints[currentInter].getLat(), interPoints[currentInter].getLng());
            //System.out.println("distance change to"+distance);
            speed=location.getSpeed();
            speedometer.setSpeed(speed*2.2369);
            if(distance<50||directionCheck)
                if(tempflag) {
                    tempDistance = distance;
                    System.out.println("^^^"+tempDistance);
                    tempflag = !tempflag;
                }
                else {
                    tempflag = !tempflag;
                    System.out.println("~~~"+tempDistance);

                }
        }
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
//			UpdateWithNewLocation(null);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    };

    private final double EARTH_RADIUS = 6378137.0;

    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {

        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    /*------------------ GPS module-----------------------  */

    //acquireWakeLock��keep cpu working
    WakeLock wakeLock = null;
    private void acquireWakeLock()
    {
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,  this.getClass().getCanonicalName());
//           wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock && !wakeLock.isHeld())
            {
                wakeLock.acquire();
            }
        }
    }

    //releaseWakeLock
    private void releaseWakeLock()
    {
        if (null != wakeLock&& wakeLock.isHeld())
        {
            wakeLock.release();
            wakeLock = null;
        }
    }

}
