package com.diagrams.core.navigatemgr;

//能够处理NaviBuilder（根据NaviBuilder提供的信息进行导航）的类
public interface INavigable {

	NavigableItems getNavigableID();

	// 返回true则取消后续导航行为
	boolean onNavigate(NaviBuilder builder, NavigableItems toID, NaviPathParam currentParams);
}
