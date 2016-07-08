package com.rankwave.connect.sdk;


import android.content.Context;
import android.content.SharedPreferences;

public class ConnectSession {
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_ID_TYPE = "idType";
    private static final String PROPERTY_SNS_TYPE = "snsType";


    /**
     * Session singleton object
     */
    private static ConnectSession instance = null;

    public ConnectSession() {
        init();
    }

    public static ConnectSession getInstance() {
        if (instance == null) {
            synchronized (ConnectSession.class) {
                instance = new ConnectSession();
            }
        }

        return instance;
    }

    public static ConnectSession getConnectSession() {
        return instance;
    }

    /**
     * ConnectSession init
     *
     * @Method Name   : init
     */
    private void init() {
        //user = new User();
    }


    private SharedPreferences getUserPreferences(Context context) {
        return context.getSharedPreferences(Connect.SDK_PREFERENCES,
                Context.MODE_PRIVATE);
    }

    public String loadId() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        String id = prefs.getString(PROPERTY_ID, "");

        return id;
    }

    public void storeId(String id) {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_ID, id);
        editor.commit();
    }

    public void deleteId() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_ID, "");
        editor.commit();
    }

    public String loadIdType() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        String idType = prefs.getString(PROPERTY_ID_TYPE, "");

        return idType;
    }

    public void storeIdType(String idType) {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_ID_TYPE, idType);
        editor.commit();
    }

    public void deleteIdType() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_ID_TYPE, "");
        editor.commit();
    }

    public String loadSnsType() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        String snsType = prefs.getString(PROPERTY_SNS_TYPE, "");

        return snsType;
    }

    public void storeSnsType(String snsType) {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SNS_TYPE, snsType);
        editor.commit();
    }

    public void deleteSnsType() {
        final SharedPreferences prefs = getUserPreferences(Connect.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_SNS_TYPE, "");
        editor.commit();
    }


    public void connectSessionClear() {
        ConnectSession.getInstance().deleteIdType();
        ConnectSession.getInstance().deleteSnsType();
        ConnectSession.getInstance().deleteId();
    }

}
