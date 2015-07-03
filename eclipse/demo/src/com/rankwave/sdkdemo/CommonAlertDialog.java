
package com.rankwave.sdkdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;
import android.view.View;

public class CommonAlertDialog
{
	public static AlertDialog showDefaultDialog(Context context, String title, String message, String confirmButtonText, OnClickListener listener)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setPositiveButton(confirmButtonText, listener);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setOnKeyListener(onKeyListener);
		
		return alertDialogBuilder.show();
	}
	
	public static AlertDialog showDefaultDialog(Context context, int title, int message, int resIdConfirmButtonText, OnClickListener listener)
	{
		String sTitle 	= context.getString(title);
		String sMessage = context.getString(message);
		String confirmButtonText = context.getString(resIdConfirmButtonText);
		
		return showDefaultDialog(context, sTitle, sMessage, confirmButtonText, listener);
	}

	public static AlertDialog showDefaultDialogForTwoBtn(Context context, String title, String message, String rightButtonText, OnClickListener rightListener, String leftButtonText, OnClickListener leftListener)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setOnKeyListener(onKeyListener);

		// Left Button
		alertDialogBuilder.setPositiveButton(leftButtonText, leftListener);
		
		// Right Button
		alertDialogBuilder.setNegativeButton(rightButtonText, rightListener);
		
		return alertDialogBuilder.show();
	}
	
	public static AlertDialog showDefaultDialogForTwoBtn(Context context, String title, String message, String rightButtonText, OnClickListener rightListener, String leftButtonText, OnClickListener leftListener, View view)
	{
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setView(view);
		alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setOnKeyListener(onKeyListener);

		// Left Button
		alertDialogBuilder.setPositiveButton(leftButtonText, leftListener);
		
		// Right Button
		alertDialogBuilder.setNegativeButton(rightButtonText, rightListener);
		
		return alertDialogBuilder.show();
	}	
	
	public static AlertDialog showDefaultDialogForTwoBtn(Context context, int title, int message, int rightButtonText,  OnClickListener rightListener, int leftButtonText, OnClickListener leftListener ){
		String sTitle 	= context.getString(title);
		String sMessage = context.getString(message);
		String confirmButtonText = context.getString(leftButtonText);
		String cancelButtonText = context.getString(rightButtonText);
		
		return showDefaultDialogForTwoBtn(context, sTitle, sMessage, cancelButtonText, rightListener, confirmButtonText, leftListener);
	}
	
	static DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
		{
			//There you catch the key, do whatever you want to do.
			//Return true if you handled the key event, so nothing will trigger.
			//Return false if you want your activity to handle.
			return true;
		}
	};
	
}
