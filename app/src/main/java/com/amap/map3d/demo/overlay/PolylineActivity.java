package com.amap.map3d.demo.overlay;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.map3d.demo.R;
import com.amap.map3d.demo.util.Constants;

/**
 * AMapV2地图中简单介绍一些Polyline的用法.
 */
public class PolylineActivity extends Activity implements
		OnSeekBarChangeListener {
	private static final int WIDTH_MAX = 50;
	private static final int HUE_MAX = 255;
	private static final int ALPHA_MAX = 255;

	private AMap aMap;
	private MapView mapView;
	private Polyline polyline;
	private SeekBar mColorBar;
	private SeekBar mAlphaBar;
	private SeekBar mWidthBar;
	
	/*
	 * 为方便展示多线段纹理颜色等示例事先准备好的经纬度
	 */
//	private double Lat_A = 35.909736;
//	private double Lon_A = 80.947266;
//
//	private double Lat_B = 35.909736;
//	private double Lon_B = 89.947266;
//
//	private double Lat_C = 31.909736;
//	private double Lon_C = 89.947266;
//
//	private double Lat_D = 31.909736;
//	private double Lon_D = 99.947266;

	//[{22.906544,113.210873},{22.906554,113.210883},{22.906564,113.210893}]

	private double Lat_A = 22.906544;
	private double Lon_A = 113.210873;

	private double Lat_B = 22.906554;
	private double Lon_B = 113.210883;

	private double Lat_C = 22.906564;
	private double Lon_C = 113.210893;

	private double Lat_D = 22.906574;
	private double Lon_D = 113.210903;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.polyline_activity);
        /*
         * 设置离线地图存储目录，在下载离线地图或初始化地图设置;
         * 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
         * 则需要在离线地图下载和使用地图页面都进行路径设置
         * */
	    //Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
//        MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);// 此方法必须重写
		init();
		
		addPolylinessoild();//画实线
		addPolylinesdotted();//画虚线
		addPolylinesWithTexture();//画纹理线

//		local();
	}

	//声明AMapLocationClient类对象
	public AMapLocationClient mLocationClient = null;
	//声明定位回调监听器
	public AMapLocationListener mLocationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation aMapLocation) {
			Log.d("TAG", "错误码："+aMapLocation.getErrorCode()+"纬度"+aMapLocation.getLatitude()+" 经度："+aMapLocation.getLongitude());
			Toast.makeText(PolylineActivity.this, "错误码："+aMapLocation.getErrorCode()+" 纬度" + aMapLocation.getLatitude() + " 经度：" + aMapLocation.getLongitude(), Toast.LENGTH_SHORT).show();
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
		//启动定位
		//mLocationClient.startLocation();

		requestLocationPermission();
	}

	private final int REQ_LOCATION=0x12;
	public void requestLocationPermission(){
		ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQ_LOCATION);
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode==REQ_LOCATION){
			if(grantResults!=null&&grantResults.length>0){
				if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
					mLocationClient.startLocation();
				}else{
					Toast.makeText(PolylineActivity.this,"缺少定位权限，无法完成定位~",Toast.LENGTH_LONG).show();
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}




	/**
	 * 初始化AMap对象
	 */
	private void init() {
		mColorBar = (SeekBar) findViewById(R.id.hueSeekBar);
		mColorBar.setMax(HUE_MAX);
		mColorBar.setProgress(50);

		mAlphaBar = (SeekBar) findViewById(R.id.alphaSeekBar);
		mAlphaBar.setMax(ALPHA_MAX);
		mAlphaBar.setProgress(255);

		mWidthBar = (SeekBar) findViewById(R.id.widthSeekBar);
		mWidthBar.setMax(WIDTH_MAX);
		mWidthBar.setProgress(10);
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
	}

	private void setUpMap() {
		mColorBar.setOnSeekBarChangeListener(this);
		mAlphaBar.setOnSeekBarChangeListener(this);
		mWidthBar.setOnSeekBarChangeListener(this);
		aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat_A, Lon_A), 14));
		aMap.setMapTextZIndex(2);
	}
	//绘制一条实线
	private void addPolylinessoild() {
		LatLng A = new LatLng(Lat_A, Lon_A);
		LatLng B = new LatLng(Lat_B, Lon_B);
	    LatLng C = new LatLng(Lat_C, Lon_C);
		LatLng D = new LatLng(Lat_D, Lon_D);
		aMap.addPolyline((new PolylineOptions())
				.add(A, B, C, D)
				.width(10)
				.color(Color.argb(255, 1, 1, 1)));
	}
	// 绘制一条虚线
	private void addPolylinesdotted() {
		polyline = aMap.addPolyline((new PolylineOptions())
				.add(Constants.SHANGHAI, Constants.BEIJING, Constants.CHENGDU)
				.width(10)
				.setDottedLine(true)//设置虚线
				.color(Color.argb(255, 1, 1, 1)));
	}
	//绘制一条纹理线
	private void addPolylinesWithTexture() {
		//四个点
		LatLng A = new LatLng(Lat_A+1, Lon_A+1);
		LatLng B = new LatLng(Lat_B+1, Lon_B+1);
		LatLng C = new LatLng(Lat_C+1, Lon_C+1);
		LatLng D = new LatLng(Lat_D+1, Lon_D+1);
		
		//用一个数组来存放纹理
		List<BitmapDescriptor> texTuresList = new ArrayList<BitmapDescriptor>();
		texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.map_alr));
		texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.custtexture));
		texTuresList.add(BitmapDescriptorFactory.fromResource(R.drawable.map_alr_night));
		
		//指定某一段用某个纹理，对应texTuresList的index即可, 四个点对应三段颜色
		List<Integer> texIndexList = new ArrayList<Integer>();
		texIndexList.add(0);//对应上面的第0个纹理
		texIndexList.add(2);
		texIndexList.add(1);
		
		
		PolylineOptions options = new PolylineOptions();
		options.width(20);//设置宽度
		
		//加入四个点
		options.add(A,B,C,D);
		
		//加入对应的颜色,使用setCustomTextureList 即表示使用多纹理；
		options.setCustomTextureList(texTuresList);
		
		//设置纹理对应的Index
		options.setCustomTextureIndex(texIndexList);
		
		aMap.addPolyline(options);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Polyline中对填充颜色，透明度，画笔宽度设置响应事件
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (polyline == null) {
			return;
		}
		if (seekBar == mColorBar) {
			polyline.setColor(Color.argb(progress, 1, 1, 1));
		} else if (seekBar == mAlphaBar) {
			float[] prevHSV = new float[3];
			Color.colorToHSV(polyline.getColor(), prevHSV);
			polyline.setColor(Color.HSVToColor(progress, prevHSV));
		} else if (seekBar == mWidthBar) {
			polyline.setWidth(progress);
		}
	}
	
}
