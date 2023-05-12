package com.bloggerApp.bloggerapi.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bloggerApp.bloggerapi.Adapters.AdapterPost;
import com.bloggerApp.bloggerapi.Constants.Constants;
import com.bloggerApp.bloggerapi.Models.ModelPost;
import com.bloggerApp.bloggerapi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
//    UI views
    private RecyclerView postsRv;
    private Button loadMoreBtn;
    private EditText searchEt;
    private ImageButton searchBtn;
    private ProgressDialog progressDialog;

    private String url = "";//complete url for retrieving posts
    private String nextToken = "";//next page token to load more posts
    private boolean isSearch = false;

    private ArrayList<ModelPost> postArrayList;
    private AdapterPost adapterPost;

    private static final String TAG = "MAIN_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        init UI views
        postsRv = findViewById(R.id.postsRv);
        loadMoreBtn = findViewById(R.id.loadMoreBtn);
        searchEt = findViewById(R.id.searchEt);
        searchBtn = findViewById(R.id.searchBtn);
//        setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
//        init and clear list before adding data into it
        postArrayList = new ArrayList<>();
        postArrayList.clear();

        loadPosts();
//        load more button click
        loadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEt.getText().toString().trim();
                if(TextUtils.isEmpty(query)){
                    loadPosts();
                }else{
                    searchPosts(query);
                }            }
        });
//        handle click,search posts
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextToken="";
                url = "";

                postArrayList = new ArrayList<>();
                postArrayList.clear();
