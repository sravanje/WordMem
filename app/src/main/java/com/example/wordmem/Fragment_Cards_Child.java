package com.example.wordmem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.List;


public class Fragment_Cards_Child extends Fragment {

    Integer scrollpos = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cards_child, container, false);

        final ListView listView = v.findViewById(R.id.lv);

        if (getArguments() != null){
            scrollpos = getArguments().getInt("scrollpos");
            if (scrollpos==-1)
                scrollpos = 1;
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.smoothScrollToPositionFromTop(scrollpos, 0);
                }
            });
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        final ListView lv = (ListView)view.findViewById(R.id.lv);

        final MainActivity main = (MainActivity)getActivity();
        main.scrollpos = 0;

        // Database
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());

        SimpleAdapter adapter = populateList(databaseAccess, "");
        lv.setAdapter(adapter);

        // Search View
        initSearchWidgets(view, lv, databaseAccess);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,int position, long arg3) {

                main.scrollpos = position;

                Fragment_Cards_Def defFragment = new Fragment_Cards_Def();
                final Bundle arguments = new Bundle();

                TextView textView = view.findViewById(R.id.tv1);
                String word = textView.getText().toString();

                databaseAccess.open();
                
                // Get the source from the clicked item
                HashMap<String, String> clickedItem = (HashMap<String, String>) lv.getItemAtPosition(position);
                String itemSource = clickedItem.get("Source");
                int sourceInt = Integer.parseInt(itemSource);
                
                // Get meaning from the specific source that was used to add this word
                List<String> meaning_map = databaseAccess.getMeaningBySource(word, sourceInt);

                String meaning = meaning_map.get(0);
                String source = meaning_map.get(1);

                Log.v("Meaning: ", meaning);

                databaseAccess.close();
                arguments.putString("word", word);
                arguments.putString("meaning", meaning);
                arguments.putString("source", source);
                defFragment.setArguments(arguments);

                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                fragmentTransaction.replace(R.id.cards_frame, defFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });

    }

    private void initSearchWidgets(View view, final ListView lv, final DatabaseAccess databaseAccess) {
        SearchView searchview = (SearchView)view.findViewById(R.id.searchview);

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.v("newText Searchview", newText);
                final SimpleAdapter adapter = populateList(databaseAccess, newText);
                lv.setAdapter(adapter);

                return false;
            }
        });
    }

    public SimpleAdapter populateList(DatabaseAccess dba, String searchkey){

        dba.open();
//        List<String> words = dba.getList();
        // Show all sources (-1 means no filter)
        List<HashMap<String,String>> wordMapList = dba.getMapBySource(searchkey, -1);
        dba.close();

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, words);
        SimpleAdapter adapter = new SimpleAdapter(getContext(),wordMapList, R.layout.subitemlist,
                new String[]{"Word","Date"},new int[]{R.id.tv1,R.id.tv2});

        return adapter;
    }


}
