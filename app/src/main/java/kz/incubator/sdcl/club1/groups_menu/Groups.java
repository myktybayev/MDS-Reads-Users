package kz.incubator.sdcl.club1.groups_menu;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Comparator;

@IgnoreExtraProperties
public class Groups implements Serializable {
    String group_id;
    String group_name;
    int person_count;
    int ave_point;

    public Groups() {

    }

    public Groups(String group_id, String group_name, int person_count, int ave_point) {
        this.group_id = group_id;
        this.group_name = group_name;
        this.person_count = person_count;
        this.ave_point = ave_point;
    }

    public static Comparator<Groups> groupPlace = new Comparator<Groups>() {

        public int compare(Groups g1, Groups g2) {
            int group1 = g1.getAve_point();
            int group2 = g2.getAve_point();

            //ascending order
//            return user1.compareTo(user2);

            return group2-group1;

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public int getPerson_count() {
        return person_count;
    }

    public void setPerson_count(int person_count) {
        this.person_count = person_count;
    }

    public int getAve_point() {
        return ave_point;
    }

    public void setAve_point(int ave_point) {
        this.ave_point = ave_point;
    }
}
