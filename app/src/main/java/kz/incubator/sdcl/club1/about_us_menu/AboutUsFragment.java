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
    ArrayList<Moderator> moderators = new ArrayList<>();
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

    public void initGrid() {
        initializeWidgets();
        adapter = new ModeratorsAdapter(getActivity(), moderators);
        gridView.setAdapter(adapter);
    }

    public void initializeWidgets() {
        moderators.add(new Moderator(R.drawable.user_photo, "Arafat Nurlakov",      getString(R.string.ceo_mds_program), R.color.second));
        moderators.add(new Moderator(R.drawable.user_photo, "Zhiger Telyukanov",    getString(R.string.ceo_mds_reads), R.color.second));
        moderators.add(new Moderator(R.drawable.user_photo, "Bakhytzhan Myktybayev",getString(R.string.project_developer), R.color.first));
        moderators.add(new Moderator(R.drawable.user_photo, "Adilet Amangossov",    getString(R.string.project_manager), R.color.first));
        moderators.add(new Moderator(R.drawable.user_photo, "Bakhtyar Madeniyet",   getString(R.string.project_manager), R.color.first));
    }
}
