package com.gigaStorm.twinRinks;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


/**
 * <code>Activity_About</code> shows extra information about the application to
 * the user.
 * 
 * @author Andrew Mass
 */
public class Activity_About extends Activity {

  private ActionBar actionBar;

  private Button btn_about_rate;

  private Button btn_about_share;

  private Button btn_about_send;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.layout_about);
    getWindow().setBackgroundDrawableResource(android.R.color.black);

    actionBar = getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setTitle("About");

    btn_about_rate = (Button) findViewById(R.id.btn_about_rate);
    btn_about_rate.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri
            .parse("market://details?id=com.gigaStorm.twinRinks"));
        startActivity(Intent.createChooser(rateIntent, "Choose Application:"));
      }
    });

    btn_about_share = (Button) findViewById(R.id.btn_about_share);
    btn_about_share.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent
            .putExtra(Intent.EXTRA_TEXT,
                "Check out this Twin Rinks Adult Hockey Android app. http://goo.gl/ZeGxN");
        startActivity(Intent.createChooser(shareIntent, "Choose Application:"));
      }
    });

    btn_about_send = (Button) findViewById(R.id.btn_about_send);
    btn_about_send.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
            .parse("mailto:amass1212@gmail.com"));
        startActivity(Intent.createChooser(intent, "Choose Application:"));
      }
    });
  }
}
