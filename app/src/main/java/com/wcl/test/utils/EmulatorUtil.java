package com.wcl.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.wcl.test.listener.OnFinishedListener;

public class EmulatorUtil {
    private EmulatorUtil() {
    }

    private static class SingletonHolder {
        private static final EmulatorUtil INSTANCE = new EmulatorUtil();
    }

    public static final EmulatorUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static class PropertyInfo {
        public boolean isEmulator;//是否为模拟器
        public String desc;//具体的信息
        public Object obj;

        public PropertyInfo(boolean isEmulator, String desc) {
            this.isEmulator = isEmulator;
            this.desc = desc;
        }


        @Override
        public String toString() {
            return "(" + isEmulator + ") " + desc;
        }
    }

    //获取基带信息
    private PropertyInfo getBaseBand() {
        String baseBandVersion = getProperty("gsm.version.baseband");
        boolean isEmulator = (null == baseBandVersion || baseBandVersion.contains("1.0.0.0"));
        return new PropertyInfo(isEmulator, baseBandVersion);
    }

    //获取模拟器渠道
    private PropertyInfo getBuildFlavor() {
        String buildFlavor = getProperty("ro.build.flavor");
        boolean isEmulator = false;
        if (!TextUtils.isEmpty(buildFlavor)) {
            if (buildFlavor.contains("vbox") || buildFlavor.contains("sdk_gphone")) {
                isEmulator = true;
            }
        }
        return new PropertyInfo(isEmulator, buildFlavor);
    }

    //获取处理器芯片名称
    private PropertyInfo getProductBoard() {
        String productBoard = getProperty("ro.product.board");
        boolean isEmulator = (null == productBoard || productBoard.contains("android") || productBoard.contains("goldfish"));
        return new PropertyInfo(isEmulator, productBoard);
    }

    //获取芯片平台
    private PropertyInfo getBoardPlatform() {
        String boardPlatform = getProperty("ro.board.platform");
        boolean isEmulator = (null == boardPlatform || boardPlatform.contains("android"));
        return new PropertyInfo(isEmulator, boardPlatform);
    }

    //获取模拟器名称
    private PropertyInfo getHardWare() {
        String hardWare = getProperty("ro.hardware");
        boolean isEmulator = (hardWare == null //
                || hardWare.toLowerCase().contains("ttvm") //
                || hardWare.toLowerCase().contains("nox")//
                || hardWare.toLowerCase().contains("vbox86")//
                || hardWare.toLowerCase().contains("yiwan"));//
        return new PropertyInfo(isEmulator, hardWare);
    }

    //获取传感器数量
    private PropertyInfo getSensorNum(Context context) {
        boolean isEmulator = false;
        String desc = null;

        boolean isSupportCameraFlash = context.getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        if (!isSupportCameraFlash) {
            return new PropertyInfo(true, "不支持闪光灯");
        }

        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        int sensorSize = sm.getSensorList(Sensor.TYPE_ALL).size();
        desc = sensorSize + "个";

        if (sensorSize <= 10) {
            isEmulator = true;
        }
        return new PropertyInfo(isEmulator, desc);
    }

    //获取用户自行安装的App数量
    private PropertyInfo getUserApps() {
        boolean isEmulator = false;
        String desc = null;

        String userApps = CommandUtil.getSingleInstance().exec("pm list package -3");
        int userAppSize = getUserAppNums(userApps);
        if (userAppSize <= 3) {
            isEmulator = true;
        }
        desc = userAppSize + "个";

        return new PropertyInfo(isEmulator, desc);
    }

    //获取进程组信息
    private PropertyInfo getProcGroup() {
        String filter = CommandUtil.getSingleInstance().exec("cat /proc/self/cgroup");
        boolean isEmulator = TextUtils.isEmpty(filter);
        return new PropertyInfo(isEmulator, filter);
    }

    //是否可以处理跳转到拨号的Intent
    private PropertyInfo getPhoneIntent(Context context) {
        String desc;
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);

        boolean canResolveIntent = intent.resolveActivity(context.getPackageManager()) != null;
        boolean isEmulator = !canResolveIntent;

        if (isEmulator) {
            desc = "不可以唤起";
        } else {
            desc = "可以唤起";
        }

