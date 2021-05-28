package com.example.istick.BlueTooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.istick.R;

import java.util.List;

/***************************************************************************************************
 *用于处理BLE设备扫描的Activity
 **************************************************************************************************/
public class ScanActivity2 extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int SCAN_PERIOD = 10000;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private MyDeviceAdapter myDeviceAdapter;
    private boolean mScanning;
    private Handler mHandler;
    TextView scanStatusView;
    /***************************************************************************************************
     *onCreat函数
     **************************************************************************************************/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        //蓝牙扫描结果显示适配器
        mLeDeviceListAdapter = new LeDeviceListAdapter(ScanActivity2.this,R.layout.scan_result);
        final ListView listView = (ListView)findViewById(R.id.listView_scan);
        listView.setAdapter(mLeDeviceListAdapter);
        //注册ListView Item点击事件响应函数，启动ConnectActivity连接蓝牙设备
       /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                LeDeviceListAdapter.LeDevice device = (LeDeviceListAdapter.LeDevice)mLeDeviceListAdapter.getItem(position);
                Toast.makeText(ScanActivity.this,"连接设备("+device.getMac()+")",Toast.LENGTH_SHORT).show();
                if(device.getMac().length()==17) {
                    final Intent intent = new Intent(ScanActivity.this, ConnectActivity.class);
                    intent.putExtra(EXTRAS_DEVICE_NAME,device.getName());
                    intent.putExtra(EXTRAS_DEVICE_ADDRESS,device.getMac());
                    if(mScanning)//如果在扫描的话关闭扫描
                    {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    startActivity(intent);
                }
            }
        });*/

        scanStatusView = (TextView)findViewById(R.id.scan_status);
        mHandler = new Handler();

        // 检查设备是否支持BLE，不支持则关闭APP
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "设备不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        //检查安卓版本，6.0以上申请动态权限
        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.M){
            int permissionCheck = 0;
            permissionCheck = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionCheck += this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                //注册权限
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        9527); //9527可以是任意数字，只要两边一致就行
            }
            else//已获得权限，初始化蓝牙
                initLeDevice();
        }
        else
            initLeDevice();
        //List View点击事件响应函数
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if((mScanning==false)&&(listView.pointToPosition((int)event.getX(),(int)event.getY())==AdapterView.INVALID_POSITION))
                {//点击ListView的空白处，重新启动扫描
                    mLeDeviceListAdapter.clearDevice();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    scanLeDevice(true);
                }
                return false;
            }
        });
    };

    /***************************************************************************************************
     *onResume函数，重启蓝牙设备扫描
     **************************************************************************************************/
    @Override
    protected void onResume()
    {
        super.onResume();
        if(mScanning==false) {
            mLeDeviceListAdapter.clearDevice();
            mLeDeviceListAdapter.notifyDataSetChanged();
            scanLeDevice(true);
        }
    }
    /***************************************************************************************************
     *onPause函数，关闭蓝牙设备扫描
     **************************************************************************************************/
    @Override
    protected  void onPause()
    {
        super.onPause();
        scanLeDevice(false);
    }

    /***************************************************************************************************
     *初始化蓝牙
     **************************************************************************************************/
    private void initLeDevice(){
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        //请求开启蓝牙
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
            scanLeDevice(true);
    }
    /***************************************************************************************************
     *开启蓝牙返回结果处理函数
     **************************************************************************************************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == REQUEST_ENABLE_BT)&&(resultCode==RESULT_OK))
        {
            scanLeDevice(true);//权限申请成功，启动蓝牙扫描
        }
        else
        {
            Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***************************************************************************************************
     *蓝牙扫描开启与关闭函数
     **************************************************************************************************/
    private void scanLeDevice(final boolean enable) {
        if (enable)
        {
            // 开启蓝牙扫描， SCAN_PERIOD后关闭扫描
            mHandler.postDelayed(stopLeScanRunnable, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {scanStatusView.setText("蓝牙扫描中，请选择设备连接...");}
            });
        }
        else
        {
            mHandler.removeCallbacks(stopLeScanRunnable);
            stopLeScanRunnable.run();
        }
    }
    //关闭蓝牙扫描
    Runnable stopLeScanRunnable = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {scanStatusView.setText("点击空白处启动蓝牙扫描...");
                }
            });
        }
    };
    /***************************************************************************************************
     *蓝牙扫描返回结果处理函数
     **************************************************************************************************/
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    if(device.getName()!=null)
                    {

                        final LeDeviceListAdapter.LeDevice leDevice = new LeDeviceListAdapter.LeDevice(device.getName(), device.getAddress(), rssi);
                        MyDeviceAdapter.LeDevice test = new MyDeviceAdapter.LeDevice(device.getName(), device.getAddress(), rssi);
                        runOnUiThread(() -> {
                            Log.e("scann1", "加入数据" );
                            mLeDeviceListAdapter.addDevice(leDevice);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        });
                    }
                }
            };

    /***************************************************************************************************
     *安卓权限申请返回结果处理函数
     **************************************************************************************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 9527:
                if (hasAllPermissionsGranted(grantResults)) {
                    // Permission Granted
                    initLeDevice();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "未获得权限", Toast.LENGTH_SHORT).show();
                    //finish();
                }

                break;
        }
    }
    /***************************************************************************************************
     *是否含有全部权限
     **************************************************************************************************/
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}