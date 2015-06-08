package org.ntk.mutibo.android;

import java.util.ArrayDeque;

import org.ntk.mutibo.android.helpers.BannerDrawable;
import org.ntk.mutibo.android.helpers.ImageDownloader;
import org.ntk.mutibo.android.helpers.UniversalImageLoaderHelper;
import org.ntk.mutibo.android.model.DemoGame;
import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.json.ItemSet;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewOverlay;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class QuestionFragment extends Fragment implements OnClickListener {

	private TextView[] title;
	private ImageView[] image;
	// private Button btAskFriend, btSkipQuestion;
	// private TextView lblScore;

	private Optional<ItemSet> currentQuestion;
	private Optional<Playable> currentGame;
	private BannerDrawable mScoreBanner;

	private ImageDownloader[] downloaders;
	private ProgressBar[] spinners;
	private View scoreContainer;

	public QuestionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_question2, container, false);

		resolveComponents(rootView);

		initializeDownloaders(); // should be initialized before displaying any question

		displayCurrentQuestion();

		return rootView;
	}

	private void initializeDownloaders() {
		downloaders = new ImageDownloader[4];
		for (int i = 0; i < 4; i++)
			downloaders[i] = new ImageDownloader();
	}

	private void displayCurrentQuestion() {
		if (currentQuestion != null && currentQuestion.isPresent() && currentGame != null && currentGame.isPresent())
			displayItemSet();
	}

	private void resolveComponents(View rootView) {
		title = new TextView[4];
		title[0] = (TextView) rootView.findViewById(R.id.movieTitle1);
		title[1] = (TextView) rootView.findViewById(R.id.movieTitle2);
		title[2] = (TextView) rootView.findViewById(R.id.movieTitle3);
		title[3] = (TextView) rootView.findViewById(R.id.movieTitle4);

		image = new ImageView[4];
		image[0] = (ImageView) rootView.findViewById(R.id.movieImage1);
		image[1] = (ImageView) rootView.findViewById(R.id.movieImage2);
		image[2] = (ImageView) rootView.findViewById(R.id.movieImage3);
		image[3] = (ImageView) rootView.findViewById(R.id.movieImage4);

		spinners = new ProgressBar[4];
		spinners[0] = (ProgressBar) rootView.findViewById(R.id.imageProgress1);
		spinners[1] = (ProgressBar) rootView.findViewById(R.id.imageProgress2);
		spinners[2] = (ProgressBar) rootView.findViewById(R.id.imageProgress3);
		spinners[3] = (ProgressBar) rootView.findViewById(R.id.imageProgress4);
		
		scoreContainer = rootView.findViewById(R.id.imageContainer2);
		// btAskFriend = (Button) rootView.findViewById(R.id.btAskFriend);
		// btSkipQuestion = (Button) rootView.findViewById(R.id.btSkipQuestion);
		//
		// lblScore = (TextView) rootView.findViewById(R.id.lblQuestionScore);

		for (int i = 0; i < 4; i++) {

			title[i].setOnClickListener(this);
			image[i].setOnClickListener(this);
			spinners[i].setIndeterminate(true);
		}

		placeOverlay();
	}

	private void placeOverlay() {
		final View imageToOverlay = scoreContainer; // top right
		final ViewOverlay overlay = imageToOverlay.getOverlay();
		if (mScoreBanner == null)
			mScoreBanner = new BannerDrawable("Score");
		final BannerDrawable bannerDrawable = mScoreBanner;
		imageToOverlay.post(new Runnable() {
			@Override
			public void run() {
				// top right square
				bannerDrawable.setBounds(imageToOverlay.getWidth() / 2, 0, imageToOverlay.getWidth(),
						imageToOverlay.getHeight() / 2);
				overlay.add(bannerDrawable);
			}
		});
	}

	public void setGame(Playable game) {
		currentGame = Optional.of(game);
	}

	public void startDemo(DemoGame game) {

		ArrayDeque<ItemSet> demoSets = game.getItemSets();
		if (demoSets != null && demoSets.size() > 0) {
			currentGame = Optional.of((Playable) game);
			loadItemSet(demoSets.peek());
		}
	}

	public void loadItemSet(ItemSet itemSet) {
		if (itemSet.getItems() != null && itemSet.getItems().size() == 4)
			currentQuestion = Optional.of(itemSet);

	}

	public void displayItemSet() {
		if (currentQuestion.isPresent() && isAdded()) {
			ImageLoader loader = UniversalImageLoaderHelper.getLoader(getActivity());

			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());

			Boolean downloadImages = p.getBoolean("pref_downloadMovieImages", true);

			for (int i = 0; i < 4; i++) {

				title[i].setText(currentQuestion.get().getItems().get(i).getName());
				image[i].setContentDescription("Movie " + title[i].getText());
				String imageUrl = currentQuestion.get().getItems().get(i).getImage();

				if (downloadImages && loader != null && imageUrl != null && !"".equals(imageUrl)) {
					displayImage(loader, i);
					// loader.displayImage(currentQuestion.get().getItems().get(i).getImage(), image[i]);
				} else
					image[i].setImageDrawable(this.getActivity().getApplicationContext().getResources()
							.getDrawable(R.drawable.movie));
			}

			mScoreBanner.setMessage("Score: " + currentGame.get().getScore());
			// lblScore.setText("Score: " + currentGame.get().getScore());
		}
	}

	private void displayImage(ImageLoader imageLoader, final int i) {

		imageLoader.displayImage(currentQuestion.get().getItems().get(i).getImage(), image[i],
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						spinners[i].setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						spinners[i].setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						spinners[i].setVisibility(View.GONE);
					}
				});
	}

	@Override
	public void onClick(View v) {
		GameManager manager = ((MainActivity) getActivity()).getGameManager();
		if (manager == null || !manager.hasActiveGame()) {
			Log.d("QuestionFragment", "Attempting to answer but there is no game");
			return;
		}

		if (!currentQuestion.isPresent()) {
			Log.d("QuestionFragment", "Attempting to answer but there is no current question");
			return;
		}

		Drawable answer = image[currentQuestion.get().getDifferentItemId()].getDrawable();

		switch (v.getId()) {
		case R.id.movieTitle1:
		case R.id.movieImage1:
			manager.answerItemSet(currentGame.get(), currentQuestion.get(), 0, answer);
			break;

		case R.id.movieTitle2:
		case R.id.movieImage2:
			manager.answerItemSet(currentGame.get(), currentQuestion.get(), 1, answer);
			break;

		case R.id.movieTitle3:
		case R.id.movieImage3:
			manager.answerItemSet(currentGame.get(), currentQuestion.get(), 2, answer);
			break;

		case R.id.movieTitle4:
		case R.id.movieImage4:
			manager.answerItemSet(currentGame.get(), currentQuestion.get(), 3, answer);
			break;
		}
		resetImages();

	}

	private void resetImages() {
		for (int i = 0; i < 4; i++)
			image[i].setImageBitmap(null);
	}

	public void cleanUp() {
		cancelDownloadTasks();
	}

	private void cancelDownloadTasks() {
		for (int i = 0; i < 4; i++) {
			if (downloaders[i] != null)
				downloaders[i].cancel(true);
		}
	}

}
