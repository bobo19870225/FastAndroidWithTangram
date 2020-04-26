package com.jinkan.www.fastandroidwithtangram;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.Range;
import com.jinkan.www.fastandroidwithtangram.data.DEBUG;
import com.jinkan.www.fastandroidwithtangram.data.RatioTextView;
import com.jinkan.www.fastandroidwithtangram.data.SimpleImgView;
import com.jinkan.www.fastandroidwithtangram.data.SingleImageView;
import com.jinkan.www.fastandroidwithtangram.data.TestView;
import com.jinkan.www.fastandroidwithtangram.data.TestViewHolder;
import com.jinkan.www.fastandroidwithtangram.data.TestViewHolderCell;
import com.jinkan.www.fastandroidwithtangram.data.VVTEST;
import com.jinkan.www.fastandroidwithtangram.support.SampleClickSupport;
import com.jinkan.www.fastandroidwithtangram.support.SampleErrorSupport;
import com.libra.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.tmall.wireless.tangram.TangramBuilder;
import com.tmall.wireless.tangram.TangramEngine;
import com.tmall.wireless.tangram.core.adapter.GroupBasicAdapter;
import com.tmall.wireless.tangram.dataparser.concrete.Card;
import com.tmall.wireless.tangram.structure.BaseCell;
import com.tmall.wireless.tangram.structure.viewcreator.ViewHolderCreator;
import com.tmall.wireless.tangram.support.InternalErrorSupport;
import com.tmall.wireless.tangram.support.async.AsyncLoader;
import com.tmall.wireless.tangram.support.async.AsyncPageLoader;
import com.tmall.wireless.tangram.support.async.CardLoadSupport;
import com.tmall.wireless.tangram.util.IInnerImageSetter;
import com.tmall.wireless.vaf.framework.VafContext;
import com.tmall.wireless.vaf.virtualview.Helper.ImageLoader;
import com.tmall.wireless.vaf.virtualview.view.image.ImageBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScrollingActivity extends AppCompatActivity {
    TangramEngine engine;
    TangramBuilder.InnerBuilder builder;
    RecyclerView recyclerView;
    private Handler mMainHandler;

    private static class ImageTarget implements Target {

        ImageBase mImageBase;

        ImageLoader.Listener mListener;

        ImageTarget(ImageBase imageBase) {
            mImageBase = imageBase;
        }

        ImageTarget(ImageLoader.Listener listener) {
            mListener = listener;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mImageBase.setBitmap(bitmap, true);
            if (mListener != null) {
                mListener.onImageLoadSuccess(bitmap);
            }
            Log.d("TangramActivity", "onBitmapLoaded " + from);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mListener != null) {
                mListener.onImageLoadFailed();
            }
            Log.d("TangramActivity", "onBitmapFailed ");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d("TangramActivity", "onPrepareLoad ");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        setContentView(R.layout.activity_scrolling);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        setContentView(R.layout.main_activity);

        mMainHandler = new Handler(getMainLooper());
        recyclerView = findViewById(R.id.main_view);
        //Step 1: init tangram
        TangramBuilder.init(this.getApplicationContext(), new IInnerImageSetter() {
            @Override
            public <IMAGE extends ImageView> void doLoadImageUrl(@NonNull IMAGE view,
                                                                 @Nullable String url) {
                Picasso.with(ScrollingActivity.this.getApplicationContext()).load(url).into(view);
            }
        }, ImageView.class);
        //Step 2: register build=in cells and cards
        builder = TangramBuilder.newInnerBuilder(this);

        //Step 3: register business cells and cards
        builder.registerCell("1", TestView.class);
        builder.registerCell("10", SimpleImgView.class);
        builder.registerCell("2", SimpleImgView.class);
        builder.registerCell("4", RatioTextView.class);
        builder.registerCell("110",
                TestViewHolderCell.class,
                new ViewHolderCreator<>(R.layout.item_holder, TestViewHolder.class, TextView.class));
        builder.registerCell("199", SingleImageView.class);
        builder.registerVirtualView("vvtest");
        //Step 4: new engine
        engine = builder.build();
        engine.setVirtualViewTemplate(VVTEST.BIN);
        engine.setVirtualViewTemplate(DEBUG.BIN);
        engine.getService(VafContext.class).setImageLoaderAdapter(new ImageLoader.IImageLoaderAdapter() {

            private List<ImageTarget> cache = new ArrayList<>();

            @Override
            public void bindImage(String uri, final ImageBase imageBase, int reqWidth, int reqHeight) {
                RequestCreator requestCreator = Picasso.with(ScrollingActivity.this).load(uri);
                Log.d("TangramActivity", "bindImage request width height " + reqHeight + " " + reqWidth);
                if (reqHeight > 0 || reqWidth > 0) {
                    requestCreator.resize(reqWidth, reqHeight);
                }
                ImageTarget imageTarget = new ImageTarget(imageBase);
                cache.add(imageTarget);
                requestCreator.into(imageTarget);
            }

            @Override
            public void getBitmap(String uri, int reqWidth, int reqHeight, final ImageLoader.Listener lis) {
                RequestCreator requestCreator = Picasso.with(ScrollingActivity.this).load(uri);
                Log.d("TangramActivity", "getBitmap request width height " + reqHeight + " " + reqWidth);
                if (reqHeight > 0 || reqWidth > 0) {
                    requestCreator.resize(reqWidth, reqHeight);
                }
                ImageTarget imageTarget = new ImageTarget(lis);
                cache.add(imageTarget);
                requestCreator.into(imageTarget);
            }
        });
        Utils.setUedScreenWidth(720);

        //Step 5: add card load support if you have card that loading cells async
        engine.addCardLoadSupport(new CardLoadSupport(
                new AsyncLoader() {
                    @Override
                    public void loadData(Card card, @NonNull final LoadedCallback callback) {
                        Log.w("Load Card", card.load);

                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // do loading
                                JSONArray cells = new JSONArray();

                                for (int i = 0; i < 10; i++) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        obj.put("type", 1);
                                        obj.put("msg", "async loaded");
                                        JSONObject style = new JSONObject();
                                        style.put("bgColor", "#FF1111");
                                        obj.put("style", style.toString());
                                        cells.put(obj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // callback.fail(false);
                                callback.finish(engine.parseComponent(cells));
                            }
                        }, 200);
                    }
                },

                new AsyncPageLoader() {
                    @Override
                    public void loadData(final int page, @NonNull final Card card, @NonNull final LoadedCallback callback) {
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.w("Load page", card.load + " page " + page);
                                JSONArray cells = new JSONArray();
                                for (int i = 0; i < 9; i++) {
                                    try {
                                        JSONObject obj = new JSONObject();
                                        obj.put("type", 1);
                                        obj.put("msg", String.format("async page loaded, params: %s", Objects.requireNonNull(card.getParams()).toString()));
                                        cells.put(obj);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                List<BaseCell> cs = engine.parseComponent(cells);

                                if (card.page == 1) {
                                    GroupBasicAdapter<Card, ?> adapter = engine.getGroupBasicAdapter();

                                    card.setCells(cs);
                                    adapter.refreshWithoutNotify();
                                    Range<Integer> range = adapter.getCardRange(card);

                                    adapter.notifyItemRemoved(range.getLower());
                                    adapter.notifyItemRangeInserted(range.getLower(), cs.size());

                                } else
                                    card.addCells(cs);

                                //mock load 6 pages
                                callback.finish(card.page != 6);
                                card.notifyDataChange();
                            }
                        }, 400);
                    }
                }));
        engine.addSimpleClickSupport(new SampleClickSupport());

        //Step 6: enable auto load more if your page's data is lazy loaded
        engine.enableAutoLoadMore(true);
        engine.register(InternalErrorSupport.class, new SampleErrorSupport());

        //Step 7: bind recyclerView to engine
        engine.bindView(recyclerView);

        //Step 8: listener recyclerView onScroll event to trigger auto load more
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                engine.onScrolled();
            }
        });

        //Step 9: set an offset to fix card
        engine.getLayoutManager().setFixOffset(0, 40, 0, 0);

        //Step 10: get tangram data and pass it to engine
        String json = new String(getAssertsFile(this, "data.json"));
        JSONArray data;
        try {
            data = new JSONArray(json);
            engine.setData(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getAssertsFile(Context context, String fileName) {
        InputStream inputStream;
        AssetManager assetManager = context.getAssets();
        try {
            inputStream = assetManager.open(fileName);

            int length;
            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                length = bis.available();
                byte[] data = new byte[length];
                int read = bis.read(data);

                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
