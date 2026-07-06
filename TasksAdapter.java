package com.example.memoaese_;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TasksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 0;

    private List<Task> originalList;
    private List<Object> displayList;
    private Context context;
    private DatabaseHelper dbHelper;

    public TasksAdapter(List<Task> taskList, Context context, DatabaseHelper dbHelper) {
        this.originalList = taskList;
        this.context = context;
        this.dbHelper = dbHelper;
        this.displayList = new ArrayList<>();
        updateDisplayList();
    }

    public void updateData(List<Task> newList) {
        this.originalList = newList;
        updateDisplayList();
        notifyDataSetChanged();
    }

    public void updateDisplayList() {
        displayList.clear();
        List<Task> activeTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        for (Task task : originalList) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            } else {
                activeTasks.add(task);
            }
        }

        // Tugas Aktif di Atas
        displayList.addAll(activeTasks);

        // Header dan Tugas Selesai di Bawah
        if (!completedTasks.isEmpty()) {
            displayList.add("TUGAS YANG SUDAH DISELESAIKAN");
            displayList.addAll(completedTasks);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_item_layout, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) displayList.get(position));
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind((Task) displayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        HeaderViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
            textView.setTextSize(13);
            textView.setPadding(40, 50, 0, 20);
            textView.setTextColor(0xFF757575);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setAllCaps(true);
        }
        void bind(String title) {
            textView.setText(title);
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle, tvDate; // tvDeadline dihapus

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            tvTitle = itemView.findViewById(R.id.text_task_title);
            tvDate = itemView.findViewById(R.id.text_task_date);
            // tvDeadline findViewById dihapus
        }

        void bind(final Task task) {
            tvTitle.setText(task.getTitle());
            tvDate.setText("Dibuat: " + task.getDate());

            // Mencegah bug checkbox saat scroll
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());
            updateStroke(task.isCompleted());

            // Logika Klik (Gunakan satu listener agar bisa diklik di kotak maupun di baris)
            View.OnClickListener toggleStatus = v -> {
                boolean isChecked = !task.isCompleted();
                task.setCompleted(isChecked);

                // 1. Simpan ke database
                dbHelper.updateTaskStatus(task.getId(), isChecked);

                // 2. Panggil fungsi reload dari Activity agar urutan diperbarui otomatis (Selesai ke bawah)
                if (context instanceof TaskListActivity) {
                    ((TaskListActivity) context).loadTasksFromDatabase();
                } else {
                    updateDisplayList();
                    notifyDataSetChanged();
                }
            };

            // Bisa klik di kotak centang
            checkBox.setOnClickListener(toggleStatus);
            // Bisa klik di seluruh baris tugas (lebih mudah untuk user)
            itemView.setOnClickListener(toggleStatus);

            itemView.setOnLongClickListener(v -> {
                showDeleteDialog(v.getContext(), task);
                return true;
            });
        }

        private void showDeleteDialog(Context context, Task task) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Hapus Tugas");
            builder.setMessage("Hapus tugas '" + task.getTitle() + "'?");
            builder.setPositiveButton("HAPUS", (dialog, which) -> {
                dbHelper.deleteTask(task.getId());
                originalList.remove(task);
                updateDisplayList();
                notifyDataSetChanged();
                Toast.makeText(context, "Tugas dihapus", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("BATAL", null);

            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_dialog);
            }
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.RED);
        }

        private void updateStroke(boolean isCompleted) {
            if (isCompleted) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setTextColor(Color.GRAY);
                tvTitle.setAlpha(0.5f);
                tvDate.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setTextColor(Color.BLACK);
                tvTitle.setAlpha(1.0f);
                tvDate.setAlpha(1.0f);
            }
        }
    }
}