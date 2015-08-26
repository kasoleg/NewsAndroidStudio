package net.neutrinosoft.news;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import net.neutrinosoft.news.models.Info;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class CreateActivity extends Activity implements OnClickListener {

	static final int REQUEST_IMAGE_CAPTURE = 0;
	static final int REQUEST_IMAGE_EXTERNAL_STORAGE = 1;

	private EditText etName;
	private EditText etDescription;
	private Button btnCreate;
	private Button btnImage;
	private ProgressBar prBar;
	private SharedPreferences sPref;
	private ImageView ivSelected;

	private Uri selectedImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create);
		// get links to UI objects
		etName = (EditText) findViewById(R.id.etNews);
		etDescription = (EditText) findViewById(R.id.etDescription);
		btnCreate = (Button) findViewById(R.id.btnCreate);
		btnImage = (Button) findViewById(R.id.btnImage);
		prBar = (ProgressBar) findViewById(R.id.prBar);
		ivSelected = (ImageView) findViewById(R.id.ivSelected);
		// setting up listeners
		btnCreate.setOnClickListener(this);
		btnImage.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCreate:
			requestData(getResources().getString(R.string.create_url));
			break;

		case R.id.btnImage:
			// create activity and start it for result
			AlertDialog.Builder builder = new AlertDialog.Builder(CreateActivity.this);
			builder.setTitle(R.string.load_from)
					.setItems(R.array.load_array, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								case 0:
									Intent pickPhoto = new Intent(Intent.ACTION_PICK,
											android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
									startActivityForResult(pickPhoto , REQUEST_IMAGE_EXTERNAL_STORAGE);
									break;
								case 1:
									Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
									startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
									break;
							}
						}
					});
			Dialog dialog = builder.create();
			dialog.show();
			break;
		default:
			break;
		}
	}

	private void requestData(String uri) {
		// set parameters to request and run asynctask
		RequestPackage p = new RequestPackage();
		p.setUri(uri);
		p.setParam("name", etName.getText().toString());
		p.setParam("description", etDescription.getText().toString());
		p.setImageUri(selectedImage);
		p.setHeader(LoginActivity.USER_ID, getUserId());
		new CreateNewsTask().execute(p);
	}

	private String getUserId() {
		// get UserId from SharedPrefernces
		sPref = getSharedPreferences(LoginActivity.USER_ID, MODE_PRIVATE);
		return sPref.getString(LoginActivity.USER_ID, "");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode) {
			case REQUEST_IMAGE_CAPTURE:
				if(resultCode == RESULT_OK){
					Bundle extras = data.getExtras();
					Bitmap imageBitmap = (Bitmap) extras.get("data");
					ivSelected.setImageBitmap(imageBitmap);
				}

				break;
			case REQUEST_IMAGE_EXTERNAL_STORAGE:
				if(resultCode == RESULT_OK){
					Uri selectedImage = data.getData();
					ivSelected.setImageURI(selectedImage);
				}
				break;
		}
	}

	private class CreateNewsTask extends
			AsyncTask<RequestPackage, String, String> {

		@Override
		protected void onPreExecute() {
			prBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(RequestPackage... params) {
			String json = null;
			RequestPackage p = params[0];
			ByteArrayOutputStream baos = null;
			
			//get bitmap from external storage
			if (p.getImageUri() != null) {
				Bitmap bitmap = null;
				try {
					bitmap = MediaStore.Images.Media.getBitmap(
							getApplicationContext().getContentResolver(),
							p.getImageUri());

				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				baos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 100, baos);
			}
			//create HttpClient and receive multipart request to server
			HttpClient client = new HttpClient(p.getUri());
			try {
				client.addHeader("UserId", getUserId());
				client.connectForMultipart();
				for (String key: p.getParams().keySet()) {
					client.addFormPart(key, p.getParams().get(key));
				}
				if (baos != null) {
					client.addFilePart("image", selectedImage.getLastPathSegment(),
							baos.toByteArray());
				}
				client.finishMultipart();
				json = client.getResponse();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return json;
		}

		@Override
		protected void onPostExecute(String json) {
			prBar.setVisibility(View.INVISIBLE);
			//parse message and show it
			Info message = JSONParser.parseMessage(json);
			if (message.getSuccess()) {
				Toast.makeText(getApplicationContext(),
						"News successfully added!", Toast.LENGTH_LONG).show();
				startActivity(new Intent(getApplicationContext(),
						MainActivity.class));
			} else {
				Toast.makeText(getApplicationContext(), message.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}

	}

}
