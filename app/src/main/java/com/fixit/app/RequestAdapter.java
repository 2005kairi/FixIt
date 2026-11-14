package com.fixit.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter
        extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private final Context context;
    private final List<Request> requests;

    public RequestAdapter(Context context, List<Request> requests) {
        this.context = context;
        this.requests = requests;
    }

    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RequestViewHolder holder, int position) {
        Request request = requests.get(position);

        holder.tvTitle.setText(request.title);
        holder.tvDesc.setText(request.description);

        String formattedDate = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault())
                .format(new Date(request.createdAt));
        holder.tvDate.setText(formattedDate);

        holder.tvStatus.setText(request.status);

        if (Request.STATUS_DONE.equals(request.status)) {
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.status_done));
        } else {
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(context, R.color.status_pending));
        }

        // Tenant Home does not display the unit number.
        holder.tvUnit.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<Request> newRequests) {
        requests.clear();
        requests.addAll(newRequests);
        notifyDataSetChanged();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvDate, tvStatus, tvUnit;

        RequestViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUnit = itemView.findViewById(R.id.tvUnit);
        }
    }
}
