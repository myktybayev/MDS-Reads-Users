package kz.incubator.sdcl.club1.rating_by_users;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.groups_menu.module.User;

public class UserRatingdapter extends RecyclerView.Adapter<UserRatingdapter.MyTViewHolder> {
    private Context context;
    public ArrayList<User> userList;
    String currectUserPhoneNumber;

    public static class MyTViewHolder extends RecyclerView.ViewHolder {
        public TextView userPoint, info, bookCount, userReview, user_group, userRating;
        CircleImageView person_photo;
        ImageView userTypeIcon;
        LinearLayout parentLinear;

        public MyTViewHolder(View view) {
            super(view);
            parentLinear = view.findViewById(R.id.parentLinear);
            person_photo = view.findViewById(R.id.person_photo);
            userTypeIcon = view.findViewById(R.id.userTypeIcon);
            userPoint = view.findViewById(R.id.userPoint);
            info = view.findViewById(R.id.info);
            bookCount = view.findViewById(R.id.bookCount);
            userReview = view.findViewById(R.id.userReview);
            user_group = view.findViewById(R.id.user_group);
            userRating = view.findViewById(R.id.userRating);
        }

    }

    public UserRatingdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
        Log.d("M_UserRatingdapter", "get current Firebase user");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser.getPhoneNumber() != null && currentUser.getPhoneNumber().length() > 0) { // phone login
            currectUserPhoneNumber = currentUser.getPhoneNumber();
        } else {
            currectUserPhoneNumber = currentUser.getDisplayName();
        }

    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating_user, parent, false);

        return new MyTViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position) {
        User item = userList.get(position);

        if(currectUserPhoneNumber.equals(item.getPhoneNumber())){
            holder.parentLinear.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        }else{
            holder.parentLinear.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        holder.userRating.setText("" + item.getTypeRating());
        holder.userPoint.setText("" + item.getPoint());

        holder.info.setText(item.getInfo());
        holder.bookCount.setText("" + item.getBookCount());
        holder.userReview.setText("" + item.getReview_sum());
        holder.user_group.setText(item.getGroupName());

        Glide.with(context.getApplicationContext())
                .load(item.getPhoto())
                .placeholder(R.drawable.user_def)
                .dontAnimate()
                .into(holder.person_photo);

        int uTypeIcon = R.color.transparent;

        switch (item.getUserType()) {
            case "gold":
                uTypeIcon = R.drawable.ic_gold;
                break;
            case "silver":
                uTypeIcon = R.drawable.ic_silver;
                break;
            case "bronze":
                uTypeIcon = R.drawable.ic_bronze;
                break;
        }

        holder.userTypeIcon.setImageResource(uTypeIcon);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}