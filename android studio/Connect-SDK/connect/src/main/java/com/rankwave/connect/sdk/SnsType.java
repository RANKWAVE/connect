package com.rankwave.connect.sdk;

public enum SnsType {
    SNS_TYPE_FACEBOOK,
    SNS_TYPE_TWITTER,
    SNS_TYPE_KAKAO;


    public static String toString(SnsType type) {
        if (type == SNS_TYPE_FACEBOOK)
            return "FB";
        else if (type == SNS_TYPE_TWITTER)
            return "TW";
        else if (type == SNS_TYPE_KAKAO)
            return "KO";
        return "";
    }

    public static SnsType toEnum(String type) {
        if (type.equals("FB"))
            return SnsType.SNS_TYPE_FACEBOOK;
        else if (type.equals("TW"))
            return SnsType.SNS_TYPE_TWITTER;
        else if (type.equals("KO"))
            return SnsType.SNS_TYPE_KAKAO;
        return null;
    }
}