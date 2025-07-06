package com.example.wordmem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
            if (source.contentEquals("1"))
                tvsource.setText("(Own definition)");
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
                    Snackbar.make(view, "Word or Meaning empty", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                // Remove word, meaning from db

                databaseAccess.open();
                databaseAccess.removeWord(word);
                databaseAccess.close();

//                Update fragment cards right after removing word from list (Refresh fragment  by replacing with itself):
                Fragment_Cards_Child fcc = new Fragment_Cards_Child();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.cards_frame, fcc);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                Snackbar.make(view, "Removed word from my list: " + word, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


            }
        });
    }

}
