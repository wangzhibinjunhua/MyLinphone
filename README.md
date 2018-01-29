# 基于 Linphone4Android 项目修改


# 接口
可以将项目作为一个Library，进行二次开发，根据业务需求来修改源码。

## 登录
``` java
// 开启线程登录
ServiceWaitThread mThread = new ServiceWaitThread();
mThread.start();

private void syncAccount(String username, String password, String domain) {

    LinphonePreferences mPrefs = LinphonePreferences.instance();
    if (mPrefs.isFirstLaunch()) {
        mPrefs.setAutomaticallyAcceptVideoRequests(true);
//            mPrefs.setInitiateVideoCall(true);
        mPrefs.enableVideo(true);
    }
    int nbAccounts = mPrefs.getAccountCount();
    if (nbAccounts > 0) {
        String nbUsername = mPrefs.getAccountUsername(0);
        if (nbUsername != null && !nbUsername.equals(username)) {
            mPrefs.deleteAccount(0);
            saveNewAccount(username, password, domain);
        }
    } else {
        saveNewAccount(username, password, domain);
        mPrefs.firstLaunchSuccessful();
    }
}

private void saveNewAccount(String username, String password, String domain) {
    LinphonePreferences.AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
            .setUsername(username)
            .setDomain(domain)
            .setPassword(password)
            .setDisplayName(Const.LINPHONE_NAME)
            .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);

    try {
        builder.saveNewAccount();
    } catch (LinphoneCoreException e) {
        Log.e(e);
    }
}

private class ServiceWaitThread extends Thread {
    public void run() {
        while (!LinphoneService.isReady()) {
            try {
                sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException("waiting thread sleep() has been interrupted");
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                syncAccount(Const.LINPHONE_ACCOUNT, Const.LINPHONE_PWD, Const.HOST);
            }
        });
        mThread = null;
    }
}

```

## 呼出
``` java

private void callOutgoing(String number) {
    try {
        if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
            String to = String.format("sip:%s@%s", number, Const.HOST);
            LinphoneManager.getInstance().newOutgoingCall(to, displayName);

            startActivity(new Intent()
                    .setClass(this, LinphoneActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    } catch (LinphoneCoreException e) {
        LinphoneManager.getInstance().terminateCall();
    }
}

```

