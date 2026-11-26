package com.originos.biometric.imefix;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class BiometricImeFixHook implements IXposedHookLoadPackage {
    private static final String TARGET_PKG = "com.android.systemui";
    private static final String AUTH_CONTAINER_CLS = "com.android.systemui.biometrics.AuthContainerView";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!TARGET_PKG.equals(lpparam.packageName)) {
            return;
        }
        try {
            XposedHelpers.findAndHookMethod(
                AUTH_CONTAINER_CLS,
                lpparam.classLoader,
                "addCredentialView",
                boolean.class,
                boolean.class,
                new AddCredentialHook()
            );
            XposedBridge.log("[BiometricImeFix] Hook installed on " + AUTH_CONTAINER_CLS);
        } catch (Throwable t) {
            XposedBridge.log("[BiometricImeFix] Hook failed: " + t);
        }
    }

    private static final class AddCredentialHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) {
            if (CredentialGuard.isAlreadyAttached(param.thisObject)) {
                XposedBridge.log("[BiometricImeFix] Skipped duplicate addCredentialView()");
                param.setResult(null);
            }
        }
    }
}
