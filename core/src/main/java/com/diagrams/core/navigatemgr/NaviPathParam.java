package com.diagrams.core.navigatemgr;

import android.text.TextUtils;
import com.diagrams.core.debug.KwDebug;
import com.diagrams.lib.util.StringUtils;
import com.diagrams.lib.util.crypt.Base64Coder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

//导航协议中，路径的参数 bean
public final class NaviPathParam {
	
	public int size() {
		return paras.size();
	}

	public void addString(final String key,final String value) {
		paras.put(key,value);
	}
	
	public void addObject(final String key,final Serializable obj) {
		paras.put(key,serialize(obj));
	}
	
	public String getString(final String key) {
		return paras.get(key);
	}
	
	public Object getObject(final String key) {
		return unserialize(paras.get(key));
	}
	
	private static String serialize(final Serializable object) {
        try {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            return Base64Coder.encode(bytes);
        } catch (Throwable e) {
        	KwDebug.classicAssert(false,e);
        	return "##error " + KwDebug.throwable2String(e);
        }
    }
	
	private static Object unserialize(final String data) {
		if (TextUtils.isEmpty(data)) {
			return null;
		}
        try {
    		byte[] bytes = Base64Coder.decode(data);
             ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Throwable e) {
        	KwDebug.classicAssert(false,e);
        }
        return null;
    }
	
	protected void param2String(final StringBuilder builder) {
		boolean first = true;
		for (Map.Entry<String, String> entry : paras.entrySet()) {
			if (!first) {
				builder.append('&');
			}
			first = false;
			String value = entry.getValue();
			String encodedValue = "";
			if (!TextUtils.isEmpty(value)) {
				String base64Value = Base64Coder.encodeString(value);
				try {
					encodedValue = URLEncoder.encode(base64Value, "utf-8");
				} catch (Throwable e) {
					encodedValue = "##error " + KwDebug.throwable2String(e);
				}
			}
			builder.append(entry.getKey()).append('=')
			.append(encodedValue);
		}
	}
	
	protected void fromString(final String parasString) {
		paras.clear();
		String[] paraStrs = StringUtils.split(parasString, '&');
		for (int i = 0; i < paraStrs.length; i++) {
			String onePara = paraStrs[i];
			int pos = onePara.indexOf('=');
			if (pos < 0) {
				KwDebug.classicAssert(false);
				continue;
			}
			String key = onePara.substring(0,pos);
			if (pos < onePara.length() - 1) {
				String value = onePara.substring(pos + 1);
				try {
					String base64Value = URLDecoder.decode(value, "utf-8");
					paras.put(key, Base64Coder.decodeString(base64Value, "utf-8"));
				} catch (Throwable e) {
					KwDebug.classicAssert(false, e);
					paras.put(key, "##error " + KwDebug.throwable2String(e));
					continue;
				}
			} else {
				paras.put(key, "");
			}
		}
	}
	
	private Map<String, String> paras = new HashMap<String,String>();
}
