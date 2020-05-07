package com.ssm.speechrecognizer;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;
import com.iflytek.cloud.SpeechUtility;
import java.io.File;

/**
 * 科大讯飞的语音评测SDK
 */
public class SpeechEvaluatorSDK {

    private SpeechEvaluator mIse;
    private String mEvaText;
    private int mRetryTimes = 3;

    /**
     * Unity调用的接口:初始化科大讯飞的语音评测SDK
     */
    public void InitSpeechEvaluator() {
        //参数1: activity
        //参数2: 应用编号(由科大讯飞后台分配，在后台创建应用时获取)
        SpeechUtility.createUtility(AndroidToUnity.getActivity(), SpeechConstant.APPID + SDKConfig.Speech_appId);
        mIse = SpeechEvaluator.createEvaluator(AndroidToUnity.getActivity(), mInitListener);
    }

    public InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int i) {
        }
    };

    /**
     * Unity调用的接口:设置语音评测的内容
     */
    public void SetEvaluationContent(String content) {
        mEvaText = content;
    }

    /**
     * Unity调用的接口:设置语音评测参数
     * @param language 评测语言:en_us（英语）、zh_cn（汉语）[必填]
     * @param category 评测题型:read_syllable（单字，汉语专有）、read_word（词语）、read_sentence（句子）、read_chapter（篇章）[必填]
     * @param result_level 评测结果等级:plain、complete，默认为complete。（中文仅支持complete）[选填]
     * @param vad_bos 语音前端点:语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理。[选填]
     * @param vad_eos 语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音。[选填]
     * @param speech_timeout 语音输入超时时间:录音超时，当录音达到时限将自动触发vad停止录音，默认-1（无超时）。[选填]
     */
    public void SetSpeechEvaluatorParams(String language,String category,String result_level,String vad_bos,String vad_eos,String speech_timeout) {
        mIse.setParameter(SpeechConstant.LANGUAGE, language);
        mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
        mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        mIse.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, getWavFilePath());

        if(!result_level.equals("")) {
            mIse.setParameter(SpeechConstant.RESULT_LEVEL, result_level);
        }
        if(!vad_bos.equals("")) {
            mIse.setParameter(SpeechConstant.VAD_BOS, vad_bos);
        }
        if(!vad_eos.equals("")) {
            mIse.setParameter(SpeechConstant.VAD_EOS, vad_eos);
        }
        if(!speech_timeout.equals("")) {
            mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout);
        }
    }

    public String getWavFilePath(){
        String mAudioWavPath;

        String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mAudioWavPath = fileBasePath + "/"+ "EnglishRecord.wav";

        //EvaluationContent(mAudioWavPath);
        return mAudioWavPath;
    }

    /**
     * Unity调用的接口:开始语音评测
     */
    public void StartSpeechEvaluator() {
        EvaluationContent(mEvaText);
        int ret = mIse.startEvaluating(mEvaText, null, mEvaluatorListener);

        if (ret != ErrorCode.SUCCESS) {
            VoiceInputStatus("语音评测失败:" + ret);
            return;
        }
        VoiceInputStatus("语音评测结束");
    }

    /**
     * 语音评测监听接口
     */
    public EvaluatorListener mEvaluatorListener = new EvaluatorListener() {

        @Override
        public void onResult(EvaluatorResult result, boolean isLast) {
            // 此回调表示：评测结果已经返回
            if (isLast) {
                StringBuilder builder = new StringBuilder();
                builder.append(result.getResultString());

                if (!TextUtils.isEmpty(builder)) {
                    SpeechEvaluatorResult(builder.toString());
                } else {
                    SpeechEvaluatorResult("评测结果为空");
                }
            } else {

            }
        }

        @Override
        public void onError(SpeechError error) {
            // 此回调表示：语音评测出现了错误
            if (error != null) {
                if(mRetryTimes > 0){
                    mRetryTimes--;
                    StartSpeechEvaluator();
                } else {
                    mRetryTimes = 3;
                    VoiceInputStatus("语音评测失败:" + error.getErrorCode() + "," + error.getErrorDescription());
                }
            } else {
            }
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            VoiceInputStatus("请开始发音，进行语音评测");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            VoiceInputStatus("结束发音，语音评测中");
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            // 此回调表示：输入的音量发生了变化
            //Integer i = volume;
            //VoiceInputStatus("音量发生变化："+i.toString());
            VoiceInputStatus("请继续发音");
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //    String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //    VoiceInputStatus("会话id ：" + sid);
            //}
        }
    };

    /**
     * Unity调用的接口:停止语音评测
     */
    public void StopSpeechEvaluator() {
        if (mIse.isEvaluating()) {
            mIse.stopEvaluating();
        }
        VoiceInputStatus("停止语音评测");
    }

    /**
     * Unity调用的接口:关闭语音评测
     */
    public void CancelSpeechEvaluator() {
        mIse.cancel();
        VoiceInputStatus("关闭语音评测");
    }

    /* ------------------------------------------------------------------------------------- */
    /* 以下方法为调用Unity场景中【SDKHandle游戏对象】上挂载脚本中的方法 */
    /* ------------------------------------------------------------------------------------- */
    /**
     * 语音评测内容
     * @param message 语音评测内容
     */
    private void EvaluationContent(String message) {
        AndroidToUnity.callUnity("SDKHandle", "EvaluationContent", message);
    }

    /**
     * 语音输入状态
     * @param message 语音输入状态
     */
    private void VoiceInputStatus(String message) {
        AndroidToUnity.callUnity("SDKHandle", "VoiceInputStatus", message);
    }

    /**
     * 语音评测结果
     * @param message 语音评测结果
     */
    private void SpeechEvaluatorResult(String message) {
        AndroidToUnity.callUnity("SDKHandle", "SpeechEvaluatorResult", message);
    }
}