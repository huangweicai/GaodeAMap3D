package com.amap.map3d.demo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 创建时间：2017/3/23
 * 编写者：黄伟才
 * 功能描述：
 */

public class MyMapActivity extends FragmentActivity implements LocationSource {
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();

        //销毁定位客户端
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    private Context context;
    private MapView mMapView = null;
    private AMap aMap;

    private TextView tv_time;
    private TextView tv_speed;
    private TextView tv_KCal;
    private TextView tv_distanse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aa_map_layout);
        context = this;

        tv_time = (TextView) findViewById(R.id.tv_time);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_KCal = (TextView) findViewById(R.id.tv_KCal);
        tv_distanse = (TextView) findViewById(R.id.tv_distanse);


        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mMapView.getMap();
            Log.d("TAG", "aMap:"+aMap);
            aMap.setMapTextZIndex(2);

            // 设置定位监听
            aMap.setLocationSource(this);
            aMap.getUiSettings().setMyLocationButtonEnabled(true);
            // 设置为true表示系统定位按钮显示并响应点击，false表示隐藏，默认是false
            aMap.setMyLocationEnabled(true);
        }

        requestLocationPermission();
        local();
        //addPolylinessoild();
    }


    List<LatLng> latLngList = new ArrayList<>();
    LatLng currentLatLng;
    LatLng lastLatLng;
    double totalDistance = 0;//距离总和

    double latitude;
    double longitude;
    //------------------定位------------------>
    //声明定位回调监听器
    private LocationSource.OnLocationChangedListener mListener;
    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
            mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务
        }
        mLocationClient = null;

        lastLatLng = null;
    }

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0) {
                    //方案二：
                    if (null == lastLatLng) {
                        lastLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        latitude = aMapLocation.getLatitude();
                        longitude = aMapLocation.getLongitude();
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 17));//定位点放大

                        //定位成功
                        if (null != mListener) {
                            mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                        }
                        return;
                    }

                    //暂定偏差参数
                    if (Math.abs(latitude - aMapLocation.getLatitude()) > 0.03) {
                        Toast.makeText(context, "获取精度信息：" + aMapLocation.getAccuracy() + " 纬度偏差", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Math.abs(longitude - aMapLocation.getLongitude()) > 0.03) {
                        Toast.makeText(context, "获取精度信息：" + aMapLocation.getAccuracy() + " 经度偏差", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    lastLatLng = new LatLng(latitude, longitude);
                    currentLatLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

                    latitude = aMapLocation.getLatitude();
                    longitude = aMapLocation.getLongitude();

                    //绘线
                    aMap.addPolyline((new PolylineOptions())
                            .add(lastLatLng, currentLatLng)
                            .width(10)
                            .color(Color.argb(255, 1, 1, 1)));

                    //速度
                    tv_speed.setText("速度："+aMapLocation.getSpeed());

                    //获取定位时间（定位启动的时间）
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(aMapLocation.getTime());
                    tv_time.setText("时间："+df.format(date));

                    tv_KCal.setText("卡路里："+getConsumeKCalValue());

                    totalDistance = totalDistance + AMapUtils.calculateLineDistance(lastLatLng,currentLatLng);
                    tv_distanse.setText("距离："+totalDistance);


                    //方案一：
//                    LatLng A = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
//                    latLngList.add(A);
//                    LatLng[] latLngArr = latLngList.toArray(new LatLng[latLngList.size()]);
//
//                    //LatLng B = new LatLng(aMapLocation.getLatitude()+0.00000000000000001, aMapLocation.getLongitude()+0.00000000000000001);
//                    aMap.addPolyline((new PolylineOptions())
//                            .add(latLngArr)
//                            .width(10)
//                            .color(Color.argb(255, 1, 1, 1)));

                    Log.d("TAG", "获取精度信息：" + aMapLocation.getAccuracy());
                    Log.d("TAG", "纬度：" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude());
                    Toast.makeText(context, "获取精度信息：" + aMapLocation.getAccuracy() + " 纬度：" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(context, "集合大小："+latLngList.size()+" 纬度：" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "定位失败，请检查是否开启GPS或禁止了位置权限", Toast.LENGTH_SHORT).show();
                }
            }

            //Log.d("TAG", "错误码：" + aMapLocation.getErrorCode() + "纬度" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude());
            //Toast.makeText(context, "错误码：" + aMapLocation.getErrorCode() + " 纬度" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        }
    };
    //声明AMapLocationClientOption对象
    //初始化AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = new AMapLocationClientOption();

    private void local() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
        //设置定位模式为AMapLocationMode.Device_Sensors，仅设备模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //获取一次定位结果：
        //该方法默认为false，false是默认多次定位
        mLocationOption.setOnceLocation(false);//这里我需要连续定位

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(2000);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }


    //    能量消耗计算公式
//    一、	步行能量消耗（50-130m/min的速度）
//    能量消耗（kcal）={0.1×速度（m/min）+3.5 }÷3.5×1.05×时间（小时）×体重（公斤）
//    二、	跑步时能量消耗（131-350m/min的速度）
//    能量消耗(kcal)= {0.2×速度（m/min）+3.5 }÷3.5×1.05×时间（小时）×体重（公斤）
    public double getConsumeKCalValue() {


        return 0.0;
    }




    //------------------权限------------------>
    private final int REQ_LOCATION = 0x12;

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_LOCATION) {
            if (grantResults != null && grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationClient.startLocation();
                } else {
                    Toast.makeText(context, "缺少定位权限，无法完成定位~", Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }





//======================================================

    LatLng AA = new LatLng(22.906709, 113.210848);
    LatLng A;
    LatLng B;
    double count = 0;
    boolean tag = true;

    public void addPoint(View view) {
        if (B != null) {
            AA = B;
        }

        A = new LatLng(22.906709 + count, 113.210848 + count);
        count = count + 0.000022;
        B = new LatLng(22.906709 + count, 113.210848 + count);
        count = count + 0.000002;

        aMap.addPolyline((new PolylineOptions())
                .add(AA, A, B)
                .width(10)
                .color(Color.argb(255, 1, 1, 1)));

        Log.d("TAG", "纬度：" + (22.906709 + count));
        Log.d("TAG", "经度：" + (113.210848 + count));
    }

}
