package com.shishire.portalcoordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{

	private EditText username_field;
	private EditText reference_file_field;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.username_field = (EditText) findViewById(R.id.username_field);
		this.reference_file_field = (EditText) findViewById(R.id.reference_file_field);

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);

		this.username_field.setText(preferences.getString("username", ""), TextView.BufferType.EDITABLE);
		this.reference_file_field.setText(preferences.getString("reference_file", ""), TextView.BufferType.EDITABLE);

	}

	private class FetchButtonClick extends AsyncTask<String, Void, Void>
	{
		protected Void doInBackground(String... strings)
		{
			String username = strings[0];
			String reference_file = strings[1];
			try
			{
				HashSet<ResonatorDeployment> portalListString = parsePortalList(
				    fetchRemotePortalList(reference_file), username);

				Intent intent = new Intent(MainActivity.this, MainActivity.class);
				PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				Iterator<ResonatorDeployment> iter = portalListString.iterator();

				int id = 0;
				while (iter.hasNext())
				{
					id++;
					ResonatorDeployment position = iter.next();
					Notification noti = new NotificationCompat.Builder(MainActivity.this)
					    .setSmallIcon(R.drawable.ic_launcher)
					    .setContentIntent(pIntent)
					    .setContentTitle(position.getPortal())
					    .setContentText("Deploy on octant " + position.getPosition())
					    .build();

					notificationManager.notify(id, noti);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		private HashSet<ResonatorDeployment> parsePortalList(String portalListString, String username)
		    throws JSONException
		{
			JSONObject portalListContainer = null;
			portalListContainer = new JSONObject(portalListString);

			JSONArray portalListJson = portalListContainer.getJSONArray(username);

			HashSet<ResonatorDeployment> portalList = new HashSet<ResonatorDeployment>();
			for (int i = 0; i < portalListJson.length(); i++)
			{
				JSONObject portalObject = portalListJson.getJSONObject(i);
				portalList.add(new ResonatorDeployment(portalObject.getString("portal-name"), portalObject
				    .getString("deploy-octant")));
			}
			return portalList;
		}

		public String fetchRemotePortalList(String reference_file)
		{
			StringBuilder builder = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(reference_file);
			try
			{
				HttpResponse response = client.execute(httpGet);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200)
				{
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null)
					{
						builder.append(line);
					}
				}
				else
				{
					Log.e(this.getClass().getName(), "Failed to download file");
				}
			}
			catch (ClientProtocolException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return builder.toString();
		}
	}

	public void fetchButtonClick(View button)
	{
		String username = this.username_field.getText().toString();
		String reference_file = this.reference_file_field.getText().toString();

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("username", username);
		editor.putString("reference_file", reference_file);
		editor.commit();

		new FetchButtonClick().execute(username, reference_file);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
