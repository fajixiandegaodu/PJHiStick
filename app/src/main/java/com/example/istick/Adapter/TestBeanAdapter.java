package com.example.istick.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.istick.Bean.TestBean;
import com.example.istick.BlueTooth.LeDeviceListAdapter;
import com.example.istick.R;

import java.util.ArrayList;
/*展示数据*/
public class TestBeanAdapter extends BaseAdapter {
    private ArrayList<TestBean> testBeansArray;//蓝牙设备列表
    private int resourceID;//保存初始化时传入的viewId
    private Context context;//保存初始化时传进来的context
    /*构造方法，初始化相关参数*/

    public TestBeanAdapter(ArrayList<TestBean> testBeansArray, Context context) {
        this.testBeansArray = testBeansArray;
        this.context = context;
    }

    @Override
    public int getCount() {
        return testBeansArray.size();
    }

    @Override
    public TestBean getItem(int position) {
        return testBeansArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("lev","getView"+"方法执行");
        if (convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.test_list_item,parent,false);
        }
        TextView itemView= convertView.findViewById(R.id.test_item);
        itemView.setText(((TestBean)testBeansArray.get(position)).getName());
        Log.e("lev",testBeansArray.get(position).toString());
        return convertView;
    }


}
