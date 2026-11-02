package com.autenticar.tuid.resaiver;

import android.content.*;
import android.os.Bundle;
import android.telephony.*;
import android.util.Log;

public class SMSListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context cntxt, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String msgBody = msgs[i].getMessageBody();
                        SMSCallback.smsReceived(msgBody);
                    }
                } catch (Exception e) {
                    Log.e("ERROR ", String.valueOf(e));
                }
            }
        }
    }
}
