package com.example.martinn_todoapp.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.martinn_todoapp.AddNewTodo;
import com.example.martinn_todoapp.MainActivity;
import com.example.martinn_todoapp.Model.ToDoModel;
import com.example.martinn_todoapp.R;
import com.example.martinn_todoapp.Utils.DatabaseHandler;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> toDoList;
    private MainActivity activity;
    private DatabaseHandler db;

    public ToDoAdapter(DatabaseHandler db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        db.openDatabase();
        ToDoModel item = toDoList.get(position);

        // Set task text and status
        holder.task.setText(item.getTask());

        holder.task.setOnCheckedChangeListener(null); // Remove previous listener to avoid unwanted triggering
        holder.task.setChecked(toBoolean(item.getStatus()));

        // Set priority
        holder.priority.setText("Priority: " + item.getPriority().toString());

        // Set deadline
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        holder.deadline.setText("Deadline: " + item.getDeadline().format(formatter));

        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int newStatus = isChecked ? 0 : 1;

                if (isChecked) {
                    db.updateStatus(item.getId(), 1);
                } else {
                    db.updateStatus(item.getId(), 0);
                }

                List<ToDoModel> updatedList = db.getToDos(newStatus);

                setToDos(updatedList);

                notifyItemChanged(position);
            }
        });
    }

    public int getItemCount() {
        return toDoList.size();
    }

    private boolean toBoolean(int n) {
        return n == 1;
    }

    public Context getContext() {
        return activity;
    }

    public void setToDos(List<ToDoModel> toDoList) {
        this.toDoList = toDoList;
        notifyDataSetChanged();
    }

    public void deleteToDo(int position) {
        ToDoModel toDo = toDoList.get(position);
        db.deleteToDo(toDo.getId());
        toDoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editToDo(int position) {
        ToDoModel toDo = toDoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", toDo.getId());
        bundle.putString("task", toDo.getTask());
        AddNewTodo fragment = new AddNewTodo();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTodo.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView priority;
        TextView deadline;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.tdo_checkBox);
            priority = view.findViewById(R.id.tdo_priority); // Reference to Priority TextView
            deadline = view.findViewById(R.id.tdo_deadline); // Reference to Deadline TextView
        }
    }
}
