package com.app.smartpos.common;

import static com.app.smartpos.common.CrashReport.CustomExceptionHandler.addToDatabase;

import android.content.pm.PackageManager;

import com.app.smartpos.R;
import com.app.smartpos.utils.MultiLanguageApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class RootUtil {
    public static boolean isDeviceRooted() {
        return checkBuildTags() ||
                checkSuExists() ||
                checkSuBinary() ||
                checkRootNative() ||
                checkDangerousProps() ||
                checkRootApps() ||
                checkRWPaths() ||
                testRootAccess();
    }

    // Your existing methods (slightly improved)
    private static boolean checkBuildTags() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkSuExists() {
        String[] paths = {
                "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
                "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su",
                "/su/bin/su", "/magisk/.magisk/modules"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkSuBinary() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

// Additional important checks:

    private static boolean checkRootNative() {
        // Check for native root binaries and properties
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            String roDebuggable = (String) get.invoke(null, "ro.debuggable");
            if ("1".equals(roDebuggable)) return true;

            String roSecure = (String) get.invoke(null, "ro.secure");
            if ("0".equals(roSecure)) return true;
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private static boolean checkDangerousProps() {
        // Check for dangerous system properties
        String[] dangerousProps = {
                "ro.debuggable", "ro.secure", "ro.build.type", "ro.build.tags"
        };

        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);

            for (String prop : dangerousProps) {
                String value = (String) get.invoke(null, prop);
                if (("ro.debuggable".equals(prop) && "1".equals(value)) ||
                        ("ro.secure".equals(prop) && "0".equals(value)) ||
                        ("ro.build.type".equals(prop) && "userdebug".equals(value)) ||
                        ("ro.build.tags".equals(prop) && value != null && value.contains("test-keys"))) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private static boolean checkRootApps() {
        // Check for known root management apps
        String[] rootApps = {
                "com.noshufou.android.su",
                "com.thirdparty.superuser",
                "eu.chainfire.supersu",
                "com.koushikdutta.superuser",
                "com.zachspong.temprootremovejb",
                "com.ramdroid.appquarantine",
                "com.topjohnwu.magisk"
        };

        PackageManager pm = MultiLanguageApp.getApp().getPackageManager();
        for (String packageName : rootApps) {
            try {
                pm.getPackageInfo(packageName, 0);
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // App not installed
            }
        }
        return false;
    }

    private static boolean checkRWPaths() {
        // Check if system paths are writable (shouldn't be on normal devices)
        String[] paths = {"/system", "/system/bin", "/system/xbin", "/system/app", "/proc"};
        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.canWrite()) {
                return true;
            }
        }
        return false;
    }

    private static boolean testRootAccess() {
        // Actually try to get root access
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            OutputStream os = process.getOutputStream();
            os.write("exit\n".getBytes());
            os.flush();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}