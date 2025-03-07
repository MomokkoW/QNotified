/*
 * QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2021 dmca@ioctl.cc
 * https://github.com/ferredoxin/QNotified
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by ferredoxin.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/ferredoxin/QNotified/blob/master/LICENSE.md>.
 */
package cc.ioctl.hook;

import static nil.nadph.qnotified.util.Initiator._BaseSessionInfo;
import static nil.nadph.qnotified.util.Initiator._SessionInfo;
import static nil.nadph.qnotified.util.Initiator.load;
import static nil.nadph.qnotified.util.Utils.log;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import java.util.Random;
import me.singleneuron.qn_kernel.data.HostInformationProviderKt;
import me.singleneuron.util.QQVersion;
import nil.nadph.qnotified.base.annotation.FunctionEntry;
import nil.nadph.qnotified.hook.CommonDelayableHook;
import nil.nadph.qnotified.step.DexDeobfStep;
import nil.nadph.qnotified.ui.CustomDialog;
import nil.nadph.qnotified.util.DexKit;
import nil.nadph.qnotified.util.LicenseStatus;
import nil.nadph.qnotified.util.Toasts;

@FunctionEntry
public class CheatHook extends CommonDelayableHook {

    public static final CheatHook INSTANCE = new CheatHook();
    private final String[] diceItem = {"1", "2", "3", "4", "5", "6"};
    private final String[] morraItem = {"石头", "剪刀", "布"};

    private int diceNum = -1;
    private int morraNum = -1;

    private CheatHook() {
        super("qh_random_cheat", new DexDeobfStep(DexKit.C_PNG_FRAME_UTIL),
            new DexDeobfStep(DexKit.C_PIC_EMOTICON_INFO));
    }

    @Override
    public boolean initOnce() {
        try {
            XposedHelpers
                .findAndHookMethod(DexKit.doFindClass(DexKit.C_PNG_FRAME_UTIL), "a", int.class,
                    new XC_MethodHook(43) {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (LicenseStatus.sDisableCommonHooks) {
                                return;
                            }
                            try {
                                if (!isEnabled()) {
                                    return;
                                }
                            } catch (Throwable e) {
                                log(e);
                            }
                            int num = (int) param.args[0];
                            if (num == 6) {
                                if (diceNum == -1) {
                                    Toasts.error(
                                        HostInformationProviderKt.getHostInfo().getApplication(),
                                        "diceNum/E unexpected -1");
                                } else {
                                    param.setResult(diceNum);
                                }
                            } else if (num == 3) {
                                if (morraNum == -1) {
                                    Toasts.error(
                                        HostInformationProviderKt.getHostInfo().getApplication(),
                                        "morraNum/E unexpected -1");
                                } else {
                                    param.setResult(morraNum);
                                }
                            }
                        }
                    });

            XC_MethodHook hook = new XC_MethodHook(43) {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (LicenseStatus.sDisableCommonHooks) {
                        return;
                    }
                    try {
                        if (!isEnabled()) {
                            return;
                        }
                    } catch (Throwable e) {
                        log(e);
                    }
                    Context context = (Context) param.args[1];
                    Object emoticon = param.args[3];
                    String name = (String) XposedHelpers.getObjectField(emoticon,
                        "name");
                    if ("随机骰子".equals(name) || "骰子".equals(name)) {
                        param.setResult(null);
                        showDiceDialog(context, param);
                    } else if ("猜拳".equals(name)) {
                        param.setResult(null);
                        showMorraDialog(context, param);
                    }
                }
            };
            String Method = "a";

            if (HostInformationProviderKt.requireMinQQVersion(QQVersion.QQ_8_4_8)) {
                Method = "sendMagicEmoticon";
            }if (HostInformationProviderKt.requireMinQQVersion(QQVersion.QQ_8_6_0)) {
                XposedHelpers.findAndHookMethod(Class.forName("com.tencent.mobileqq.emoticonview" +
                        ".sender.PicEmoticonInfoSender"),
                    Method, load("com.tencent.common.app.business.BaseQQAppInterface"),
                    Context.class, _BaseSessionInfo(),
                    load("com.tencent.mobileqq.data.Emoticon"),
                    load("com.tencent.mobileqq.emoticon.StickerInfo"), hook);
            } else if (HostInformationProviderKt.requireMinQQVersion(QQVersion.QQ_8_5_0)) {
                XposedHelpers.findAndHookMethod(Class.forName("com.tencent.mobileqq.emoticonview" +
                        ".sender.PicEmoticonInfoSender"),
                    Method, load("com.tencent.mobileqq.app.QQAppInterface"),
                    Context.class, _SessionInfo(), load("com.tencent.mobileqq.data.Emoticon"),
                    load("com.tencent.mobileqq.emoticon.EmojiStickerManager$StickerInfo"), hook);
            } else {
                XposedHelpers.findAndHookMethod(DexKit.doFindClass(DexKit.C_PIC_EMOTICON_INFO),
                    Method, load("com.tencent.mobileqq.app.QQAppInterface"),
                    Context.class, _SessionInfo(), load("com.tencent.mobileqq.data.Emoticon"),
                    load("com.tencent.mobileqq.emoticon.EmojiStickerManager$StickerInfo"), hook);
            }
            return true;
        } catch (Throwable e) {
            log(e);
            return false;
        }

    }

    private void showDiceDialog(Context context, XC_MethodHook.MethodHookParam param) {
        AlertDialog alertDialog = new AlertDialog.Builder(context, CustomDialog.themeIdForDialog())
            .setTitle("自定义骰子")
            .setSingleChoiceItems(diceItem, diceNum, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    diceNum = which;
                }
            })
            .setNegativeButton("取消", null)
            .setNeutralButton("随机", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    diceNum = Math.abs(new Random().nextInt(6));
                    try {
                        XposedBridge
                            .invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                }
            })
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        XposedBridge
                            .invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                }
            })
            .create();
        alertDialog.show();
    }

    private void showMorraDialog(Context context, XC_MethodHook.MethodHookParam param) {
        AlertDialog alertDialog = new AlertDialog.Builder(context, CustomDialog.themeIdForDialog())
            .setTitle("自定义猜拳")
            .setSingleChoiceItems(morraItem, morraNum, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    morraNum = which;
                }
            })
            .setNegativeButton("取消", null)
            .setNeutralButton("随机", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    morraNum = Math.abs(new Random().nextInt(3));
                    try {
                        XposedBridge
                            .invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                }
            })
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        XposedBridge
                            .invokeOriginalMethod(param.method, param.thisObject, param.args);
                    } catch (Exception e) {
                        XposedBridge.log(e);
                    }
                }
            })
            .create();
        alertDialog.show();
    }
}
