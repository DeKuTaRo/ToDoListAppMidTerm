package com.example.todolistapp.CustomAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.Models.NoteItem;
import com.example.todolistapp.R;
import com.example.todolistapp.databinding.ActivityGridViewItemNoteItemBinding;
import com.example.todolistapp.databinding.ActivityListViewItemNoteItemBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewNoteCustomAdapter extends RecyclerView.Adapter<RecyclerViewNoteCustomAdapter.ViewHolder> {
    public static final int TYPE_LIST_VIEW = 0;
    public static final int TYPE_GRID_VIEW = 1;

    private int type;

    private List<NoteItem> dataSource;

    private final IItemClick itemClick;

    public interface IItemClick {

        void onClick(NoteItem noteItem);

        void onLongClick(NoteItem noteItem, CardView cardView);
    }

    public RecyclerViewNoteCustomAdapter(Context context, List<NoteItem> dataSource, IItemClick itemClick) {
        this.dataSource = dataSource;
        this.itemClick = itemClick;
        this.type = RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (this.type == RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW ){
            ActivityListViewItemNoteItemBinding viewRoot = ActivityListViewItemNoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(viewRoot);
        }

        if ( this.type == RecyclerViewNoteCustomAdapter.TYPE_GRID_VIEW ){
            ActivityGridViewItemNoteItemBinding viewRoot = ActivityGridViewItemNoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(viewRoot);
        }

        // Base case
        ActivityListViewItemNoteItemBinding viewRoot = ActivityListViewItemNoteItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(viewRoot);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindData(this.dataSource.get(position), position);
    }

    @Override
    public int getItemCount() {
        return this.dataSource.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private ActivityListViewItemNoteItemBinding binding_List_View;
        private ActivityGridViewItemNoteItemBinding binding_Grid_View;

        public ViewHolder(@NonNull ActivityListViewItemNoteItemBinding itemView) {
            super(itemView.getRoot());
            this.binding_List_View = itemView;
        }

        public ViewHolder(@NonNull ActivityGridViewItemNoteItemBinding itemView) {
            super(itemView.getRoot());
            this.binding_Grid_View = itemView;
        }

        private void bindData(NoteItem noteItem, int position) {
            if ( type == RecyclerViewNoteCustomAdapter.TYPE_LIST_VIEW){
                if (noteItem.isPinned()) {
                    this.binding_List_View.imageViewPin.setImageResource(R.drawable.ic_pin);
                } else {
                    this.binding_List_View.imageViewPin.setImageResource(0);
                }

                if (noteItem.getSubtitle().trim().isEmpty()) {
                    this.binding_List_View.subtitle.setVisibility(View.GONE);
                } else {
                    this.binding_List_View.subtitle.setText(noteItem.getSubtitle());
                }

                if (noteItem.getColor() != null) {
                    this.binding_List_View.mainCardView.setCardBackgroundColor(Color.parseColor(noteItem.getColor()));
                } else {
                    this.binding_List_View.mainCardView.setCardBackgroundColor(Color.parseColor("#333333"));
                }

                if (noteItem.getPasswordNote().isEmpty()) {
                    this.binding_List_View.imageViewPassword.setImageResource(0);
                } else {
                    this.binding_List_View.imageViewPassword.setImageResource(R.drawable.ic_lock);
                }

                this.binding_List_View.label.setText(noteItem.getLabel());
                this.binding_List_View.textContent.setText(noteItem.getText_content());
                this.binding_List_View.timeCreate.setText(noteItem.getDate());


                this.binding_List_View.mainCardView.setOnClickListener(view -> itemClick.onClick(noteItem));

                this.binding_List_View.mainCardView.setOnLongClickListener(view -> {
                    itemClick.onLongClick(noteItem, binding_List_View.mainCardView);
                    return true;
                });
            }

            if ( type == RecyclerViewNoteCustomAdapter.TYPE_GRID_VIEW ){
                if (noteItem.isPinned()) {
                    this.binding_Grid_View.imageViewPin.setImageResource(R.drawable.ic_pin);
                } else {
                    this.binding_Grid_View.imageViewPin.setImageResource(0);
                }

                if (noteItem.getSubtitle().trim().isEmpty()) {
                    this.binding_Grid_View.subtitle.setVisibility(View.GONE);
                } else {
                    this.binding_Grid_View.subtitle.setText(noteItem.getSubtitle());
                }

                if (noteItem.getColor() != null) {
                    this.binding_Grid_View.mainCardView.setCardBackgroundColor(Color.parseColor(noteItem.getColor()));
                } else {
                    this.binding_Grid_View.mainCardView.setCardBackgroundColor(Color.parseColor("#333333"));
                }

                if (noteItem.getPasswordNote().trim().isEmpty()) {
                    this.binding_Grid_View.imageViewPassword.setImageResource(0);
                } else {
                    this.binding_Grid_View.imageViewPassword.setImageResource(R.drawable.ic_lock);
                }

                if (noteItem.getImagePath() == null || noteItem.getImagePath().trim().isEmpty()) {
                    this.binding_Grid_View.imageNote.setVisibility(View.GONE);
                    Picasso.get().load((Uri) null)
                            .into(binding_Grid_View.imageNote);
                } else {
                    this.binding_Grid_View.imageNote.setVisibility(View.VISIBLE);
                    Picasso.get().load(noteItem.getImagePath())
                            .into(binding_Grid_View.imageNote);
                }

                this.binding_Grid_View.label.setText(noteItem.getLabel());
                this.binding_Grid_View.timeCreate.setText(noteItem.getDate());
                this.binding_Grid_View.textContent.setText(noteItem.getText_content());

                this.binding_Grid_View.mainCardView.setOnClickListener(view -> itemClick.onClick(noteItem));

                this.binding_Grid_View.mainCardView.setOnLongClickListener(view -> {
                    itemClick.onLongClick(noteItem, binding_Grid_View.mainCardView);
                    return true;
                });
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(List<NoteItem> noteItemArrayList) {
        dataSource = noteItemArrayList;
        notifyDataSetChanged();
    }

    //Use this method to set type when clicking switch layout
    public void setType(int type) {
        this.type = type;
    }

    //Use this method to set type when clicking switch layout
    public int getType() {
        return this.type;
    }

    @Override
    //Which type of layout is used to get the current Item
    public int getItemViewType(int position) {
        return type;
    }
}
