package com.fanfan.robot.presenter;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;

import com.fanfan.robot.app.enums.SpecialType;
import com.fanfan.robot.listener.base.recog.AlarmListener;
import com.fanfan.robot.listener.base.recog.IRecogListener;
import com.fanfan.robot.listener.base.recog.local.MyRecognizerLocal;
import com.fanfan.robot.listener.base.synthesizer.EarListener;
import com.fanfan.robot.listener.base.synthesizer.ISynthListener;
import com.fanfan.robot.listener.base.synthesizer.cloud.MySynthesizer;
import com.fanfan.robot.listener.base.synthesizer.local.MySynthesizerLocal;
import com.fanfan.robot.model.local.Asr;
import com.fanfan.robot.presenter.ipersenter.ILocalSoundPresenter;
import com.fanfan.robot.other.stragry.TranficCalculator;
import com.fanfan.robot.other.stragry.local.ArtificialStrategy;
import com.fanfan.robot.other.stragry.local.BackStrategy;
import com.fanfan.robot.other.stragry.local.ControlStrategy;
import com.fanfan.robot.other.stragry.local.FaceStrategy;
import com.fanfan.robot.other.stragry.local.LogoutStrategy;
import com.fanfan.robot.other.stragry.local.MapStrategy;
import com.fanfan.robot.other.stragry.local.MoveStrategy;
import com.fanfan.robot.other.stragry.local.StopStrategy;
import com.fanfan.novel.utils.FucUtil;
import com.fanfan.robot.app.RobotInfo;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;

import java.util.Random;

/**
 * Created by android on 2017/12/20.
 */

public class LocalSoundPresenter extends ILocalSoundPresenter {

    private ILocalSoundPresenter.ILocalSoundView mSoundView;

    private Handler mHandler = new Handler();

    private MyRecognizerLocal myRecognizer;
    private MySynthesizerLocal mySynthesizer;

    public LocalSoundPresenter(ILocalSoundView baseView) {
        super(baseView);
        mSoundView = baseView;
    }

    @Override
    public void start() {
        RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_LOCAL);

        ISynthListener iSynthListener = new EarListener() {
            @Override
            public void onCompleted() {
                super.onCompleted();
                LocalSoundPresenter.this.onCompleted();
            }
        };
        mySynthesizer = new MySynthesizerLocal(mSoundView.getContext(), iSynthListener);

        IRecogListener iRecogListener = new AlarmListener() {

            @Override
            public void onAsrLocalFinalResult(String result) {
                super.onAsrLocalFinalResult(result);
                onRecognResult(result);
            }

            @Override
            public void onAsrLocalDegreeLow(Asr local, int degree) {
                super.onAsrLocalDegreeLow(local, degree);
                onCompleted();
            }

            @Override
            public void onAsrFinishError(int errorCode, String errorMessage) {
                super.onAsrFinishError(errorCode, errorMessage);
                onCompleted();
            }
        };
        myRecognizer = new MyRecognizerLocal(mSoundView.getContext(), iRecogListener);
    }

    @Override
    public void finish() {
        RobotInfo.getInstance().setEngineType(SpeechConstant.TYPE_CLOUD);
        mySynthesizer.release();
        myRecognizer.onDestroy();
    }

    @Override
    public void doAnswer(String answer) {
        stopEvery();
        mySynthesizer.speak(answer);
    }

    @Override
    public void onResume() {
        mySynthesizer.onResume();
        myRecognizer.onResume();
    }

    @Override
    public void onPause() {
        stopEvery();
    }

    @Override
    public void stopEvery() {
        mySynthesizer.stop();
        myRecognizer.stop();
        stopHandler();
    }

    @Override
    public void onCompleted() {
        mHandler.postDelayed(runnable, 0);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            myRecognizer.start();
            mSoundView.onCompleted();
        }
    };


    private void stopHandler() {
        mHandler.removeCallbacks(runnable);
    }

    private void onRecognResult(String result) {
        myRecognizer.stop();

        Print.e(result);

        TranficCalculator calculator = new TranficCalculator();

        SpecialType myType = calculator.specialLocal(result, new MoveStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.spakeMove(myType, result);
            return;
        }
        myType = calculator.specialLocal(result, new LogoutStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.logout();
            return;
        }
        myType = calculator.specialLocal(result, new MapStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.openMap();
            return;
        }
        myType = calculator.specialLocal(result, new StopStrategy());
        if (SpecialType.NoSpecial != myType) {

            stopEvery();
            return;
        }
        myType = calculator.specialLocal(result, new BackStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.back();
            return;
        }
        myType = calculator.specialLocal(result, new ArtificialStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.artificial();
            return;
        }
        myType = calculator.specialLocal(result, new FaceStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.face(myType, result);
            return;
        }
        myType = calculator.specialLocal(result, new ControlStrategy());
        if (SpecialType.NoSpecial != myType) {
            mSoundView.control(myType, result);
            return;
        }
        mSoundView.refLocalPage(result);
    }


    private String resFoFinal(int id) {
        String[] arrResult = ((Activity) mSoundView).getResources().getStringArray(id);
        return arrResult[new Random().nextInt(arrResult.length)];
    }

}
