package org.ntk.mutibo.android;

import org.ntk.mutibo.android.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GangFragment extends Fragment {
	
	public GangFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_gang, container, false);
         
        return rootView;
    }
}
