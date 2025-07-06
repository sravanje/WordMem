package com.example.wordmem;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Fragment_Words_Def extends Fragment implements AsyncResponse {

    private String word;
    private StringBuffer meaning = new StringBuffer();
    String source;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_words_def, container, false);

        TextView tvmeaning= v.findViewById(R.id.tvDef);
        TextView tvword= v.findViewById(R.id.tvWord);
        TextView tvsource= v.findViewById(R.id.source);

        EditText owndef = v.findViewById(R.id.owndef);

        if (getArguments() != null){
            word = getArguments().getString("word");
            meaning.append(getArguments().getString("meaning"));
            tvword.setText(word);

            if (!"Word not found.".contentEquals(meaning)){
                tvmeaning.setText(meaning);
                source = getArguments().getString("source");
                if (source.contentEquals("1"))
                    tvsource.setText("(Own Definition)");
                Log.v("meaning.toString()", "Word found");
            }
            else {
                Log.v("meaning.toString()", "Word not found, trying to fetch from site");
                if (!isOnline()){
                    Toast.makeText(getContext(),"Word not found in database, Network not available.",Toast.LENGTH_LONG);
                    tvmeaning.setText("Word not found, and network not available.");
                    tvsource.setVisibility(View.INVISIBLE);
                    owndef.setVisibility(View.VISIBLE);
                }
                else {
                    Task t = new Task();
                    t.delegate = (AsyncResponse)this;
                    t.execute(word);
                }
            }
            meaning.setLength(0);
        }

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Database
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());

        View view = getView();
        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        final TextView tvmeaning = view.findViewById(R.id.tvDef);
        final EditText owndef = view.findViewById(R.id.owndef);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (word.isEmpty() | tvmeaning.getText().length()==0) {
                    Snackbar.make(view, "Word or Meaning empty", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                // Add word, meaning to db

                if (tvmeaning.getText().toString()!="Word not found." && tvmeaning.getText().toString()!="Word not found, and network not available.") {
                    databaseAccess.open();

                    String date_added = databaseAccess.date_added(word);

                    if (date_added.contentEquals("")) {
                        databaseAccess.addWord(word);

//                        Update fragment cards right after adding word to list (Refresh fragment  by replacing with itself):
                        Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.cards_frame, fcc);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        Snackbar.make(view, "Added word to my words: " + word, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                    else
                        Snackbar.make(view, "Already in my words. Added on: " + date_added, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    databaseAccess.close();

                }

                else if (owndef.getText().length()!=0){
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
                    databaseAccess.open();
                    databaseAccess.newEntry(word, owndef.getText().toString(), 1);
                    databaseAccess.addWord(word);
                    databaseAccess.close();

//                    Update fragment cards right after adding word to list (Refresh fragment  by replacing with itself):
                    Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.cards_frame, fcc);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    Snackbar.make(view, "Added word to my words: " + word, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    owndef.setText("");
                    owndef.setVisibility(View.INVISIBLE);
                }

                else {
                    Snackbar.make(view, "Can't add this word: " + word + ". Add your own definition and try again", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });
    }




    protected boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if(netInfo!=null && netInfo.isConnectedOrConnecting()){
            return true;
        }
        return false;
    }

    @Override
    public void processFinish(String output) {
        View view = getView();
        TextView tvmeaning = view.findViewById(R.id.tvDef);
        EditText owndef = view.findViewById(R.id.owndef);

        if (output.contentEquals("Try the world's fastest, smartest dictionary: Start typing a word and you'll see the definition. Unlike most online dictionaries, we want you to find your word's meaning quickly. We don't care how many ads you see or how many pages you view. In fact, most of the time you'll find the word you are looking for after typing only one or two letters.")) {
            tvmeaning.setText("Word not found.");
            owndef.setVisibility(View.VISIBLE);
        }
        else {
            tvmeaning.setText(output);
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
            databaseAccess.open();
            databaseAccess.newEntry(word, output, 0);
            databaseAccess.close();
        }
    }


    private class Task extends AsyncTask<String, Void, String> {

        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(String... strings) {

            String word = strings[0];
            List<String> metalist = new ArrayList<>();
            String def;

            try {
                Document document = Jsoup.connect("https://www.vocabulary.com/dictionary/"+word).get();
                Elements meta = document.select("meta[name=description]");


                for (Element e : meta){
                    metalist.add(e.attr("content"));
                }

                Log.v("Response", metalist.toString());
                def = metalist.get(0);

            } catch (IOException e) {
                Log.v("Request Error","Could not fetch");
                def = "Failed";
                e.printStackTrace();
            }

            return def;
        }

        @Override
        protected void onPostExecute(String def) {
            super.onPostExecute(def);

            delegate.processFinish(def);

//            final TextView tv = (TextView)findViewById(R.id.tv);
//            tv.setText(def);
        }
    }

}
