package com.example.istick.BlueTooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationAttributes;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.istick.R;
import com.example.istick.Util.TypeConversion;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BluetoothConnect extends Activity {
    /*对应的键值对*/
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName;
    private String mDeviceAddress;

    /*判断是否已经连接*/

    private boolean mConnected = false;

   /*是否在扫描*/

    private TextView connectStatusView;

   /*处理连接的类*/

    private BluetoothLeService mBluetoothLeService;

    /*存储连接相关的特征值消息*/

    private ArrayList<BluetoothGattCharacteristic> mCharacteristics = new ArrayList<BluetoothGattCharacteristic>();

    //服务的UUID

    private final static UUID UUID_SERVICE = UUID.fromString("70617374-6172-2d74-6563-6820436f4700");

   //遍历特征值用到

    private int position=0;

    //写入的特征值

    private static BluetoothGattCharacteristic writeCharacter;

    private String writeCharacterUUID="0000fff3-0000-1000-8000-00805f9b34fb";

    //读取的特征值

    public static BluetoothGattCharacteristic readCharacter;

    private String readCharacterUUID="0000fff1-0000-1000-8000-00805f9b34fb";

    /*存储回显的结果*/

    private String result;

    /*对应的服务的值*/

    private String IStick_Server_UUID="0000fff0-0000-1000-8000-00805f9b34fb";

    /*定时扫描的操作*/

    private Handler pollingHandler = new Handler();

    /*间隔的扫描时间*/

    private static final int POLLING_PERIOD = 200;

    /*处理BLE设备扫描的Activity*/

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        /***************************************************************************************************
         *用于处理服务连接事件的回调函数
         **************************************************************************************************/
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {//初始化不成功
                Log.e("connect","初始化不成功");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            Log.e("connect","连接蓝牙设备");
            mBluetoothLeService.connect(mDeviceAddress);//连接蓝牙设备
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {connectStatusView.setText("开始连接设备("+mDeviceAddress+")");
                }
            });//显示状态*/
        }
        /***************************************************************************************************
         *用于处理服务断开连接事件的回调函数
         **************************************************************************************************/
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Toast.makeText(BluetoothConnect.this,"设备("+mDeviceAddress+")已断开连接", Toast.LENGTH_LONG).show();
            finish();
        }
    };

    /***************************************************************************************************
     *用于处理广播接收事件的回调函数
     **************************************************************************************************/
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))//连接成功
            {
                mConnected = true;
                Log.e("connect","连接成功");
               /* runOnUiThread(new Runnable()
                {
                    @Override
                    public void run() {connectStatusView.setText("设备("+mDeviceAddress+")已连接");}
                });//显示状态*/
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                Log.e("connect","断开连接");
                mConnected = false;
                Toast.makeText(BluetoothConnect.this,"设备("+mDeviceAddress+")已断开连接", Toast.LENGTH_LONG).show();
                finish();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))//服务发现完成
            {
                Log.e("connect","服务发现完成");
               validateService();//验证服务和特性是否满足要求
                 /*发送启动消息*/
                //writeCharacteristic("1");
                polingRunnable.run();//立即开始传感器数据采集
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                Log.e("connect","处理回调的数据并展示");
                receiveData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//处理传感器采集数据
            }
        }
    };


    /*初始化函数*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        /*获取intent，是从ScanActivity传过来的*/
        final Intent intent = getIntent();
        /*给设备名称设备地址赋值后面连接需要用到*/
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        /*获取连接信息*/
        connectStatusView = findViewById(R.id.scan_status);

        /*注册按钮对应的监听函数*/


        Button  sendDataButton= (Button)findViewById(R.id.sendData);

        sendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeCharacteristic("1");
            }
        });

        /*停止发送数据的按钮*/
        Button stopSendButton=(Button) findViewById(R.id.StopSendData);
        stopSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeCharacteristic("0");
            }
        });
        /*连接GATT服务*/


         Log.e("connect","启动服务");
        Intent connectIntent = new Intent(this, BluetoothLeService.class);

        bindService(connectIntent,mServiceConnection,BIND_AUTO_CREATE);
    }

    /***************************************************************************************************
     *OnResume函数
     **************************************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());//注册广播接收器
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);//重连服务
        }
    }
    /***************************************************************************************************
     *广播过滤器
     **************************************************************************************************/
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    /***************************************************************************************************
     *OnPause函数
     **************************************************************************************************/
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);//注销广播接收器
        Log.e("connect","注销广播接收器");
       pollingHandler.removeCallbacks(polingRunnable);//停止信息收集
    }
    /***************************************************************************************************
     *OnCDestroy函数
     **************************************************************************************************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);//解绑服务
        mBluetoothLeService = null;
    }

    /***************************************************************************************************
     *验证服务和特性是否满足要求
     **************************************************************************************************/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void validateService()
    {
        List<BluetoothGattService> gattServicesList = mBluetoothLeService.getSupportedGattServices();//获取发现的服务和特性
        mCharacteristics.clear();
        /*服务遍历*/
        for (BluetoothGattService bluetoothGattService : gattServicesList) {
            /*判断是不是我们需要的服务*/
            if (IStick_Server_UUID.equals(bluetoothGattService.getUuid().toString())){
                Log.e("connect","服务对应的特征值："+bluetoothGattService.getUuid().toString());
                /*服务对应的特征值遍历*/
                List<BluetoothGattCharacteristic> characteristicsList = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : characteristicsList) {
                    /*初始化写特征与读特征*/

                    if (Objects.equals(writeCharacterUUID,bluetoothGattCharacteristic.getUuid().toString())){
                        Log.e("connect","初始化写特征");
                        writeCharacter=bluetoothGattCharacteristic;
                        Log.e("connect","初始化写特征的UUID："+writeCharacter.getUuid().toString());
                    }

                    if (Objects.equals(readCharacterUUID,bluetoothGattCharacteristic.getUuid().toString())){
                        Log.e("connect","初始化读特征");
                        readCharacter=bluetoothGattCharacteristic;
                        Log.e("connect","初始化读特征的UUID："+readCharacter.getUuid().toString());

                    }
                    mCharacteristics.add(bluetoothGattCharacteristic);//加入特征值
                    mBluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristic,true);
                   //Log.e("connect","特性对应的特征值："+bluetoothGattCharacteristic.getUuid().toString());
                }
            }
        }
    }
    /***************************************************************************************************
     *定时采样3个通道称重传感器的重量信息
     **************************************************************************************************/
    Runnable polingRunnable = new Runnable() {
        @Override
        public void run() {
            //调用方法传递特征值并进行广播处理
            mBluetoothLeService.readCharacteristic(readCharacter);

            pollingHandler.postDelayed(this,5000);
        }
    };
    /***************************************************************************************************
     *读取并解析特性和特性值
     **************************************************************************************************/
    private void receiveData(String s)
    {

        Log.e("connect","###############receiveData分隔符###################33");
        Log.e("connect","###############receiveData分隔符###################33");
        Log.e("connect","###############receiveData分隔符###################33");

        final byte[] datas = mCharacteristics.get(2).getValue();//获取特性值
        if (datas != null && datas.length > 0)
        {
            String readstr = mCharacteristics.get(2).getUuid().toString();//获取UUID
            for(byte data:datas)
            {
                readstr += (char)data;//特性值以存储值的形式追加到UUID后面
            }
            Log.e("connect","receiveData方法中获取写取特征的值："+readstr);
        }else{
            Log.e("connect","receiveData方法中获取写特征的值为空");
        }

        Log.e("connect","下一步准备展示数据");
        String temp="";
            if (readCharacter!=null&&readCharacter.getValue()!=null){
                byte[] value = readCharacter.getValue();
                int length = value.length;
                Log.e("connect","数组长度为:"+length);
                String result = TypeConversion.bytes2HexString(value, value.length);
                Log.e("connect","收到字符串的值："+result);
                /*获取并展示对应的数据*/
                TextView ResultIStick =(TextView) findViewById(R.id.connectInformation);
                ResultIStick.setText("对应的数据为:"+result+"长度为："+length);
               // Log.e("connect","打印读取的UUID:"+uuidString);
                Log.e("connect","Result的值："+result);
            }else{
                Log.e("connect","readCharacter值为空");
            }



    }
    /*发送数据*/
    private void writeCharacteristic(String data){
        Log.e("connect","已经发送了对应的数据");
        Log.e("connect",writeCharacter.getUuid().toString());
        mBluetoothLeService.writeCharacteristic(writeCharacter,data);
    }



}
