package com.example.istick.BlueTooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.istick.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/***************************************************************************************************
 * 用于显示蓝牙扫描结果的适配器
 **************************************************************************************************/
public class LeDeviceListAdapter2 extends BaseAdapter {
    private ArrayList<LeDevice> mLeDevices;//蓝牙设备列表
    private int resourceID;//保存初始化时传入的viewId
    private Context context;//保存初始化时传进来的context
    /***************************************************************************************************
     * LeDeviceListAdapter构造函数
     **************************************************************************************************/
    public LeDeviceListAdapter2(Context context, int viewResourceId) {
        super();
        this.context = context;
        resourceID = viewResourceId;//获取数据
        mLeDevices = new ArrayList<LeDevice>();
    }
    /***************************************************************************************************
     * 添加扫描到的设备
     **************************************************************************************************/
    public void addDevice(LeDevice device) {

        Log.e("scann1", "增加的设备的名词"+device.name );
        //设备已经存在，仅更新RSS值
        for(int i = 0; i<mLeDevices.size(); i++){
            if(mLeDevices.get(i).getMac().equals(device.getMac()))
            {
                mLeDevices.get(i).setRss(device.getRss());
                return;
            }
        }

        //新设备，添加到列表
        mLeDevices.add(device);
        Collections.sort(mLeDevices,new SortByRss());//按信号强度值从大到小排序

    }
    /***************************************************************************************************
     *Sort的比较器
     **************************************************************************************************/
    class SortByRss implements Comparator {

        public int compare(Object o1, Object o2) {
            Log.e("scann1", "比较排序");
            LeDevice s1 = (LeDevice) o1;
            LeDevice s2 = (LeDevice) o2;
            if (s1.getRss() < s2.getRss())
              return 1;
            else
              return -1;
        }
    }
    /***************************************************************************************************
     * 清除设备列表
     **************************************************************************************************/
    public void clearDevice() {
        if (mLeDevices.size() > 0) {
            mLeDevices.clear();
        }
    }
    /***************************************************************************************************
     * 获取扫描到设备的数量
     **************************************************************************************************/
    @Override
    public int getCount() {
        Log.e("scann1", "获取扫描到设备的数量"+mLeDevices.size());
        for (LeDevice mLeDevice : mLeDevices) {
            Log.e("scann1", "扫描到设备的名称"+mLeDevice.name);
        }
        return mLeDevices.size();
    }

    /***************************************************************************************************
     * 获取设备
     **************************************************************************************************/
    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }
    /***************************************************************************************************
     * 获取设备Id
     **************************************************************************************************/
    @Override
    public long getItemId(int i) {
        return i;
    }
    /***************************************************************************************************
     * getView函数
     **************************************************************************************************/
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        Log.e("scann1", "获取视图");
        ViewHolder viewHolder;
        View view;
        // General ListView optimization code.
        if (convertView == null) {//第一次，初始化
            view = LayoutInflater.from(context).inflate(resourceID, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.device_name);
            viewHolder.mac = (TextView) view.findViewById(R.id.mac_address);
            viewHolder.rss = (TextView) view.findViewById(R.id.receive_rss);
            view.setTag(viewHolder);
        } else {//直接用缓存的view
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        //设置显示内容
        LeDevice device = mLeDevices.get(i);
        viewHolder.name.setText(device.getName());
        viewHolder.mac.setText(device.getMac());
        viewHolder.rss.setText(device.getRss() + " dBm");


        return view;
    }
    class ViewHolder {
        TextView name;
        TextView mac;
        TextView rss;
    }

    /**
     * Created by Administrator on 2018/3/14.
     */
    /***************************************************************************************************
     * LeDevice类
     **************************************************************************************************/
    public static class LeDevice {
        String name;
        String mac;
        int rss;

        public LeDevice(String name, String mac, int rss) {
            this.name = name;
            this.mac = mac;
            this.rss = rss;
        }

        public String getName(){
            return name;
        }

        public String getMac(){
            return mac;
        }

        public int getRss(){
            return rss;
        }

        public void setRss(int rss){
            this.rss = rss;
        }
    }
}
