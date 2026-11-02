package com.autenticar.tuid.resaiver;

import com.autenticar.tuid.utils.HelperFunctions;

public class SMSCallback{

    private static String code="";
    public static void smsReceived(String sms) {
        String value[]= sms.split(" ");
        if(value[0].toUpperCase().equals("TUID")){
            if(value[1].length()==4){
                code=value[1];
                HelperFunctions.setCodeMovil(code);
            }else{
                HelperFunctions.setCodeMovil("");
            }
        }
    }
}
