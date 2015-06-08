package org.ntk.mutibo.android;

import java.util.List;

import org.ntk.mutibo.android.model.GameEventListener;
import org.ntk.mutibo.android.model.MutiboUserAdapter;
import org.ntk.mutibo.android.model.Playable.Type;
import org.ntk.mutibo.json.MutiboUser;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class PickUserFragment extends Fragment {

	// private TextView txtPickUserTitle;
	private ListView lvUsers;

	private Type gameType;

	public PickUserFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_pickuser, container, false);

		resolveComponents(rootView);

		return rootView;
	}

	public void initializeUserList(List<MutiboUser> result) {
		if (isAdded() && lvUsers != null) {
			final MutiboUserAdapter adapter = new MutiboUserAdapter(getActivity(), result);
			lvUsers.setAdapter(adapter);
			lvUsers.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
					Object selected = adapter.getItem(position);
					if (selected == null)
						Toast.makeText(getActivity(), "Please select a user", Toast.LENGTH_SHORT).show();
					else {
						userSelected((MutiboUser) selected);
					}
				}
			});
		}
	}

	protected void userSelected(MutiboUser selected) {
		((GameEventListener) getActivity()).onUserSelected(gameType, selected);
	}

	private void resolveComponents(View rootView) {
		// txtPickUserTitle = (TextView) rootView.findViewById(R.id.txtPickUserTitle);
		lvUsers = (ListView) rootView.findViewById(R.id.lvUsers);
	}

	public Type getGameType() {
		return gameType;
	}

	public void setGameType(Type gameType) {
		this.gameType = gameType;
	}

}
