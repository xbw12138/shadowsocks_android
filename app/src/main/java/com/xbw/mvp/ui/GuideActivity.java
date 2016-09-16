package com.xbw.mvp.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xbw.mvp.R;

import java.util.ArrayList;

public class GuideActivity extends Activity {
    private ViewPager viewPager;

    private ArrayList<View> pageViews;
    private ImageView imageView;
    private ImageView[] imageViews;
    private ViewGroup viewPics;
    private ViewGroup viewPoints;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        pageViews = new ArrayList<View>();
        pageViews.add(inflater.inflate(R.layout.viewpager_page1, null));
        pageViews.add(inflater.inflate(R.layout.viewpager_page2, null));
        pageViews.add(inflater.inflate(R.layout.viewpager_page3, null));
        imageViews = new ImageView[pageViews.size()];
        viewPics = (ViewGroup) inflater.inflate(R.layout.activity_guide, null);
        viewPoints = (ViewGroup) viewPics.findViewById(R.id.viewGroup);
        viewPager = (ViewPager) viewPics.findViewById(R.id.guidePages);
        for(int i=0;i<pageViews.size();i++){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    20, 20);
            params.setMargins(0, 13, 13, 13);
            imageView = new ImageView(GuideActivity.this);
            imageView.setLayoutParams(params);
            imageViews[i] = imageView;
            if(i==0){
                imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused);
            }else{
                imageViews[i].setBackgroundResource(R.drawable.page_indicator);
            }
            viewPoints.addView(imageViews[i]);
        }
        setContentView(viewPics);
        viewPager.setAdapter(new GuidePageAdapter());
        viewPager.setOnPageChangeListener(new GuidePageChangeListener());
    }
    private ImageButton.OnClickListener  Button_OnClickListener = new ImageButton.OnClickListener() {
        public void onClick(View v) {
            setGuided();
            Intent mIntent = new Intent();
            mIntent.setClass(GuideActivity.this, LoginActivity.class);
            GuideActivity.this.startActivity(mIntent);
            GuideActivity.this.finish();
        }
    };

    private static final String SHAREDPREFERENCES_NAME = "my_pref";
    private static final String KEY_GUIDE_ACTIVITY = "guide_activity";
    private void setGuided(){
        SharedPreferences settings = getSharedPreferences(SHAREDPREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_GUIDE_ACTIVITY, "false");
        editor.commit();
    }
    class GuidePageAdapter extends PagerAdapter {
        @Override
        public void destroyItem(View v, int position, Object arg2) {
            // TODO Auto-generated method stub 
            ((ViewPager)v).removeView(pageViews.get(position));
        }
        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub      
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub 
            return pageViews.size();
        }
        @Override
        public Object instantiateItem(View v, int position) {
            // TODO Auto-generated method stub 
            ((ViewPager) v).addView(pageViews.get(position));
            if (position == 2) {
                TextView btn = (TextView) v.findViewById(R.id.btn_close_guide);
                btn.setOnClickListener(Button_OnClickListener);
            }
            return pageViews.get(position);
        }
        @Override
        public boolean isViewFromObject(View v, Object arg1) {
            // TODO Auto-generated method stub 
            return v == arg1;
        }
        @Override
        public void startUpdate(View arg0) {
            // TODO Auto-generated method stub        
        }
        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub 
            return super.getItemPosition(object);
        }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub     
        }
        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub 
            return null;
        }
    }
    class GuidePageChangeListener implements OnPageChangeListener{
        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub 
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub 
        }
        @Override
        public void onPageSelected(int position) {
            // TODO Auto-generated method stub 
            for(int i=0;i<imageViews.length;i++){
                imageViews[position].setBackgroundResource(R.drawable.page_indicator_focused);
                if(position !=i){
                    imageViews[i].setBackgroundResource(R.drawable.page_indicator);
                }
            }

        }
    }
}

