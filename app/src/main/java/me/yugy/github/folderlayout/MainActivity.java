package me.yugy.github.folderlayout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by yugy on 14/12/2.
 */
public class MainActivity extends FragmentActivity {

    private FolderLayout mFolderLayout;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;

    private ViewPager mViewPager;
    private ListView mListView;
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFolderLayout = (FolderLayout) findViewById(R.id.folder_layout);
        text1 = (TextView) findViewById(R.id.item1);
        text2 = (TextView) findViewById(R.id.item2);
        text3 = (TextView) findViewById(R.id.item3);
        text4 = (TextView) findViewById(R.id.item4);

        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderLayout.toggleItem(3);
            }
        });
        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderLayout.toggleItem(2);
            }
        });
        text3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderLayout.toggleItem(1);
            }
        });
        text4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFolderLayout.toggleItem(0);
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new ListFragment();
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                new String[]{
                        "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item",
                        "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item",
                        "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item",
                        "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item", "list item",
                }));

        mGridView = (GridView) findViewById(R.id.grid);
        mGridView.setAdapter(mListView.getAdapter());
    }

}
