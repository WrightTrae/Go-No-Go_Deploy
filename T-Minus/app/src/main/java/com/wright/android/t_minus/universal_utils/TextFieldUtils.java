package com.wright.android.t_minus.universal_utils;

public class TextFieldUtils {
    public static boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public static boolean isPasswordInvalid(String password) {
        return password.length() <= 5;
    }

    public static boolean isPhoneNumberInvalid(String number){
        String regexStr = "^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])" +
                "\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})" +
                "\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$";
        return !number.matches(regexStr);
    }
}
