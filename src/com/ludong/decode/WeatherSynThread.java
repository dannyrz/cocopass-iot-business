package com.ludong.decode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.cocopass.helper.CDate;
import com.cocopass.helper.CString;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WeatherSynThread extends Thread {

	static Logger LOG = Logger.getLogger(WeatherSynThread.class.getName());
	// Gson gson=new Gson();

	public void run() {
		com.cocopass.bll.Weather bll = new com.cocopass.bll.Weather();
		String jsonCityList = null;
		try {

			jsonCityList = com.cocopass.helper.CHttp.GetResponseBody(Config.CityListURL, null, null);
			// System.out.println(jsonCityList);
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			LOG.error(e1.getMessage());
		}

		if (jsonCityList != null && !jsonCityList.equals("")) {
			List<com.cocopass.model.City> list = Global.gson.fromJson(jsonCityList,
					new TypeToken<List<com.cocopass.model.City>>() {
					}.getType());
			for (com.cocopass.model.City city : list) {
				String url = null;

				url = Config.WeatherAPIURL.replace("#cityName", city.getName());

				// System.out.println(url);
				try {
					String response = com.cocopass.helper.CHttp.GetResponseBody(url, null, null);
					if (response != null && !response.equals("")) {
						com.cocopass.model.BaiDuWeatherAPIResponse baiDuWeatherAPIResponse = Global.gson
								.fromJson(response, com.cocopass.model.BaiDuWeatherAPIResponse.class);
						if (baiDuWeatherAPIResponse.getStatus().equals("success")) {
							com.cocopass.model.Weather weather = new com.cocopass.model.Weather();
							weather.setCityCode(city.getCode());
							weather.setUpdateTime(CDate.GetNow());
							String strTemperature = baiDuWeatherAPIResponse.getResults().get(0).getWeather_data().get(0)
									.getDate();
							float temperature = Float.parseFloat(CString.GetNumber(strTemperature.split("\\(")[1]));
							weather.setTemperature(temperature);
							bll.Update(weather);
						}
					}
					// LOG.debug(response);

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					LOG.error(e.getMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOG.error(e.getMessage());
				}
			}

			try {
				Thread.sleep(3600000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LOG.error(e.getMessage());
			}
		}
	}

}
