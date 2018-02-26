package com.fanfan.novel.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.fanfan.novel.adapter.NavigationAdapter;
import com.fanfan.novel.common.activity.BarBaseActivity;
import com.fanfan.novel.db.manager.NavigationDBManager;
import com.fanfan.novel.model.NavigationBean;
import com.fanfan.novel.utils.DialogUtils;
import com.fanfan.robot.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Created by android on 2018/1/8.
 */

public class DataNavigationActivity extends BarBaseActivity {

    @BindView(R.id.ptr_framelayout)
    PtrFrameLayout mPtrFrameLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    public static void newInstance(Activity context) {
        Intent intent = new Intent(context, DataNavigationActivity.class);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private NavigationDBManager mNavigationDBManager;

    private List<NavigationBean> navigationBeanList = new ArrayList<>();

    private NavigationAdapter navigationAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_navigation;
    }

    @Override
    protected void initView() {
        super.initView();
        mPtrFrameLayout.disableWhenHorizontalMove(true);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, recyclerView, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initData();
                    }
                }, 200);
            }
        });

        navigationAdapter = new NavigationAdapter(navigationBeanList);
        navigationAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                showNeutralNotitleDialog(position);
                return false;
            }
        });
        navigationAdapter.isFirstOnly(false); //设置不仅是首次填充数据时有动画,以后上下滑动也会有动画
        navigationAdapter.openLoadAnimation();
        recyclerView.setAdapter(navigationAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mNavigationDBManager = new NavigationDBManager();
    }


    @Override
    protected void initData() {

        navigationBeanList = mNavigationDBManager.loadAll();
        mPtrFrameLayout.refreshComplete();
        if (navigationBeanList != null && navigationBeanList.size() > 0) {
            isNuEmpty();
            navigationAdapter.replaceData(navigationBeanList);
        } else {
            isEmpty();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.add_black, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
//                new MaterialDialog.Builder(this)
//                        .title("选择导航图")
//                        .content("目前只支持此张地图")
//                        .items(Constants.NAVIGATIONS)
//                        .itemsCallback(new MaterialDialog.ListCallback() {
//                            @Override
//                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
//                                ExhibitionActivity.newInstance(DataNavigationActivity.this, (String) text);
//                            }
//                        })
//                        .show();
                AddNavigationActivity.newInstance(DataNavigationActivity.this, AddNavigationActivity.ADD_NAVIGATION_REQUESTCODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddNavigationActivity.ADD_NAVIGATION_REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra(AddNavigationActivity.RESULT_CODE, -1);
                if (id != -1) {
                    isNuEmpty();
                    NavigationBean bean = mNavigationDBManager.selectByPrimaryKey(id);
                    int pos = navigationBeanList.indexOf(bean);
                    if (pos >= 0) {
                        navigationBeanList.remove(pos);
                        navigationBeanList.add(pos, bean);
                        navigationAdapter.remove(pos);
                        navigationAdapter.addData(pos, bean);
                    } else {
                        if(navigationBeanList.size() == 0){
                            isNuEmpty();
                        }
                        navigationBeanList.add(bean);
                        navigationAdapter.addData(bean);
                    }
                }
            }
        }
    }

    private void showNeutralNotitleDialog(final int position) {
        DialogUtils.showNeutralNotitleDialog(this, "选择您要执行的操作", "删除所有",
                "删除此条", "修改此条", new DialogUtils.OnNeutralDialogListener() {
                    @Override
                    public void neutralText() {
                        if (mNavigationDBManager.deleteAll()) {
                            navigationBeanList.clear();
                            navigationAdapter.replaceData(navigationBeanList);
                        }
                    }

                    @Override
                    public void negativeText() {
                        if (mNavigationDBManager.delete(navigationBeanList.get(position))) {
                            navigationBeanList.remove(position);
                            navigationAdapter.remove(position);
                        }
                    }

                    @Override
                    public void positiveText() {
                        NavigationBean navigationBean = navigationAdapter.getData().get(position);
                        AddNavigationActivity.newInstance(DataNavigationActivity.this, navigationBean.getId(), AddNavigationActivity.ADD_NAVIGATION_REQUESTCODE);
                    }
                });
    }
}
