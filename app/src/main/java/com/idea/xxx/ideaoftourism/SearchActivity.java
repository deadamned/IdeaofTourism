package com.idea.xxx.ideaoftourism;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {
    private SearchMapFragmentView m_searchmapFragmentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        m_searchmapFragmentView = new SearchMapFragmentView(this);
    }
}
