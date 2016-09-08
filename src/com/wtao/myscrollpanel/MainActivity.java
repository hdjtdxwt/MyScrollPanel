package com.wtao.myscrollpanel;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wtao.myscrollpanel.ScrollPanelListView.OnPositionChangedListener;


public class MainActivity extends Activity implements OnPositionChangedListener {
	MyListAdapter adapter = new MyListAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ScrollPanelListView listView = (ScrollPanelListView) findViewById(R.id.mylist);
        
        listView.setAdapter(adapter);
        listView.setOnPositionChangedListener(this);
        
    }

	class MyListAdapter extends BaseAdapter{
		List<String> list ;
		public MyListAdapter(){
			list = new ArrayList<String>();
			for(int i=0;i<50;i++){
				list.add("item"+i);
			}
		}
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.adapter_item, null);
				ViewHolder holder = new ViewHolder(convertView);
				convertView.setTag(holder);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.textView.setText(list.get(position));
			return convertView;
		}
		class ViewHolder {
			TextView textView;
			public ViewHolder(View view){
				textView = (TextView) view.findViewById(R.id.text);
			}
		}
		
	}

	@Override
	public void onPositionChanged(ScrollPanelListView listView, int position,
			View indicatorView) {
		((TextView)indicatorView).setText("Position "+position);
	}
}
