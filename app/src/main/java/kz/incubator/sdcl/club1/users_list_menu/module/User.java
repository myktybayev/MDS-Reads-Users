package kz.incubator.sdcl.club1.users_list_menu.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Comparator;

@IgnoreExtraProperties
public class  User implements Serializable {
    String info;
    String photo;
    String phoneNumber;
    String imgStorageName;
    String group_id;
    String groupName;
    String email;
    int point;
    int review_sum;
    int bookCount;
    int ratingInGroups;

    public User() {

    }

    public User(String info, String email, String phoneNumber, String group_id, String groupName, String photo, String imgStorageName, int bookCount, int point, int review_sum, int ratingInGroups){
        this.info = info;
        this.group_id = group_id;
        this.groupName = groupName;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
        this.email = email;
        this.imgStorageName = imgStorageName;
        this.bookCount = bookCount;
        this.point = point;
        this.review_sum = review_sum;
        this.ratingInGroups = ratingInGroups;
    }

    public static Comparator<User> userNameComprator = new Comparator<User>() {

        public int compare(User u1, User u2) {
            String user1 = u1.getInfo().toUpperCase();
            String user2 = u2.getInfo().toUpperCase();

            //ascending order
            return user1.compareTo(user2);

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public static Comparator<User> userReadedBooks = new Comparator<User>() {

        public int compare(User u1, User u2) {
            int user1 = u1.getBookCount();
            int user2 = u2.getBookCount();

            //ascending order
//            return user1.compareTo(user2);

            return user2-user1;

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public static Comparator<User> userPoint = new Comparator<User>() {

        public int compare(User u1, User u2) {
            int user1 = u1.getPoint();
            int user2 = u2.getPoint();

            //ascending order
//            return user1.compareTo(user2);

            return user2-user1;

            //descending order
            //return StudentName2.compareTo(StudentName1);
        }
    };

    public int getRatingInGroups() {
        return ratingInGroups;
    }

    public void setRatingInGroups(int ratingInGroups) {
        this.ratingInGroups = ratingInGroups;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getReview_sum() {
        return review_sum;
    }

    public void setReview_sum(int review_sum) {
        this.review_sum = review_sum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public String getImgStorageName() {
        return imgStorageName;
    }

    public void setImgStorageName(String imgStorageName) {
        this.imgStorageName = imgStorageName;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
