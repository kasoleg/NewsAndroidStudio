package net.neutrinosoft.news;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.util.Log;

public class HttpClient {

	private final String delimiter = "--";
	private final String boundary = "SwA"
			+ Long.toString(System.currentTimeMillis()) + "SwA";

	private String url;
	private HttpURLConnection connection;
	private OutputStream os;

	public HttpClient(String url) {
		this.url = url;
		try {
			connection = (HttpURLConnection) (new URL(url)).openConnection();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set connection parameters to multipart request in HttpUrlConnection
	 */
	public void connectForMultipart() throws Exception {
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type",
				"multipart/form-data; boundary=" + boundary);
		connection.connect();
		os = connection.getOutputStream();
	}

	/**
     * Add parameter as (key, value) to request in HttpUrlConnection
     *
     * @param paramName - key of adding parameter
     * @param value - value adding parameter
     * @throws IOException if reading or writing the cache directory fails
     */
	public void addFormPart(String paramName, String value) throws Exception {
		writeParamData(paramName, value);
	}

	/**
     * Add file part to output stream for request in HttpUrlConnection
     *
     * @param paramName - name of adding file in request
     * @param fileName - name of file on disk
     * @throws Exception if adding file fails
     */
	public void addFilePart(String paramName, String fileName, byte[] data)
			throws Exception {
		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName
				+ "\"; filename=\"" + fileName + "\"\r\n").getBytes());
		os.write(("Content-Type: application/octet-stream\r\n").getBytes());
		os.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
		os.write("\r\n".getBytes());

		os.write(data);

		os.write("\r\n".getBytes());
	}

	/**
     * Add finish symbols to output stream to request in HttpUrlConnection
     */
	public void finishMultipart() throws Exception {
		os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
	}

	/**
     * Get response from server and return response as string
     */
	public String getResponse() throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line + '\n');
		}
		return sb.toString();
	}

	/**
     * Add parameter to multipart request in output stream of HttpUrlConnection
     *
     * @param paramName - key of adding parameter
     * @param value - value adding parameter
     * @throws Exception if adding parameter fails
     */
	private void writeParamData(String paramName, String value)
			throws Exception {

		os.write((delimiter + boundary + "\r\n").getBytes());
		os.write("Content-Type: text/plain\r\n".getBytes());
		os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n")
				.getBytes());
		;
		os.write(("\r\n" + value + "\r\n").getBytes());

	}

	/**
     * Add header as (key, value) to request in HttpUrlConnection
     *
     * @param key - key of adding parameter
     * @param value - value adding parameter
     * @throws IOException if adding header fails
     */
	public void addHeader(String key, String value) throws Exception {
		connection.addRequestProperty(key, value);
	}
}
