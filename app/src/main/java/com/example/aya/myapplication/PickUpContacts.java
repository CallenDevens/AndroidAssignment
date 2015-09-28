package com.example.aya.myapplication;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Handler;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PickUpContacts extends ListActivity{

    public  static final int UPDATE_CONTACT = 1;
    private ListView lstViewContacts;
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> phones = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up_contacts);

        lstViewContacts = getListView();
        final ContactAdapter adapter = new ContactAdapter(this);
        lstViewContacts.setAdapter(adapter);
        lstViewContacts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lstViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setBackgroundColor(getResources().getColor(R.color.light_blue));
                String name = adapter.getItem(position).getName();
                String phone = adapter.getItem(position).getPhoneNumber();

                names.add(name);
                phones.add(phone);
                //Toast.makeText(PickUpContacts.this, phoneList,Toast.LENGTH_SHORT).show();

                Bundle b=new Bundle();
                b.putStringArrayList("names", names);
                b.putStringArrayList("phones", phones);
                Intent i =new Intent();
                i.putExtras(b);
                setResult(UPDATE_CONTACT, i);
            }
        });

        /*
        lstViewContacts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {


                Toast.makeText(PickUpContacts.this, "你点了第" + arg3 + "项", Toast.LENGTH_LONG).show();
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        */
    }


    class ContactAdapter extends BaseAdapter {
        private List<Contact> items = new ArrayList<Contact>();
        private Context context;
        private int size;

        public ContactAdapter( Context c){
            this.context = c;
            this.setListAdapter();
            this.size = items.size();
        }

        private void setListAdapter() {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            Cursor cursor=managedQuery(uri,
                    new String[] {
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.PHOTO_ID
                    }, null, null, null);
            Cursor phoneCur = null;

            while (cursor.moveToNext()){
                int nameFieldColumnIndex = cursor.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME);
                String name = cursor.getString(nameFieldColumnIndex);

                String contactId = cursor.getString(cursor.getColumnIndex(android.provider.ContactsContract.Contacts._ID));
                phoneCur = managedQuery(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

                //Fetch the first phone number
                String phoneNumber = "";
                if(phoneCur!=null) {
                    phoneCur.moveToFirst();
                    phoneNumber = phoneCur.getString(phoneCur.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                items.add(new Contact(name, phoneNumber));
            }
            if(phoneCur!=null) {
                phoneCur.close();
            }
            cursor.close();
        }

        public int getCount(){
            return size;
        }

        public Contact getItem(int position){
            return items.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View contentView, ViewGroup parent){
            Contact contact = items.get(position);

            LinearLayout contactLayout = (LinearLayout) LayoutInflater.from(context)
                    .inflate(R.layout.contact_item, parent, false);

            TextView txtName = (TextView) contactLayout.findViewById(R.id.txtContactName);
            txtName.setText(contact.getName());

            TextView txtPhone = (TextView) contactLayout.findViewById(R.id.txtContactPhone);
            txtPhone.setText(contact.getPhoneNumber());
            return contactLayout;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pick_up_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


