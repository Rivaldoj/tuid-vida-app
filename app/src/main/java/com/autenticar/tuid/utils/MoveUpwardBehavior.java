package com.autenticar.tuid.utils;

import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.design.widget.CoordinatorLayout;
//import android.support.design.widget.Snackbar;
//import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.Snackbar;

public class MoveUpwardBehavior extends CoordinatorLayout.Behavior<View> {
    public MoveUpwardBehavior(){
        super();
    }
    public MoveUpwardBehavior(Context  context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
        float translationY=Math.min(0, ViewCompat.getTranslationY(dependency)- dependency.getHeight());
        ViewCompat.setTranslationY(child, translationY);
        return  true;
    }
    public void  onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency){
        ViewCompat.animate(child).translationY(0).start();
    }
}
