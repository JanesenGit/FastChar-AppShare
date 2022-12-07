package com.fastchar.appshare;

import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastWeb;

/**
 * @author 沈建（Janesen）
 * @date 2020/5/19 18:19
 */
public class FastAppShareWeb implements IFastWeb {
    @Override
    public void onInit(FastEngine engine) throws Exception {
        if (engine.getConstant().getAttachMaxPostSize() < 500 * 1024 * 1024) {
            engine.getConstant().setAttachMaxPostSize(500 * 1024 * 1024);
        }
        engine.getFindClass()
                .find("net.dongliu.apk.parser.ApkFile", "https://mvnrepository.com/artifact/net.dongliu/apk-parser")
                .find("com.dd.plist.PropertyListParser", "https://mvnrepository.com/artifact/com.googlecode.plist/dd-plist");
    }

    @Override
    public void onDestroy(FastEngine engine) throws Exception {

    }
}
