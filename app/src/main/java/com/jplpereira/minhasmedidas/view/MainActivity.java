package com.jplpereira.minhasmedidas.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jplpereira.minhasmedidas.R;
import com.jplpereira.minhasmedidas.database.DatabaseHelper;
import com.jplpereira.minhasmedidas.database.model.Measurement;
import com.jplpereira.minhasmedidas.utils.MyDividerItemDecoration;
import com.jplpereira.minhasmedidas.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private MeasurementAdapter mAdapter;
    private List<Measurement> measurementList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noMeasurementsView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noMeasurementsView = findViewById(R.id.empty_measurements_view);

        db = new DatabaseHelper(this);

        measurementList.addAll(db.getAllMeasurements());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMeasurementDialog(false, null, -1);
            }
        });

        mAdapter = new MeasurementAdapter(this, measurementList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(
                getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this,
                LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyMeasurements();

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private void createMeasurement(int glucose, int systolic, int diastolic) {
        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertMeasurement(glucose, systolic, diastolic);

        // get the newly inserted note from db
        Measurement m = db.getMeasurement(id);

        if (m != null) {
            // adding new note to array list at 0 position
            measurementList.add(0, m);

            // refreshing the list
            mAdapter.notifyDataSetChanged();

            toggleEmptyMeasurements();
        }
    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateMeasurement(int glucose, int systolic, int diastolic, int position) {
        Measurement measurement = measurementList.get(position);
        // updating note text
        measurement.setGlucose(glucose);
        measurement.setSystolic(systolic);
        measurement.setDiastolic(diastolic);

        // updating note in db
        db.updateMeasurement(measurement);

        // refreshing the list
        measurementList.set(position, measurement);
        mAdapter.notifyItemChanged(position);

        toggleEmptyMeasurements();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteMeasurement(int position) {
        // deleting the note from db
        db.deleteMeasurement(measurementList.get(position));

        // removing the note from the list
        measurementList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyMeasurements();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Editar", "Apagar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha uma opção");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showMeasurementDialog(true, measurementList.get(position), position);
                } else {
                    deleteMeasurement(position);
                }
            }
        });
        builder.show();
    }

    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showMeasurementDialog(final boolean shouldUpdate, final Measurement measurement,
                                       final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.measurement_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(
                MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputGlucose = view.findViewById(R.id.glucose);
        final EditText inputSystolic = view.findViewById(R.id.systolic);
        final EditText inputDiastolic = view.findViewById(R.id.diastolic);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_measurements_title) :
                getString(R.string.lbl_edit_measurements_title));

        if (shouldUpdate && measurement != null) {
            inputGlucose.setText(String.valueOf(measurement.getGlucose()));
            inputSystolic.setText(String.valueOf(measurement.getSystolic()));
            inputDiastolic.setText(String.valueOf(measurement.getDiastolic()));
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "atualizar" : "salvar",
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputGlucose.getText().toString())
                        || TextUtils.isEmpty(inputSystolic.getText().toString())
                        || TextUtils.isEmpty(inputDiastolic.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos!",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && measurement != null) {
                    // update note by it's id
                    updateMeasurement(Integer.parseInt(inputGlucose.getText().toString()),
                            Integer.parseInt(inputSystolic.getText().toString()),
                            Integer.parseInt(inputDiastolic.getText().toString()),
                            position);
                } else {
                    // create new note
                    createMeasurement(Integer.parseInt(inputGlucose.getText().toString()),
                            Integer.parseInt(inputSystolic.getText().toString()),
                            Integer.parseInt(inputDiastolic.getText().toString()));
                }
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyMeasurements() {
        // you can check notesList.size() > 0

        if (db.getMeasurementsCount() > 0) {
            noMeasurementsView.setVisibility(View.GONE);
        } else {
            noMeasurementsView.setVisibility(View.VISIBLE);
        }
    }
}