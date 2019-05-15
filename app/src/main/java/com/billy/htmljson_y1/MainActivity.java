package com.billy.htmljson_y1;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button btn;
    private RecyclerView recyclerView;

    private RecyclerView.LayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.button);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                new HtmlAsyncTask().execute("http://www.djhub.net/api/top?type=downloads");
                break;
        }
    }

    private List<MusicItem> parseJson(InputStream is){
        List<MusicItem> items = new ArrayList<>();

        try {
            JsonReader jr = new JsonReader(new InputStreamReader(is));
            jr.beginArray();

            while(jr.hasNext()){
                jr.beginObject();

                MusicItem item = new MusicItem();
                while(jr.hasNext()){
                    String name = jr.nextName();
                    if("name".equals(name)){
                        item.setName(jr.nextString());
                    }else if("url".equals(name)){
                        item.setUrl(jr.nextString());
                    }else{
                        jr.skipValue();
                    }
                }

                items.add(item);
                jr.endObject();
            }

            jr.endArray();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public class MusicItemAdapter extends ArrayAdapter<MusicItem>{

        private List<MusicItem> items;

        public MusicItemAdapter(@NonNull Context context, int resource, @NonNull List<MusicItem> objects) {
            super(context, resource, objects);

            items = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MusicItem item = items.get(position);

            View view = (View) LayoutInflater.from(getContext()).inflate(R.layout.music_item, null);

            TextView nameTV = view.findViewById(R.id.name_tv);
            nameTV.setText(item.getName());

            TextView urlTV = view.findViewById(R.id.url_tv);
            urlTV.setText(item.getUrl());

            return view;
        }
    }

    public class MusicItemRecyclerAdapter extends RecyclerView.Adapter<MusicItemRecyclerAdapter.MyViewHolder>{
        private List<MusicItem> items;

        public MusicItemRecyclerAdapter(List<MusicItem> items){
            this.items = items;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MusicItem item = items.get(position);
            holder.name_tv.setText(item.getName());
            holder.url_tv.setText(item.getUrl());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        protected class MyViewHolder extends RecyclerView.ViewHolder {
            protected TextView name_tv, url_tv;

            public MyViewHolder(View itemView) {
                super(itemView);

                name_tv = itemView.findViewById(R.id.name_tv);
                url_tv = itemView.findViewById(R.id.url_tv);
            }
        }
    }

    public class HtmlAsyncTask extends AsyncTask<String, Double, List<MusicItem>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btn.setEnabled(false);
        }

        @Override
        protected List<MusicItem> doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                InputStream is = con.getInputStream();
                return parseJson(is);

            }catch (Exception e){
                e.printStackTrace();
            }

            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<MusicItem> musicItems) {
            super.onPostExecute(musicItems);
            recyclerView.setAdapter(new MusicItemRecyclerAdapter(musicItems));

            btn.setEnabled(true);
        }
    }
}
