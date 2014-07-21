package com.denisk.bullshitbingochampion;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import org.askerov.dynamicgrid.BaseDynamicGridAdapter;
import org.askerov.dynamicgrid.DynamicGridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BullshitBingoActivity extends Activity {

    public static final String BULLSHIT_FILES_PATH_KEY = "bullshitFilesPath";
    public static final String DIR_NAME = "bullshitbingochamp";
    public static final String PREFS_NAME = "bullshitbingochamp";

    int dim = 5;
    private DynamicGridView gridView;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createDirIfNeeded();

        setContentView(R.layout.bingo_activity);

        gridView = (DynamicGridView) findViewById(R.id.gridview);
        gridView.setNumColumns(dim);

        final ArrayList<StringHolder> words = new ArrayList<>(dim*dim);
        for (int i = 0; i < dim; i++) {
            for(int j = 0; j < dim; j++) {
//                words.add(new StringHolder("bb"));
                words.add(new StringHolder(i + "_" + j));
            }
        }
        gridView.setOnDragListener(new DynamicGridView.OnDragListener() {
            @Override
            public void onDragStarted(int position) {
                Log.d("===", "drag started at position " + position);
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition) {
                Log.d("===", String.format("drag item position changed from %d to %d", oldPosition, newPosition));
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                gridView.startEditMode(position);
                return true;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(BullshitBingoActivity.this, parent.getAdapter().getItem(position).toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        gridView.post(new Runnable() {
            @Override
            public void run() {
                final int gridWidth = gridView.getWidth();
                final int gridHeight = gridView.getHeight();
                gridView.setAdapter(new BaseDynamicGridAdapter(BullshitBingoActivity.this, words, dim) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        StringHolder text = (StringHolder) getItem(position);
                        TextView textView;
                        if (!(convertView instanceof TextView)) {
                            textView = (TextView) getLayoutInflater().inflate(R.layout.word, null);
                            textView.setWidth(gridWidth / dim);
                            textView.setHeight(gridHeight / dim);

                        } else {
                            textView = (TextView) convertView;
                        }

                        textView.setText(text.s);
                        return textView;
                    }
                });
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bbc_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private static class StringHolder {
        String s;

        StringHolder(String s) {
            this.s = s;
        }
    }
    @Override
    public void onBackPressed() {
        if (gridView.isEditMode()) {
            gridView.stopEditMode();
        } else {
            super.onBackPressed();
        }
    }

    private void createDirIfNeeded() {
        File bullshitDir;
        if(isExternalStorageWritable()) {
            bullshitDir = Environment.getExternalStoragePublicDirectory(DIR_NAME);
        } else {
            //use internal storage
            bullshitDir = getDir(DIR_NAME, MODE_PRIVATE);
        }

        bullshitDir.mkdirs();

        try {
            new File(bullshitDir, ".nomedia").createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Can't create .nomedia file", e);
        }

        if(! bullshitDir.exists()) {
            throw new IllegalStateException("Can't create directory to store *.bullshit files at " + bullshitDir);
        } else {
            Log.i(BullshitBingoActivity.class.getName(), "===Bullshit dir: " + bullshitDir);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(BULLSHIT_FILES_PATH_KEY, bullshitDir.toString()).commit();
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
