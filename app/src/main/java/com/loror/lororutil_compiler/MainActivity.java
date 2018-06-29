package com.loror.lororutil_compiler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororUtil.view.ViewUtil;

public class MainActivity extends AppCompatActivity {

    @Find(R.id.text)
    TextView textView;
    @Find(R.id.list)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtil.find(this);
        textView.setText("oihfiohifn");
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.test_list_item, new String[]{"1", "2", "3"}));
    }

    @Click(id = R.id.text)
    public void cick(View view) {
        Toast.makeText(this, "点击了text", Toast.LENGTH_SHORT).show();
    }

    @ItemClick(id = R.id.list)
    public void itemClick(View v, int position) {
        Toast.makeText(this, "点击了list:" + position, Toast.LENGTH_SHORT).show();
    }
}