        return new PropertyInfo(isEmulator, desc);
    }

    //判断是否为平板
    private PropertyInfo isPad(Context context) {
        if (Build.MODEL.contains("MI PAD")) {
            return new PropertyInfo(false, "小米平板");
        }
        
        //基带信息为null并且传感器数量>10就是平板
        PropertyInfo baseBand = getBaseBand();
        if (baseBand.isEmulator){
            SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            int sensorSize = sm.getSensorList(Sensor.TYPE_ALL).size();

            if (sensorSize > 10) {
            	return new PropertyInfo(false, "平板");
            }
        }
        
        return new PropertyInfo(false, "未知");
    }

    /**
     * 判断是否为模拟器
     */
    public void isEmulator(final Context context, final OnFinishedListener<Boolean, PropertyInfo> listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                PropertyInfo baseBand = getBaseBand();
                PropertyInfo buildFlavor = getBuildFlavor();
                PropertyInfo productBoard = getProductBoard();
                PropertyInfo boardPlatform = getBoardPlatform();
                PropertyInfo hardWare = getHardWare();
                PropertyInfo snsorNum = getSensorNum(context);
                PropertyInfo userApps = getUserApps();
                PropertyInfo procGroup = getProcGroup();
                PropertyInfo phoneIntent = getPhoneIntent(context);

                int count = 0;
                StringBuilder builder = new StringBuilder();
                builder.append("基带信息：" + baseBand);
                builder.append("\n\n模拟器渠道：" + buildFlavor);
                builder.append("\n\n处理器芯片名称：" + productBoard);
                builder.append("\n\n芯片平台：" + boardPlatform);
                builder.append("\n\n模拟器名称：" + hardWare);
                builder.append("\n\n传感器数量<=10会被判断为模拟器：" + snsorNum);
                builder.append("\n\n用户安装的软件数量<=3会被判断为模拟器：" + userApps);
                builder.append("\n\n正常手机有进程组，若无则被判断为模拟器：" + procGroup);
                builder.append("\n\n可否唤起拨号键盘页面：" + phoneIntent);

                if (baseBand.isEmulator) {
                    count++;
                }
                if (buildFlavor.isEmulator) {
                    count++;
                }
                if (productBoard.isEmulator) {
                    count++;
                }
                if (boardPlatform.isEmulator) {
                    count++;
                }
                if (hardWare.desc != null) {
                    if (hardWare.desc.toLowerCase().contains("ttvm") || hardWare.desc.toLowerCase().contains("nox")) {
                        count = 10;
                    }
                }
                if (snsorNum.isEmulator) {
                    count++;
                }
                if (userApps.isEmulator) {
                    count++;
                }
                if (procGroup.isEmulator) {
                    count++;
                }
                if (phoneIntent.isEmulator) {
                    count++;
                }

                boolean isEmulator = (count >= 2);
                final PropertyInfo info = new PropertyInfo(isEmulator, builder.toString());
                if (!isPad(context).isEmulator) {
                    info.isEmulator = false;
                }
                
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("baseBand", baseBand.isEmulator + "");
				map.put("baseBand_val", baseBand.desc);
				map.put("buildFlavor", buildFlavor.isEmulator + "");
				map.put("buildFlavor_val", buildFlavor.desc + "");
				map.put("productBoard", productBoard.isEmulator + "");
				map.put("productBoard_val", productBoard.desc + "");
				map.put("boardPlatform", boardPlatform.isEmulator + "");
				map.put("boardPlatform_val", boardPlatform.desc + "");
				map.put("hardWare", hardWare.isEmulator + "");
				map.put("hardWare_val", hardWare.desc + "");
				map.put("snsorNum", snsorNum.isEmulator + "");
				map.put("snsorNum_val", snsorNum.desc + "");
				map.put("userApps", userApps.isEmulator + "");
				map.put("userApps_val", userApps.desc + "");
				map.put("procGroup", procGroup.isEmulator + "");
				map.put("procGroup_val", procGroup.desc + "");
				map.put("phoneIntent", phoneIntent.isEmulator + "");
				map.put("phoneIntent_val", phoneIntent.desc + "");
				info.obj = map;

                Activity activity = (Activity) context;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFinished(true, info);
                    }
                });
            }
        }).start();

    }

    private int getUserAppNums(String userApps) {
        String[] result = userApps.split("package:");
        return result.length;
    }

    private String getProperty(String propName) {
        String property = CommandUtil.getSingleInstance().getProperty(propName);
        return TextUtils.isEmpty(property) ? null : property;
    }

    public static class CommandUtil {
        private CommandUtil() {
        }

        private static class SingletonHolder {
            private static final CommandUtil INSTANCE = new CommandUtil();
        }

        public static final CommandUtil getSingleInstance() {
            return SingletonHolder.INSTANCE;
        }

        public String getProperty(String propName) {
            String value = null;
            Object roSecureObj;
            try {
                roSecureObj = Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, propName);
                if (roSecureObj != null)
                    value = (String) roSecureObj;
            } catch (Exception e) {
                value = null;
            } finally {
                return value;
            }
        }

        public String exec(String command) {
            BufferedOutputStream bufferedOutputStream = null;
            BufferedInputStream bufferedInputStream = null;
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("sh");
                bufferedOutputStream = new BufferedOutputStream(process.getOutputStream());

                bufferedInputStream = new BufferedInputStream(process.getInputStream());
                bufferedOutputStream.write(command.getBytes());
                bufferedOutputStream.write('\n');
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                process.waitFor();

                String outputStr = getStrFromBufferInputSteam(bufferedInputStream);
                return outputStr;
            } catch (Exception e) {
                return null;
            } finally {
                if (bufferedOutputStream != null) {
                    try {
                        bufferedOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (process != null) {
                    process.destroy();
                }
            }
        }

        private static String getStrFromBufferInputSteam(BufferedInputStream bufferedInputStream) {
            if (null == bufferedInputStream) {
                return "";
            }
            int BUFFER_SIZE = 512;
            byte[] buffer = new byte[BUFFER_SIZE];
            StringBuilder result = new StringBuilder();
            try {
                while (true) {
                    int read = bufferedInputStream.read(buffer);
                    if (read > 0) {
                        result.append(new String(buffer, 0, read));
                    }
                    if (read < BUFFER_SIZE) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        }
    }

}