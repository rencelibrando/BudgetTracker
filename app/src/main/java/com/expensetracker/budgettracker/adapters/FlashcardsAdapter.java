package com.expensetracker.budgettracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.budgettracker.R;
import com.expensetracker.budgettracker.models.Flashcard;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FlashcardsAdapter extends RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Flashcard flashcard, int position);
    }

    // Modified constructor to copy the input list
    public FlashcardsAdapter(Context context, List<Flashcard> flashcards, OnItemClickListener listener) {
        this.flashcards = new ArrayList<>(flashcards); // Create a copy
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
            holder.icon.setImageResource(flashcard.getIconResId());
            holder.label.setText(flashcard.getLabel());
            holder.amount.setText(flashcard.getAmount());
        } catch (Exception e) {
            // Handle potential null/data issues
            e.printStackTrace();
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

    // Improved update method with null safety
    public void updateFlashcards(List<Flashcard> newFlashcards) {
        List<Flashcard> safeList = new ArrayList<>(newFlashcards != null ? newFlashcards : new ArrayList<>());

        FlashcardsDiffCallback diffCallback = new FlashcardsDiffCallback(this.flashcards, safeList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.flashcards.clear();
        this.flashcards.addAll(safeList);
        diffResult.dispatchUpdatesTo(this);
    }

    // Added null check for remove operation
    public void removeFlashcard(int position) {
        if (position >= 0 && position < flashcards.size()) {
            flashcards.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder remains the same
    protected static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;
        final TextView amount;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.card_icon);
            label = itemView.findViewById(R.id.card_label);
            amount = itemView.findViewById(R.id.card_amount);
        }
    }

    // Added null check in swipe handler
    public static void attachSwipeGesture(RecyclerView recyclerView, FlashcardsAdapter adapter) {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Flashcard removedFlashcard = adapter.flashcards.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    adapter.removeFlashcard(position);
                    Snackbar.make(recyclerView, "Flashcard deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> {
                                if (position <= adapter.flashcards.size()) {
                                    adapter.flashcards.add(position, removedFlashcard);
                                    adapter.notifyItemInserted(position);
                                }
                            }).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    Snackbar.make(recyclerView, "Editing feature coming soon!", Snackbar.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    // Enhanced DiffUtil callback
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

    // Added range check for update
    public void updateFlashcard(int position, String newAmount) {
        if (position >= 0 && position < flashcards.size()) {
            flashcards.get(position).setAmount(newAmount);
            notifyItemChanged(position);
        }
    }
}