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

public class bo_content extends Fragment implements View.OnClickListener
{
    ImageView nextIv,playIv,lastIv,albumIv;
    TextView singerTv,songTv;
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
        View view=inflater.inflate(R.layout.bo_content,null);
        nextIv = view.findViewById(R.id.local_music_bottom_iv_next);
        playIv = view.findViewById(R.id.local_music_bottom_iv_play);
        lastIv = view.findViewById(R.id.local_music_bottom_iv_last);
        albumIv = view.findViewById(R.id.local_music_bottom_iv_icon);
        singerTv = view.findViewById(R.id.local_music_bottom_tv_singer);
        songTv = view.findViewById(R.id.local_music_bottom_tv_song);
        sharedPreferences = getActivity().getSharedPreferences("hhh", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        currnetPlayPosition = sharedPreferences.getInt("flag",0);
        editor.putString("bo","n");
        editor.commit();
        stopMusic();
        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();

        String albumArt = sharedPreferences.getString("album","");
        Bitmap bm = BitmapFactory.decodeFile(albumArt);
        Log.i("lsh123", "playMusicInMusicBean: bm=="+bm);
        albumIv.setImageBitmap(bm);
        singerTv.setText(sharedPreferences.getString("singer",""));
        songTv.setText(sharedPreferences.getString("song",""));
//     创建适配器对象
        adapter = new LocalMusicAdapter(getActivity(), mDatas);

//        设置布局管理器
        verifyStoragePermissions(getActivity());
//        加载本地数据源
        loadLocalMusicData();
//
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

    public void playMusicInMusicBean(LocalMusicBean musicBean) {
        /*根据传入对象播放音乐*/
        //设置底部显示的歌手名称和歌曲名
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
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
            albumIv.setImageBitmap(bm);
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
            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }
    private void pauseMusic() {
        /* 暂停音乐的函数*/
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }
    private void stopMusic() {
        /* 停止音乐的函数*/
        if (mediaPlayer!=null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_music_bottom_iv_last:
                if (currnetPlayPosition ==0) {
                    Toast.makeText(getActivity(),"已经是第一首了，没有上一曲！",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition-1;
                LocalMusicBean lastBean = mDatas.get(currnetPlayPosition);
                playMusicInMusicBean(lastBean);
                break;
            case R.id.local_music_bottom_iv_next:
                if (currnetPlayPosition ==mDatas.size()-1) {
                    Toast.makeText(getActivity(),"已经是最后一首了，没有下一曲！",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition+1;
                LocalMusicBean nextBean = mDatas.get(currnetPlayPosition);
                playMusicInMusicBean(nextBean);
                break;
            case R.id.local_music_bottom_iv_play:
                if (currnetPlayPosition == -1) {
//                    并没有选中要播放的音乐
                    Toast.makeText(getActivity(),"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying() ||  sharedPreferences.getString("bo","").equals("y")) {
                    editor.putString("bo","n");
                    editor.commit();
//                    此时处于播放状态，需要暂停音乐
                    pauseMusic();
                }else {
//                    此时没有播放音乐，点击开始播放音乐
                    playMusic();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = getActivity().getSharedPreferences("hhh", Context.MODE_PRIVATE);
        currnetPlayPosition = sharedPreferences.getInt("flag",0);
        LocalMusicBean nextBean = mDatas.get(currnetPlayPosition);
        playMusicInMusicBean(nextBean);
        if(sharedPreferences.getString("bo","").equals("y")){
            String albumArt = sharedPreferences.getString("album","");
            Bitmap bm = BitmapFactory.decodeFile(albumArt);
            Log.i("lsh123", "playMusicInMusicBean: bm=="+bm);
            albumIv.setImageBitmap(bm);
            playIv.setImageResource(R.mipmap.icon_pause);
            singerTv.setText(sharedPreferences.getString("singer",""));
            songTv.setText(sharedPreferences.getString("song",""));
        }else {
            playIv.setImageResource(R.mipmap.icon_play);
        }

    }
}
