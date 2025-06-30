package test.test.bkoclient_tsd_v02.ProdLorry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import test.test.bkoclient_tsd_v02.ProdLorry.Models.ProdLorryLabel;
import test.test.bkoclient_tsd_v02.R;

public class ProdLorryDataAdapter extends RecyclerView.Adapter<ProdLorryDataAdapter.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private List<ProdLorryLabel> labels;

    ProdLorryDataAdapter(Context context, List<ProdLorryLabel> labels) {
        this.labels = labels;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ProdLorryDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.label_prodlorry_item, parent, false);
        return new ProdLorryDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdLorryDataAdapter.ViewHolder holder, int position) {
        String stoveReplId = labels.get(position).getStoveReplId();
        String lorryNum = labels.get(position).getLorryNum();
        String emplId = labels.get(position).getEmplId();
        String wrkCtrId = labels.get(position).getWrkCtrId();
        String shiftId = labels.get(position).getShiftId();
        String dateScan = labels.get(position).getDateScan();
        String textScanId = String.valueOf(labels.get(position).getScanId());

        holder.txtStoveReplId.setText(stoveReplId);
        holder.txtLorryNum.setText(lorryNum);
        holder.txtEmplId.setText(emplId);
        holder.txtShiftId.setText(shiftId);
        holder.txtWrkCtrId.setText(wrkCtrId);
        holder.txtDateScan.setText(dateScan);
        holder.txtScanId.setText(textScanId);
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        final TextView txtStoveReplId, txtLorryNum, txtEmplId, txtShiftId, txtWrkCtrId, txtDateScan, txtScanId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtStoveReplId = (TextView) itemView.findViewById(R.id.txtItemStoveReplId);
            txtLorryNum = (TextView) itemView.findViewById(R.id.txtItemLorryNum);
            txtEmplId = (TextView) itemView.findViewById(R.id.txtItemEmplId);
            txtShiftId = (TextView) itemView.findViewById(R.id.txtItemShiftId);
            txtWrkCtrId = (TextView) itemView.findViewById(R.id.txtItemWrkCtrId);
            txtDateScan = (TextView) itemView.findViewById(R.id.txtDateScanProdLorry);
            txtScanId = (TextView) itemView.findViewById(R.id.txtItemScanId);
        }
    }
}
