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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
            source = getArguments().getString("source");
            tvword.setText(word);

            if (!"Word not found.".contentEquals(meaning) && !"Word not found in selected source.".contentEquals(meaning)){
                tvmeaning.setText(meaning);
                if (source.contentEquals("0"))
                    tvsource.setText("(Vocabulary.com)");
                else if (source.contentEquals("1"))
                    tvsource.setText("(Own Definition)");
                else if (source.contentEquals("2"))
                    tvsource.setText("(Dictionary API)");
                Log.v("Word_def: meaning.toString()", "Word found");
            }
            else if ("Word not found in selected source.".contentEquals(meaning) && source.contentEquals("1")) {
                Log.v("Word_def: meaning.toString()", "Word not found in selected source");
                tvmeaning.setText("Word not found in selected source.");
                tvsource.setText(":(");
                owndef.setVisibility(View.VISIBLE);
            }
            else {
                Log.v("Word_def: meaning.toString()", "Word not found, trying to fetch from web");
                if (!isOnline()){
                    Toast.makeText(getContext(),"Word not found in database, Network not available.",Toast.LENGTH_SHORT).show();
                    tvmeaning.setText("Word not found, and network not available.");
                    tvsource.setVisibility(View.INVISIBLE);
                    owndef.setVisibility(View.VISIBLE);
                }
                else {
                    Task t = new Task();
                    t.delegate = (AsyncResponse)this;
                    t.execute(word, "dictionaryapi"); // Default to dictionaryapi
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
                    Toast.makeText(getContext(), "Word or Meaning empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add word, meaning to db

                if (tvmeaning.getText().toString()!="Word not found." && tvmeaning.getText().toString()!="Word not found, and network not available." && tvmeaning.getText().toString()!="Word not found in selected source.") {
                    databaseAccess.open();

                    // Get the source from the arguments passed to this fragment
                    int currentSource = Integer.parseInt(source);
                    String date_added = databaseAccess.date_added_with_source(word, currentSource);

                    if (date_added.contentEquals("")) {
                        databaseAccess.addWordWithSource(word, currentSource);

//                        Update fragment cards right after adding word to list (Refresh fragment  by replacing with itself):
                        Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.cards_frame, fcc);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        Toast.makeText(getContext(), "Added word to my words: " + word, Toast.LENGTH_SHORT).show();
                    }
                    else
                        Toast.makeText(getContext(), "Already in my words. Added on: " + date_added, Toast.LENGTH_SHORT).show();
                    databaseAccess.close();

                }

                else if (owndef.getText().length()!=0){
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
                    databaseAccess.open();
                    databaseAccess.newEntry(word, owndef.getText().toString(), 1);
                    databaseAccess.addWordWithSource(word, 1); // Own definition source is 1
                    databaseAccess.close();

//                    Update fragment cards right after adding word to list (Refresh fragment  by replacing with itself):
                    Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.cards_frame, fcc);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    Toast.makeText(getContext(), "Added word to my words: " + word, Toast.LENGTH_SHORT).show();
                    owndef.setText("");
                    owndef.setVisibility(View.INVISIBLE);
                }

                else {
                    Toast.makeText(getContext(), "Can't add this word: " + word + ". Add your own definition and try again", Toast.LENGTH_SHORT).show();
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
        TextView tvsource = view.findViewById(R.id.source);
        EditText owndef = view.findViewById(R.id.owndef);

        // Parse output to get definition and source
        String[] parts = output.split("\\|");
        String definition = parts[0];
        String fetchSource = parts.length > 1 ? parts[1] : "dictionaryapi";

        if (definition.contentEquals("Failed") || definition.contentEquals("No definition found")) {
            tvmeaning.setText("Word not found.");
            owndef.setVisibility(View.VISIBLE);
            tvsource.setText(":(");
        }
        else if (definition.contentEquals("Try the world's fastest, smartest dictionary: Start typing a word and you'll see the definition. Unlike most online dictionaries, we want you to find your word's meaning quickly. We don't care how many ads you see or how many pages you view. In fact, most of the time you'll find the word you are looking for after typing only one or two letters.")) {
            tvmeaning.setText("Word not found.");
            // owndef.setVisibility(View.VISIBLE);
            tvsource.setText("(Vocabulary.com)");
        }
        else {
            tvmeaning.setText(definition);
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());
            databaseAccess.open();
            
            if (fetchSource.equals("vocabulary")) {
                tvsource.setText("(Vocabulary.com)");
                databaseAccess.newEntry(word, definition, 0);
            } else {
                tvsource.setText("(Dictionary API)");
                databaseAccess.newEntry(word, definition, 2);
            }
            
            databaseAccess.close();
        }
    }


    private class Task extends AsyncTask<String, Void, String[]> {

        public AsyncResponse delegate = null;

        @Override
        protected String[] doInBackground(String... strings) {

            String word = strings[0];
            String source = strings.length > 1 ? strings[1] : "dictionaryapi"; // Default to dictionaryapi
            String def;

            try {
                if (source.equals("vocabulary")) {
                    def = fetchFromVocabulary(word);
                } else {
                    def = fetchFromDictionaryAPI(word);
                }
            } catch (Exception e) {
                Log.v("Word_def: Request Error", "Could not fetch from " + source);
                def = "Failed";
                e.printStackTrace();
            }

            return new String[]{def, source};
        }

        private String fetchFromVocabulary(String word) throws IOException {
            List<String> metalist = new ArrayList<>();
            Document document = Jsoup.connect("https://www.vocabulary.com/dictionary/" + word).get();
            Elements meta = document.select("meta[name=description]");

            for (Element e : meta) {
                metalist.add(e.attr("content"));
            }

            Log.v("Word_def: Vocabulary Response", metalist.toString());
            return metalist.get(0);
        }

        private String fetchFromDictionaryAPI(String word) throws IOException, JSONException {
            URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.v("Word_def: DictionaryAPI Response", response.toString());

            // Parse JSON response
            JSONArray jsonArray = new JSONArray(response.toString());
            if (jsonArray.length() > 0) {
                JSONObject wordObject = jsonArray.getJSONObject(0);
                JSONArray meanings = wordObject.getJSONArray("meanings");
                
                StringBuilder definition = new StringBuilder();
                
                for (int i = 0; i < meanings.length(); i++) {
                    JSONObject meaning = meanings.getJSONObject(i);
                    String partOfSpeech = meaning.getString("partOfSpeech");
                    JSONArray definitions = meaning.getJSONArray("definitions");
                    
                    if (i > 0) definition.append("\n\n");
                    definition.append("(").append(partOfSpeech).append(") ");
                    
                    for (int j = 0; j < Math.min(definitions.length(), 2); j++) { // Limit to 2 definitions per part of speech
                        JSONObject def = definitions.getJSONObject(j);
                        if (j > 0) definition.append("; ");
                        definition.append(def.getString("definition"));
                    }
                }
                
                return definition.toString();
            }
            
            return "No definition found";
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            delegate.processFinish(result[0] + "|" + result[1]); // Pass definition and source
        }
    }

    // Helper method to fetch definition with specific source
    public void fetchDefinition(String word, String source) {
        if (!isOnline()) {
            View view = getView();
            TextView tvmeaning = view.findViewById(R.id.tvDef);
            TextView tvsource = view.findViewById(R.id.source);
            EditText owndef = view.findViewById(R.id.owndef);
            
            Toast.makeText(getContext(), "Word not found in database, Network not available.", Toast.LENGTH_SHORT).show();
            tvmeaning.setText("Word not found, and network not available.");
            tvsource.setVisibility(View.INVISIBLE);
            owndef.setVisibility(View.VISIBLE);
        } else {
            Task t = new Task();
            t.delegate = (AsyncResponse) this;
            t.execute(word, source);
        }
    }

}
