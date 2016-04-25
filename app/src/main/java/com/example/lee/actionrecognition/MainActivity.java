package com.example.lee.actionrecognition;

import android.content.Context;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.location.Criteria;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Environment;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;
        import java.io.File;
        import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "sensor";
    private SensorManager sm;
    private String filename=null;
    private Thread thread=null;
    FileOutputStream outStream=null;
    private String xvalue;
    private String yvalue;
    private String zvalue;
    private Location location=null;
    private  LocationManager lm;
    private String bestProvider;
    private String JingduValue="0.0";
    private String WeiduValue="0.0";
    private String SpeedValue="0.0";
    private Socket socket;
    private OutputStream out;
    private Button btn1;
    private Button btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        btn1 = (Button)findViewById(R.id.button);
        btn2 = (Button)findViewById(R.id.button2);
        btn1.setEnabled(true);
        btn2.setEnabled(false);
//        Log.i(TAG, "................................................ ");
//        initClientSocket();
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn1.setEnabled(false);
                btn2.setEnabled(true);
                thread= new Thread(new ThreadShow());
                thread.start();
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               thread.stop();
                closeSocket();
            }
        });
        //创建一个SensorManager来获取系统的传感器服务
        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //选取加速度感应器
        int sensorType = Sensor.TYPE_ACCELEROMETER;
        /*
         * 最常用的一个方法 注册事件
         * 参数1 ：SensorEventListener监听器
         * 参数2 ：Sensor 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor
         * 参数3 ：模式 可选数据变化的刷新频率
         * */
        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(sensorType), SensorManager.SENSOR_DELAY_FASTEST);

        lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        getLocation();
        lm.requestLocationUpdates(bestProvider, 10, 0, locationListener);


    }


    public void getLocation(){

        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE 比较粗略， Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否需要海拔信息 Altitude
        criteria.setAltitudeRequired(true);
        // 设置是否需要方位信息 Bearing
        criteria.setBearingRequired(true);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        // 获取GPS信息提供者
        bestProvider = lm.getBestProvider(criteria, true);
        Log.i("yao", "bestProvider = " + bestProvider);
        // 获取定位信息
        location = lm.getLastKnownLocation(bestProvider);
    }

    private void updateLocation(Location location) {
        if (location != null) {
            // tv1.setText("定位对象信息如下：" + location.toString() + "\n\t其中经度：" + location.getLongitude() + "\n\t其中纬度："
            //         + location.getLatitude()+ "\n\t其中速度：" + location.getSpeed());
            TextView edit1= (TextView)findViewById(R.id.jingvalue);
            JingduValue= ""+location.getLongitude();
            edit1.setText(JingduValue);
            TextView edit2= (TextView)findViewById(R.id.weivalue);
            WeiduValue= ""+location.getLatitude();
            edit2.setText(WeiduValue);
            TextView edit3= (TextView)findViewById(R.id.speedvalue);
            SpeedValue= ""+location.getSpeed();
            edit3.setText(SpeedValue);
        } else {
            Log.i("yao", "没有获取到定位对象Location");
        }
    }

    LocationListener locationListener = new LocationListener() {

        // 当位置改变时触发
        @Override
        public void onLocationChanged(Location location) {
            Log.i("yao", location.toString());
            updateLocation(location);
        }
        // Provider失效时触发
        @Override
        public void onProviderDisabled(String arg0) {
            Log.i("yao", arg0);

        }
        // Provider可用时触发
        @Override
        public void onProviderEnabled(String arg0) {
            Log.i("yao", arg0);
        }
        // Provider状态改变时触发
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            Log.i("yao", "onStatusChanged");
        }
    };

    final SensorEventListener myAccelerometerListener = new SensorEventListener(){
        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                // Log.i(TAG,"onSensorChanged");
                //图解中已经解释三个值的含义
                float X_lateral = sensorEvent.values[0];
                xvalue=String.valueOf(X_lateral);
                float Y_longitudinal = sensorEvent.values[1];
                yvalue=String.valueOf(Y_longitudinal);
                float Z_vertical = sensorEvent.values[2];
                zvalue=String.valueOf(Z_vertical);
                //  Log.i(TAG,"\n heading "+X_lateral);
                //  Log.i(TAG,"\n pitch "+Y_longitudinal);
                //  Log.i(TAG,"\n roll "+Z_vertical);
                TextView textX = (TextView)findViewById(R.id.Xvalue);
                textX.setText(xvalue);
                TextView textY = (TextView)findViewById(R.id.Yvalue);
                textY.setText(yvalue);
                TextView textZ = (TextView)findViewById(R.id.Zvalue);
                textZ.setText(zvalue);
            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

//    public void onPause(){
//        /*
//         * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
//         * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
//         * */
//        sm.unregisterListener(myAccelerometerListener);
//        super.onPause();
//    }

      private void initClientSocket()
    {
        try
        {
      /* 连接服务器 */
            Log.i(TAG, "initClientSocket 11111......................... ");
            socket = new Socket("192.168.23.1", 10000);//执行不了 啊啊啊啊啊

            Log.i(TAG, "initClientSocket 22222......................... ");
      /* 获取输出流 */
            out = socket.getOutputStream();
            Log.i(TAG, "initClientSocket 33333......................... ");
//            btn1.setEnabled(false);
//            btn2.setEnabled(true);
           }
        catch (UnknownHostException e)
        {
            handleException(e, "unknown host exception: " + e.toString());
        }
        catch (Exception e)
        {
            handleException(e, "io exception: " + e.toString());
        }
    }

    public void handleException(Exception e, String prefix)
    {
        e.printStackTrace();
        toastText(prefix + e.toString());
    }

    public void toastText(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void closeSocket()
    {
        try
        {
            out.close();
            socket.close();
        }
        catch (IOException e)
        {
            handleException(e, "close exception: ");
        }
    }


    class ThreadShow implements Runnable {
        @Override
        public void run() {
            initClientSocket();
              try {
                while (true) {
                    Thread.sleep(20);
                    String sensor=SpeedValue+"\t"+xvalue+"\t"+yvalue+"\t"+zvalue+"\n";
//                  outStream.write(sensor.getBytes());
                    System.out.println("send..."+sensor);
                    out.write(sensor.getBytes());

                }
            }catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.out.println("thread error...");
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

