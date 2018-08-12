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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private MeasurementAdapter mAdapter;
    private List<Measurement> measurementList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noMeasurementsView;

    private DatabaseHelper db;

    private Boolean isFabOpen = false;
    private FloatingActionButton newFab, presentFab, pastFab;
    private Animation open, close, rotate_forward, rotate_backward;
    private TextView presentTextView, pastTextView;

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

        newFab = findViewById(R.id.newFab);
        presentFab = findViewById(R.id.presentFab);
        pastFab = findViewById(R.id.pastFab);
        presentTextView = findViewById(R.id.presentTextView);
        pastTextView = findViewById(R.id.pastTextView);

        open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.open);
        close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.rotate_backward);

        newFab.setOnClickListener(this);
        presentFab.setOnClickListener(this);
        pastFab.setOnClickListener(this);


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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && newFab.isShown()){
                    newFab.hide();
                } else {
                    // nothing to do
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        recyclerView.canScrollVertically(1)) {
                    newFab.show();
                } else {
                    // nothing to do
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.newFab:
                animateFAB();
                break;
            case R.id.presentFab:
                showPresentMeasurementDialog();
                break;
            case R.id.pastFab:
                showPastMeasurementDialog(false, null, -1);
                break;
        }
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private void createPresentMeasurement(int glucose, int systolic, int diastolic) {
        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertPresentMeasurement(glucose, systolic, diastolic);

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

    private void createPastMeasurement(String timestamp, int glucose, int systolic, int diastolic) {
        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertPastMeasurement(timestamp, glucose, systolic, diastolic);

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
    private void updateMeasurement(String timestamp, int glucose, int systolic, int diastolic,
                                   int position) {
        Measurement measurement = measurementList.get(position);
        // updating note text
        measurement.setGlucose(glucose);
        measurement.setSystolic(systolic);
        measurement.setDiastolic(diastolic);
        measurement.setTimestamp(timestamp);

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
                    showPastMeasurementDialog(true,
                            measurementList.get(position), position);
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
    private void showPresentMeasurementDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(
                MainActivity.this);

        View view = layoutInflaterAndroid.inflate(R.layout.present_measurement_dialog, null);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputGlucose = view.findViewById(R.id.glucose);
        final EditText inputSystolic = view.findViewById(R.id.systolic);
        final EditText inputDiastolic = view.findViewById(R.id.diastolic);

        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(getString(R.string.lbl_new_present_measurements_title));

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("salvar", new DialogInterface.OnClickListener() {
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

                createPresentMeasurement(Integer.parseInt(inputGlucose.getText().toString()),
                        Integer.parseInt(inputSystolic.getText().toString()),
                        Integer.parseInt(inputDiastolic.getText().toString()));
            }
        });
    }

    private void showPastMeasurementDialog(final boolean shouldUpdate, final Measurement measurement,
                                       final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(
                MainActivity.this);

        View view = layoutInflaterAndroid.inflate(R.layout.past_measurement_dialog, null);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputGlucose = view.findViewById(R.id.glucose);
        final EditText inputSystolic = view.findViewById(R.id.systolic);
        final EditText inputDiastolic = view.findViewById(R.id.diastolic);
        final EditText inputDate = view.findViewById(R.id.date);
        final EditText inputTime = view.findViewById(R.id.time);

        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_present_measurements_title) :
                getString(R.string.lbl_edit_measurements_title));

        if (shouldUpdate && measurement != null) {
            inputGlucose.setText(String.valueOf(measurement.getGlucose()));
            inputSystolic.setText(String.valueOf(measurement.getSystolic()));
            inputDiastolic.setText(String.valueOf(measurement.getDiastolic()));
            inputDate.setText(mAdapter.showDate(measurement.getTimestamp()));
            inputTime.setText(mAdapter.showTime(measurement.getTimestamp()));
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
                                || TextUtils.isEmpty(inputDiastolic.getText().toString())
                                || TextUtils.isEmpty(inputDate.getText().toString())
                                || TextUtils.isEmpty(inputTime.getText().toString()) ) {
                            Toast.makeText(MainActivity.this, "Preencha todos os campos!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!mAdapter.checkDatePattern(inputDate.getText().toString())) {
                            Toast.makeText(MainActivity.this,
                                    "Formato de data incorreto!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (!mAdapter.checkTimePattern(inputTime.getText().toString())) {
                            Toast.makeText(MainActivity.this,
                                    "Formato de hora incorreto!", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            alertDialog.dismiss();
                        }

                        // check if user updating note
                        if (shouldUpdate && measurement != null) {
                            // update note by it's id
                            updateMeasurement(mAdapter.joinDateTime(inputDate.getText()
                                            .toString(), inputTime.getText().toString()),
                                    Integer.parseInt(inputGlucose.getText().toString()),
                                    Integer.parseInt(inputSystolic.getText().toString()),
                                    Integer.parseInt(inputDiastolic.getText().toString()),
                                    position);
                        } else {
                            // create new note
                            createPastMeasurement(mAdapter.joinDateTime(inputDate.getText()
                                            .toString(), inputTime.getText().toString()),
                                    Integer.parseInt(inputGlucose.getText().toString()),
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

    public void animateFAB(){

        if(isFabOpen){
            newFab.startAnimation(rotate_backward);
            presentFab.startAnimation(close);
            presentTextView.startAnimation(close);
            pastFab.startAnimation(close);
            pastTextView.startAnimation(close);
            presentFab.setClickable(false);
            pastFab.setClickable(false);
            isFabOpen = false;
        } else {
            newFab.startAnimation(rotate_forward);
            presentFab.startAnimation(open);
            presentTextView.startAnimation(open);
            pastFab.startAnimation(open);
            pastTextView.startAnimation(open);
            presentFab.setClickable(true);
            pastFab.setClickable(true);
            isFabOpen = true;
        }
    }
}