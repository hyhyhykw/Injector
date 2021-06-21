package com.hyhyhykw.inject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hyhyhykw.annotation.BindView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created time : 2021/6/21 11:56.
 *
 * @author 10585
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(
                parent.getContext()
        ).inflate(R.layout.activity_test, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView("tv1")
        TextView mTv;
        @BindView("btn1")
        Button mButton;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            Inject.get().inject(this,itemView);
        }
    }
}