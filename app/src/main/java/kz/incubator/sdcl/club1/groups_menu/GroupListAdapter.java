package kz.incubator.sdcl.club1.groups_menu;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.book_list_menu.OneBookAcvitiy;
import kz.incubator.sdcl.club1.book_list_menu.interfaces.ItemClickListener;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.MyTViewHolder>{

    private Context context;
    private List<Groups> groupList;
    int gradientStore[] = {R.drawable.gradient1};
    int subjectStore[] = {R.drawable.subject1};


    public class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView groupName, personCount, groupPoint;
        RelativeLayout gradientLayout;
        ImageView groupPhoto;
        ItemClickListener clickListener;
        public MyTViewHolder(View view) {
            super(view);
            groupName = view.findViewById(R.id.groupName);
            personCount = view.findViewById(R.id.personCount);
            groupPoint = view.findViewById(R.id.groupPoint);
//            groupPlace = view.findViewById(R.id.groupPlace);
            groupPhoto = view.findViewById(R.id.groupPhoto);
            gradientLayout = view.findViewById(R.id.gradientLayout);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.clickListener.onItemClick(view,getLayoutPosition());
        }

        public void setOnClick(ItemClickListener clickListener){
            this.clickListener = clickListener;
        }
    }

    public GroupListAdapter(Context context, List<Groups> groupList) {
        this.context = context;
        this.groupList = groupList;

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_groups2, parent, false);

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {

        final Groups item = groupList.get(position);

        holder.groupName.setText(item.getGroup_name());
        holder.personCount.setText(""+item.getPerson_count());
        holder.groupPoint.setText(""+item.getAve_point());
//        holder.groupPlace.setText(""+(position+1));

        holder.gradientLayout.setBackground(context.getResources().getDrawable(gradientStore[0]));
        holder.groupPhoto.setBackground(context.getResources().getDrawable(subjectStore[0]));


        holder.setOnClick(new ItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(View v, int pos) {


                Intent intent = new Intent(v.getContext(), GroupUsersActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("groupId", item.getGroup_id());
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);


            }
        });

    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

}