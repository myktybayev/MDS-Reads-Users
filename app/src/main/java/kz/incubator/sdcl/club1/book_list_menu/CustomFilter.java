package kz.incubator.sdcl.club1.book_list_menu;

import android.widget.Filter;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.users_list_menu.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.users_list_menu.module.User;

public class CustomFilter extends Filter {
    UserListAdapter adapter;
    ArrayList<User> filterList;

    public CustomFilter(ArrayList<User> filterList,UserListAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();

        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<User> filteredPlayers=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getInfo().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredPlayers.add(filterList.get(i));
                }
            }

            results.count=filteredPlayers.size();
            results.values=filteredPlayers;
        }else
        {
            results.count=filterList.size();
            results.values=filterList;

        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.userList = (ArrayList<User>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();
    }
}
