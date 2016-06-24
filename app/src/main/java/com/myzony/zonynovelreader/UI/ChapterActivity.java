package com.myzony.zonynovelreader.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.toolbox.Volley;
import com.myzony.zonynovelreader.Common.AppContext;
import com.myzony.zonynovelreader.NovelCore.Plug_CallBack_Chapter;
import com.myzony.zonynovelreader.R;
import com.myzony.zonynovelreader.bean.ChapterInfo;
import com.myzony.zonynovelreader.bean.NovelInfo;
import com.myzony.zonynovelreader.cache.CacheManager;
import com.myzony.zonynovelreader.widget.TipInfoLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.InjectView;


/**
 * Created by mo199 on 2016/6/5.
 */
public class ChapterActivity extends BaseActivity implements Plug_CallBack_Chapter {
    public static String VIEW_CHAPTER_INFO = "view_chapter_info";

    private NovelInfo currentNovelInfo;
    private ArrayAdapter<String> chapter_list_title;
    private ArrayList<ChapterInfo> chapterInfoArrayList;
    private String cacheKey;

    @InjectView(R.id.chapter_listview)
    ListView chapterListView;

    @InjectView(R.id.chapter_tipinfo)
    TipInfoLayout tipInfoLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getExtras() != null){
            currentNovelInfo = (NovelInfo) getIntent().getSerializableExtra(VIEW_CHAPTER_INFO);
        }
        toolbar.setTitle(currentNovelInfo.getName());
        toolbar.setSubtitle(currentNovelInfo.getAuthor());
        toolbar.setSubtitleTextColor(getResources().getColor(android.R.color.white));
        // 初始化视图
        initView();

        cacheKey = VIEW_CHAPTER_INFO + currentNovelInfo.getUrl().replace("/","") + "_" +AppContext.flags;
    }

    private void initView(){
        chapter_list_title = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        tipInfoLayout.setLoading();
        setListView(false);

        // 设置监听器
        // 阅读章节
        chapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChapterActivity.this,ReadActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("pos",position);
                bundle.putString("novelUrl",currentNovelInfo.getUrl());
                bundle.putSerializable("chapterInfoList",chapterInfoArrayList);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        // 加载章节
        AppContext.getPlug().bindCB_Chapter(this);
        AppContext.getPlug().getChapterList(currentNovelInfo.getUrl(),this,Volley.newRequestQueue(this));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.chapter_layout;
    }

    /**
     * 设置是否显示列表视图
     * @param visiable true为显示，false为不显示
     */
    private void setListView(boolean visiable){
        if(visiable){
            chapterListView.setVisibility(View.VISIBLE);
            tipInfoLayout.setVisibility(View.GONE);
        }else{
            chapterListView.setVisibility(View.GONE);
            tipInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 检测该键是否有缓存数据
     * @return 存在返回true，否则返回false
     */
    private boolean isReadCacheData(){
        return CacheManager.isExistDataCache(this,cacheKey);
    }

    @Override
    public void call_Chapter(ArrayList<ChapterInfo> list) {
        chapterInfoArrayList = list;
        // 章节排序
        Collections.sort(list, new Comparator<ChapterInfo>() {
            @Override
            public int compare(ChapterInfo lhs, ChapterInfo rhs) {
                return lhs.getId().compareTo(rhs.getId());
            }
        });
        // 循环添加到adapter
        for(int i=0;i<list.size();i++){
            chapter_list_title.add(list.get(i).getTitle());
        }
        // 刷新列表
        chapterListView.setAdapter(chapter_list_title);
        setListView(true);
        // 检测是否有上次阅读位置信息
        if(isReadCacheData()){
            Serializable sri = CacheManager.readObject(this,cacheKey);
            Integer lastPos = (Integer) sri;
            // 自动加载
            Intent intent = new Intent(ChapterActivity.this,ReadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("pos",lastPos);
            bundle.putString("novelUrl",currentNovelInfo.getUrl());
            bundle.putSerializable("chapterInfoList",chapterInfoArrayList);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
