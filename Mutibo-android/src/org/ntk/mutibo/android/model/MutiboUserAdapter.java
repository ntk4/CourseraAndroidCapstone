package org.ntk.mutibo.android.model;

import java.util.ArrayList;
import java.util.List;

import org.ntk.mutibo.android.R;
import org.ntk.mutibo.json.MutiboUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MutiboUserAdapter extends BaseAdapter {

	// List of MutiboUsers
	private final List<MutiboUser> mItems;

	private final Context mContext;

	public MutiboUserAdapter(Context context) {

		mContext = context;
		mItems = new ArrayList<MutiboUser>();

	}
	
	public MutiboUserAdapter(Context context, List<MutiboUser> items) {

		mContext = context;
		mItems = items;

	}

	// Add a MutiboUser to the adapter
	// Notify observers that the data set has changed

	public void add(MutiboUser item) {

		mItems.add(item);
		notifyDataSetChanged();

	}

	// Clears the list adapter of all items.

	public void clear() {

		mItems.clear();
		notifyDataSetChanged();

	}

	// Returns the number of MutiboUsers

	@Override
	public int getCount() {

		return mItems.size();

	}

	// Retrieve the number of MutiboUsers

	@Override
	public Object getItem(int pos) {

		return mItems.get(pos);

	}

	// Get the ID for the MutiboUser
	// In this case it's just the position

	@Override
	public long getItemId(int pos) {

		return pos;

	}

	// Create a View to display the MutiboUser
	// at specified position in mItems

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO - Get the current MutiboUser
		final MutiboUser MutiboUser = (MutiboUser) getItem(position);
		RelativeLayout itemLayout = null;
		
		if (convertView == null) {
			// TODO - Inflate the View for this MutiboUser
			// from todo_item.xml.
//			LayoutInflater inflater = mContext.getLayoutInflater();
			LayoutInflater inflater = LayoutInflater.from(mContext); 
//			inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemLayout = (RelativeLayout) inflater.inflate(R.layout.user_item, parent, false);
	
	
			final TextView titleView = (TextView) itemLayout.findViewById(R.id.userItemName);
			titleView.setText(MutiboUser.getName());
		} 	else { // Use the view that's already there
			itemLayout = (RelativeLayout)convertView;
		}
			// Return the View you just created
		return itemLayout;

	}

}
