package com.example.martinn_todoapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.martinn_todoapp.Model.ToDoModel;
import com.example.martinn_todoapp.Utils.DatabaseHandler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AddNewTodo extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";

    private EditText newTaskInput;
    private Spinner prioritySpinner;
    private DatePicker datePicker;
    private Button newTaskSubmitButton;
    private DatabaseHandler db;

    public static AddNewTodo newInstance() {
        return new AddNewTodo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_DialogStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTaskInput = getView().findViewById(R.id.tsk_newTaskInput);
        prioritySpinner = getView().findViewById(R.id.tsk_prioritySpinner);
        newTaskSubmitButton = getView().findViewById(R.id.tsk_newTaskButton);
        datePicker = view.findViewById(R.id.tsk_datePicker);

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        // Create ArrayAdapter for priority Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pri_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        prioritySpinner.setAdapter(adapter);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            newTaskInput.setText(task);

            if (task.length() > 0) {
                newTaskSubmitButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            }
        }

        newTaskInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    newTaskSubmitButton.setEnabled(false);
                    newTaskSubmitButton.setTextColor(Color.GRAY);
                } else {
                    newTaskSubmitButton.setEnabled(true);
                    newTaskSubmitButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        boolean finalIsUpdate = isUpdate;
        newTaskSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskInput.getText().toString(); // Get user input
                String priority = prioritySpinner.getSelectedItem().toString(); // Get selected priority

                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();

                ToDoModel.Priority priorityEnum = ToDoModel.Priority.valueOf(priority); // Convert string to enum
                LocalDate deadline = LocalDate.of(year, month + 1, day); // Create LocalDate for deadline

                if (finalIsUpdate) {
                    db.updateToDo(bundle.getInt("id"), text);
                } else {
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setStatus(0);
                    task.setPriority(priorityEnum);
                    task.setDeadline(deadline);
                    db.addToDo(task);
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Activity activity = getActivity();
        if (activity instanceof DialogCloseListener) {
            ((DialogCloseListener) activity).handleDialogClose(dialog);
        }
    }
}
