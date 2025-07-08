package com.example.wordmem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Fragment_Cards_Def extends Fragment {

    private String word;
    private StringBuffer meaning = new StringBuffer();
    private String source;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cards_def, container, false);

        TextView tvmeaning= v.findViewById(R.id.tvDef);
        TextView tvword= v.findViewById(R.id.tvWord);
        TextView tvsource= v.findViewById(R.id.source);

        if (getArguments() != null){
            word = getArguments().getString("word");
            meaning.append(getArguments().getString("meaning"));
            tvword.setText(word);
            tvmeaning.setText(meaning);
            meaning.setLength(0);

            source = getArguments().getString("source");
            if (source.contentEquals("0"))
                tvsource.setText("(Vocabulary.com)");
            else if (source.contentEquals("1"))
                tvsource.setText("(Own definition)");
            else if (source.contentEquals("2"))
                tvsource.setText("(Dictionary API)");
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (word.isEmpty() | tvmeaning.getText().length()==0) {
                    Toast.makeText(getContext(), "Word or Meaning empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Remove word, meaning from db

                databaseAccess.open();
                // Remove the word from the specific source it was added with
                int currentSource = Integer.parseInt(source);
                databaseAccess.removeWordWithSource(word, currentSource);
                databaseAccess.close();

//                Update fragment cards right after removing word from list (Refresh fragment  by replacing with itself):
                Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.cards_frame, fcc);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                Toast.makeText(getContext(), "Removed word from my list: " + word, Toast.LENGTH_SHORT).show();


            }
        });
    }

}
