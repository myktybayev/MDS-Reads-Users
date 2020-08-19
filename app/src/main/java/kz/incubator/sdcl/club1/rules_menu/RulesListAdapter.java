package kz.incubator.sdcl.club1.rules_menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kz.incubator.sdcl.club1.R;

public class RulesListAdapter extends RecyclerView.Adapter<RulesListAdapter.MyTViewHolder> {

    private Context context;
    private List<Rules> dataList;
    TypedArray colorStore;
    DatabaseReference databaseReference;

    public class MyTViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.backgroundColor)
        RelativeLayout backgroundColor;
        @BindView(R.id.ruleNumber) TextView ruleNumber;
        @BindView(R.id.ruleTitle) TextView ruleTitle;
        @BindView(R.id.ruleDesc) TextView ruleDesc;

        public MyTViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

    public RulesListAdapter(Context context, List<Rules> dataList) {
        this.context = context;
        this.dataList = dataList;
        colorStore = context.getResources().obtainTypedArray(R.array.colorStore);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rules, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {

        final Rules rule = dataList.get(position);

        holder.ruleNumber.setText("Rule #"+(position+1));
        holder.ruleTitle.setText(rule.getTitle());
        holder.ruleDesc.setText(rule.getDesc());

        int color = colorStore.getResourceId(position, 0);

        holder.backgroundColor.setBackgroundColor(context.getResources().getColor(color));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

}