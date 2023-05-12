package com.bloggerApp.bloggerapi.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloggerApp.bloggerapi.Models.ModelPost;
import com.bloggerApp.bloggerapi.Activities.PostDetailsActivity;
import com.bloggerApp.bloggerapi.R;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterPost extends RecyclerView.Adapter<AdapterPost.HolderPost> {

    private Context context;
    private ArrayList<ModelPost> postArrayList;

    public AdapterPost(Context context, ArrayList<ModelPost> postArrayList) {
        this.context = context;
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public HolderPost onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_post,parent,false);
        return new HolderPost(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPost holder, int position) {
//        get data, set data, handle click
        ModelPost model = postArrayList.get(position);//get data at specific position
//        get data
        String authorName = model.getAuthorName();
        String content = model.getContent();
        String id = model.getId();
        String published = model.getPublished();
        String selfLink = model.getSelfLink();
        String title = model.getTitle();
        String updated = model.getUpdated();
        String url = model.getUrl();
//        content description is in HTML /WE BFORM. we need to convert it to simple text uding jsoup
        Document document = Jsoup.parse(content);
        try{
            Elements elements = document.select("img");
            String image  = elements.get(0).attr("src");
            Picasso.get().load(image).placeholder(R.drawable.baseline_image_24).into(holder.imageIv);
        }catch (Exception e){
//            exception occured while getting image, set default
                holder.imageIv.setImageResource(R.drawable.baseline_image_24);
        }
//        format date
        String gmtDate = published;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy K:mm a");// e.g 25/10/2020 02:12 PM
        String formattedDate = "";
        try{
            Date date = dateFormat.parse(gmtDate);
            formattedDate = dateFormat2.format(date);
        }
        catch(Exception e){
//            IF THERE IS ANY EXCEPTION WHILE FORMATTING DATE, SHOW THE SAME WE GOT FROM FROM api
            formattedDate = published;
            e.printStackTrace();
        }
        holder.titleTv.setText(title);
        holder.descriptionTv.setText(document.text());
        holder.publishInfoTv.setText("By"+authorName+" "+formattedDate); // e.g By Atif Pervaiz 25/10/2020 02:12 PM

//        handle click,start activity wth post id
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent to start activity
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId",id); //key,value pair to pass to post details activity
                context.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
//        returns number of records, list size
    }

    // View holder class that holds UI views of row_post.xml
    class HolderPost extends RecyclerView.ViewHolder{
//Ui views of row_post.xml
    ImageButton moreBtn;
    TextView titleTv,publishInfoTv,descriptionTv;
    ImageView imageIv;
    public HolderPost(@NonNull View itemView) {
        super(itemView);
//        init UI views
        moreBtn = itemView.findViewById(R.id.moreBtn);
        titleTv = itemView.findViewById(R.id.titleTv);
        publishInfoTv = itemView.findViewById(R.id.publishInfoTv);
        imageIv = itemView.findViewById(R.id.imageIv);
        descriptionTv = itemView.findViewById(R.id.descriptionTv);
    }
}
}
