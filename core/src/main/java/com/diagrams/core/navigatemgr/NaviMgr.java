package com.diagrams.core.navigatemgr;

import android.content.Intent;
import com.diagrams.core.debug.KwDebug;
import java.util.ArrayList;
import java.util.LinkedList;

//解耦导航用的，例如，模块开发，进程间导航。 最好的实现就是一个模块或者一个进程只有一个Activity，
// 其它所有页面都以Fragment的形式显示出来，这样这个Activity就是一个路由Activity了。

//链接INavigable和NaviBuilder的工具类，其实觉着叫NaviHelper更合适~
public final class NaviMgr {

	public static void iamvisible(final INavigable item) {
		if(!alive.contains(item)){
			alive.add(item);
		}
		if (currentNaviBuilder!=null && nextNaviPath != null && nextNaviPath.id == item.getNavigableID()) {
			NaviPathParam currentParams = nextNaviPath.params;
			ArrayList<NaviPath> allPath = currentNaviBuilder.allPath;
			nextNaviPathDepth++;
			nextNaviPath = null;
			NavigableItems nextNaviPathID = NavigableItems.NAVI_NONE;
			if (nextNaviPathDepth < allPath.size()) {
				nextNaviPath = allPath.get(nextNaviPathDepth);
				nextNaviPathID = nextNaviPath.id;
			}
			boolean stop = item.onNavigate(currentNaviBuilder, nextNaviPathID, currentParams);
			if (stop || nextNaviPathDepth == allPath.size()) {
				stop();
			}
		}
	}

	public static void iamnotvisible(final INavigable item) {
		alive.remove(item);
		if (currentNaviBuilder!=null && nextNaviPath!=null) {
			ArrayList<NaviPath> allPath = currentNaviBuilder.allPath;
			NavigableItems itemID = item.getNavigableID();
			for (int i = allPath.size() - 1; i >= 0; i--) {
				NaviPath pathItem = allPath.get(i);
				if (itemID == pathItem.id) {
					stop();
					break;
				}
			}
		}
	}
	
	public static void transferNaviParam(final Intent from, final Intent to) {
		String urlString = from.getStringExtra(NaviBuilder.NAVIGATE_PARAS_KEY);
		if (urlString == null) {
			return;
		}
		from.removeExtra(NaviBuilder.NAVIGATE_PARAS_KEY);
		to.putExtra(NaviBuilder.NAVIGATE_PARAS_KEY, urlString);
	}

	// Activity分发导航消息
	public static boolean dispatch(final Intent intent) {
		if (intent==null) {
			return true;
		}
		String urlString = intent.getStringExtra(NaviBuilder.NAVIGATE_PARAS_KEY);
		if (urlString == null) {
			return false;
		}
		intent.removeExtra(NaviBuilder.NAVIGATE_PARAS_KEY);
		currentNaviBuilder = NaviBuilder.from(urlString);
		ArrayList<NaviPath> allPath = currentNaviBuilder.allPath;
		int pathSize = allPath.size();
		//注意，是按照路径倒着来导航的，这么做的好处是一旦拦截到了符合页面的Path（协议path中“领头path”需要指定导航的页面【NAVI_MAIN】），
		// 那么后续的path肯定都是非“领头path”。
		for (int i = pathSize - 1; i >= 0; i--) {
			NaviPath pathItem = allPath.get(i);
			for (INavigable item : alive) {
				if (item.getNavigableID() == pathItem.id) {
					nextNaviPathDepth = i + 1;
					nextNaviPath = null;
					//通过这里可以看到，是存在着“领头path”的，就是首选根据“领头path”取出对应的INavigable，
					// 然后再使用下一个path来调用INavigable的onNavigate()方法来进行导航
					NavigableItems nextNaviPathID = NavigableItems.NAVI_NONE;
					if (nextNaviPathDepth < pathSize) {
						nextNaviPath = allPath.get(nextNaviPathDepth);
						nextNaviPathID = nextNaviPath.id;
					}
					boolean stop = item.onNavigate(currentNaviBuilder, nextNaviPathID, pathItem.params);
					if (stop || nextNaviPathDepth == pathSize) {
						stop();
					}
					return true;
				}
			}
		}

		KwDebug.classicAssert(false);
		return false;
	}

	//一旦执行了INavigable的onNavigate()方法，就应该调用iamnotvisible()方法，来标记当前的INavigable，
	// 然后再次回到INavigable时，调用iamvisible()方法来标记INavigable，这样就能够达到连续协议中指定的
	// 连续跳转效果了，但是在android中（酷我项目中），只是在Activity的onResume()中调用了iamvisible()方法，
	// 在onPause()方法中调用了iamnotvisible()方法，如果导航是打开的Activity，那么没有问题，但是如果导航是
	// 打开的Fragment，那么其实就有问题了，不会进行连续跳转了。
	public static void stop() {
		nextNaviPath = null;
	}
	
	public static void cancel() {
		stop();
		currentNaviBuilder = null;
	}

	public static NaviBuilder NaviBuilder(final Class<?> dstActivity) {
		return new NaviBuilder(dstActivity);
	}

	public static NaviBuilder NaviBuilder(String dstActivity){
		return new NaviBuilder(dstActivity);
	}
	
	public static NaviBuilder currentNaviBuilder() {
		return currentNaviBuilder;
	}

	private static NaviPath nextNaviPath;
	private static int nextNaviPathDepth;

	private static NaviBuilder currentNaviBuilder;
	private static LinkedList<INavigable> alive = new LinkedList<INavigable>();
}
