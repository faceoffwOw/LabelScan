package test.test.bkoclient_tsd_v02.Transfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import test.test.bkoclient_tsd_v02.Inventory.DataAdapter;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabels;
import test.test.bkoclient_tsd_v02.R;
import test.test.bkoclient_tsd_v02.Transfer.Models.TransferLabel;

public class TransferDataAdapter extends RecyclerView.Adapter<TransferDataAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<TransferLabel> labels;

    TransferDataAdapter(Context context, List<TransferLabel> labels) {
        this.labels = labels;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public TransferDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.label_transfer_item, parent, false);
        return new TransferDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferDataAdapter.ViewHolder holder, int position) {
        String journalId = labels.get(position).getJournalId();
        String fromLocationId = labels.get(position).getFromInventLocationId();
        String toLocationId = labels.get(position).getToInventLocationId();
        String dateScan = labels.get(position).getDateScan();

        holder.txtJournalId.setText(labels.get(position).getJournalId());
        holder.txtFromLocationId.setText(labels.get(position).getFromInventLocationId());
        holder.txtToLocationId.setText(labels.get(position).getToInventLocationId());
        holder.txtDateScan.setText(labels.get(position).getDateScan());
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView txtJournalId, txtFromLocationId, txtToLocationId, txtDateScan;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtJournalId = (TextView) itemView.findViewById(R.id.txtJournalId);
            txtFromLocationId = (TextView) itemView.findViewById(R.id.txtFromLocationId);
            txtToLocationId = (TextView) itemView.findViewById(R.id.txtToLocationId);
            txtDateScan = (TextView) itemView.findViewById(R.id.txtDateScan);
        }
    }
}
