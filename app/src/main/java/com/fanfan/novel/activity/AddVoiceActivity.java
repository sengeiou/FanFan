package com.fanfan.novel.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.common.instance.SpeakIat;
import com.fanfan.novel.db.manager.NavigationDBManager;
import com.fanfan.novel.db.manager.VideoDBManager;
import com.fanfan.novel.db.manager.VoiceDBManager;
import com.fanfan.novel.model.NavigationBean;
import com.fanfan.novel.model.VideoBean;
import com.fanfan.novel.model.VoiceBean;
import com.fanfan.novel.utils.BitmapUtils;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.novel.utils.LocalLexicon;
import com.fanfan.novel.utils.PreferencesUtils;
import com.fanfan.robot.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.seabreeze.log.Print;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by android on 2018/1/8.
 */

public class AddVoiceActivity extends BarBaseActivity implements LocalLexicon.RobotLexiconListener {


    @BindView(R.id.et_question)
    EditText etQuestion;
    @BindView(R.id.et_content)
    EditText etContent;
    @BindView(R.id.tv_expression)
    TextView tvExpression;
    @BindView(R.id.tv_action)
    TextView tvAction;
    @BindView(R.id.tv_img)
    TextView tvImg;
    @BindView(R.id.img_voice)
    ImageView imgVoice;

    public static final String VOICE_ID = "voiceId";
    public static final int ADD_VOICE_REQUESTCODE = 224;

    private static final int REQCODE_SELALBUM = 101;

    public static final int CHOOSE_PHOTO = 2;//选择相册
    public static final int PICTURE_CUT = 3;//剪切图片

    public static void newInstance(Activity context, int requestCode) {
        Intent intent = new Intent(context, AddVoiceActivity.class);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public static void newInstance(Activity context, long id, int requestCode) {
        Intent intent = new Intent(context, AddVoiceActivity.class);
        intent.putExtra(VOICE_ID, id);
        context.startActivityForResult(intent, requestCode);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private long saveLocalId;

    private VoiceDBManager mVoiceDBManager;

    private int curExpression;
    private int curAction;

    private String imagePath;//打开相册选择照片的路径
    private Uri outputUri;//裁剪万照片保存地址
    private boolean isClickCamera;//是否是拍照裁剪

    private VoiceBean voiceBean;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_voice;
    }

    @Override
    protected void initData() {
        saveLocalId = getIntent().getLongExtra(VOICE_ID, -1);

        mVoiceDBManager = new VoiceDBManager();

        if (saveLocalId != -1) {

            voiceBean = mVoiceDBManager.selectByPrimaryKey(saveLocalId);
            etQuestion.setText(voiceBean.getShowTitle());
            etContent.setText(voiceBean.getVoiceAnswer());
            curExpression = valueForArray(R.array.expression_data, voiceBean.getExpressionData());
            curAction = valueForArray(R.array.action_order, voiceBean.getActionData());
            String savePath = voiceBean.getImgUrl();
            if (savePath != null) {
                if (new File(savePath).exists()) {
                    imgVoice.setVisibility(View.VISIBLE);
                    Glide.with(AddVoiceActivity.this).load(savePath)
                            .apply(new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.mipmap.ic_logo))
                            .into(imgVoice);
                }
            }
        } else {
            voiceBean = new VoiceBean();
            curExpression = 0;
            curAction = 0;
        }

        tvExpression.setText(getResources().getStringArray(R.array.expression)[curExpression]);
        tvAction.setText(getResources().getStringArray(R.array.action)[curAction]);

    }

