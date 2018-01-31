package com.fanfan.robot.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fanfan.novel.common.Constants;
import com.fanfan.novel.common.base.BaseFragment;
import com.fanfan.novel.common.base.simple.BaseRecyclerAdapter;
import com.fanfan.novel.common.glide.GlideRoundTransform;
import com.fanfan.novel.service.PlayService;
import com.fanfan.novel.service.cache.MusicCache;
import com.fanfan.novel.service.music.OnPlayerEventListener;
import com.fanfan.novel.utils.music.MusicUtils;
import com.fanfan.robot.R;
import com.fanfan.robot.activity.MultimediaActivity;
import com.fanfan.robot.adapter.LocalMusicAdapter;
import com.fanfan.robot.model.Music;
import com.seabreeze.log.Print;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by android on 2018/1/10.
 */

public class SongFragment extends BaseFragment implements OnPlayerEventListener {

    private static final String LOCAL_MUSIC_POSITION = "local_music_position";
    private static final String LOCAL_MUSIC_OFFSET = "local_music_offset";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.iv_play_bar_cover)
    ImageView ivPlayBarCover;
    @BindView(R.id.tv_play_bar_title)
    TextView tvPlayBarTitle;
    @BindView(R.id.tv_play_bar_artist)
    TextView tvPlayBarArtist;
    @BindView(R.id.iv_play_bar_play)
    ImageView ivPlayBarPlay;
    @BindView(R.id.iv_play_bar_next)
    ImageView ivPlayBarNext;
    @BindView(R.id.pb_play_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.fl_play_bar)
    RelativeLayout flPlayBar;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;

    public static SongFragment newInstance() {
        return new SongFragment();
    }

    private LocalMusicAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_song;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
    }

    @Override
    protected void initData() {

        mAdapter = new LocalMusicAdapter(getActivity(), MusicCache.get().getMusicList());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                stopListener();
                getPlayService().play(position);
            }
        });

        onChangeImpl(getPlayService().getPlayingMusic());
    }

    @Override
    protected void setListener(View view) {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        int position = mAdapter.getPlayingPosition();
        int offset = (recyclerView.getChildAt(0) == null) ? 0 : recyclerView.getChildAt(0).getTop();
        outState.putInt(LOCAL_MUSIC_POSITION, position);
        outState.putInt(LOCAL_MUSIC_OFFSET, offset);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(LOCAL_MUSIC_POSITION);
                int offset = savedInstanceState.getInt(LOCAL_MUSIC_OFFSET);
                Print.e(position + " " + offset);
            }
        });
    }

    @Override
    public void onDestroy() {
        stopMusic();
        PlayService service = MusicCache.get().getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        RequestOptions options = new RequestOptions()
                .error(R.mipmap.default_cover)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new GlideRoundTransform());
        Glide.with(this)
                .load(music.getAlbumId() == -1 ? MusicUtils.getMediaDataAlbumPic(music.getPath()) : MusicUtils.getMediaStoreAlbumCoverUri(music.getAlbumId()))
                .apply(options)
                .into(ivPlayBarCover);
        tvPlayBarTitle.setText(music.getTitle());
        tvPlayBarArtist.setText(music.getArtist());
        ivPlayBarPlay.setSelected(getPlayService().isPlaying() || getPlayService().isPreparing());
        mProgressBar.setMax((int) music.getDuration());
        mProgressBar.setProgress((int) getPlayService().getCurrentPosition());

        if (MusicCache.get().getMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.updatePlayingPosition(getPlayService());
        recyclerView.smoothScrollToPosition(getPlayService().getPlayingPosition());
    }


    @OnClick({R.id.fl_play_bar, R.id.iv_play_bar_play, R.id.iv_play_bar_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fl_play_bar:
                break;
            case R.id.iv_play_bar_play:
                if (getPlayService().isPlaying()) {
                    startListener();
                } else {
                    stopListener();
                }
                play();
                break;
            case R.id.iv_play_bar_next:
                stopListener();
                next();
                break;
        }
    }

    private void play() {
        getPlayService().playPause();
    }

    private void next() {
        getPlayService().next();
    }

    @Override
    public void onChange(Music music) {
        onChangeImpl(music);
    }

    @Override
    public void onPlayerStart() {
        ivPlayBarPlay.setSelected(true);
    }

    @Override
    public void onPlayerPause() {
        if (ivPlayBarPlay != null) {
            ivPlayBarPlay.setSelected(false);
        }
    }

    @Override
    public void onPublish(int progress) {
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onTimer(long remain) {

    }

    @Override
    public void onMusicListUpdate() {
        if (MusicCache.get().getMusicList().isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        mAdapter.refreshData(MusicCache.get().getMusicList());
        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }


    public void stopMusic() {
        getPlayService().stop();
    }

    public void stopListener() {
        Print.e("停止监听 ...... ");
        assert ((MultimediaActivity) getActivity()) != null;
        ((MultimediaActivity) getActivity()).stopListener();
    }

    public void startListener() {
        Print.e("启动监听 ...... ");
        assert ((MultimediaActivity) getActivity()) != null;
        ((MultimediaActivity) getActivity()).startListener();
    }
}
