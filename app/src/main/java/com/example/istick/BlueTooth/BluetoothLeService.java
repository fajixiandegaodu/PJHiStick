package com.example.istick.BlueTooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.istick.Util.TypeConversion;

import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/***************************************************************************************************
 *以服务的形式提供BLE的连接、服务发现、特性值读写等功能
 **************************************************************************************************/
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.yxu.administrator.centroid.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.yxu.administrator.centroid.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.yxu.administrator.centroid.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.yxu.administrator.centroid.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.yxu.administrator.centroid.EXTRA_DATA";
    private final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    /***************************************************************************************************
     * 处理BLE GATT事件的回调函数
     **************************************************************************************************/
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /***************************************************************************************************
         * 处理GATT连接状态改变事件的回调函数
         **************************************************************************************************/
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                Log.e("connect","发送连接成功的广播");
                broadcastUpdate(intentAction);//发送广播通知“连接成功”
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断开连接
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);//发送广播通知“断开连接”
            }
        }
        /***************************************************************************************************
         * 处理GATT服务发现完成事件的回调函数
         **************************************************************************************************/
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e("connect","发送广播通知服务发现完成，下一步为服务发现完成");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);//发送广播通知服务发现完成
            }
        }
        /***************************************************************************************************
         * 处理GATT特性读取完成事件的回调函数
         **************************************************************************************************/
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("connect","发送广播通知特性读取完成，下一步为处理回调的数据并展示");
                broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);//发送广播通知特性读取完成
            }
        }
        /***************************************************************************************************
         * 处理GATT特性值改变的回调函数，特性为notify且发生改变时才会调用
         **************************************************************************************************/
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e("connect","发送广播通知改变的特性为notify且发生改变时才会调用");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);//发送广播通知特性读取完成
        }

        /*
        * 写函数的回调方法
        * */

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e("connect","发送了数据之后的回调");
            broadcastUpdate(ACTION_DATA_AVAILABLE,characteristic);
        }
    };
    /***************************************************************************************************
     * 发送广播通知BLE GATT状态改变事件
     **************************************************************************************************/
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);//以action为参数
        sendBroadcast(intent);//发送广播
    }
    /***************************************************************************************************
     * 发送广播通知特性读取完成事件，以UUID和特性值作为额外参数
     **************************************************************************************************/
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);//以action为参数
        // For all other profiles, writes the data formatted in HEX.
        final byte[] datas = characteristic.getValue();//获取特性值

        if (datas != null && datas.length > 0)
        {
            String s = characteristic.getUuid().toString();//获取UUID
            //Log.e("connect","UUID:"+s);
            String s1 = TypeConversion.bytes2HexString(datas, datas.length);

            // intent.putExtra(EXTRA_DATA, s);

            Log.e("connect","#####测试分隔符########");
            Log.e("connect","#####测试分隔符########");
            Log.e("connect","#####测试分隔符########");
            Log.e("connect","characteristicUUID为："+s);
            Log.e("connect","characteristic内容为："+s1);

           // sendBroadcast(intent);//发送广播
        }


        Log.e("connect","读取数据成功，发送广播");
        intent.putExtra(EXTRA_DATA, "下一步为显示数据");
        sendBroadcast(intent);//发送广播
    }
    /***************************************************************************************************
     * LocalBinder类，getService函数返回BluetoothLeService的实例以便可以使用服务中的公用方法
     **************************************************************************************************/
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    /***************************************************************************************************
     * 绑定服务时才会调用，必须要实现的方法，方便Activity后续的连接和读写操作；
     **************************************************************************************************/
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /***************************************************************************************************
     * 解绑时调用的函数当服务调用unbindService时，服务的生命周期将会进入onUnbind()方法；接着执行了关闭蓝牙的方法；
     **************************************************************************************************/
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /***************************************************************************************************
     * 初始化BLE，成功返回true，失败返回false
     **************************************************************************************************/
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
       Log.e("connect","初始化蓝牙适配器成功");
        return true;
    }

    /***************************************************************************************************
     * 以mac地址为参数连接蓝牙设备的GATT服务器
     **************************************************************************************************/
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // 之前已经连接过，重新连接
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //第一次，通过mac地址连接
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);//连接GATT服务器
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;

        Log.e("connect","使用connect连接蓝牙成功");
        return true;
    }

    /***************************************************************************************************
     * 断开GATT服务器连接
     **************************************************************************************************/
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /***************************************************************************************************
     * 关闭GATT服务器
     **************************************************************************************************/
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /***************************************************************************************************
     * 读取GATT特性，结果在onCharacteristicRead函数中处理
     **************************************************************************************************/
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.e("connect","正在读取数据");
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    /***************************************************************************************************
     * 写GATT特性，结果暂时不用处理
     **************************************************************************************************/
    /*data为写入的数据，characteristic为对应的特性值*/
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,String data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        byte[] value=new byte[8];
        value[0]=(byte)0x01;
        //1 为发送数据 0为停止发送数据
        if ("1".equals(data)){
            Log.e("connect","发送数据");
            value[0]=(byte)0x01;
        }else if ("0".equals(data)){
            Log.e("connect","停止发送数据");
            value[0]=(byte)0x00;
        }
        characteristic.setValue(value[0], BluetoothGattCharacteristic.FORMAT_UINT8,0);
        Log.e("connect","写入字符串的值为："+value[0]);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    /***************************************************************************************************
     * 打开或关闭特性的notify功能
     **************************************************************************************************/
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        final int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0)
            return;

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        final BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic,true);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    /***************************************************************************************************
     * 获取蓝牙设备支持的服务及对应的特性，在发现服务完成后才能调用
     **************************************************************************************************/
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }


}
