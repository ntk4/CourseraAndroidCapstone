package org.ntk.mutibo.android;

import org.ntk.mutibo.android.model.GameManager;
import org.ntk.mutibo.android.model.Playable;
import org.ntk.mutibo.json.ItemSet;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Optional;

public class ExplanationFragment extends Fragment implements OnClickListener {

	private Optional<ItemSet> currentQuestion;
	private Optional<Playable> currentGame;
	private Optional<Integer> currentAnswer;

	private TextView lblResult, lblMovieTitle, lblAnswerExplanation, lblScore;
	private ImageView imgMovie, imgLike, imgDislike;
	private Drawable answerBitmap;

	public ExplanationFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_explanation, container, false);

		resolveComponents(rootView);

		displayCurrentExplanation();

		return rootView;
	}

	private void displayCurrentExplanation() {
		if (currentQuestion.isPresent() && currentAnswer.isPresent() && currentGame.isPresent()) {
			if (currentQuestion.get().getDifferentItemId() == currentAnswer.get()) {
				lblResult.setText(getResources().getString(R.string.correct_answer));
			} else if (currentGame.get().isGameOver()) {
				// Toast.makeText(getActivity(), getResources().getString(R.string.game_over),
				// Toast.LENGTH_LONG).show();
				GameManager manager = ((MainActivity) getActivity()).getGameManager();
				manager.finishGame(currentGame.get());
				lblResult.setText(String.format(getResources().getString(R.string.game_over), currentGame.get()
						.getLives()));
			} else {
				lblResult.setText(String.format(getResources().getString(R.string.incorrect_answer), currentGame.get()
						.getLives()));
			}

			lblMovieTitle.setText(currentQuestion.get().getItems().get(currentQuestion.get().getDifferentItemId())
					.getName());
			lblAnswerExplanation.setText(currentQuestion.get().getExplanation());
			lblScore.setText("Score: " + currentGame.get().getScore());
			imgMovie.setImageDrawable(getResources().getDrawable(R.drawable.movie));

			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());

			Boolean downloadImages = p.getBoolean("pref_downloadMovieImages", true);

			if (downloadImages && answerBitmap != null) // instead of the drawable, here download the poster from
														// themoviedb.org
				imgMovie.setImageDrawable(answerBitmap);
			else
				imgMovie.setImageDrawable(this.getActivity().getApplicationContext().getResources()
						.getDrawable(R.drawable.movie));

			imgMovie.setContentDescription("The correct answer is the movie called "
					+ currentQuestion.get().getItems().get(currentQuestion.get().getDifferentItemId()).getName());
		}
	}

	public void loadItemSet(Playable playable, ItemSet itemSet, int answer) {
		if (itemSet.getItems() != null && itemSet.getItems().size() == 4 && itemSet.getDifferentItemId() >= 0
				&& itemSet.getDifferentItemId() < 4) {

			currentGame = Optional.of(playable);
			currentQuestion = Optional.of(itemSet);
			currentAnswer = Optional.of(answer);
		}

	}

	private void resolveComponents(View rootView) {
		lblResult = (TextView) rootView.findViewById(R.id.lblResult);
		lblMovieTitle = (TextView) rootView.findViewById(R.id.lbMovieTitle);
		lblAnswerExplanation = (TextView) rootView.findViewById(R.id.lblAnswerExplanation);
		lblScore = (TextView) rootView.findViewById(R.id.lblScore);
		imgMovie = (ImageView) rootView.findViewById(R.id.imgMovie);
		imgLike = (ImageView) rootView.findViewById(R.id.imgLike);
		imgDislike = (ImageView) rootView.findViewById(R.id.imgDislike);

		lblResult.setOnClickListener(this);
		lblMovieTitle.setOnClickListener(this);
		lblAnswerExplanation.setOnClickListener(this);
		imgMovie.setOnClickListener(this);
		lblScore.setOnClickListener(this);
		imgLike.setOnClickListener(this);
		imgDislike.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (!isAdded())
			return; // otherwise getActivity() won't work!

		GameManager manager = ((MainActivity) getActivity()).getGameManager();

		if (manager != null && currentGame.isPresent()) {
			switch (v.getId()) {
			case R.id.imgLike:
				manager.likeItemSet(currentQuestion.get());
				break;
			case R.id.imgDislike:
				manager.dislikeItemSet(currentQuestion.get());
				break;
			default:
				if (!currentGame.get().isGameOver()) {
					manager.loadNextItem(currentGame.get());
					resetImage();
				}
			}

		}

	}

	private void resetImage() {
		this.imgMovie.setImageBitmap(null);
		setAnswerBitmap(null);
	}

	public void setAnswerBitmap(Drawable answerBitmap) {
		this.answerBitmap = answerBitmap;
	}
}
