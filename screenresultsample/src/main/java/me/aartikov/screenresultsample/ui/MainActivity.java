package me.aartikov.screenresultsample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.aartikov.alligator.NavigationContext;
import me.aartikov.alligator.NavigationContextBinder;
import me.aartikov.alligator.Navigator;
import me.aartikov.alligator.Screen;
import me.aartikov.alligator.ScreenResult;
import me.aartikov.alligator.ScreenResultListener;
import me.aartikov.screenresultsample.R;
import me.aartikov.screenresultsample.SampleApplication;
import me.aartikov.screenresultsample.screens.ImagePickerScreen;
import me.aartikov.screenresultsample.screens.MessageInputScreen;

/**
 * Date: 12.03.2016
 * Time: 15:53
 *
 * @author Artur Artikov
 */
public class MainActivity extends AppCompatActivity implements ScreenResultListener {
	private Navigator mNavigator = SampleApplication.getNavigator();
	private NavigationContextBinder mNavigationContextBinder = SampleApplication.getNavigationContextBinder();

	@BindView(R.id.input_message_button)
	Button mInputMessageButton;

	@BindView(R.id.pick_image_button)
	Button mPickImageButton;

	@BindView(R.id.message_text_view)
	TextView mMessageTextView;

	@BindView(R.id.image_view)
	ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		mInputMessageButton.setOnClickListener(v -> mNavigator.goForward(new MessageInputScreen()));    // goForward works as startActivityForResult if a screen is registered for result.
		mPickImageButton.setOnClickListener(v -> mNavigator.goForward(new ImagePickerScreen()));
	}


	// Use ScreenResultResolver to translate onActivityResult arguments to ScreenResultListener call.

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		SampleApplication.getScreenResultResolver().handleActivityResult(requestCode, resultCode, data, this);
	}

	@Override
	public void onScreenResult(Class<? extends Screen> screenClass, @Nullable ScreenResult result) {
		if (screenClass == MessageInputScreen.class) {
			onMessageInputted((MessageInputScreen.Result) result);
		} else if (screenClass == ImagePickerScreen.class) {
			onImagePicked((ImagePickerScreen.Result) result);
		}
	}

	private void onMessageInputted(MessageInputScreen.Result messageInputResult) {
		if (messageInputResult != null) {
			mMessageTextView.setText(getString(R.string.inputted_message_template, messageInputResult.getMessage()));
		} else {
			Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
		}
	}

	private void onImagePicked(ImagePickerScreen.Result imagePickerResult) {
		if (imagePickerResult != null) {
			Picasso.with(this).load(imagePickerResult.getUri()).into(mImageView);
		} else {
			Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		NavigationContext navigationContext = new NavigationContext.Builder(this).build();
		mNavigationContextBinder.bind(navigationContext);
	}

	@Override
	protected void onPause() {
		mNavigationContextBinder.unbind();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		mNavigator.goBack();
	}
}
