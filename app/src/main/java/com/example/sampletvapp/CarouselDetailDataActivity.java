package com.example.sampletvapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.leanback.widget.HorizontalGridView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.sampletvapp.utilities.Colors;
import com.example.sampletvapp.utilities.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CarouselDetailDataActivity - Displays content in three different modes:
 * Premium (11), MI Radio (22), and Karaoke (33)
 *
 * This is optimized for Android TV navigation
 */
public class CarouselDetailDataActivity extends AppCompatActivity {

    private static final String TAG = "CarouselDetailData";

    // UI Components
    private HorizontalGridView lowerMenuRecyclerView;
    private HorizontalGridView lowerMenuRecyclerView2;
    private RecyclerView videoListRecyclerView;
    private RecyclerView premiumSubCategoriesRecyclerView;
    private RecyclerView karaokeListRecyclerView;

    private EditText searchEditText;
    private ProgressBar loadingProgressBar;
    private TextView noRecordFoundTextView;
    private TextView titleTextView;

    // Data Lists
    private List<LowerMenuItem> lowerMenuList = new ArrayList<>();
    private List<LowerMenuItem> lowerMenuList2 = new ArrayList<>();
    private List<VideoItem> videoList = new ArrayList<>();
    private List<PremiumSubCategory> premiumSubCategoriesList = new ArrayList<>();
    private List<KaraokeItem> karaokeList = new ArrayList<>();
    private List<KaraokeItem> filteredList = new ArrayList<>();

    // Adapters
    private LowerMenuAdapter lowerMenuAdapter;
    private LowerMenuAdapter lowerMenuAdapter2;
    private VideoListAdapter videoListAdapter;
    private PremiumSubCategoriesAdapter premiumSubCategoriesAdapter;
    private KaraokeAdapter karaokeAdapter;

    // State Variables
    private String token = "";
    private int displayType = 0;
    private boolean loading = false;
    private String searchText = "";
    private int offset = 1;
    private String primaryTvCategoryId = "52";
    private String primaryTvSubCategoryId = "";
    private String tvChannelTitle = "Luzon tv";

