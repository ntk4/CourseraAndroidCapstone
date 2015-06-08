package org.ntk.mutibo.android;

import org.ntk.mutibo.android.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HotScoresFragment extends Fragment {
	
	public HotScoresFragment(){}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_hot_scores, container, false);
         
        return rootView;
    }
}
