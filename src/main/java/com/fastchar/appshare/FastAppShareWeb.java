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
        engine.getConstant().setAttachMaxPostSize(100 * 1024 * 1024);
    }

    @Override
    public void onDestroy(FastEngine engine) throws Exception {

    }
}
