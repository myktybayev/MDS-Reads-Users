package kz.incubator.sdcl.club1.rules_menu;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import kz.incubator.sdcl.club1.MenuActivity;
import kz.incubator.sdcl.club1.R;

public class RuleFragment extends Fragment {


    CarouselView carouselView;
    View view;
    MenuActivity menuActivity;

    public RuleFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_rule, container, false);
        initCarousel();
        return view;
    }

    private void initCarousel() {
        menuActivity = (MenuActivity) getActivity();

        carouselView = view.findViewById(R.id.carouselView);
        carouselView.setPageCount(6);
        carouselView.setFillColor(getResources().getColor(R.color.back));
        carouselView.setViewListener(new ViewListener() {
            @Override
            public View setViewForPosition(int position) {
                View customView = getLayoutInflater().inflate(R.layout.design_of_carousel,null);
                TextView month = customView.findViewById(R.id.month);
                TextView price = customView.findViewById(R.id.price);
                TextView textDesc = customView.findViewById(R.id.textDesc);
                LinearLayout linearLayout = customView.findViewById(R.id.backgroundColor);
                switch (position){
                    case 0:
                        month.setText("1 MONTH");
                        price.setText("0 KZT");
                        textDesc.setText("If you give 1 book(bestseller) to Rent Books, we will give you gift 1 month subscription free.");
                        textDesc.setTextColor(Color.BLACK);

                        linearLayout.setBackgroundColor(getResources().getColor(R.color.cyan));
                        setResideMenuEnabled(false);
                        break;
                    case 1:
                        month.setText("1 MONTH");
                        price.setText("890 KZT");
                        textDesc.setText("Our 1 monthly plan grants access to all features of Rent Books for 1 Month.");
                        textDesc.setTextColor(Color.GRAY);
                        linearLayout.setBackgroundColor(getResources().getColor(R.color.bigColor));
                        setResideMenuEnabled(false);
                        break;
                    case 2:
                        month.setText("2 MONTH");
                        price.setText("1590 KZT");
                        linearLayout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        textDesc.setText("Our 2 monthly plan grants access to all features of Rent Books for 2 Month.");
                        textDesc.setTextColor(Color.GRAY);
                        setResideMenuEnabled(false);
                        break;
                    case 3:
                        month.setText("3 MONTH");
                        price.setText("2290 KZT");
                        linearLayout.setBackgroundColor(getResources().getColor(R.color.third));
                        textDesc.setText("Our 3 monthly plan grants access to all features of Rent Books for 3 Month.");
                        setResideMenuEnabled(false);
                        break;
                    case 4:
                        month.setText("6 MONTH");
                        price.setText("3190 KZT");
                        linearLayout.setBackgroundColor(getResources().getColor(R.color.orange));
                        textDesc.setText("Our 6 monthly plan grants access to all features of Rent Books for 6 Month.");
                        setResideMenuEnabled(false);
                        break;
                    case 5:
                        month.setText("1 YEAR");
                        price.setText("4990 KZT");
                        linearLayout.setBackgroundColor(getResources().getColor(R.color.blue));
                        textDesc.setText("Our 1 year plan grants access to all features of Rent Books for 1 year.");
                        setResideMenuEnabled(false);
                        break;
                }
                return customView;
            }
        });
    }

    boolean resideMenuEnabled = true;

    public void setResideMenuEnabled(boolean enabled) {
        if (resideMenuEnabled != enabled) {
            if (enabled) menuActivity.resideMenu.removeIgnoredView(carouselView);
            else menuActivity.resideMenu.addIgnoredView(carouselView);
            resideMenuEnabled = enabled;
        }
    }
}
