package com.example.martinn_todoapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.martinn_todoapp.Adapter.ToDoAdapter;
import com.example.martinn_todoapp.Model.ToDoModel;
import com.example.martinn_todoapp.Utils.DatabaseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {

    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton addButton;
    private Spinner sortSpinner;
    private Button filterButton;
    private int currentStatusFilter = 0;
    private List<ToDoModel> taskList;
    private DatabaseHandler db;
    public enum SortOption {
        NONE,
        PRIORITY,
        DEADLINE
    }
    private SortOption currentSortOption = SortOption.NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Database

        db = new DatabaseHandler(this);
//        db.deleteDatabase(this);  // Resetting Database
        db.openDatabase();  // Open the database

        // Initialize TaskList

        taskList = new ArrayList<>();

        // Initialize sortSpinner
        sortSpinner = findViewById(R.id.tsk_sortSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.srt_options_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // None
                        currentSortOption = SortOption.NONE;
                        break;
                    case 1: // Priority
                        currentSortOption = SortOption.PRIORITY;
                        break;
                    case 2: // Deadline
                        currentSortOption = SortOption.DEADLINE;
                        break;
                }
                updateTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Initialize filterButton
        filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentStatusFilter == 0) {
                    currentStatusFilter = 1;
                    updateTasks();
                } else {
                    currentStatusFilter = 0;
                    updateTasks();
                }
            }
        });

        // Get RecyclerView

        tasksRecyclerView = findViewById(R.id.tsk_recyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        addButton = findViewById(R.id.tsk_addButton);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        updateTasks();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTodo.newInstance().show(getSupportFragmentManager(), AddNewTodo.TAG);
            }
        });
    }

    private void updateTasks() {
        switch (currentSortOption) {
            case PRIORITY:
                taskList = db.getToDosSortedByPriority(currentStatusFilter);
                break;
            case DEADLINE:
                taskList = db.getToDosSortedByDeadline(currentStatusFilter);
                break;
            case NONE:
            default:
                taskList = db.getToDos(currentStatusFilter);
                break;
        }
        Collections.reverse(taskList); // Reverse the list if needed
        tasksAdapter.setToDos(taskList);
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getToDos(currentStatusFilter);
        Collections.reverse(taskList);
        tasksAdapter.setToDos(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}