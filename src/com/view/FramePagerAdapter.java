/**
 * jianchuanli 2015/9/10/ 16:33
 */
package com.view;

import android.view.ViewGroup;

public abstract class FramePagerAdapter {
    public abstract Object instantiateItem(ViewGroup parent,int position,boolean next);
    public abstract void destroyItem(ViewGroup parent,int position,Object object);
    public abstract int getCount();
}
