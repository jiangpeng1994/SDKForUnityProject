package com.ssm.speechrecognizer;

import com.mob.MobSDK;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class SMSMessageSDK {

    public void InitSMSMessage() {
        //sms初始化
        MobSDK.init(AndroidToUnity.getActivity(),"29a32e555cdfa", "bdf009d2d2fb925f612d7332ec3b1bb8");
        //注册回调
        SMSSDK.registerEventHandler(eh);
    }

    EventHandler eh=new EventHandler(){

        @Override
        public void afterEvent(int event, int result, Object data) {

            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // 成功得到验证码
                        // 请注意，此时只是完成了发送验证码的请求，验证码短信还需要几秒钟之后才送达
                        GetVerificationCodeSuccess();
                    } else {
                        // 处理错误的结果
                        GetVerificationCodeFail();
                        ((Throwable) data).printStackTrace();
                    }
                } else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    if (result == SMSSDK.RESULT_COMPLETE) {
                        // 验证码验证通过
                        verifySuccess();
                    } else {
                        // 处理错误的结果
                        verifyFail();
                        ((Throwable) data).printStackTrace();
                    }
                }
            }else{
                ((Throwable)data).printStackTrace();
            }
        }
    };

    //开始获取验证码
    public void GetVerificationCode(String phone) {
        // 请求验证码，其中country表示国家代码，如“86”；phone表示手机号码，如“13800138000”
        SMSSDK.getVerificationCode("86", phone);
    }

    //开始验证验证码
    public void SubmitVerificationCode(String phone,String code) {
        // 提交验证码，其中的code表示验证码，如“1357”
        SMSSDK.submitVerificationCode("86", phone, code);
    }

    /**
     * 验证码获取成功
     */
    private void GetVerificationCodeSuccess() {
        AndroidToUnity.callUnity("SDKHandle", "GetVerificationCodeSuccess", "成功获取验证码");
    }

    /**
     * 验证码获取失败
     */
    private void GetVerificationCodeFail() {
        AndroidToUnity.callUnity("SDKHandle", "GetVerificationCodeFail", "获取验证码失败");
    }

    /**
     * 验证码验证成功
     */
    private void verifySuccess() {
        AndroidToUnity.callUnity("SDKHandle", "VerifySuccess", "验证成功");
    }

    /**
     * 验证码验证失败
     */
    private void verifyFail() {
        AndroidToUnity.callUnity("SDKHandle", "VerifyFail", "验证失败");
    }
}
