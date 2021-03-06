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

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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



    double distance=3400;  //feet -> m 0.3048
    double speed=55;       //mph -> m/s  *0.447
    double maxRange=300000*0.3048;
    double nextRange=300*0.3048;
    int signalClock=0; //s

    double acc=0;

    double advSpeed[]= new double[2];

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
    int currentInter=0;
    boolean firstRun=true;

    TextView TestView=null;
    TextView ResultShowing=null;
    TextView SignalShowing=null;
    Button refreshButton=null;

    LinearLayout linear;

    int clockLength=134;
    int timeRemain;
    int oldtimeRemain;

    int siganlStatus;
    int oldsiganlStatus;
    int greenEnd=5;
    int redEnd=67;
    int posEnd=90;
    double maxSpeedLimit=50*0.447;
    double minSpeedLimit=20*0.447;
    double locs[]=new double[2];
    int falseDirCount=0;

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
        interPoints[4]=new Intersections(1405,"Grandview Ave",40.53977,-74.33867);
        interPoints[5]=new Intersections(1406,"PrinceSt",40.523625,-74.360836);
        interPoints[6]=new Intersections(1407,"Forest Haven Blvd",40.519740,-74.365958);
        interPoints[7]=new Intersections(1408,"Old Post Rd North",40.515172,-74.374984);
        interPoints[8]=new Intersections(1409,"Old Post Rd South",40.510579, -74.385787);
        interPoints[9]=new Intersections(1410,"Wooding Ave",40.507901, -74.391546);
        interPoints[10]=new Intersections(1411,"Plainfield Av",40.504078, -74.398781);

//        interPoints[0]=new Intersections(1401,"Green St",40.563083, -74.300473,74,60);Z
//        interPoints[1]=new Intersections(1402,"Gill Ln" ,40.557134,-74.308436,51,84);
//        interPoints[2]=new Intersections(1403,"Ford Ave",40.550120,-74.321814);
//        interPoints[3]=new Intersections(1404,"Parsonage",40.544934,-74.331105);
//        interPoints[4]=new Intersections(1405,"Grandview Ave",40.539864,-74.338640);
//        interPoints[5]=new Intersections(1406,"PrinceSt",40.523625,-74.360836);
//        interPoints[6]=new Intersections(1407,"Forest Haven Blvd",40.519740,-74.365958);
//        interPoints[7]=new Intersections(1408,"Old Post Rd North",40.515172,-74.374984);
//        interPoints[8]=new Intersections(1409,"Old Post Rd South",40.510651,-74.385792);
//        interPoints[9]=new Intersections(1410,"Wooding Ave",40.507970,-74.391544);
//        interPoints[10]=new Intersections(1411,"Plainfield Av",40.504138,-74.398789);


        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        GetProvider();
        OpenGPS();
        location = locationManager.getLastKnownLocation(provider);
