package com.example.memoaese_;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TasksAdapter adapter;
    private List<Task> taskList;          // List untuk ditampilkan
    private List<Task> allTasksList;     // List sumber data asli dari DB
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAddTask;
    private EditText searchTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        dbHelper = new DatabaseHelper(this);
        taskList = new ArrayList<>();
        allTasksList = new ArrayList<>();

        recyclerView = findViewById(R.id.recycler_view_tasks);
        searchTask = findViewById(R.id.search_task);
        fabAddTask = findViewById(R.id.fab_add_task_local);
        ImageButton btnBack = findViewById(R.id.button_back_task);

        // Setup RecyclerView
        adapter = new TasksAdapter(taskList, this, dbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Handle Status Bar/Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupSearchLogic();

        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> showAddTaskDialog());
        }
    }

    private void setupSearchLogic() {
        searchTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTasks(String query) {
        List<Task> filtered = new ArrayList<>();
        for (Task task : allTasksList) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(task);
            }
        }
        updateDisplay(filtered);
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Pastikan di R.layout.dialog_add_task Anda sudah menghapus TextView tv_set_deadline
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText inputTitle = dialogView.findViewById(R.id.edit_task_title);

        builder.setView(dialogView)
                .setPositiveButton("SIMPAN", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        String dateCreated = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());

                        // Simpan tugas baru ke database (Tanpa Parameter Deadline)
                        dbHelper.addTask(new Task(title, dateCreated, false));

                        loadTasksFromDatabase();
                        Toast.makeText(this, "Tugas ditambahkan", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Judul tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("BATAL", null);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_dialog);
        }
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#4CAF50"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#757575"));
    }

    /**
     * PUBLIC: Dipanggil dari TasksAdapter saat checkbox/item diklik
     */
    public void loadTasksFromDatabase() {
        // Ambil data terbaru dari DB (Urutan: Aktif di atas, Selesai di bawah)
        allTasksList = dbHelper.getAllTasks();

        String currentQuery = searchTask.getText().toString();
        if (currentQuery.isEmpty()) {
            updateDisplay(allTasksList);
        } else {
            filterTasks(currentQuery);
        }
    }

    private void updateDisplay(List<Task> listToDisplay) {
        if (adapter != null) {
            // Berikan data ke adapter untuk proses pengelompokan ulang
            adapter.updateData(new ArrayList<>(listToDisplay));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromDatabase();
    }
}