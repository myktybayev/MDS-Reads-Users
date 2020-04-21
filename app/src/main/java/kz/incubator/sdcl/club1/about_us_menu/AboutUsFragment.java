package kz.incubator.sdcl.club1.about_us_menu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.R;

public class AboutUsFragment extends Fragment {
    View view;
    GridView gridView;
    ArrayList<Moderator> Moderators = new ArrayList<>();
    ModeratorsAdapter adapter;

    public AboutUsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_moderator, container, false);
        gridView = view.findViewById(R.id.gridView);
        initGrid();
        return view;
    }

    public void initGrid(){
        initializeWidgets();
        adapter = new ModeratorsAdapter(getActivity(), Moderators);
        gridView.setAdapter(adapter);
    }

    public void initializeWidgets(){
        Moderators.add(new Moderator(R.drawable.girl,"Zamira Akkulova\n+77075050713","CEO Rent Books",R.color.second));
        Moderators.add(new Moderator(R.drawable.girl,"Girl 1\n+77075050713","IT support",R.color.first));
        Moderators.add(new Moderator(R.drawable.girl,"Girl 2\n+77075050713","Moderator",R.color.first));
        Moderators.add(new Moderator(R.drawable.girl,"Girl 3\n+77075050713","Moderator",R.color.first));
    }
}