//		UpdateWithNewLocation(location);
        locationManager.requestLocationUpdates(provider, 200, (float) 0.05, locationListener);


        TestView=(TextView)findViewById(R.id.titletxt);
        ResultShowing =(TextView) findViewById(R.id.message);
        SignalShowing =(TextView) findViewById(R.id.SignalStatus);
        refreshButton=(Button)findViewById(R.id.btn);
        refreshButton.setVisibility(View.VISIBLE);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interCheck=true;
                for(int i=0; i<11;i++)
                    interPoints[i].setPassed(false);
                nextInter();
            }
        });

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


        linear = (LinearLayout) findViewById(R.id.parent);

        ViewTreeObserver vto2 = linear.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                linear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int size = 0;
                if (linear.getHeight() >= linear.getWidth()) {
                    size = linear.getHeight() - 10;
                    Log.e("Sizes", "" + size);
                } else {
                    size = linear.getWidth() - 10;
                    Log.e("Sizes", "" + size);
                }
                speedometer.setLayoutParams(new LayoutParams(size, size));
            }
        });


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

    @Override
    protected void onStop()
    {
        super.onStop();
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
                TestView.setText("Next is " + interPoints[currentInter].getName() + "!");
                SignalShowing.setText("In range");
                checkNextGapTime=1000;
                timerGr = new Timer();
                timerGr.scheduleAtFixedRate(new MyCheckRangeTask(), 2000, 1000);
            }
            else {
//                SignalShowing.setText("still"+distance+"m");
            }
        }
    };


    private class MyCheckRangeTask extends TimerTask {
        @Override
        public void run() {
            if(firstRun)
            {
                interPoints[currentInter].getPossibleEnd(posEnd);
                redEnd=interPoints[currentInter].getRedEnd();
                greenEnd=interPoints[currentInter].getGreenEnd();
                clockLength=interPoints[currentInter].getCycleLength();
                firstRun=false;
                falseDirCount=0;
                tempDistance=100000;
            }
            timeRemain=signalRemain(timeRemain);
            signalInfo=DBSignalSecond.ReadSignalTable(interPoints[currentInter].getId());
            signalClock=signalInfo[0];
            siganlStatus=signalInfo[1];
            acc=accEstimated();
            System.out.println("acc:"+acc);
            advSpeed=SpeedRange();

//            UploadInfo();
//            System.out.println("locs"+locs[0]);
//            System.out.println("gps"+location.getLongitude());
            if(false&&checkDirection(distance, tempDistance))
            {
                interPoints[currentInter].setPassed(true);
                interCheck = true;

                nextInter();
//                interPoints[currentInter].getPossibleEnd(posEnd);
//                redEnd=interPoints[currentInter].getRedEnd();
//                greenEnd=interPoints[currentInter].getGreenEnd();
//                clockLength=interPoints[currentInter].getCycleLength();
                firstRun=true;
                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);
            }
             else if(distance<=100&&checkPassing(distance, tempDistance))
            {
                directionCheck=false;
                tempDistance = 9000;


                interPoints[currentInter].setPassed(true);
                inRange = false;
                interCheck = true;
                nextInter();
//                interPoints[currentInter].getPossibleEnd(posEnd);
//                redEnd=interPoints[currentInter].getRedEnd();
//                greenEnd=interPoints[currentInter].getGreenEnd();
//                clockLength=interPoints[currentInter].getCycleLength();
                firstRun=true;
                Message message = new Message();
                message.what = 2;
                mHandler.sendMessage(message);
//                mHandler.sendMessageDelayed(message,500);

            }
            else
            {
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            }
            directionCheck=true;
        }
    }



    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 1:
                    TestView.setText("Next is " + interPoints[currentInter].getName() + "!");
                    signalShow(timeRemain);
                    speedometer.addColoredRange(0, 100, Color.RED);
                    if(advSpeed[0]<=0||advSpeed[1]<=0)
                        ResultShowing.setText("Prepare to stop!");
// for tesing

