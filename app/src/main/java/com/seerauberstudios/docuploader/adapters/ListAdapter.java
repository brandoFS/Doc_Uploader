package com.seerauberstudios.docuploader.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import com.parse.ParseObject;
import com.seerauberstudios.docuploader.R;
import com.seerauberstudios.docuploader.ViewDocumentActivity;
import com.seerauberstudios.docuploader.util.ParseConstants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by brando on 6/17/15.
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private ArrayList<ParseObject> mDataset = new ArrayList<ParseObject>();
    Context context1;


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textInput;
        public CardView cardItemView;
        public ImageView docImageView;


        //Create viewholder that will represent each card
        public ViewHolder(View v) {
            super(v);

            cardItemView = (CardView) v.findViewById(R.id.card_view);
            textInput = (TextView) v.findViewById(R.id.listitem_text);
            docImageView = (ImageView) v.findViewById(R.id.listitem_image);

        }

    }


    public ListAdapter(ArrayList<ParseObject> input, Context context) {
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ParseFile parseFile = mDataset.get(position).getParseFile("Document");
        Uri fileUri = Uri.parse(parseFile.getUrl());
        Picasso.with(context1).load(fileUri.toString()).placeholder(R.mipmap.ic_insert_drive_file_black_36dp).fit().into(holder.docImageView);
        holder.textInput.setText(context1.getString(R.string.doc_status_title) + " " + (mDataset.get(position).getBoolean(ParseConstants.KEY_REVIEW) ? context1.getString(R.string.doc_status_reviewedapproved) : context1.getString(R.string.doc_status_waitingreview)));
        holder.cardItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(),ViewDocumentActivity.class );
                ParseFile file = mDataset.get(position).getParseFile("Document");
                Uri fileUri = Uri.parse(file.getUrl());
                intent.setData(fileUri);
                v.getContext().startActivity(intent);
            }
        });

    }

    // Return the size of dataset
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    //Add item remeber
    public void addItem(ParseObject item){
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

    public void refill(List<ParseObject> documents){
        mDataset.clear();
        mDataset.addAll(documents);
        notifyDataSetChanged();
    }


}