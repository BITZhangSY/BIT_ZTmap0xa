package com.bit_zt.bit_ztmap0xa;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private MapView my_MapView;
    private BaiduMap my_BaiduMap;

    // 定位相关
    private LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private BitmapDescriptor mCurrentMarker;
    private LatLng current_latlng;

    private List<LatLng> datalist = new ArrayList<>();

    boolean isFirstLoc = true;// 是否首次定位

    private HeatMap heatmap;
    private Button mAdd;
    private Button mRemove;

    private boolean isDestroy = false;
    private final int REFRESH_STEP = 60;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        my_MapView = (MapView) findViewById(R.id.my_MapView);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        my_BaiduMap = my_MapView.getMap();
        MapStatus mapStatus = new MapStatus.Builder().zoom(18.0f).target(current_latlng).build();
        MapStatusUpdate mapstates = MapStatusUpdateFactory.newMapStatus(mapStatus);
        my_BaiduMap.setMapStatus(mapstates);

        // 开启定位图层
        my_BaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption mOption = new LocationClientOption();
//        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mOption.setAddrType("all");
        mOption.setCoorType("bd09ll");
        mOption.setOpenGps(true);
        mOption.setScanSpan(1000);
        mOption.setIsNeedAddress(true);
        mOption.setNeedDeviceDirect(true);

        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(mOption);
        mLocClient.start();


        mAdd = (Button) findViewById(R.id.add);
        mRemove = (Button) findViewById(R.id.remove);
        mAdd.setEnabled(true);
        mRemove.setEnabled(false);
        mAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addHeatMap();
            }
        });
        mRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                heatmap.removeHeatMap();
                mAdd.setEnabled(true);
                mRemove.setEnabled(false);
            }
        });
    }

    private void addHeatMap() {
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(!isDestroy){
                    my_BaiduMap.addHeatMap(heatmap);
                }
                mAdd.setEnabled(false);
                mRemove.setEnabled(true);
            }
        };
        new Thread() {
            @Override
            public void run() {
                super.run();
                List<LatLng> data = datalist;
                heatmap = new HeatMap.Builder().data(data).build();
                h.sendEmptyMessage(0);
            }
        }.start();
    }

    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || my_MapView == null) {
                return;
            }

            LatLng temp = new LatLng(location.getLatitude(),location.getLongitude());
            datalist.add(temp);
            current_latlng = temp;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            my_BaiduMap.setMyLocationData(locData);

            my_BaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                    mCurrentMode, true, null));

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                my_BaiduMap.animateMapStatus(u);
            }

            if(count == REFRESH_STEP){
          //      addHeatMap();
                count = 0;
            }
            count++;
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isDestroy = true;
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        my_BaiduMap.setMyLocationEnabled(false);
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        my_MapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        my_MapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        my_MapView.onPause();
    }

}
