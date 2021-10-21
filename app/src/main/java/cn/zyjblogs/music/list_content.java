package cn.zyjblogs.music;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class list_content extends Fragment {
    ImageView playIv,albumIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    //    数据源
    List<LocalMusicBean> mDatas;
    private LocalMusicAdapter adapter;

    //    记录当前正在播放的音乐的位置
    int currnetPlayPosition = -1;
    //    记录暂停音乐时进度条的位置
    int currentPausePositionInSong = 0;
    MediaPlayer mediaPlayer;
    //针对sd卡读取权限申请
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view=inflater.inflate(R.layout.list_content,null);
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();
        sharedPreferences = getActivity().getSharedPreferences("hhh", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
       // albumIv = view.findViewById(R.id.local_music_bottom_iv_icon);
       // singerTv = view.findViewById(R.id.local_music_bottom_tv_singer);
       // songTv = view.findViewById(R.id.local_music_bottom_tv_song);
        musicRv = view.findViewById(R.id.local_music_rv);
//     创建适配器对象
        adapter = new LocalMusicAdapter(getActivity(), mDatas);
        musicRv.setAdapter(adapter);
//        设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
        verifyStoragePermissions(getActivity());
//        加载本地数据源
        loadLocalMusicData();
//        设置每一项的点击事件
        setEventListener();
        return view;
    }

    //申请权限

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }
    private void setEventListener() {
        /* 设置每一项的点击事件*/
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currnetPlayPosition = position;
                editor.putInt("flag",currnetPlayPosition);
                LocalMusicBean musicBean = mDatas.get(position);
                playMusicInMusicBean(musicBean);
            }
        });
    }

    public void playMusicInMusicBean(LocalMusicBean musicBean) {
        /*根据传入对象播放音乐*/
        //设置底部显示的歌手名称和歌曲名
        //singerTv.setText(musicBean.getSinger());
        //songTv.setText(musicBean.getSong());
        editor.putString("singer",musicBean.getSinger());
        editor.putString("song",musicBean.getSong());
        editor.commit();
        stopMusic();
//                重置多媒体播放器
        mediaPlayer.reset();
//                设置新的播放路径
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            String albumArt = musicBean.getAlbumArt();
            Log.i("lsh123", "playMusicInMusicBean: albumpath=="+albumArt);
            Bitmap bm = BitmapFactory.decodeFile(albumArt);
            Log.i("lsh123", "playMusicInMusicBean: bm=="+bm);
           // albumIv.setImageBitmap(bm);
            editor.putString("album",albumArt);
            editor.commit();
            playMusic();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 点击播放按钮播放音乐，或者暂停从新播放
     * 播放音乐有两种情况：
     * 1.从暂停到播放
     * 2.从停止到播放
     * */
    private void playMusic() {
        /* 播放音乐的函数*/
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    Log.i("music", "1");

                    mediaPlayer.start();
                    Log.i("music", "1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
//                从暂停到播放
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
           // playIv.setImageResource(R.mipmap.icon_pause);
            editor.putString("bo","y");
            editor.commit();
        }
    }


    private void stopMusic() {
        /* 停止音乐的函数*/
        if (mediaPlayer!=null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMusic();
    }


    private void loadLocalMusicData() {
        /* 加载本地存储当中的音乐mp3文件到集合当中*/
//        1.获取ContentResolver对象
        ContentResolver resolver = getActivity().getContentResolver();
//        2.获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        3 开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
//        4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date(duration));
//          获取专辑图片主要是通过album_id进行查询
            String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String albumArt = getAlbumArt(album_id);
//            将一行当中的数据封装到对象当中
            LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path,albumArt);
            mDatas.add(bean);
        }
//        数据源变化，提示适配器更新
        adapter.notifyDataSetChanged();
    }


    private String getAlbumArt(String album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = getActivity().getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }

}
