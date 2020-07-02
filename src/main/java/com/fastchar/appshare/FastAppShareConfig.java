package com.fastchar.appshare;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastConfig;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.IconFace;
import net.dongliu.apk.parser.bean.Permission;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 13:39
 */
public class FastAppShareConfig implements IFastConfig {

    private boolean debug;

    public boolean isDebug() {
        return debug;
    }

    public FastAppShareConfig setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }



}
