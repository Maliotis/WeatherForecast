package com.maliotis.petros.stormy;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.maliotis.petros.stormy.weather.Day;
import com.maliotis.petros.stormy.weather.Hour;

public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private Hour[] mHours;
    private Context mContext;
    private Day[] mDays;

    public HourAdapter(Context context,Hour[] hours,Day[] days){
        mContext = context;
        mHours = hours;
        mDays = days;
    }

    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_list_item,parent,false);
        HourViewHolder hourViewHolder = new HourViewHolder(view);
        return hourViewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder holder, int position) {
        holder.bindHour(mHours[position]);

    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    public class HourViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTimeLabel;
        public TextView mSummaryLabel;
        public TextView mTemperatureLabel;
        public ImageView mIconImageView;
        public TextView mHourDay;
        public TextView mDegree;


        public HourViewHolder(View itemView) {
            super(itemView);

            mTimeLabel = (TextView) itemView.findViewById(R.id.timeLabel);
            mSummaryLabel  = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTemperatureLabel = (TextView) itemView.findViewById(R.id.temperatureLabel);
            mIconImageView = (ImageView) itemView.findViewById(R.id.iconImageView);
            mHourDay = (TextView) itemView.findViewById(R.id.hourDayLabel);
            mDegree = (TextView) itemView.findViewById(R.id.degreeLabel);


            itemView.setOnClickListener(this);
        }

        public void bindHour(Hour hour){

            if(hour.getHour().equals("0:00")){
                mHourDay.setText("");
                mTimeLabel.setText("");
                mSummaryLabel.setText("");
                mTemperatureLabel.setText("");
                mIconImageView.setImageResource(android.R.color.transparent);
                mHourDay.setText("");
                mDegree.setText("");
                //setting the day of the week.
                mHourDay.setText(hour.getDayOfTheWeekH());
            }
            else {
                mHourDay.setText("");
                mDegree.setText("o");
                mTimeLabel.setText(hour.getHour());
                mSummaryLabel.setText(hour.getSummary());
                mTemperatureLabel.setText(hour.getTemperature() + "");
                mIconImageView.setImageResource(hour.getIconId());
            }
        }

        @Override
        public void onClick(View v) {
            String time = mTimeLabel.getText().toString();
            String temperature = mSummaryLabel.getText().toString();
            String summary = mSummaryLabel.getText().toString();
            String message = String.format("At %s it will be %s and %s",time,temperature,summary);
            Toast.makeText(mContext,message,Toast.LENGTH_LONG).show();
        }
    }
}
