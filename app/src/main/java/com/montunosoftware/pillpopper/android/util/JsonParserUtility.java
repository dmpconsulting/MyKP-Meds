/**
 * 
 */
package com.montunosoftware.pillpopper.android.util;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.interrupts.model.Questions;
import com.montunosoftware.pillpopper.android.interrupts.model.SecretQuestionsResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author M8081366
 * 
 */
public class JsonParserUtility {
	private static JsonParserUtility sJsonParser = new JsonParserUtility();

	public static JsonParserUtility getInstance() {
		return sJsonParser;
	}

	/**
	 * Method to parse any json data.
	 * @param <T>  object.
	 * @param data string data.
	 * @return object.
	 */
	public <T> T parseJson(String data, Class<T> className) {
		try {
			T parseResponse = null;
			Gson gson = new Gson();
			parseResponse = gson.fromJson(data, className);
			if (parseResponse == null) {
				return className.newInstance();
			}
			return parseResponse;
		} catch (InstantiationException | IllegalAccessException e) {
			LoggerUtils.exception(e.getMessage());
			return null;
		} catch (Exception e) {
			LoggerUtils.exception(e.getMessage());
			return null;
		}
    }

	public SecretQuestionsResponse parseJsonForSecretQuestions(String data) {

		SecretQuestionsResponse secretQuestionsResponse = new SecretQuestionsResponse();
		Map<String, List<Questions>> questionsMap = new HashMap<>();
		try {
			JSONObject newJson = new JSONObject(data);
			Gson gson = new Gson();
			// Get keys from json
			Iterator<String> panelKeys = newJson.keys();

			while (panelKeys.hasNext()) {
				String key = panelKeys.next();
				JSONArray panel = newJson.getJSONArray(key);
				List<Questions> questionsById = new ArrayList<>();

				for (int i = 0; i < panel.length(); i++) {
					JSONObject innerObj = panel.getJSONObject(i);
					Questions questions = gson.fromJson(innerObj.toString(), Questions.class);
					questionsById.add(questions);
				}
				questionsMap.put(key, questionsById);
			}
			secretQuestionsResponse.setSecretQuestionsMap(questionsMap);
		} catch (JSONException e) {
			LoggerUtils.error(e.getMessage());
		}
		return secretQuestionsResponse;
	}
}
