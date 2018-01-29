package org.linphone.wzb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.linphone.CallActivity;
import org.linphone.CallIncomingActivity;
import org.linphone.CallOutgoingActivity;
import org.linphone.LinphoneManager;
import org.linphone.core.LinphoneCall;

/**
 * Created by Administrator on 2018-01-29.
 */

public class CoreReceiver  extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        android.util.Log.e("wzb","CoreReceiver action="+action);
        if(action.equals(CommonAction.CUSTOM_ACTION_GOTO_CALLINCOMING)){
            showCallIncoming(context);
        }else if(action.equals(CommonAction.CUSTOM_ACTION_GOTO_CALLOUTGOING)){
            showCallOutgoing(context);
        }else if(action.equals(CommonAction.CUSTOM_ACTION_GOTO_GoBackToCallIfStillRunning)){
            showGoBackToCallIfStillRunning(context);
        }
    }


    private void showCallIncoming(Context context){
        Intent intent=new Intent(context,CallIncomingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showCallOutgoing(Context context){
        Intent intent=new Intent(context,CallOutgoingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showGoBackToCallIfStillRunning(Context context){

        if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
            if (call.getState() == LinphoneCall.State.IncomingReceived) {
                Intent intent=new Intent(context,CallIncomingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Intent intent=new Intent(context,CallActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }

    }
}
