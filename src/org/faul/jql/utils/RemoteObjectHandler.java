/*
 * This file is part of JQL.
 *
 * JQL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JQL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jql.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.faul.jql.utils;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * A specialized JSON handler for encoding and decoding List<Map> based objects.
 */
public class RemoteObjectHandler implements JsonDeserializer<Object>,
		JsonSerializer<Object> {
	static Gson gson = new Gson();
	
	public Object deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		String str = null;
		if (json instanceof JsonPrimitive) {
			str = json.getAsJsonPrimitive().getAsString();

			JsonPrimitive p = (JsonPrimitive) json;
			return getPrimitive(p);
		}
		if (json instanceof JsonArray) {
			JsonArray array = (JsonArray) json;
			return getList(array);
		}

		if (json instanceof JsonObject) {
			// Now we will assume its a map
			JsonObject jso = json.getAsJsonObject();
			Map map = new HashMap();
			mapHandler(map, jso);
			return map;
		}
		throw new JsonParseException("Can't handle: " + json);
	}

	Object getList(JsonArray array) {
		List list = new ArrayList();
		Iterator it = array.iterator();
		Object target = null;
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof JsonArray) {
				target = getList((JsonArray) obj);
			}
			if (obj instanceof JsonPrimitive) {
				target = getPrimitive((JsonPrimitive) obj);
			}
			if (obj instanceof JsonObject) {
				Map map = new HashMap();
				mapHandler(map, (JsonObject)obj);
				target = map;
			}
			list.add(target);
			target = null;
		}

		return list;
	}

	Object getPrimitive(JsonPrimitive p) {
		if (p.isNumber()) {
			String str = p.getAsString();
			try {
				return Long.parseLong(str);
			} catch (Exception error) {}
			return Double.parseDouble(str);
		}

		if (p.isBoolean()) {
			return p.getAsBoolean();
		}

		if (p.isJsonArray()) {
			return p.getAsJsonArray();
		}

		if (p.isString()) {
			return p.getAsString();
		}

		return null;
	}

	/**
	 * Makes a map object from the Json Object.
	 * @param jso JsonObject. The object to construct from the map.
	 * @param src Object. The map source.
	 */
	void makeMap(JsonObject jso, Object src) {
		Map map = (HashMap) src;
		Set set = map.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = map.get(key);
			if (value instanceof HashMap) {
				JsonObject newJso = new JsonObject();
				makeMap(newJso, value);
				jso.add(key, newJso);
			} else
				jso.add(key, makePrimitive(value));
		}
	}

	JsonPrimitive makePrimitive(Object src) {

		if (src instanceof int[]) {
			int[] array = (int[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof String[]) {
			String[] array = (String[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof double[]) {
			double[] array = (double[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof float[]) {
			float[] array = (float[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof boolean[]) {
			boolean[] array = (boolean[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof long[]) {
			long[] array = (long[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}
		if (src instanceof Long[]) {
			Long[] array = (Long[]) src;
			String str = gson.toJson(array);
			return new JsonPrimitive(str);
		}

		if (src instanceof Integer) {
			Integer x = (Integer) src;
			return new JsonPrimitive(x);
		}
		if (src instanceof Long) {
			Long x = (Long) src;
			return new JsonPrimitive(x);
		}
		if (src instanceof Float) {
			Float x = (Float) src;
			return new JsonPrimitive(x);
		}
		if (src instanceof Double) {
			Double x = (Double) src;
			return new JsonPrimitive(x);
		}
		if (src instanceof Boolean) {
			Boolean x = (Boolean) src;
			return new JsonPrimitive(x);
		}

		// ///////////////////////////

		return new JsonPrimitive(src.toString());
	}

	void mapHandler(Map map, JsonObject jso) {
		Set set = jso.entrySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			Map.Entry e = (Map.Entry) o;
			String key = (String) e.getKey();

			Object test = e.getValue();
			if (test instanceof JsonArray) {
				JsonArray array = (JsonArray) test;
				map.put(key, getList(array));
			}

			if (test instanceof JsonPrimitive) {
				JsonPrimitive p = (JsonPrimitive) test;
				map.put(key, getPrimitive(p));
			}

			if (test instanceof JsonObject) {
				JsonObject value = (JsonObject) test;
				Map mapNext = new HashMap();
				mapHandler(mapNext, value);
				map.put(key, mapNext);
			}
		}

	}

	/**
	 * The serializer for the generalized object
	 */
	public JsonElement serialize(Object src, Type typeOfSrc,
			JsonSerializationContext context) {
		if (src instanceof HashMap) {
			Map map = (HashMap) src;
			JsonObject jso = new JsonObject();
			Set set = map.keySet();
			Iterator it = set.iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				Object value = map.get(key);
				if (value instanceof HashMap) {
					JsonObject newJso = new JsonObject();
					makeMap(newJso, value);
					jso.add(key, newJso);
				} else
					jso.add(key, makePrimitive(value));
			}
			return jso;
		}

		return makePrimitive(src);
	}
}
