package test.test.bkoclient_tsd_v02.Inventory;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabels;
import test.test.bkoclient_tsd_v02.R;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder>{
    private Context context;
    private LayoutInflater inflater;
    private List<InventLabels> labels;

    int row_index = -1;

    DataAdapter(Context context, List<InventLabels> labels) {
        this.labels = labels;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.label_item, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        final TextView txtNumBatch, txtName, txtFrakcia, txtNumLabel,
                txtQtyTrayIn, txtQtyGood, txtCount;

        ViewHolder(final View view){
            super(view);

            txtNumBatch = (TextView) view.findViewById(R.id.txtNumBatch);
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtFrakcia = (TextView) view.findViewById(R.id.txtFrakcia);
            txtNumLabel = (TextView) view.findViewById(R.id.txtNumLabel);
            txtQtyTrayIn = (TextView) view.findViewById(R.id.txtQtyTrayIn);
            txtQtyGood = (TextView) view.findViewById(R.id.txtQtyGood);
            txtCount = (TextView) view.findViewById(R.id.txtCount);

            //нажатием пальцем
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    row_index = getAdapterPosition();

                    if (row_index != RecyclerView.NO_POSITION) {
                        InventLabels curLabel = labels.get(row_index);

                        Intent intent = new Intent(context, Activity_edit_item.class);
                        intent.putExtra("position", getAdapterPosition());
                        intent.putExtra("NumBatch", curLabel.getNumBatch());
                        intent.putExtra("ItemIdName", curLabel.getItemIdName());
                        intent.putExtra("Frakcia", curLabel.getFrakcia());
                        intent.putExtra("QtyGood", curLabel.getQtyGood());
                        intent.putExtra("QtyTrayIn", curLabel.getQtyTrayIn());

                        context.startActivity(intent);
                        notifyDataSetChanged();
                    }
                }
            });

            /*//держать палец
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    labels.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    return true;
                }
            });*/
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DataAdapter.ViewHolder holder, final int position) {

        String BatchNum = labels.get(position).getNumBatch();
        String Name = labels.get(position).getItemIdName();
        String Frakcia = labels.get(position).getFrakcia();
        String NumLabel = labels.get(position).getNumId();
        float QtyGood = labels.get(position).getQtyGood();
        int QtyTrayIn = labels.get(position).getQtyTrayIn();
        int Count = labels.get(position).getCount();

        holder.txtNumBatch.setText("Партия: " + BatchNum);
        holder.txtName.setText("Марка: " + Name);
        holder.txtFrakcia.setText("Фракция: " + Frakcia);
        holder.txtNumLabel.setText(NumLabel);
        holder.txtQtyGood.setText("Кол-во, т: " + String.valueOf(QtyGood));
        holder.txtQtyTrayIn.setText(QtyTrayIn + " шт.");
        holder.txtCount.setText(Count + " под.");

        if(row_index == position) {
            holder.itemView.setBackgroundResource(R.color.colorLightGreen);
        }
        else {
            holder.itemView.setBackgroundResource(R.color.colorWhite);
        }
    }

    @Override
    public int getItemCount() {
        return labels.size();
    }
}
