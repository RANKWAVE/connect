package com.rankwave.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class IntroActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		
		ImageView iv_cloud1 = (ImageView)findViewById(R.id.iv_cloud_1);		
		ImageView iv_cloud2 = (ImageView)findViewById(R.id.iv_cloud_2);
		ImageView iv_cloud3 = (ImageView)findViewById(R.id.iv_cloud_3);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
		translateAnimation(-metrics.widthPixels, metrics.widthPixels, 2000, iv_cloud1);
		translateAnimation(metrics.widthPixels, -metrics.widthPixels, 2000, iv_cloud2);
		translateAnimation(-metrics.widthPixels, metrics.widthPixels, 4000, iv_cloud3);
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				startActivity(new Intent(IntroActivity.this, LoginActivity.class));
				finish();
			}
		}, 2000);
	}
	
	public void translateAnimation(int formX, int toX, int duration, View view) {
		
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
			
		Animation translate = new TranslateAnimation(formX, toX, 0.0f,
				0.0f);

		translate.setDuration(duration);
		translate.setInterpolator(AnimationUtils.loadInterpolator(
				this,
				android.R.anim.accelerate_interpolator));

		translate.setFillAfter(true);
		
		view.startAnimation(translate);		
	}
}
