package com.rankwave.connect.sdk;

public enum IdType {
    ID_TYPE_SNS,
    ID_TYPE_EMAIL,
    ID_TYPE_ANONYMOUS;

    public static String toString(IdType type) {
        if (type == ID_TYPE_SNS)
            return "sns";
        else if (type == ID_TYPE_EMAIL)
            return "email";
        else if (type == ID_TYPE_ANONYMOUS)
            return "anonymous";
        return "";
    }

    public static IdType toEnum(String type) {
        if (type.equals("sns"))
            return IdType.ID_TYPE_SNS;
        else if (type.equals("email")) {
            return IdType.ID_TYPE_EMAIL;
        } else if (type.equals("anonymous")) {
            return IdType.ID_TYPE_ANONYMOUS;
        }
        return null;
    }
}