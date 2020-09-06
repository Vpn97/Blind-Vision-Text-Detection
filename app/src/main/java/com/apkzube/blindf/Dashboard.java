package com.apkzube.blindf;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.apkzube.blindf.Fragments.TextDetectionFragment;

public class Dashboard extends AppCompatActivity {

    //Widget Init[
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        allocation();
        setEvent();
    }

    private void allocation() {

        //allocation of widget
        viewPager=findViewById(R.id.viewPager);
        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));

    }

    private void setEvent() {

    }
}

    //ViewPager Adapter Class:
     class MyPagerAdapter extends FragmentPagerAdapter{

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //position of fragment:
            switch (position){
                //Color Detection
                case 0:
                    return TextDetectionFragment.newInstance("Text Detection");

                //Text Detection


                //Text Detection Default
                default:
                    return TextDetectionFragment.newInstance("Text Detection");
            }
        }



        @Override
        public int getCount() {
            return 1;
        }
    }
