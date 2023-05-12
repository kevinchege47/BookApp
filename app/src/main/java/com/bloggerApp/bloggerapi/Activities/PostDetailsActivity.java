package com.bloggerApp.bloggerapi.Activities;

import static javax.xml.transform.OutputKeys.ENCODING;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bloggerApp.bloggerapi.Adapters.AdapterComment;
import com.bloggerApp.bloggerapi.Adapters.AdapterLabel;
import com.bloggerApp.bloggerapi.Constants.Constants;
import com.bloggerApp.bloggerapi.Models.ModelComment;
import com.bloggerApp.bloggerapi.Models.ModelLabel;
import com.bloggerApp.bloggerapi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostDetailsActivity extends AppCompatActivity {
    //    Ui Views
    private TextView titleTv, publishInfoTv;
    private WebView webView;
    private RecyclerView labelsRv;
    private RecyclerView commentsRv;
    private String postId;//will get from intent, was passed in intent from AdapterPost
    private static final String TAG = "POST_DETAILS_TAG";
    private static final String TAG_COMMENTS = "POST_COMMENTS_TAG";
    private ArrayList<ModelLabel> labelArrayList;
    private AdapterLabel adapterLabel;
    private ArrayList<ModelComment> modelCommentArrayList;
    private AdapterComment adapterComment;
    //    ACTION BAR
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
//        init actionvbar
        actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
//        add back button
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
//        init UI Views
        titleTv = findViewById(R.id.titleTv);
        publishInfoTv = findViewById(R.id.publishInfoTv);
        webView = findViewById(R.id.webView);
        labelsRv = findViewById(R.id.labelsRv);
        commentsRv = findViewById(R.id.commentsRv);
//        will get id from intent
        postId = getIntent().getStringExtra("postId");
        Log.d(TAG, "onCreate: " + postId);
//        setup webview
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        loadPostDetails();
        loadPostComments();

    }

    private void loadPostDetails() {
        String url = "https://www.googleapis.com/blogger/v3/blogs/" + Constants.BLOG_ID + "/posts/" + postId + "?key=" + Constants.API_KEY;
        Log.d(TAG, "loadPostDetails: URL" + url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                successfully received response
//                Response is a JSON object
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    ........getdata............
                    String title = jsonObject.getString("title");
                    String published = jsonObject.getString("published");
                    String content = jsonObject.getString("content");
                    String url = jsonObject.getString("url");
                    String displayName = jsonObject.getJSONObject("author").getString("displayName");

//                    convert GMT date to proper format
                    //        format date
                    String gmtDate = published;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy K:mm a");// e.g 25/10/2020 02:12 PM
                    String formattedDate = "";
                    try {
                        Date date = dateFormat.parse(gmtDate);
                        formattedDate = dateFormat2.format(date);
                    } catch (Exception e) {
//            IF THERE IS ANY EXCEPTION WHILE FORMATTING DATE, SHOW THE SAME WE GOT FROM FROM api
                        formattedDate = published;
                        e.printStackTrace();
                    }
//                    .....set data......
                    actionBar.setSubtitle(title);
                    titleTv.setText(title);
                    publishInfoTv.setText("By " + displayName + " " + formattedDate);//By Kevin Chege 08/12/2023
//                    content contains web page like html,so load in webview
                    webView.loadDataWithBaseURL(null, content, "text/html", ENCODING, null);
//                    get labels of post
                    try {
//                        init and clear list before adding data
                        labelArrayList = new ArrayList<>();
                        labelArrayList.clear();

//                        json array of labels
                        JSONArray jsoNarray = jsonObject.getJSONArray("labels");
                        for (int i = 0; i < jsoNarray.length(); i++) {
                            String label = jsoNarray.getString(i);
//                            add label in model
                            ModelLabel modelLabel = new ModelLabel(label);
//                            add model in list
                            labelArrayList.add(modelLabel);
                        }
//                        setup adapter
                        adapterLabel = new AdapterLabel(PostDetailsActivity.this, labelArrayList);
//                        set adapter to recycler view
                        labelsRv.setAdapter(adapterLabel);

                    } catch (Exception e) {
                        Log.d(TAG, "onResponse: " + e.getMessage());
                    }
//post details loaded,now load comments
//                    loadPostComments();

                } catch (Exception e) {
                    Log.d(TAG, "onResponse: " + e.getMessage());
                    Toast.makeText(PostDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                failed receiving response
                Toast.makeText(PostDetailsActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
//        ADD REQUEST TO QUEUE
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void loadPostComments() {
        String url = "https://www.googleapis.com/blogger/v3/blogs/"
                + Constants.BLOG_ID + "/posts/"
                + postId + "/comments?"
                + Constants.API_KEY;
        Log.d(TAG_COMMENTS, "loadPostComments: " + url);
//        Volley string request
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Got the response
                Log.d(TAG, "onResponse: " + response);
//                init and clear array list before adding data
                modelCommentArrayList = new ArrayList<>();
                modelCommentArrayList.clear();
                try {
//                    response is in JSON Object form
                    JSONObject jsonObject = new JSONObject(response);
//                    get array of comments
                    JSONArray jsonArrayItems = jsonObject.getJSONArray("items");
                    for (int i = 0; i < jsonArrayItems.length(); i++) {
//                        get specific comment Json object
                        JSONObject jsonObjectComment = jsonArrayItems.getJSONObject(i);
//                        get data from the json object
                        String id = jsonObjectComment.getString("id");
                        String published = jsonObjectComment.getString("published");
                        String content = jsonObjectComment.getString("content");
                        String displayName = jsonObject.getJSONObject("author").getString("displayName");
                        String profileImage = jsonObject.getJSONObject("author").getJSONObject("image").getString("url");
                        Log.d(TAG_COMMENTS, "onResponse: " + profileImage);
//                        add data to model
                        ModelComment modelComment = new ModelComment("" + id, "" + displayName, "" + profileImage, "" + published, "" + content);
//                        add model to arraylist
                        modelCommentArrayList.add(modelComment);
                    }
//                    setup adapter
                    adapterComment = new AdapterComment(PostDetailsActivity.this, modelCommentArrayList);
                    commentsRv.setAdapter(adapterComment);
//                    set adapter to recycler view
                } catch (Exception e) {

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                failed getting a response
                Log.d(TAG_COMMENTS, "onErrorResponse: ");
            }
        });
//        add request to queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go to previous activity,when back button is pressed
        return super.onSupportNavigateUp();
    }
}