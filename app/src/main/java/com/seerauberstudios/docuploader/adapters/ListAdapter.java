package com.seerauberstudios.docuploader.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.seerauberstudios.docuploader.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * Created by brando on 6/17/15.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private ArrayList<ParseFile> mDataset = new ArrayList<ParseFile>();
    Context context1;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView textInput;
        public CardView cardItemView;
        public ImageView docImageView;


        //Create viewholder that will represent each card
        public ViewHolder(View v) {
            super(v);

            cardItemView = (CardView)v.findViewById(R.id.card_view);
            textInput = (TextView)v.findViewById(R.id.listitem_text);
            docImageView = (ImageView)v.findViewById(R.id.listitem_image);


        }
        //Handle clicks
        @Override
        public void onClick(View v) {
                    Toast.makeText(v.getContext(), "Text Copied!", Toast.LENGTH_SHORT).show();

            }
        }


    public ListAdapter(ArrayList<ParseFile> input, Context context) {
        mDataset = input;
        context1 = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Uri fileUri = Uri.parse(mDataset.get(position).getUrl());
        Picasso.with(context1).load(fileUri.toString()).into(holder.docImageView);
        holder.textInput.setText("HI!");
        System.out.println("HERE!!!!!!!! 4");


    }

    // Return the size of dataset
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    //Add item remeber to
    public void addItem(ParseFile item){
        mDataset.add(mDataset.size(), item);
        notifyItemInserted(mDataset.size());
        notifyDataSetChanged();
    }


    public void removeItem(String item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }


}