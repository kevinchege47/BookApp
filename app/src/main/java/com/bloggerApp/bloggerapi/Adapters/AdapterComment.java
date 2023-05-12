package com.bloggerApp.bloggerapi.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bloggerApp.bloggerapi.Models.ModelComment;
import com.bloggerApp.bloggerapi.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.HolderComment> {
    private Context context;
    private ArrayList<ModelComment> commentArrayList;
//    constructor

    public AdapterComment(Context context, ArrayList<ModelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_comment,parent,false);
        return new HolderComment(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
//Get DATA
        ModelComment modelComment = commentArrayList.get(position);
        String id = modelComment.getId();
        String name = modelComment.getName();
        String published = modelComment.getPublished();
        String comment = modelComment.getComment();
        String image = modelComment.getProfileImage();
//        format date
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
//        set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.dateTv.setText(formattedDate);
        try{
            Picasso.get().load(image).placeholder(R.drawable.baseline_person_gray).into(holder.profileIv);
        }catch (Exception e){
            holder.profileIv.setImageResource(R.drawable.baseline_person_gray);
        }


    }

    @Override
    public int getItemCount() {
        return commentArrayList.size();
    }

    //    View Holder class for row_conment.xml
    class HolderComment extends RecyclerView.ViewHolder{
//UI VIEWS
    ImageView profileIv;
    TextView nameTv,dateTv,commentTv;
    public HolderComment(@NonNull View itemView) {
        super(itemView);
//        init UI Views
        profileIv = itemView.findViewById(R.id.profileIv);
        nameTv = itemView.findViewById(R.id.nameTv);
        dateTv = itemView.findViewById(R.id.dateTv);
        commentTv = itemView.findViewById(R.id.commentTv);
    }
}
}
