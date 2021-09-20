package com.montunosoftware.pillpopper.android.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Simple class that allows a series of comparators to be added.
// The compare() function tries each one in order, returning on the first
// one that indicates a difference in sort order.
public class ComposableComparator<ComparableClass> implements Comparator<ComparableClass>
{
	private List<Comparator<ComparableClass>>_comparatorList = new ArrayList<>();
	
	public ComposableComparator<ComparableClass> by(Comparator<ComparableClass> comparator)
	{
		_comparatorList.add(comparator);
		return this;
	}
	
	@Override
	public int compare(ComparableClass lhs, ComparableClass rhs)
	{
		for (Comparator<ComparableClass> comparator: _comparatorList) {
			int retval = comparator.compare(lhs, rhs);
			
			if (retval != 0) {
				return retval;
			}
		}
		
		return 0;
	}
}