//                            ResultShowing.setText("Distance remaining:" + (int) (distance * 3.28) + "  Ft");
                    else {
//                        System.out.println("speed:"+advSpeed[0]);
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
                    timerGr = new Timer();
                    timerGr.scheduleAtFixedRate(new MyCheckRangeTask(), 1000, 1000);
                    break;
            } super.handleMessage(msg);
        };
    };



    private boolean checkPassing(double curDis,double oldDis){
        if(curDis>oldDis+0.5&&speed>0.5)
        {
//            System.out.println("***"+"wrong"+curDis+";"+oldDis);
            return true;
        }

        else
        {
//            System.out.println("###"+curDis+";"+oldDis);
            return false;
        }
    }

    private boolean checkDirection(double curDis,double oldDis){
        if(curDis>oldDis+3&&speed>4)
        {
            System.out.println("***"+"wrong"+curDis+";"+oldDis);
            falseDirCount++;
        }

        if(falseDirCount>3)
            return true;
        //            System.out.println("###"+curDis+";"+oldDis);
            return false;
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

    /*------------------ Green Remain Time module-----------------------




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
        double[] wrongresult ={-1,-1};
        if(timeRemain<=0)
            return wrongresult;
        double advSpeed;
        if(acc>=-3.0&&acc<=3.0){
            advSpeed=speed+acc*timeRemain;

            if(siganlStatus==2)
            {
                minS=minSpeedLimit;
                maxS=advSpeed<maxSpeedLimit?advSpeed:maxSpeedLimit;
                if(maxS<=minS)
                    return wrongresult;
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

    private double accEstimated(){

        return 2*(distance-speed*timeRemain)/timeRemain/timeRemain;
    }
    /*------------------ Advisor Speed module-----------------------  */


    /*------------------ Signal module-----------------------  */

    private int signalRemain(int oldRT){
        int rT;
//        if(siganlStatus==2)
//            if(redEnd<greenEnd)
//                rT=redEnd+clockLength-1-signalClock;
//            else
//                rT=redEnd-signalClock;
//        else
//            if(redEnd<greenEnd)
//                rT=greenEnd-signalClock;
//            else
//                if(signalClock<=greenEnd)
//                    rT=greenEnd-signalClock;
//                else
//                    rT=greenEnd+clockLength-1-signalClock;
        if(signalClock<=greenEnd)
            if(siganlStatus!=2)
                rT=greenEnd-signalClock;
            else
                rT=-1;
        else if(signalClock<=redEnd)
            if(siganlStatus==2)
                rT=redEnd-signalClock;
            else
                rT=-1;
        else
        if(siganlStatus!=2)
            rT=greenEnd+clockLength-1-signalClock;
        else
            rT=-1;




//        if(rT<0)
//        {
//            System.out.println(rT+"time");
//            rT =oldRT>0? oldRT-1:0;
//        }
        return rT;

    }

    private void signalShow(int tR){
//        if(Math.abs((oldtimeRemain-tR))>10&&oldsiganlStatus!=siganlStatus)
//        {
//            System.out.println("time change lot!! :" + oldtimeRemain+":"+tR);
//            tR = oldtimeRemain > 0 ? (oldtimeRemain-1) : 0;
//        }
        if(siganlStatus==2) {
            if(tR<=0)
                SignalShowing.setText("Recalculating");
            else
                SignalShowing.setText("Estimated Time to Green : " + "\n" + tR + "s");
            SignalShowing.setBackgroundResource(R.drawable.textview_style_red);
            SignalShowing.setTextColor(Color.WHITE);
        }
        else {
            if(tR<=0)
                SignalShowing.setText("Recalculating");
            else
                SignalShowing.setText("Estimated Time to Red : " + "\n" + tR + "s");
            SignalShowing.setBackgroundResource(R.drawable.textview_style_green);
            SignalShowing.setTextColor(Color.BLACK);
        }
//        oldsiganlStatus=siganlStatus;
//        oldtimeRemain=tR;
    }
	/*------------------ Signal module-----------------------  */


    /*------------------ Intersection Check module-----------------------  */
    private void nextInter()
    {
        int tempInter=currentInter;
        if(interCheck)
        {
            try{

                tempInter = findNearInter(currentInter, 11);
            }
            catch (Exception e)
            {
                onStop();
//                for(int i=0; i<11;i++)
//                    interPoints[i].setPassed(false);
//                nextInter();
            }
        }
//            else
//                return;

        if(currentInter!=tempInter)
        {
            interCheck = false;
            currentInter = tempInter;
//            interPoints[currentInter].getPossibleEnd(posEnd);
//            redEnd=interPoints[currentInter].getRedEnd();
//            greenEnd=interPoints[currentInter].getGreenEnd();
//            clockLength=interPoints[currentInter].getCycleLength();
        }
    }


    private int findNearInter(int startI, int endI){
        double minDis=100000000;
        double curDis=0;
        int interIndex=-1;
        for(int i=0; i<endI;i++){
            if(!interPoints[i].isPassed())
//            if(i!=startI)
            {
                if(locs[0]!=0)
                    curDis=gps2m(locs[0],locs[1], interPoints[i].getLat(), interPoints[i].getLng());
                else
                    curDis=gps2m(40.566293, -74.297787, interPoints[i].getLat(), interPoints[i].getLng());
                if(minDis>curDis){
                    minDis=curDis;
                    System.out.println("inter"+i+" !!dis "+curDis);
                    interIndex=i;
                    directionCheck=true;
                }

            }
//            else
//                continue;
        }
        System.out.println("min is"+interIndex);
        return interIndex;
    }
    /*------------------ Intersection Check module-----------------------  */


    /*------------------ Uploading field module-----------------------  */
    public void UploadInfo()
    {
        DBupdateInfo.updateDB(interPoints[currentInter].getId(),distance,speed,acc,siganlStatus,timeRemain,advSpeed[0],advSpeed[1]);
    }


    /*------------------ Uploading field module-----------------------  */
    /*------------------ GPS module-----------------------  */

    private LocationManager locationManager;
    private String provider;
    private Location location;




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
            locs[0]=location.getLatitude();
            locs[1]=location.getLongitude();
            //System.out.println("distance change to"+distance);
            speed=location.getSpeed();
            speedometer.setSpeed(speed*2.2369);
            if(true||distance<100||directionCheck)
//                tempDistance = distance;
                if(tempflag) {
                    tempDistance = distance;
//                    System.out.println("^^^"+tempDistance);
                    tempflag = !tempflag;
                }
                else {
                    tempflag = !tempflag;
//                    System.out.println("~~~"+tempDistance);

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
