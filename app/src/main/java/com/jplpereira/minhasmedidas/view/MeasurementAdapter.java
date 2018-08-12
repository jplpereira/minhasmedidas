package com.jplpereira.minhasmedidas.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jplpereira.minhasmedidas.R;
import com.jplpereira.minhasmedidas.database.model.Measurement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.MyViewHolder> {

    private Context context;
    private List<Measurement> measurementList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView date;
        public TextView time;
        public TextView glucose;
        public TextView pressure;

        public MyViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.date_field);
            time = view.findViewById(R.id.time_field);
            glucose = view.findViewById(R.id.glucose_field);
            pressure = view.findViewById(R.id.pressure_field);
        }
    }

    public MeasurementAdapter(Context context, List<Measurement> measurementList) {
        this.context = context;
        this.measurementList = measurementList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.measument_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Measurement measurement = measurementList.get(position);

        holder.glucose.setText(String.valueOf(measurement.getGlucose()));
        holder.pressure.setText(String.format("%d/%d", measurement.getSystolic(),
                measurement.getDiastolic()));

        // Formatting and displaying timestamp
        holder.date.setText(formatDate(measurement.getTimestamp()));
        holder.time.setText(formatTime(measurement.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return measurementList.size();
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd/MM/yyyy");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }
        return "";
    }

    private String formatTime(String dateStr){
        try{
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fmt.setTimeZone(TimeZone.getTimeZone("-03"));
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("HH:mm");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }
        return "";
    }
}
