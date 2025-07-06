package com.example.wordmem;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;


//public class Fragment_Words_Input extends Fragment implements AsyncResponse{
public class Fragment_Words_Input extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words_input, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // getting view
        View view = getView();
        // getting elements in view
        final EditText ed = view.findViewById(R.id.word_input);

        // Database
        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getContext());

        // next fragment
        final Fragment_Words_Def defFragment = new Fragment_Words_Def();
        final Bundle arguments = new Bundle();

        // setting methods for elements in view

        // edit text
        ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    String word = ed.getText().toString().toLowerCase();

                    databaseAccess.open();
                    List<String> meaning_map = databaseAccess.getMeaning(word);

                    String meaning = meaning_map.get(0);
                    String source = meaning_map.get(1);

                    Log.v("Meaning: ", meaning);

                    databaseAccess.close();
                    arguments.putString("word", word);
                    arguments.putString("meaning", meaning);
                    arguments.putString("source", source);
                    defFragment.setArguments(arguments);

                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                    fragmentTransaction.replace(R.id.words_frame, defFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    Log.i("Fragment words input: ", "Fragment replaced");

                    return true;
                }

                return false;
            }

        });



    }

}
