package com.mymoneybd.app;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.view.*;import android.widget.*;import java.text.*;import java.util.*;
public class MainActivity extends Activity {
 DB db; LinearLayout root,items; TextView total,in,out;
 String[] accounts={"Cash","bKash","Nagad","Bank"},categories={"Food","Shopping","Transport","Bills","Salary","Business","Health","Education","Other"};
 TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setPadding(8,12,8,12);return t;}
 void add(LinearLayout p,View v){p.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 String money(long cents){return "৳ "+String.format(Locale.US,"%,.2f",cents/100.0);}
 public void onCreate(Bundle b){super.onCreate(b);db=new DB(this);home();}
 void home(){ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setPadding(24,28,24,24);root.setOrientation(1);scroll.addView(root);setContentView(scroll);
 add(root,label("My Money BD",28));add(root,label("Personal Finance • Offline",14));
 total=label("",26);in=label("",17);out=label("",17);add(root,total);add(root,in);add(root,out);
 Button btn=new Button(this);btn.setText("+ Add Transaction");add(root,btn);btn.setOnClickListener(v->form());
 add(root,label("Recent Transactions (long press to delete)",18));items=new LinearLayout(this);items.setOrientation(1);add(root,items);refresh();}
 void refresh(){long bal=0,mi=0,mo=0;String month=new SimpleDateFormat("yyyy-MM",Locale.US).format(new Date());items.removeAllViews();
 Cursor c=db.getReadableDatabase().rawQuery("SELECT _id,type,amount,account,category,note,created_at FROM transactions ORDER BY _id DESC",null);int n=0;
 while(c.moveToNext()){long id=c.getLong(0),amount=c.getLong(2);boolean income=c.getString(1).equals("Income");bal+=income?amount:-amount;
 if(c.getString(6).startsWith(month)){if(income)mi+=amount;else mo+=amount;}
 if(n++>=100)continue;String line=(income?"+ ":"- ")+money(amount)+" • "+c.getString(4)+" • "+c.getString(3)+"\n"+c.getString(6)+" "+c.getString(5);
 TextView t=label(line,16);add(items,t);t.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Delete transaction?").setPositiveButton("Delete",(d,w)->{db.getWritableDatabase().delete("transactions","_id=?",new String[]{""+id});refresh();}).setNegativeButton("Cancel",null).show();return true;});}
 c.close();total.setText("Balance: "+money(bal));in.setText("This month income: "+money(mi));out.setText("This month expense: "+money(mo));if(n==0)add(items,label("No transactions yet",15));}
 void form(){LinearLayout layout=new LinearLayout(this);layout.setPadding(24,10,24,10);layout.setOrientation(1);
 Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Expense","Income"}));add(layout,type);
 EditText amount=new EditText(this);amount.setHint("Amount in BDT");amount.setInputType(8194);add(layout,amount);
 Spinner account=new Spinner(this);account.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,accounts));add(layout,account);
 Spinner category=new Spinner(this);category.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,categories));add(layout,category);
 EditText note=new EditText(this);note.setHint("Note (optional)");add(layout,note);
 AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Add Transaction").setView(layout).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
 dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{try{java.math.BigDecimal a=new java.math.BigDecimal(amount.getText().toString().trim());long cents=a.movePointRight(2).longValueExact();if(cents<=0)throw new Exception();ContentValues values=new ContentValues();values.put("type",type.getSelectedItem().toString());values.put("amount",cents);values.put("account",account.getSelectedItem().toString());values.put("category",category.getSelectedItem().toString());values.put("note",note.getText().toString());values.put("created_at",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US).format(new Date()));db.getWritableDatabase().insertOrThrow("transactions",null,values);dialog.dismiss();refresh();}catch(Exception e){amount.setError("Enter a valid positive amount with max 2 decimals");}}));dialog.show();}
 static class DB extends SQLiteOpenHelper{DB(Context c){super(c,"mymoney.db",null,1);}public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE transactions(_id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT NOT NULL,amount INTEGER NOT NULL,account TEXT NOT NULL,category TEXT NOT NULL,note TEXT NOT NULL,created_at TEXT NOT NULL)");}public void onUpgrade(SQLiteDatabase d,int o,int n){}}
}