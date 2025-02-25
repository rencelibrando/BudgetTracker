package com.expensetracker.budgettracker.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Flashcard;
import java.util.ArrayList;
import java.util.List;

public class FlashcardsAdapter extends RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder> {

    private final List<Flashcard> flashcards;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Flashcard flashcard, int position);
    }

    public FlashcardsAdapter(Context context, List<Flashcard> flashcards, OnItemClickListener listener) {
        this.flashcards = new ArrayList<>(flashcards != null ? flashcards : new ArrayList<>());
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);

        try {
            holder.bind(flashcard);
        } catch (Exception e) {
            Log.e("FlashcardsAdapter", "Error binding flashcard: " + e.getMessage());
            holder.bindFallback();
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                listener.onItemClick(flashcard, adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public void updateFlashcards(List<Flashcard> newFlashcards) {
        List<Flashcard> safeList = new ArrayList<>(newFlashcards != null ? newFlashcards : new ArrayList<>());
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new FlashcardsDiffCallback(this.flashcards, safeList));
        this.flashcards.clear();
        this.flashcards.addAll(safeList);
        diffResult.dispatchUpdatesTo(this);
    }

    public void updateFlashcard(int position, String newAmount) {
        if (position >= 0 && position < flashcards.size()) {
            flashcards.get(position).setAmount(newAmount);
            notifyItemChanged(position);
        }
    }

    protected static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView label;
        private final TextView amount;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.card_icon);
            label = itemView.findViewById(R.id.card_label);
            amount = itemView.findViewById(R.id.card_amount);
        }

        public void bind(Flashcard flashcard) {
            icon.setImageResource(flashcard.getIconResId());
            label.setText(flashcard.getLabel());
            amount.setText(flashcard.getAmount());
        }

        public void bindFallback() {
            icon.setImageResource(R.drawable.ic_category_default); // Fallback icon
            label.setText("Error");
            amount.setText("₱0.00");
        }
    }

    static class FlashcardsDiffCallback extends DiffUtil.Callback {
        private final List<Flashcard> oldList;
        private final List<Flashcard> newList;

        public FlashcardsDiffCallback(List<Flashcard> oldList, List<Flashcard> newList) {
            this.oldList = new ArrayList<>(oldList);
            this.newList = new ArrayList<>(newList);
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).getLabel().equals(newList.get(newPos).getLabel());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            Flashcard oldItem = oldList.get(oldPos);
            Flashcard newItem = newList.get(newPos);
            return oldItem.getLabel().equals(newItem.getLabel()) &&
                    oldItem.getAmount().equals(newItem.getAmount()) &&
                    oldItem.getIconResId() == newItem.getIconResId();
        }
    }
}