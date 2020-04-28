package com.webank.wecube.platform.gateway.route;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * NOT thread-safe
 * 
 * @author Gavin
 *
 */
public class DynamicRouteContext {
	public static final String DYNAMIC_ROUTE_CONTEXT_KEY = "dynamic_route_context";

	private List<HttpDestination> httpDestinations = new ArrayList<>();
	private volatile int currentIndex = -1;
	
	private DynamicRouteContext() {}
	
	public static DynamicRouteContext newInstance() {
		return new DynamicRouteContext();
	}

	public HttpDestination next() {
		if (!hasNext()) {
			if ((httpDestinations != null) && (!httpDestinations.isEmpty())) {
				// to improve here?
				return httpDestinations.get(0);
			} else {
				return null;
			}

		}

		currentIndex++;
		int index = (currentIndex >= httpDestinations.size()) ? 0 : currentIndex;

		return httpDestinations.get(index);

	}
	
	public DynamicRouteContext addHttpDestinations(Collection<HttpDestination> httpDests) {
		if (httpDests == null || httpDests.isEmpty()) {
			return this;
		}
		
		httpDests.forEach(hd -> {
			httpDestinations.add(hd.clone());
		});


		return this;
	}
	
	public DynamicRouteContext sortByWeight(){
	    Collections.sort(httpDestinations,new Comparator<HttpDestination>(){
            @Override
            public int compare(HttpDestination o1, HttpDestination o2) {
                return o2.getWeight() - o1.getWeight();
            }
        });
	    
	    return this;
	}

	public DynamicRouteContext addHttpDestination(HttpDestination httpDest) {
		if (httpDest == null) {
			return this;
		}

		httpDestinations.add(httpDest.clone());

		return this;
	}

	public boolean hasNext() {
		if (httpDestinations == null) {
			return false;
		}

		if (httpDestinations.isEmpty()) {
			return false;
		}

		if (currentIndex >= (httpDestinations.size()-1)) {
			return false;
		}
		return true;
	}
}
