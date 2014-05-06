/**
 * 
 */
package se.kth.ubicomp.slcompanion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import se.kth.ubicomp.slcompanion.model.WeatherInfo;
import android.graphics.Bitmap;
import android.widget.TextView;

/**
 * @author jasperh
 * 
 */
public class YahooWeather {

	private  String woeid;

	String temperature, date, condition, humidity, wind, link;
	int conditionCode;
	Bitmap icon = null;
	TextView title, tempText, dateText, conditionText, windText, humidityText,
			day1, day2, day3, day4;
	ArrayList<String> weather = new ArrayList<String>();

	public YahooWeather(String locationId) {
		this.woeid = locationId;
	}
	
	public WeatherInfo fetchWeatherInfo() {

		String qResult = "";
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(
				"http://weather.yahooapis.com/forecastrss?w=" + woeid
						+ "&u=c&#8221;");

		try {
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream inputStream = entity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();
				String stringReadLine = null;
				while ((stringReadLine = bufferedreader.readLine()) != null) {
					if (stringReadLine.startsWith("<yweather:condition")) {
						// FIXME: currently pretty dirty to just use that one
						// line instead of actually parsing
						stringBuilder.append(stringReadLine + "\n");
					}

				}

				qResult = stringBuilder.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO extract weather details
		String restOfString = qResult.substring(27); // cut to weather condition
		// DIRTY: read in condition description:
		condition = "";
		while (!restOfString.startsWith("\"")) {
			condition += restOfString.substring(0, 1);
			restOfString = restOfString.substring(1);
		}
		// move to value of condition code
		restOfString = restOfString.substring(9);
		// and extract
		String conditioncodeString = "";
		while (!restOfString.startsWith("\"")) {
			conditioncodeString += restOfString.substring(0, 1);
			restOfString = restOfString.substring(1);
		}
		conditionCode = Integer.parseInt(conditioncodeString); // 32 = sunny,
																	// 34 =
																	// fair, 36
																	// = hot

		// move to temp value
		temperature = "";
		restOfString = restOfString.substring(9);
		while (!restOfString.startsWith("\"")) {
			temperature += restOfString.substring(0, 1);
			restOfString = restOfString.substring(1);
		}
		
		WeatherInfo info = new WeatherInfo(temperature, condition, conditionCode);
		return info;

	}

}
