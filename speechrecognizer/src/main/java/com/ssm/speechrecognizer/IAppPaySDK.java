package com.ssm.speechrecognizer;

import android.widget.Toast;
import com.iapppay.interfaces.callback.IPayResultCallback;
import com.iapppay.sdk.main.IAppPay;
import com.iapppay.sdk.main.IAppPayOrderUtils;

/**
 * 爱贝支付SDK
 */
public class IAppPaySDK {

    /**
     * Unity调用的接口:初始化爱贝支付SDK
     */
    public void InitIAppPay() {
        //参数1: activity
        //参数2: 支付页面的横竖屏方式(IAppPay.PORTRAIT 竖屏 IAppPay.LANDSCAPE 横屏 IAppPay.SENSOR_LANDSCAPE 横屏自动切换)
        //参数3: 应用编号(由爱贝后台分配，在后台创建应用时获取)
        IAppPay.init(AndroidToUnity.getActivity(), IAppPay.PORTRAIT, SDKConfig.IAppPay_appId);
    }

    /**
     * Unity调用的接口:调起支付
     * @param waresid 商品编号:应用中的商品编号。爱贝后台的商品编号。[必填]
     * @param waresname 商品名称:对于消费型_应用传入价格的计费方式有效，如果不传则展示后台设置的商品名称。[选填]
     * @param orderid 商户订单号:商户生成的订单号，需要保证系统唯一。[必填]
     * @param price 支付金额:对于消费型_应用传入价格的计费方式有效，其它计费方式不需要传入本参数（单位：元）。[选填]
     * @param appuserid 用户在商户应用的唯一标识:建议为用户帐号。[必填]
     * @param cpprivateinfo 商户私有信息:支付完成后发送支付结果通知时会透传给商户。[选填]
     */
    public void Pay(int waresid, String waresname, String orderid, double price, String appuserid, String cpprivateinfo) {
        String params = getTransdata(waresid, waresname, orderid, price, appuserid, cpprivateinfo);
        //参数1: activity
        //参数2: 订单信息
        //参数3: 支付结果回调
        IAppPay.startPay(AndroidToUnity.getActivity(), params, iPayResultCallback);
    }

    /**
     * Unity调用的接口:调起支付
     * @param transid 商品编号:应用中的商品编号。爱贝后台的商品编号。[必填]
     */
    private void StartPay(String transid) {
        String param = "transid=" + transid + "&appid=" + SDKConfig.IAppPay_appId;
        PayParam(param);
        IAppPay.startPay(AndroidToUnity.getActivity(), param, iPayResultCallback);
    }

    /**
     * 设置订单信息
     */
    private String getTransdata(int waresid, String waresname, String orderid, double price, String appuserid, String cpprivateinfo) {
        IAppPayOrderUtils orderUtils = new IAppPayOrderUtils();
        orderUtils.setAppid(SDKConfig.IAppPay_appId);
        orderUtils.setWaresid(waresid);
        orderUtils.setCporderid(orderid);
        orderUtils.setAppuserid(appuserid);

        if(!waresname.equals("")) {
            orderUtils.setWaresname(waresname);
        }
        if(price >= 0) {
            orderUtils.setPrice(price);
        }
        if(!cpprivateinfo.equals("")) {
            orderUtils.setCpprivateinfo(cpprivateinfo);
        }

        return orderUtils.getTransdata(SDKConfig.IAppPay_privateKey);
    }

    /**
     * 支付结果回调
     */
    IPayResultCallback iPayResultCallback = new IPayResultCallback() {

        @Override
        public void onPayResult(int resultCode, String signValue, String resultInfo) {
            PayResult("signValue:"+signValue+"  resultInfo:"+resultInfo);
            switch (resultCode) {
                case IAppPay.PAY_SUCCESS:
                    PaySuccess();
                    Toast.makeText(AndroidToUnity.getActivity(), "支付成功", Toast.LENGTH_LONG).show();
                    break;
                default:
                    PayError(resultInfo);
                    Toast.makeText(AndroidToUnity.getActivity(), resultInfo, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    /* ------------------------------------------------------------------------------------- */
    /* 以下方法为调用Unity场景中【SDKHandle游戏对象】上挂载脚本中的方法 */
    /* ------------------------------------------------------------------------------------- */
    /**
     * 支付成功回调
     */
    private void PaySuccess() {
        AndroidToUnity.callUnity("SDKHandle", "PaySuccess", "支付成功");
    }

    /**
     * 支付失败回调
     */
    private void PayError(String message) {
        AndroidToUnity.callUnity("SDKHandle", "PayError", "支付失败:" + message);
    }

    /**
     * 支付结果回调
     */
    private void PayResult(String message) {
        AndroidToUnity.callUnity("SDKHandle", "PayResult", message);
    }

    /**
     * 支付请求参数回调
     */
    private void PayParam(String message) {
        AndroidToUnity.callUnity("SDKHandle", "PayParam", message);
    }
}