    @OnClick({R.id.tv_img, R.id.tv_expression, R.id.tv_action})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_img:
                if (isEmpty(etQuestion)) {
                    showToast("输入不能为空！");
                } else {
                    selectFromAlbum();//打开相册
                }
                break;
            case R.id.tv_expression:
                DialogUtils.showLongListDialog(AddVoiceActivity.this, "面部表情", R.array.expression, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        curExpression = position;
                        tvExpression.setText(text);
                    }
                });
                break;
            case R.id.tv_action:
                DialogUtils.showLongListDialog(AddVoiceActivity.this, "执行动作", R.array.action, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        curAction = position;
                        tvAction.setText(text);
                    }
                });
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.finish_white, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish:

                if (isEmpty(etQuestion)) {
                    showToast("问题不能为空！");
                    break;
                }
                if (isEmpty(etContent)) {
                    showToast("答案不能为空！");
                    break;
                }
                if (etQuestion.getText().toString().trim().length() > 20) {
                    showToast("输入 20 字以内");
                    break;
                }
                if (saveLocalId == -1) {//直接添加，判断是否存在
                    List<VoiceBean> been = mVoiceDBManager.queryVoiceByQuestion(etQuestion.getText().toString().trim());
                    if (!been.isEmpty()) {
                        showToast("请不要添加相同的问题！");
                        break;
                    }
                }
                voiceIsexit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO://打开相册
                // 判断手机系统版本号
                if (data != null) {
                    Uri uri = data.getData();
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        imagePath = BitmapUtils.handleImageOnKitKat(this, uri);
                        outputUri = BitmapUtils.cropPhoto(this, uri, etQuestion.getText().toString() + ".jpg", PICTURE_CUT);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        imagePath = BitmapUtils.getImagePath(this, uri, null);
                        outputUri = BitmapUtils.cropPhoto(AddVoiceActivity.this, uri, etQuestion.getText().toString() + ".jpg", PICTURE_CUT);
                    }
                }
                break;
            case PICTURE_CUT://裁剪完成
                isClickCamera = true;
                if (isClickCamera) {
                    imgVoice.setVisibility(View.VISIBLE);
                    Glide.with(AddVoiceActivity.this).load(outputUri)
                            .apply(new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.mipmap.ic_logo))
                            .into(imgVoice);
                } else {
                    Glide.with(AddVoiceActivity.this).load(outputUri)
                            .apply(new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(R.mipmap.ic_logo))
                            .into(imgVoice);
                }
                break;
        }
    }

    private void selectFromAlbum() {
        if (ContextCompat.checkSelfPermission(AddVoiceActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddVoiceActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQCODE_SELALBUM);
        } else {
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }


    private void voiceIsexit() {
        voiceBean.setSaveTime(System.currentTimeMillis());
        voiceBean.setShowTitle(getText(etQuestion));
        voiceBean.setVoiceAnswer(getText(etContent));
        voiceBean.setExpression(resArray(R.array.expression)[curExpression]);
        voiceBean.setExpressionData(resArray(R.array.expression_data)[curExpression]);
        voiceBean.setAction(resArray(R.array.action)[curAction]);
        voiceBean.setActionData(resArray(R.array.action_order)[curAction]);
        setVoiceimg(voiceBean);
        if (saveLocalId == -1) {//直接添加
            mVoiceDBManager.insert(voiceBean);
        } else {//更新
            voiceBean.setId(saveLocalId);
            mVoiceDBManager.update(voiceBean);
        }
        LocalLexicon.getInstance().initDBManager().setListener(this).updateContents();
    }

    private void setVoiceimg(VoiceBean bean) {
        if (outputUri != null) {
            String imagePath = BitmapUtils.getPathByUri4kitkat(AddVoiceActivity.this, outputUri);
            Print.e(imagePath);
            bean.setImgUrl(imagePath);
        }
    }


    private int valueForArray(int resId, String compare) {
        String[] arrays = resArray(resId);
        return Arrays.binarySearch(arrays, compare);
    }

    private boolean isEmpty(TextView textView) {
        return textView.getText().toString().trim().equals("") || textView.getText().toString().trim().equals("");
    }

    private String[] resArray(int resId) {
        return getResources().getStringArray(resId);
    }

    private String getText(TextView textView) {
        return textView.getText().toString().trim();
    }

    @Override
    public void onLexiconSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onLexiconError(String error) {
        showToast(error);
    }
}
