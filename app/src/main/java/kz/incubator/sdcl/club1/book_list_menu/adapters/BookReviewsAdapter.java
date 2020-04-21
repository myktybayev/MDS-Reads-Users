package kz.incubator.sdcl.club1.book_list_menu.adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.book_list_menu.module.ReviewInUser;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_BOOKS;

public class BookReviewsAdapter extends RecyclerView.Adapter<BookReviewsAdapter.MyTViewHolder> {
    private Context context;
    private List<ReviewInUser> bookList;
    DateFormat dateF;
    String date;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;

    public class MyTViewHolder extends RecyclerView.ViewHolder{
        public TextView info;
        TextView review_text;
        RatingBar bookRating;
        ImageView notebook;

        public MyTViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.titleOfBook);
            review_text = view.findViewById(R.id.review_text);
            bookRating = view.findViewById(R.id.bookRating);
//            notebook = view.findViewById(R.id.notebook);
//
//            notebook.setImageResource(R.drawable.done);
        }
    }

    public BookReviewsAdapter(Context context, List<ReviewInUser> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        manageDate();

        storeDb = new StoreDatabase(context);
        sqdb = storeDb.getWritableDatabase();

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        ReviewInUser item = bookList.get(position);
        String bookKey = item.getBook_id();
        String bookName = "";
        int bookRating = item.getUser_rate();

        Cursor userCursor = getBookByFKey(bookKey);
        if (userCursor != null && userCursor.getCount() > 0) {
            userCursor.moveToNext();
            bookName = userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME));
        }

        holder.info.setText(bookName);
        holder.review_text.setText("\""+item.getReview_text()+"\"");
        holder.bookRating.setRating(bookRating);

    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public Cursor getBookByFKey(String fkey) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " +
                COLUMN_FKEY + "=?", new String[]{fkey});
        return res;

    }
}