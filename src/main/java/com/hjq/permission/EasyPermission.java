package com.hjq.permission;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by HJQ on 2017-5-9.
 */

public final class EasyPermission extends BasePermission {

    //不能被实例化
    private EasyPermission() {}

    public interface OnRequestCallBack {

        /**
         * 权限请求成功时回调
         * @param succeedPermissions    请求成功的权限组
         */
        void requestSucceed(String[] succeedPermissions);

        /**
         * 权限请求失败时回调
         * @param failPermissions   请求失败的权限组
         */
        void requestFail(String[] failPermissions);
    }

    private final static HashMap<Integer, OnRequestCallBack> mHashMap = new HashMap();

    /**
     * 请求权限，不需要指定请求码，请求结果通过回调接口方式实现
     * @param activity          Activity对象
     * @param call              用于接收结果的回调
     * @param permissions       需要请求的权限组，可变参数类型
     */
    public static void requestPermissions(Activity activity, OnRequestCallBack call, String... permissions) {
        requestPermissions((Object) activity, call, permissions);
    }

    /**
     * 请求权限，不需要指定请求码，请求结果通过回调接口方式实现
     * @param fragment          Fragment对象
     * @param call              用于接收结果的回调
     * @param permissions       需要请求的权限组，可变参数类型
     */
    public static void requestPermissions(Fragment fragment, OnRequestCallBack call, String... permissions) {
        requestPermissions((Object) fragment, call, permissions);
    }

    /**
     * 请求权限，不需要指定请求码，请求结果通过回调接口方式实现
     * @param fragment          v4包下的Fragment对象
     * @param call              用于接收结果的回调
     * @param permissions       需要请求的权限组，可变参数类型
     */
    public static void requestPermissions(android.support.v4.app.Fragment fragment, OnRequestCallBack call, String... permissions) {
        requestPermissions((Object) fragment, call, permissions);
    }

    private static void requestPermissions(Object object, OnRequestCallBack call, String... permissions) {

        PermissionUtils.checkObject(object);

        PermissionUtils.isEmptyPermissions(permissions);

        if (call == null) throw new NullPointerException("权限请求回调接口必须要实现");

        int requestCode;

        //请求码随机生成，避免随机产生之前的请求码，必须进行循环判断
        do {
            requestCode = new Random().nextInt(65535);//Studio编译的APK请求码必须小于65536
//            requestCode = new Random().nextInt(255);//Eclipse编译的APK请求码必须小于256
        } while (mHashMap.get(requestCode) != null);

        String[] failPermissions = PermissionUtils.getFailPermissions(PermissionUtils.getContext(object), permissions);

        if (failPermissions.length == 0) {
            //证明权限已经全部授予过
            call.requestSucceed(permissions);
        } else {
            mHashMap.put(requestCode, call);
            //申请没有授予过的权限
            PermissionUtils.requestPermissions(object, failPermissions, requestCode);
        }
    }

    /**
     * 在Activity或Fragment中的同名方法调用此方法
     */
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        OnRequestCallBack call = mHashMap.get(requestCode);

        //根据请求码取出的对象为空，就直接返回不处理
        if (call == null) return;

        //再次获取没有授予的权限
        String[] failPermissions = PermissionUtils.getFailPermissions(permissions, grantResults);
        if (failPermissions.length == 0) {
            //代表申请的所有的权限都授予了
            call.requestSucceed(permissions);
        } else {
            //代表申请的权限中有不同意授予的
            call.requestFail(failPermissions);
        }

        //权限回调结束后要删除集合中的对象，避免重复请求
        mHashMap.remove(requestCode);
    }
}
