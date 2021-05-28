package com.example.istick;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.istick.Adapter.TestBeanAdapter;
import com.example.istick.Bean.TestBean;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<TestBean> list=new ArrayList<TestBean>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_list);
        Log.e("lev","正在初始化数组");
        for (int i = 0; i < 10; i++) {
            TestBean testBean = new TestBean( "名字" + i);
            list.add(testBean);
        }
        Log.e("lev","拿到listview对象,是展示的对象");
        /*拿到listview对象,是展示的对象*/
        ListView listview= findViewById(R.id.test_list);
        Log.e("lev","设置Adapt对象");
        /*设置Adapt对象*/
        listview.setAdapter(new TestBeanAdapter(list,this));
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("leo", "onItemClick: "+position );
            }
        });
    }
}