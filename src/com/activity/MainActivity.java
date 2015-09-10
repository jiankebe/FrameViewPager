package com.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.view.FramePagerAdapter;
import com.view.FrameViewPager;

public class MainActivity extends Activity{
    private FrameViewPager myViewPager;
    private int[] colors = { Color.BLUE, Color.LTGRAY, Color.DKGRAY};
    class FrameAdapter extends FramePagerAdapter {

        @Override
        public void destroyItem(ViewGroup parent, int position, Object object) {
            // TODO Auto-generated method stub
            parent.removeView((View) object);

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 11;
        }

        @Override
        public Object instantiateItem(ViewGroup parent, final int position, boolean next) {
            RelativeLayout tView = new RelativeLayout(getApplicationContext());
            TextView tiTextView = new TextView(getApplicationContext());
            tiTextView.setText("position:" + position);
            tView.addView(tiTextView);
            tView.setBackgroundColor(colors[(int) (Math.random() * colors.length)]);

            tiTextView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Toast.makeText(getApplicationContext(), position + "", Toast.LENGTH_SHORT).show();
                }
            });
            if (next) {
                parent.addView(tView, 0);
            } else {
                parent.addView(tView);
            }
            return tView;
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LinearLayout relativeLayout = new LinearLayout(getApplicationContext());
        relativeLayout.setOrientation(LinearLayout.VERTICAL);
        myViewPager = new FrameViewPager(getApplicationContext());
        FrameAdapter pagerAdapter = new FrameAdapter();
        myViewPager.setAdapter(pagerAdapter);
        myViewPager.setLayoutParams(new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
        Button btn = new Button(getApplicationContext());
        btn.setText("下一页");
        Button btn2 = new Button(getApplicationContext());
        btn2.setText("上一页");
        relativeLayout.addView(btn);
        relativeLayout.addView(btn2);
        relativeLayout.addView(myViewPager);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myViewPager.setCurrentItem(myViewPager.getCurrentItem() + 1);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myViewPager.setCurrentItem(myViewPager.getCurrentItem() - 1);
            }
        });
        setContentView(relativeLayout);
    }
}