    /**
     * Activity lifecycle methods and initialization
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carousel_detail_data);

        initViews();
        setupLowerMenus();
        getIntentData();
        getTokenAndInitialize();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Request focus on the first item when the activity is resumed
        if (displayType == 11) {
            if (premiumSubCategoriesRecyclerView != null && premiumSubCategoriesRecyclerView.getChildCount() > 0) {
                premiumSubCategoriesRecyclerView.getChildAt(0).requestFocus();
            }
        } else if (displayType == 22) {
            if (findViewById(R.id.tv_channels_recycler_view) != null) {
                RecyclerView tvChannelsRecyclerView = findViewById(R.id.tv_channels_recycler_view);
                if (tvChannelsRecyclerView.getChildCount() > 0) {
                    tvChannelsRecyclerView.getChildAt(0).requestFocus();
                }
            }
        } else if (displayType == 33) {
            if (karaokeListRecyclerView != null && karaokeListRecyclerView.getChildCount() > 0) {
                View firstItem = karaokeListRecyclerView.getChildAt(0);
                if (firstItem != null) {
                    View playButton = firstItem.findViewById(R.id.play_button);
                    if (playButton != null) {
                        playButton.requestFocus();
                    }
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Implement custom key handling for D-pad navigation if needed
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    // You can add custom navigation logic here if needed
                    break;

                case KeyEvent.KEYCODE_BACK:
                    Intent intent = new Intent(this, NewHomeScreenActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NewHomeScreenActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Initialize views and setup UI components
     */
    private void initViews() {
        lowerMenuRecyclerView = findViewById(R.id.lower_menu_recycler_view);
        lowerMenuRecyclerView2 = findViewById(R.id.lower_menu_recycler_view2);
        videoListRecyclerView = findViewById(R.id.video_list_recycler_view);
        premiumSubCategoriesRecyclerView = findViewById(R.id.premium_sub_categories_recycler_view);
        karaokeListRecyclerView = findViewById(R.id.karaoke_list_recycler_view);

        searchEditText = findViewById(R.id.search_edit_text);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        noRecordFoundTextView = findViewById(R.id.no_record_found_text_view);
        titleTextView = findViewById(R.id.title_text_view);

        // Configure RecyclerViews
        lowerMenuRecyclerView.setHasFixedSize(true);
        lowerMenuRecyclerView.setFocusable(true);
        lowerMenuRecyclerView2.setHasFixedSize(true);
        lowerMenuRecyclerView2.setFocusable(true);

        // Set up adapters
        lowerMenuAdapter = new LowerMenuAdapter(lowerMenuList);
        lowerMenuAdapter2 = new LowerMenuAdapter(lowerMenuList2);
        videoListAdapter = new VideoListAdapter(videoList);
        premiumSubCategoriesAdapter = new PremiumSubCategoriesAdapter(premiumSubCategoriesList);
        karaokeAdapter = new KaraokeAdapter(karaokeList);

        lowerMenuRecyclerView.setAdapter(lowerMenuAdapter);
        lowerMenuRecyclerView2.setAdapter(lowerMenuAdapter2);

        // For Video List - grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        videoListRecyclerView.setLayoutManager(gridLayoutManager);
        videoListRecyclerView.setAdapter(videoListAdapter);

        // For Premium Sub Categories - grid layout
        GridLayoutManager premiumGridLayoutManager = new GridLayoutManager(this, 2);
        premiumSubCategoriesRecyclerView.setLayoutManager(premiumGridLayoutManager);
        premiumSubCategoriesRecyclerView.setAdapter(premiumSubCategoriesAdapter);

        // For Karaoke List - linear layout
        LinearLayoutManager karaokeLayoutManager = new LinearLayoutManager(this);
        karaokeListRecyclerView.setLayoutManager(karaokeLayoutManager);
        karaokeListRecyclerView.setAdapter(karaokeAdapter);

        setupSearchListener();

        // Set up home button click listener
        findViewById(R.id.home_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, NewHomeScreenActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Setup lower menu data
     */
    private void setupLowerMenus() {
        // Setup first lower menu
        lowerMenuList.add(new LowerMenuItem(1, R.drawable.lets_meet_lower));
        lowerMenuList.add(new LowerMenuItem(2, R.drawable.realtime_lower));
        lowerMenuList.add(new LowerMenuItem(3, R.drawable.pager_lower));
        lowerMenuList.add(new LowerMenuItem(4, R.drawable.conc_yellow));
        lowerMenuAdapter.notifyDataSetChanged();

        // Setup second lower menu
        lowerMenuList2.add(new LowerMenuItem(52, "Luzon tv", R.drawable.luzontv_lower));
        lowerMenuList2.add(new LowerMenuItem(53, "Mindanao Tv", R.drawable.mindanao_lower));
        lowerMenuList2.add(new LowerMenuItem(54, "Visayas Tv", R.drawable.visayastv_lower));
        lowerMenuList2.add(new LowerMenuItem(55, "dbuzz", R.drawable.dbuzz_lower));
        lowerMenuAdapter2.notifyDataSetChanged();
    }

    /**
     * Get data from intent
     */
    private void getIntentData() {
        String productTitle = getIntent().getStringExtra("producttitle");
        if (productTitle != null) {
            if (productTitle.toLowerCase().equals("premium")) {
                displayType = 11;
            } else if (productTitle.toLowerCase().equals("mi radio")) {
                displayType = 22;
                if (primaryTvCategoryId.equals("52")) {
                    tvChannelTitle = "Luzon Tv";
                } else if (primaryTvCategoryId.equals("53")) {
                    tvChannelTitle = "Mindanao Tv";
                } else if (primaryTvCategoryId.equals("54")) {
                    tvChannelTitle = "Visayas Tv";
                }
            } else if (productTitle.toLowerCase().equals("karaoke")) {
                displayType = 33;
            }
        }
    }

    /**
     * Get token and initialize data
     */
    private void getTokenAndInitialize() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        if (!token.isEmpty()) {
            if (displayType == 11) {
                getTargetAppData();
                makeApiRequest(token, "action", "11");
            } else if (displayType == 22) {
                getTvChannelsCategory(primaryTvCategoryId);
            } else if (displayType == 33) {
                getKaraokeData();
            }

            updateUI();
        }
    }

    /**
     * Update UI based on display type
     */
    private void updateUI() {
        initializeDisplayType();
    }


