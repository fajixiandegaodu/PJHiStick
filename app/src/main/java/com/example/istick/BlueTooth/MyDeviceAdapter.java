package com.example.istick.BlueTooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.istick.Bean.TestBean;
import com.example.istick.R;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;

public class MyDeviceAdapter extends BaseAdapter {
    private ArrayList<LeDevice> mLeDevices;//蓝牙设备列表
    private int resourceID;//保存初始化时传入的viewId
    private Context context;//保存初始化时传进来的context

    public MyDeviceAdapter(int resourceID, Context context) {
        this.resourceID = resourceID;
        this.context = context;
        mLeDevices=new ArrayList<LeDevice>(); //创建一个新的链表
    }

    /***************************************************************************************************
     * 添加扫描到的设备
     **************************************************************************************************/
    public void addDevice(LeDevice device) {
        Log.e("scanner2" , "添加设备:" );
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
            Log.e("scanner2" , "比较方法:" );
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
        Log.e("scanner2" , "获取蓝牙的数量 :"+mLeDevices.size() );
        Log.e("scanner2" , "遍历蓝牙数组" );
        for (LeDevice mLeDevice : mLeDevices) {
            Log.e("myAdapt" , "蓝牙名称："+mLeDevice.name );
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("scanner2" , "getView方法执行 " );
        if (convertView==null){
            convertView= LayoutInflater.from(context).inflate(resourceID,parent,false);
        }
        TextView itemView= convertView.findViewById(R.id.test_item);
        itemView.setText( "等待");
        return convertView;
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
