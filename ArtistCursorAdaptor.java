package com.example.android.spotifystreamer3;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by neeraja on 9/29/2015.
 */
public class ArtistCursorAdaptor extends CursorAdapter {

    public ArtistCursorAdaptor(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.row_items, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Retrieve student records
        String URL = "content://com.example.provider.SpotifyDB/spotifyArtistTable";

        Uri spotifyArtistTable = Uri.parse(URL);
        //cursor = managedQuery(spotifyArtistTable, null, null, null, "artistNAME");


        // Find fields to populate in inflated template
        TextView textView = (TextView) view
                .findViewById(R.id.list_item_artistname_textview1);
        //the image view
        ImageView imagenow = (ImageView) view
                .findViewById(R.id.list_item_thumbnail_Image);

        // Extract properties from cursor
        String artistName = cursor.getString(cursor.getColumnIndexOrThrow("artistName"));
        String artistImageUrl = cursor.getString(cursor.getColumnIndexOrThrow("artistImageUrl"));

        // Populate fields with extracted properties
        textView.setText(artistName);
        if (!artistImageUrl.isEmpty())
            Picasso.with(context).load(artistImageUrl).error(R.drawable.images1).resize(200, 200).into(imagenow);
        else {
            Picasso.with(context).load(R.drawable.images1).error(R.drawable.images1).resize(200, 200).into(imagenow);
        }
    }
}
