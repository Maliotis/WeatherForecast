package com.maliotis.petros.weather.animations;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import java.util.ArrayList;

public class FadeAnimation {

    private ArrayList<ObjectAnimator> mObjectAnimators = new ArrayList<>();
    private AnimatorSet mSet = new AnimatorSet();

    public void add(View view){
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(view);
        objectAnimator.setProperty(View.ALPHA);
        objectAnimator.setFloatValues(0.0f,1.0f);
        objectAnimator.setDuration(1200);
        mObjectAnimators.add(objectAnimator);
        addMove(view);
    }

    private void addMove(View view) {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(view);
        objectAnimator.setProperty(View.TRANSLATION_Y);
        objectAnimator.setFloatValues(0, -30f);
        objectAnimator.setDuration(1000);
        mObjectAnimators.add(objectAnimator);
    }

    public AnimatorSet getAnimatorSet(){
        int i;
        int size = mObjectAnimators.size();
        for(i=0 ; i < size ;i++){
            mSet.playSequentially(mObjectAnimators.get(i));
        }
        return mSet;
    }
}
