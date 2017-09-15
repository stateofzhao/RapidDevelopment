package com.diagrams.core.navigatemgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.diagrams.core.debug.KwDebug;
import com.diagrams.lib.util.StringUtils;
import com.diagrams.lib.util.crypt.Base64Coder;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

//拼接下面导航协议的类
// kwnavi://fromproc.dstActivity.backurlb64.otherinfob64/NAVI_MAIN?a=datab64/NAVI_DEPTH1/NAVI_DEPTH2?name=aaab64&age=10b64
public final class NaviBuilder {
	private static final String KWNAVI_HEADER 		= "kwnavi://";
	public static final String NAVIGATE_PARAS_KEY 	= "__NAVIGATE_PARAS_KEY";

	private NaviBuilder() {
	}

	protected NaviBuilder(final Class<?> dstActivity) {
		this.dstActivity = dstActivity.getName();
		NaviPath firstPath = new NaviPath();
		//注意，这里默认添加id为NavigableItems.NAVI_ROOT_ACTIVITY的“领头path”，因为app设计之初，
        // 每个模块只有一个Activity，并且在`KwActivity`中实现了INavigable，在INavigable#getNavigableID()
        // 中返回了 NavigableItems.NAVI_ROOT_ACTIVITY
		firstPath.id = NavigableItems.NAVI_ROOT_ACTIVITY;
		allPath.add(firstPath);
		currentPath = firstPath;
	}

    protected NaviBuilder(final String dstActivity){
        this.dstActivity = dstActivity;
    }

	public static NaviBuilder from(final String url) {
		KwDebug.classicAssert(url.startsWith(KWNAVI_HEADER));

		String urlData = url.substring(KWNAVI_HEADER.length());
		String[] segments = StringUtils.split(urlData, '/');
		KwDebug.classicAssert(segments.length > 1);

		NaviBuilder builder = new NaviBuilder();
		parseInfos(builder, segments[0]);

		for (int i = 1; i < segments.length; i++) {
			parsePath(builder, segments[i]);
		}
		if (builder.allPath.size()>0) {
			builder.currentPath = builder.allPath.get(builder.allPath.size()-1);
		}

		return builder;
	}

	private static void parseInfos(NaviBuilder builder, final String infoSegment) {
		String[] infos = StringUtils.split(infoSegment, '.');
		KwDebug.classicAssert(infos.length >= 2);

		builder.fromProc = Integer.parseInt(infos[0]);
		try {
			builder.dstActivity = Base64Coder.decodeString(infos[1], "utf-8");
		} catch (UnsupportedEncodingException e1) {
			KwDebug.classicAssert(false, e1);
			builder.dstActivity = "##error " + KwDebug.throwable2String(e1);
		}
		if (infos.length > 2 && !TextUtils.isEmpty(infos[2])) {
			try {
				builder.backUrl = Base64Coder.decodeString(infos[2], "utf-8");
			} catch (Exception e) {
				KwDebug.classicAssert(false, e);
				builder.backUrl = "##error " + KwDebug.throwable2String(e);
			}
		}
		if (infos.length > 3 && !TextUtils.isEmpty(infos[3])) {
			try {
				builder.addInfo = Base64Coder.decodeString(infos[3], "utf-8");
			} catch (Exception e) {
				KwDebug.classicAssert(false, e);
				builder.addInfo = "##error " + KwDebug.throwable2String(e);
			}
		}
	}

	private static void parsePath(NaviBuilder builder, final String pathSegment) {
		KwDebug.classicAssert(pathSegment.length() > 0);

		NaviPath path = new NaviPath();
		builder.allPath.add(path);
		int pos = pathSegment.indexOf('?');
		if (pos < 0) {
			path.id = NavigableItems.values()[Integer.parseInt(pathSegment)];
			return;
		}

		path.id = NavigableItems.values()[Integer.parseInt(pathSegment.substring(0, pos))];
		if (pos == pathSegment.length() - 1) {
			return;
		}
		path.params.fromString(pathSegment.substring(pos + 1));
	}

	public NaviBuilder back(final NaviBuilder back) {
		backUrl = back.url();
		return this;
	}

	public NaviBuilder addPath(final NavigableItems addPath) {
		currentPath = new NaviPath();
		currentPath.id = addPath;
		allPath.add(currentPath);
		return this;
	}

	public NaviBuilder addParam(final String key, final String value) {
		KwDebug.classicAssert(currentPath != null);
		currentPath.params.addString(key, value);
		return this;
	}

	public NaviBuilder addParam(final NavigableItems path, final String key, final String value) {
		for (NaviPath p : allPath) {
			if (p.id == path) {
				p.params.addString(key, value.toString());
				return this;
			}
		}
		KwDebug.classicAssert(false, path.name() + "不在路径里！");
		return this;
	}

	public NaviBuilder addParam(final String key, final Serializable value) {
		KwDebug.classicAssert(currentPath != null);
		currentPath.params.addObject(key, value);
		return this;
	}

	public NaviBuilder addParam(final NavigableItems path, final String key, final Serializable value) {
		KwDebug.classicAssert(currentPath != null);
		for (NaviPath p : allPath) {
			if (p.id == path) {
				p.params.addObject(key, value);
				return this;
			}
		}
		KwDebug.classicAssert(false, path.name() + "不在路径里！");
		return this;
	}

	public NaviBuilder addInfo(final String info) {
		addInfo = info;
		return this;
	}

	public String url() {
		StringBuilder builder = new StringBuilder(100);
		builder.append("kwnavi://").append(fromProc)
		.append('.').append(Base64Coder.encodeString(dstActivity))
		.append('.').append(Base64Coder.encodeString(backUrl))
		.append('.').append(Base64Coder.encodeString(addInfo));
		path2String(builder);
		return builder.toString();
	}

	private void path2String(final StringBuilder builder) {
		for (NaviPath path : allPath) {
			builder.append('/').append(path.id.ordinal());
			if (path.params.size() > 0) {
				builder.append('?');
				path.params.param2String(builder);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void navigate(final Context ctx) {
		KwDebug.classicAssert(!TextUtils.isEmpty(dstActivity));

		Class<? extends Activity> c;
		try {
			c = (Class<? extends Activity>) Class.forName(dstActivity);
		} catch (Exception e) {
			KwDebug.classicAssert(false, e);
			return;
		}
		fromProc = android.os.Process.myPid();
		Intent intent = new Intent(ctx, c);
		intent.putExtra(NAVIGATE_PARAS_KEY, url());

		ctx.startActivity(intent);

		NaviMgr.cancel();
	}

	public boolean canGoBack() {
		return !TextUtils.isEmpty(backUrl);
	}

	public NaviBuilder getBackBuilder() {
		return from(backUrl);
	}

	@Override
	public String toString() {
		return url();
	}

	protected NaviPath currentPath;

	private int fromProc;
	protected String dstActivity = "";
	private String backUrl = "";
	private String addInfo = "";
	ArrayList<NaviPath> allPath = new ArrayList<>();
}
