package com.ssm.speechrecognizer;

import android.app.Activity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Android到Unity的交互类
 */
public class AndroidToUnity {
    /* ------------------------------------------------------------------------------------- */
    /*                       以下方法为实现如何调用Unity的方法                              */
    /* --------------------------------------------------------------------------------------*/
    /**
     * unity项目的上下文
     */
    private static Activity _unityActivity;

    /**
     * 获取unity项目的上下文
     * @return unity项目的上下文
     */
    public static Activity getActivity() {
        if(null == _unityActivity) {
            try {
                Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
                Activity activity = (Activity) classtype.getDeclaredField("currentActivity").get(classtype);
                _unityActivity = activity;
            } catch (ClassNotFoundException e) {

            } catch (IllegalAccessException e) {

            } catch (NoSuchFieldException e) {

            }
        }
        return _unityActivity;
    }

    /**
     * 调用Unity的方法
     * @param gameObjectName    调用的GameObject的名称
     * @param functionName      方法名
     * @param args              参数
     * @return                  调用是否成功
     */
    public static boolean callUnity(String gameObjectName, String functionName, String args){
        try {
            Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
            Method method =classtype.getMethod("UnitySendMessage", String.class,String.class,String.class);
            method.invoke(classtype,gameObjectName,functionName,args);
            return true;
        } catch (ClassNotFoundException e) {

        } catch (NoSuchMethodException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }
        return false;
    }
}