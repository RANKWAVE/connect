package com.rankwave.sdkdemo;

import android.app.Application;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by ydse on 2015-10-06.
 */
public class DemoApplication extends Application {
    private static final String TWITTER_KEY = "hclIiq4u3s3PT6qXeCIGM3hJy";
    private static final String TWITTER_SECRET = "9ZydYUpC9RaH5DMeUkNkOv07lJcSJbIlpA9lvsdWqLtn891zOf";

    @Override
    public void onCreate() {
        super.onCreate();

        // Example: single kit
         TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
         Fabric.with(this, new Twitter(authConfig));

        // Example: multiple kits
        // Fabric.with(this, new TwitterCore(authConfig), new TweetUi());
    }
}
