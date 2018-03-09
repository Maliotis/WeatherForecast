package com.maliotis.petros.weather.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.ArrayList;

public class FadeAnimation {

    ArrayList<ObjectAnimator> mObjectAnimators = new ArrayList<>();
    AnimatorSet mSet = new AnimatorSet();

    public void add(View view){
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(view);
        objectAnimator.setProperty(view.ALPHA);
        objectAnimator.setFloatValues(0.0f,1.0f);
        objectAnimator.setDuration(1000);
        mObjectAnimators.add(objectAnimator);
    }

    public AnimatorSet getAnimatorSet(){
        int i;
        int size = mObjectAnimators.size();
        for(i=0 ; i < size ;i++){
            mSet.playTogether(mObjectAnimators.get(i));
        }
        return mSet;
    }
}
