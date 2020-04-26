package cheerly.mybaseproject.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class M2Utils {
    public static String getM2(Context context) {
        try {
            String m2 = MD5Util.md5LowerCase(getDeviceId(context) + getAndroidId(context) + getSerialNo());
            return m2;
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
        return "";
    }

    private static String getAndroidId(Context context) {
    	try{
    		  String androidId = Settings.System.getString(context.getContentResolver(), "android_id");
    	        return androidId;
    	}catch(Exception e){
    	}
    	return "";
    }

    private static String getSerialNo() {
        String sNo = null;
        try{
            Class<?> localClass = Class.forName("android.os.SystemProperties");
            Method localMethod = localClass.getMethod("get", new Class[] {
                String.class
            });
            sNo = (String) localMethod.invoke(localClass, new Object[] {
                "ro.serialno"
            });
        } catch (Exception localException){
            sNo = "";
        }
        return sNo;
    }
    
    /**
     * 返回IMEI的MD5
     * 
     * @param context
     * @return
     */
    private static String ANDROID_IMEI;
    private static String ANDROID_IMEI_MD5;
    private static String getAndroidImeiMd5(final Context context) {
    	   if ((ANDROID_IMEI_MD5 == null) && (context != null)) {
               ANDROID_IMEI_MD5 = MD5Util.md5LowerCase(getImei(context));
           }
           return ANDROID_IMEI_MD5;
    }
    
    // getDeviceId or wifiMac Address
    private static String getDeviceId(Context context) {
        String deviceId = null;
        if (context != null) {
            deviceId = getImei(context);

            if (TextUtils.isEmpty(deviceId)) {
                deviceId = getWifiMac(context);
            }
        }

        deviceId = TextUtils.isEmpty(deviceId) ? "default" : deviceId;
        return deviceId;
    }

    private static String getImei(Context context) {
    	if (!TextUtils.isEmpty(ANDROID_IMEI)) {
			return ANDROID_IMEI;
		}
    	
        String imei = null;
        if (context != null) {
            
            try{
            	TelephonyManager telManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (telManager != null) {
                    imei = telManager.getDeviceId();
                    ANDROID_IMEI = imei;
                }
            }catch(Exception e){

            }
        }
        return imei;
    }
    
    private static String getWifiMac(Context context) {
        String macAddr = null;
        if (context != null) {
            try {
                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifiMgr != null) {
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    if (wifiInfo != null) {
                        macAddr = wifiInfo.getMacAddress();
                    }
                }
            } catch (Exception e) {
            } catch (Error e) {
            }
        }
        return macAddr;
    }

}