//                get text from edit text
                String query = searchEt.getText().toString().trim();
                if(TextUtils.isEmpty(query)){
                    loadPosts();
                }else{
                    searchPosts(query);
                }
            }
        });
    }

    private void searchPosts(String query) {
        isSearch = true;
        Log.d(TAG, "searchPosts: isSearch: "+isSearch);
        progressDialog.show();
        if (nextToken.equals("")){
            Log.d(TAG,"searchPosts:  Next Page Token is Empty,no more posts");
            url = "https://www.googleapis.com/blogger/v3/blogs/"
                    + Constants.BLOG_ID
                    +"/posts/search?q="
                    +query
                    +"&key="+Constants.API_KEY;
        } else if (nextToken.equals("end")) {
            Log.d(TAG,"searchPosts:  Next Token is empty/end, no more posts");
            Toast.makeText(this, "No More Posts", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        else {
            Log.d(TAG,"searchPosts:  Next Page Token is Empty,no more posts");
            url = "https://www.googleapis.com/blogger/v3/blogs/"
                    +Constants.BLOG_ID
                    +"/posts/search?q="
                    +query
                    +"&pageToken="+nextToken
                    +"&key="+Constants.API_KEY;
        }
        Log.d(TAG,"searchPosts: URL: "+url);
//        Request data, method is GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                we got response, so didmiss dialog first
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response);
//                json data is in response parameter of this function, it may cause exception while parsing/formatting so do it in a a try catch
                try {
//                    RESPONSE IS IN json object
                    JSONObject jsonObject = new JSONObject(response);
                    try {
                        nextToken = jsonObject.getString("nextPageToken");
                        Log.d(TAG, "onResponse: NextPageToken: "+nextToken);
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Reached end of page", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onResponse: Reached end of page.."+e.getMessage());
                        nextToken = "end";
                    }
//                    get json array data from json
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
//                    continue getting data while its completed
                    for (int i = 0;i<jsonArray.length();i++){
                        try {
//                            get data
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.getString("id");
                            String title = jsonObject1.getString("title");
                            String content = jsonObject1.getString("content");
                            String published = jsonObject1.getString("published");
                            String updated = jsonObject1.getString("updated");
                            String selfLink = jsonObject1.getString("selfLink");
                            String authorName = jsonObject1.getJSONObject("author").getString("displayName");
//                            String image = jsonObject1.getJSONObject("author").getString("image");
//                            set data
                            ModelPost modelPost = new ModelPost(""+authorName,
                                    ""+content,
                                    ""+id,
                                    ""+published,
                                    ""+selfLink,
                                    ""+title,
                                    ""+updated,
                                    ""+url);
//                            add data/model to list
                            postArrayList.add(modelPost);

                        }catch (Exception e){
                            Log.d(TAG, "onResponse: 1 "+e.getMessage());
                            Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    //                        setup adapter
                    adapterPost = new AdapterPost(MainActivity.this,postArrayList);
//                        set adapter to recycler view
                    postsRv.setAdapter(adapterPost);
                    progressDialog.dismiss();

                }catch (Exception e){
                    Log.d(TAG, "onResponse: 2 "+e.getMessage());
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
                Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
//        add Request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void loadPosts() {
        isSearch = false;
        Log.d(TAG, "loadPosts: isSearch: "+isSearch);
        progressDialog.show();
        if (nextToken.equals("")){
            Log.d(TAG,"loadPosts:  Next Page Token is Empty,no more posts");
            url = "https://www.googleapis.com/blogger/v3/blogs/"
                    +Constants.BLOG_ID+"/posts?maxResults="
                    +Constants.MAX_POST_RESULTS+"&key="
                    +Constants.API_KEY;
        } else if (nextToken.equals("end")) {
            Log.d(TAG,"loadPosts:  Next Token is empty/end, no more posts");
            Toast.makeText(this, "No More Posts", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            return;
        }
        else {
            Log.d(TAG,"loadPosts:  Next Page Token is Empty,no more posts");
            url = "https://www.googleapis.com/blogger/v3/blogs/"
                    +Constants.BLOG_ID
                    +"/posts?maxResults="
                    +Constants.MAX_POST_RESULTS
                    +"&pageToken="+nextToken
                    +"&key="+Constants.API_KEY;
        }
        Log.d(TAG,"loadPosts: URL: "+url);
//        Request data, method is GET
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                we got response, so didmiss dialog first
                progressDialog.dismiss();
                Log.d(TAG, "onResponse: " + response);
//                json data is in response parameter of this function, it may cause exception while parsing/formatting so do it in a a try catch
                try {
//                    RESPONSE IS IN json object
                    JSONObject jsonObject = new JSONObject(response);
                    try {
                        nextToken = jsonObject.getString("nextPageToken");
                        Log.d(TAG, "onResponse: NextPageToken: "+nextToken);
                    }catch (Exception e){
                        Toast.makeText(MainActivity.this, "Reached end of page", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onResponse: Reached end of page.."+e.getMessage());
                        nextToken = "end";
                    }
//                    get json array data from json
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
//                    continue getting data while its completed
                    for (int i = 0;i<jsonArray.length();i++){
                        try {
//                            get data
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.getString("id");
                            String title = jsonObject1.getString("title");
                            String content = jsonObject1.getString("content");
                            String published = jsonObject1.getString("published");
                            String updated = jsonObject1.getString("updated");
                            String selfLink = jsonObject1.getString("selfLink");
                            String authorName = jsonObject1.getJSONObject("author").getString("displayName");
//                            String image = jsonObject1.getJSONObject("author").getString("image");
//                            set data
                            ModelPost modelPost = new ModelPost(""+authorName,
                                    ""+content,
                                    ""+id,
                                    ""+published,
                                    ""+selfLink,
                                    ""+title,
                                    ""+updated,
                                    ""+url);
//                            add data/model to list
                            postArrayList.add(modelPost);

                        }catch (Exception e){
                            Log.d(TAG, "onResponse: 1 "+e.getMessage());
                            Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    //                        setup adapter
                    adapterPost = new AdapterPost(MainActivity.this,postArrayList);
//                        set adapter to recycler view
                    postsRv.setAdapter(adapterPost);
                    progressDialog.dismiss();

                }catch (Exception e){
                    Log.d(TAG, "onResponse: 2 "+e.getMessage());
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: "+error.getMessage());
                Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
//        add Request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}